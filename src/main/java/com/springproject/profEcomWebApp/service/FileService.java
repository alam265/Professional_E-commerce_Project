package com.springproject.profEcomWebApp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService{
    public String uploadImage(String path, MultipartFile image) throws IOException {
        String originalFileName = image.getOriginalFilename();
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String filePath = path + File.separator + fileName;

        // cheking folder exist else create
        File folder = new File(path);
        if(!folder.exists()){
            folder.mkdir();
        }
        Files.copy(image.getInputStream(), Paths.get(filePath));

        return fileName;

    }
}
