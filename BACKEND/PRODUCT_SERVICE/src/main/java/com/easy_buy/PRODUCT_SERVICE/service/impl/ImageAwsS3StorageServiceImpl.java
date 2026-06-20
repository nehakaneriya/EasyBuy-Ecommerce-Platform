package com.easy_buy.PRODUCT_SERVICE.service.impl;

import com.easy_buy.PRODUCT_SERVICE.service.ImageStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


public class ImageAwsS3StorageServiceImpl implements ImageStorageService {
    @Override
    public String upload(MultipartFile file) {

        // Implement AWS S3 upload logic here
        // Return the URL of the uploaded image
        return null;
    }
}
