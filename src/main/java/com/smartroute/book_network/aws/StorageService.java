package com.smartroute.book_network.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile file) {
        String fileName = "bsn" + "/" + System.currentTimeMillis() + "_" +
                file.getOriginalFilename();
        File fileObj = convertMultiPartToFile(file);

        try {
            // Upload file to S3
            System.out.println("Uploading file to S3: " + fileName);
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
            System.out.println("File uploaded: " + fileName);
        } catch (Exception e) {
            log.error("Error uploading file", e);
        } finally {
            // Safely delete the file after upload
            if (fileObj.exists() && !fileObj.delete()) {
                System.err.println("Failed to delete temporary file: " +
                        fileObj.getAbsolutePath());
            }
        }
        return fileName;
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream stream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(stream);
            return content;
        } catch (Exception e) {
            log.error("Error downloading file", e);
        }
        return null;
    }

    public String deleteFile(String fileName) {
        try {
            s3Client.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            log.error("Error deleting file", e);
        }
        return "File deleted successfully";
    }

    private File convertMultiPartToFile(MultipartFile file) {
        try {
            // Create temp file in the system's temp directory
            File tempFile = File.createTempFile("upload_", "_" +
                    file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert MultipartFile to File", e);
        }
    }

    public String generatePresignedUploadUrl(String fileName) {
        // / Set URL expiration time (e.g., 15 minutes)
        Date expiration = new Date(System.currentTimeMillis() + 15 * 60 * 1000);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        String fileUrl = s3Client.generatePresignedUrl(request).toString();
        return fileUrl;
    }

}
