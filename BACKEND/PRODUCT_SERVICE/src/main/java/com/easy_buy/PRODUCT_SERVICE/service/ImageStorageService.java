package com.easy_buy.PRODUCT_SERVICE.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {


    // Method to upload image to ImageKit and return the URL of the uploaded image
    String upload(MultipartFile file);
}
