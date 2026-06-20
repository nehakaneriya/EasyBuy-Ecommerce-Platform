package com.easy_buy.PRODUCT_SERVICE.service;


import com.easy_buy.PRODUCT_SERVICE.dtos.PagedResponse;
import com.easy_buy.PRODUCT_SERVICE.dtos.ProductDto;
import com.easy_buy.PRODUCT_SERVICE.dtos.ProductReviewDto;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.UUID;

public interface ProductService {


    //all products in paginated format
    PagedResponse<ProductDto> getAllProducts(int page, int size);

    //product dto by product id
    ProductDto getProductById(UUID productId);

    //product by category id in paginated way
    PagedResponse<ProductDto> getProductsByCategoryId(UUID categoryId, int page, int size);

    //create new product
    ProductDto createProduct(ProductDto productDto);


    //update the product by product id
    ProductDto updateProduct(UUID productId, ProductDto productDto);

    //delete product by id
    void deleteProduct(UUID productId);

    //Add category to product --> product id, category id
    ProductDto addCategoryToProduct(UUID productId, UUID categoryId);

    //Remove the category from product
    ProductDto removeCategoryFromProduct(UUID productId, UUID categoryId);

    //Add Review to product--> product id ,
    ProductReviewDto addReviewToProduct(UUID productId, ProductReviewDto reviewDto);

    //Add product images
    ProductDto addProductImages(UUID productId, List<MultipartFile> files);

    //Get images of product
    List<String> getProductImages(UUID productId);
}
