package com.example.MediSearch.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {


    //    this part require some knowledge of file handling
    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

        String originalFileName = file.getOriginalFilename();
        String randomId = UUID.randomUUID().toString();

        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String filename = randomId + extension;

        // 🔥 absolute path use karo
        String uploadDir = System.getProperty("user.dir") + File.separator + path;

        File folder = new File(uploadDir);
        if (!folder.exists()) {
            folder.mkdirs();  // mkdir → mkdirs
        }

        String filePath = uploadDir + File.separator + filename;

        Files.copy(file.getInputStream(), Paths.get(filePath));

        System.out.println("Saved at: " + filePath);

        return filename;
    }

}
