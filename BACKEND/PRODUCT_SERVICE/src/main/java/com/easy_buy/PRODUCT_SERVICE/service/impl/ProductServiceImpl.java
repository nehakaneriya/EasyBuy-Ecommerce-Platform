package com.easy_buy.PRODUCT_SERVICE.service.impl;

import com.easy_buy.PRODUCT_SERVICE.dtos.CategoryDto;
import com.easy_buy.PRODUCT_SERVICE.dtos.PagedResponse;
import com.easy_buy.PRODUCT_SERVICE.dtos.ProductDto;
import com.easy_buy.PRODUCT_SERVICE.dtos.ProductReviewDto;
import com.easy_buy.PRODUCT_SERVICE.entity.Category;
import com.easy_buy.PRODUCT_SERVICE.entity.Product;
import com.easy_buy.PRODUCT_SERVICE.entity.ProductReview;
import com.easy_buy.PRODUCT_SERVICE.exception.InvalidRequestException;
import com.easy_buy.PRODUCT_SERVICE.exception.ResourceNotFoundException;
import com.easy_buy.PRODUCT_SERVICE.repositories.CategoryRepository;
import com.easy_buy.PRODUCT_SERVICE.repositories.ProductRepository;
import com.easy_buy.PRODUCT_SERVICE.service.ImageStorageService;
import com.easy_buy.PRODUCT_SERVICE.service.ProductService;
import com.easy_buy.PRODUCT_SERVICE.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final ImageStorageService imageStorageService;
    private final ReviewService reviewService;

    // This method is used to get all products with pagination
    @Override
    public PagedResponse<ProductDto> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepo.findAll(pageable);

        //method reference from java8 feature

//        Page<Product>

//       productPage.map(this::toDto)-- Page<ProductDTO>

//       toPagedResponse---> PagedResponse

        //method reference from java8 feature
        return toPagedResponse(productPage.map(this::toDto));
    }
    // This method is used to get product by ID
    @Override
    public ProductDto getProductById(UUID productId) {
        return toDto(findProduct(productId));
    }

    // This method is used to get products by category ID
    @Override
    public PagedResponse<ProductDto> getProductsByCategoryId(UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepo.findByCategories_Id(categoryId, pageable);

        //conversations
        return toPagedResponse(productPage.map(this::toDto));
    }

    // This method is used to create product
    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product product = new Product();
        applyBasicFields(product, productDto);
        List<Category> categories = resolveCategories(productDto.getCategories());
        product.setCategories(categories);
        return toDto(productRepo.save(product));
    }

    // This method is used to update product
    @Override
    public ProductDto updateProduct(UUID productId, ProductDto productDto) {
        Product product = findProduct(productId);
        applyBasicFields(product, productDto);

        if (productDto.getCategories() != null) {
            List<Category> categories = resolveCategories(productDto.getCategories());
            product.setCategories(categories);
        }

        return toDto(productRepo.save(product));
    }

    // This method is used to delete product
    @Override
    public void deleteProduct(UUID productId) {
        Product product = findProduct(productId);

        for (Category category : product.getCategories()) {
            category.getProducts().remove(product);
            categoryRepo.save(category);
        }
        productRepo.delete(product);
    }
    // This method is used to add category to product
    @Override
    public ProductDto addCategoryToProduct(UUID productId, UUID categoryId) {
        Product product = findProduct(productId);
        Category category = findCategory(categoryId);

        // Add category if not already added
        if (!product.getCategories().contains(category)) {
            product.getCategories().add(category);
        }

        // Add product inside category product list
        if (!category.getProducts().contains(product)) {
            category.getProducts().add(product);
        }

        categoryRepo.save(category);
        return toDto(productRepo.save(product));
    }

    // This method is used to remove category from product
    @Override
    public ProductDto removeCategoryFromProduct(UUID productId, UUID categoryId) {
        Product product = findProduct(productId);
        Category category = findCategory(categoryId);

        //1step
        product.getCategories().remove(category);
        //2step
        category.getProducts().remove(product);

        categoryRepo.save(category);
        return toDto(productRepo.save(product));
    }

    // This method is used to add review to product
    @Override
    public ProductReviewDto addReviewToProduct(UUID productId, ProductReviewDto reviewDto) {
        return reviewService.createReview(productId, reviewDto);
    }

    // This method is used to add product images
    @Override
    public ProductDto addProductImages(UUID productId, List<MultipartFile> files) {

        // Find product
        Product product = findProduct(productId);

        //will upload the images:
        List<String> uploadedUrls = uploadImages(files);

        // Initialize image list if null
        if (product.getProductImages() == null) {
            product.setProductImages(new ArrayList<>());
        }
        // Add uploaded image URLs
        product.getProductImages().addAll(uploadedUrls);

        // Save product and return DTO
        return toDto(productRepo.save(product));
    }

    // This method is used to get product images
    @Override
    public List<String> getProductImages(UUID productId) {
        Product product = findProduct(productId);
        return product.getProductImages() == null
                ? new ArrayList<>()
                : new ArrayList<>(product.getProductImages());
    }

    // Reusable method to find product using product ID
    private Product findProduct(UUID productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    // Reusable method to find category using category ID
    private Category findCategory(UUID categoryId) {
        return categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    // This method is used to copy basic fields from ProductDto to Product entity
    private void applyBasicFields(Product product, ProductDto productDto) {
        //custom logic
        product.setTitle(productDto.getTitle());
        product.setShortDesc(productDto.getShortDesc());
        product.setLongDesc(productDto.getLongDesc());
        product.setPrice(productDto.getPrice());
        product.setDiscount(productDto.getDiscount());
        if (productDto.getLive() != null) {
            product.setLive(productDto.getLive());
        }
        if (productDto.getProductImages() != null) {
            product.setProductImages(new ArrayList<>(productDto.getProductImages()));
        }
    }

    // This method is used to convert CategoryDto list into a Category entity list
    private List<Category> resolveCategories(List<CategoryDto> categoryDtos) {
        if (categoryDtos == null) {
            return new ArrayList<>();
        }
        List<Category> categories = new ArrayList<>();
        for (CategoryDto categoryDto : categoryDtos) {
            if (categoryDto.getId() == null) {
                Category category = new Category();
                category.setTitle(categoryDto.getTitle());
                categories.add(categoryRepo.save(category));
            } else {
                categories.add(findCategory(categoryDto.getId()));
            }
        }
        return categories;
    }

    // This method is used to upload product images
    private List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new InvalidRequestException("At least one product image is required");
        }
        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            uploadedUrls.add(imageStorageService.upload(file));
        }
        return uploadedUrls;
    }

    // This method is used to convert Product entity into ProductDto
    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setShortDesc(product.getShortDesc());
        dto.setLongDesc(product.getLongDesc());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setLive(product.getLive());
        dto.setProductImages(product.getProductImages() == null ? new ArrayList<>() : new ArrayList<>(product.getProductImages()));
        dto.setCategories(product.getCategories() == null ? new ArrayList<>() : product.getCategories().stream().map(this::toCategoryDtoShallow).collect(Collectors.toList()));
        dto.setReviews(product.getReviews() == null ? new ArrayList<>() : product.getReviews().stream().map(this::toReviewDtoShallow).collect(Collectors.toList()));
        return dto;
    }

    // This method is used to convert Category entity into shallow CategoryDto
    private CategoryDto toCategoryDtoShallow(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setTitle(category.getTitle());
        return dto;
    }

    // This method is used to convert ProductReview entity into shallow ProductReviewDto
    private ProductReviewDto toReviewDtoShallow(ProductReview review) {
        ProductReviewDto dto = new ProductReviewDto();
        dto.setId(review.getId());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setProduct(null);
        return dto;
    }

    // This method is used to convert Page<ProductDto> into custom PagedResponse
    private PagedResponse<ProductDto> toPagedResponse(Page<ProductDto> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast()
        );
    }
}
