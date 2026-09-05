package com.naveen.document_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.naveen.document_service.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService{

    private final Cloudinary cloudinary;

    public CloudinaryUploadResult upload(MultipartFile file) throws IOException{

        try {
            Map<?, ?> result = cloudinary.uploader()
                    .upload(
                            file.getBytes(),
                            ObjectUtils.asMap("folder", "loan-platform/documents", "resource_type", "auto")
                    );

            String publicId = result.get("public_id").toString();
            String secureUrl = result.get("secure_url").toString();

            return new CloudinaryUploadResult(publicId, secureUrl);
        } catch(IOException e) {
            throw new FileStorageException("Failed to upload document to Cloudinary");
        }

    }

    public void delete(String publicId) throws IOException{

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch(IOException e) {
            throw new FileStorageException("Failed to delete document from Cloudinary");
        }
    }

    public record CloudinaryUploadResult(String publicId, String secureUrl) {}
}
