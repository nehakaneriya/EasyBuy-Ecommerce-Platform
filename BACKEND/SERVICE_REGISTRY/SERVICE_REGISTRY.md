# SERVICE_REGISTRY

## 1. Overview

**SERVICE_REGISTRY** is the **service discovery** component of the EASY_BUY microservices system. It is built using **Netflix Eureka Server**, wrapped inside a Spring Boot application.

In a microservices architecture, there are many independent services (User, Product, Payment, Inventory, Cart-Order, Notification, AI, API-Gateway, Config-Server, etc.). These services need a way to **find each other** on the network without hardcoding IP addresses or ports. `SERVICE_REGISTRY` solves exactly this problem — it acts as a **central phonebook / directory** where every microservice registers itself, and from which every other service can look up the location of any registered service.

| Property | Value |
|---|---|
| Module Name | `SERVICE_REGISTRY` |
| Type | Infrastructure Service (not a business service) |
| Framework | Spring Boot 4.0.6 |
| Spring Cloud Version | 2025.1.1 |
| Java Version | 21 |
| Build Tool | Maven |
| Default Port | `8761` |
| Core Dependency | `spring-cloud-starter-netflix-eureka-server` |
| Other Dependency | `spring-boot-starter-actuator` (health/monitoring endpoints) |

---

## 2. What is Eureka?

**Eureka** is a **Service Discovery** tool created by Netflix, and it is now part of the **Spring Cloud Netflix** project.

Think of Eureka as a **contact list / address book** for microservices:

- Every microservice (client) that starts up **registers itself** with Eureka Server by sending its:
  - Service name (e.g., `USER-SERVICE`, `PRODUCT-SERVICE`)
  - IP address / hostname
  - Port number
  - Health status
- Eureka Server keeps this information in memory and continuously verifies (via heartbeats) that each service is still alive.
- Any other service that wants to talk to, say, `USER-SERVICE`, does NOT need to know its exact IP/port. It simply asks Eureka: *"Where is USER-SERVICE right now?"* and Eureka replies with the current active address(es).

There are two roles in Eureka:

1. **Eureka Server** — the registry itself (this is what `SERVICE_REGISTRY` module is).
2. **Eureka Client** — any microservice that registers itself into the server and/or looks up other services from it (e.g., `USER_SERVICE`, `API-GATEWAY`, `CONFIG-SERVER`, etc.)

---

## 3. Why Do We Use Eureka / Service Registry?

In a monolithic application, everything runs in one process, so there's no need to "discover" anything. But EASY_BUY is split into 11 independent microservices, and this creates real problems that Eureka solves:

| Problem (without Eureka) | Solution (with Eureka) |
|---|---|
| Services would need to hardcode each other's IP/port in code or config files. If a service moves to a new server or port, every other service's config must be updated manually. | Services only need to know the **logical name** (e.g., `PRODUCT-SERVICE`). Eureka resolves the actual network location dynamically at runtime. |
| In cloud/container environments, IPs change every time a container restarts or scales (auto-scaling, Docker, Kubernetes). | Eureka clients continuously re-register, so the registry always reflects the current, live addresses. |
| If a service crashes, other services would keep sending requests to a dead address, causing failures. | Eureka Server removes services that stop sending heartbeats, avoiding calls to dead instances. |
| Manually load-balancing between multiple instances of the same service is hard. | Eureka can return a list of all healthy instances of a service, which client-side load balancers (like Spring Cloud LoadBalancer, used with `lb://SERVICE-NAME` in `API-GATEWAY`) can round-robin between. |
| Scaling a service up/down (adding/removing instances) would require manual reconfiguration everywhere. | New instances self-register automatically; removed instances automatically expire from the registry — zero manual config changes needed. |

**In short: Eureka removes tight-coupling between services and enables dynamic, resilient, and scalable communication in the microservices ecosystem.**

---

## 4. Directory Structure

```
SERVICE_REGISTRY/
├── pom.xml
├── mvnw / mvnw.cmd                (Maven wrapper scripts)
├── .mvn/wrapper/maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/com/easy_buy/SERVICE_REGISTRY/
│   │   │   └── ServiceRegistryApplication.java   (main entry point)
│   │   └── resources/
│   │       └── application.yaml                  (configuration)
│   └── test/
│       └── java/com/easy_buy/SERVICE_REGISTRY/
│           └── ServiceRegistryApplicationTests.java
└── target/                                        (compiled build output)
```

This is a **minimal, out-of-the-box** Eureka Server — there are no custom controllers, security configs, or extra beans. It relies entirely on Spring Cloud's built-in Eureka Server implementation.

---

## 5. Key Files

### 5.1 `pom.xml` (Maven Dependencies)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.6</version>
</parent>

<properties>
    <java.version>21</java.version>
    <spring-cloud.version>2025.1.1</spring-cloud.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>
```

- `spring-cloud-starter-netflix-eureka-server` → This single dependency is what turns a normal Spring Boot app into a full Eureka Server.
- `spring-boot-starter-actuator` → Exposes health-check and monitoring endpoints (e.g., `/actuator/health`).

### 5.2 `ServiceRegistryApplication.java` (Main Class)

```java
package com.easy_buy.SERVICE_REGISTRY;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
```

- `@SpringBootApplication` → Standard Spring Boot bootstrap annotation.
- `@EnableEurekaServer` → **This is the most important line.** It tells Spring Cloud to activate the Eureka Server auto-configuration, which starts:
  - The Eureka registry (in-memory database of registered services)
  - The Eureka Dashboard (web UI available at `http://localhost:8761`)
  - REST APIs that clients use to register/renew/deregister/query services

### 5.3 `application.yaml` (Configuration)

```yaml
spring:
  application:
    name: SERVICE_REGISTRY
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

| Property | Meaning |
|---|---|
| `spring.application.name: SERVICE_REGISTRY` | The logical name of this application. |
| `server.port: 8761` | Eureka Server runs on port **8761** (this is the Eureka community's standard default port). |
| `eureka.client.register-with-eureka: false` | This server does NOT register itself as a client in its own registry — because it IS the registry, not a business service that needs to be discovered. |
| `eureka.client.fetch-registry: false` | This server does not try to download/fetch a copy of the registry from itself. This confirms it's running in **standalone mode** (not peer-to-peer clustered with other Eureka nodes). |

> **Note:** There is no High-Availability (HA)/clustering configuration here — this is a single-node Eureka Server setup, suitable for development. In production, multiple Eureka Server instances are normally set up as peers for fault tolerance.

---

## 6. How It Fits Into the EASY_BUY Architecture (Workflow)

### 6.1 Startup Sequence

```
1. SERVICE_REGISTRY starts first (port 8761)
        │
        ▼
2. CONFIG-SERVER starts (port 8888) and registers itself
   with SERVICE_REGISTRY at http://localhost:8761/eureka
        │
        ▼
3. All business services (USER_SERVICE, PRODUCT_SERVICE,
   INVENTORY_SERVICE, PAYMENT_SERVICE, CART-ORDER_SERVICE,
   NOTIFICATION_SERVICE, API-GATEWAY, etc.) start.
   Each of them:
     a. First contacts CONFIG-SERVER to pull its own configuration
        (including its Eureka registration URL)
     b. Then registers itself with SERVICE_REGISTRY using that
        configuration
        │
        ▼
4. SERVICE_REGISTRY now holds a live list of ALL running
   service instances (name + host + port + health status)
```

### 6.2 Request-Time Discovery Flow (Example: API-GATEWAY calling PRODUCT-SERVICE)

```
Client Request
      │
      ▼
API-GATEWAY  ── "Where is PRODUCT-SERVICE right now?" ──▶  SERVICE_REGISTRY
      │                                                          │
      │  ◀── "PRODUCT-SERVICE is at 192.168.1.5:8082" ───────────┘
      │
      ▼
API-GATEWAY forwards the request to PRODUCT-SERVICE instance
```

In the codebase, this is implemented using Spring Cloud Gateway's **load-balanced URIs**, for example:

```yaml
uri: lb://PRODUCT-SERVICE
```

The `lb://` prefix tells Spring Cloud Gateway: *"Don't use a fixed URL — ask Eureka (via the load balancer) for the current address of PRODUCT-SERVICE, and if there are multiple instances, distribute requests among them."*

### 6.3 Heartbeat & Self-Healing

- Every registered client sends a **heartbeat** (a small "I'm alive" signal) to `SERVICE_REGISTRY` every 30 seconds by default.
- If `SERVICE_REGISTRY` does not receive a heartbeat from a service within a configured timeout (default ~90 seconds), it marks that instance as **DOWN** and eventually evicts it from the registry.
- This ensures other services never get routed to a dead/crashed instance.

### 6.4 Services That Connect to SERVICE_REGISTRY

| Service | How it connects |
|---|---|
| `CONFIG-SERVER` | Directly configured locally: `eureka.client.service-url.defaultZone: http://localhost:8761/eureka` |
| `NOTIFICATION_SERVICE` | Directly configured locally: `http://localhost:8761/eureka` |
| `USER_SERVICE`, `PRODUCT_SERVICE`, `INVENTORY_SERVICE`, `PAYMENT_SERVICE`, `CART-ORDER_SERVICE`, `API-GATEWAY` | Fetch their Eureka registration settings dynamically from `CONFIG-SERVER` at startup (externalized in a Git-based config repository), then register with `SERVICE_REGISTRY` using those settings. |

---

## 7. Eureka Dashboard

Once running, Eureka Server provides a built-in web dashboard at:

```
http://localhost:8761
```

This dashboard shows:
- All currently registered service instances
- Their status (UP / DOWN)
- Number of instances per service
- General Eureka server health info

This is very useful for developers to quickly verify whether a service has successfully registered with the discovery server.

---

## 8. Running the Service

```bash
# From the SERVICE_REGISTRY directory
./mvnw spring-boot:run
```

Or build and run the jar:

```bash
./mvnw clean package
java -jar target/SERVICE_REGISTRY-0.0.1-SNAPSHOT.jar
```

The server will start on **port 8761**. It should always be started **first**, before any other microservice, since every other service depends on it being available to register.

---

## 9. Summary

| Question | Answer |
|---|---|
| What is it? | A Eureka Server that acts as the central service registry for all EASY_BUY microservices. |
| Why do we need it? | To allow microservices to dynamically discover each other's network location instead of hardcoding IPs/ports, enabling scalability, resilience, and load balancing. |
| What port does it run on? | 8761 |
| Does it register itself? | No (`register-with-eureka: false`, `fetch-registry: false`) — it is a standalone registry only. |
| Who depends on it? | Every other microservice in the system (directly or indirectly via Config Server). |
| Is it customized? | No — it's a minimal, default Spring Cloud Eureka Server setup with no additional business logic. |
