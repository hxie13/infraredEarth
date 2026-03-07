package cn.ac.sitp.infrared.controller;

import cn.ac.sitp.infrared.service.StoredFileResolver;
import cn.ac.sitp.infrared.util.RequestValueUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/rest/file")
public class StoredFileController {

    private final StoredFileResolver storedFileResolver;

    public StoredFileController(StoredFileResolver storedFileResolver) {
        this.storedFileResolver = storedFileResolver;
    }

    @GetMapping
    public ResponseEntity<UrlResource> getFile(@RequestParam("path") String path) throws MalformedURLException {
        String storedPath = RequestValueUtils.trimToNull(path);
        if (storedPath == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path resolvedPath = storedFileResolver.resolveExistingFile(storedPath);
            UrlResource resource = new UrlResource(resolvedPath.toUri());
            String contentType = probeContentType(resolvedPath);
            MediaType mediaType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
            String filename = resolvedPath.getFileName() == null ? "file" : resolvedPath.getFileName().toString();

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String probeContentType(Path resolvedPath) {
        try {
            return Files.probeContentType(resolvedPath);
        } catch (IOException ignored) {
            return null;
        }
    }
}
