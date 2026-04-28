package com.qingliao.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        try { Files.createDirectories(uploadDir); } catch (IOException e) { throw new RuntimeException(e); }
    }

    public String store(MultipartFile file) {
        return storeInDir(file, "");
    }

    public String storeImage(MultipartFile file) {
        return storeInDir(file, "images");
    }

    public String storeFile(MultipartFile file, String originalFilename) {
        return storeInDir(file, "files");
    }

    public String storeAvatar(MultipartFile file) {
        return storeInDir(file, "avatars");
    }

    private String storeInDir(MultipartFile file, String subDir) {
        String ext = getExtension(file.getOriginalFilename());
        String name = UUID.randomUUID().toString() + ext;
        try {
            Path dir = subDir.isEmpty() ? uploadDir : uploadDir.resolve(subDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(name);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String path = subDir.isEmpty() ? "/uploads/" + name : "/uploads/" + subDir + "/" + name;
            return path;
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
