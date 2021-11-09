package com.learningbybuilding.uploaddownloadfile.resource;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileResource {
    public static final String DIRECTORY  = System.getProperty("user.home") + "/Downloads/upload";

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFile(@RequestParam("files") List<MultipartFile> files) throws IOException {
        List<String> fileNames = new ArrayList<>();

        for(MultipartFile file : files) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            Path path = Paths.get(DIRECTORY, fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            fileNames.add(fileName);
        }

        return ResponseEntity.ok().body(fileNames);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {
        Path path = Paths.get(DIRECTORY, fileName);
        if (!Files.exists(path)) {
            throw new FileNotFoundException(fileName + " was not found on server");
        }
        Resource resource = new UrlResource(path.toUri());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("File-Name", fileName);
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Files.probeContentType(path)))
                .headers(httpHeaders)
                .body(resource);
    }
}
