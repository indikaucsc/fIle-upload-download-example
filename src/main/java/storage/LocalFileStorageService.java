package storage;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path root;

    public LocalFileStorageService(
            @Value("${app.file-storage.root-dir}") String rootDir) {
        this.root = Paths.get(rootDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(root);
    }

    @Override
    public UUID save(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");

        // Use a random ID; do not trust original filename for path safety
        UUID id = UUID.randomUUID();

        // Try to keep extension if present
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains(".")) ?
                original.substring(original.lastIndexOf('.')) : "";
        Path target = root.resolve(id.toString() + ext).normalize();

        // Prevent path traversal
        if (!target.getParent().equals(root)) {
            throw new SecurityException("Invalid path");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return id;
    }

    @Override
    public Resource loadAsResource(UUID id) throws IOException {
        // Match any file with this UUID prefix (id + optional ext)
        try (var stream = Files.list(root)) {
            Path found = stream
                    .filter(p -> p.getFileName().toString().startsWith(id.toString()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchFileException("Not found: " + id));
            try {
                Resource resource = new UrlResource(found.toUri());
                if (resource.exists() && resource.isReadable()) return resource;
                throw new NoSuchFileException("Not readable: " + id);
            } catch (MalformedURLException e) {
                throw new IOException(e);
            }
        }
    }

    @Override
    public void delete(UUID id) throws IOException {
        try (var stream = Files.list(root)) {
            stream.filter(p -> p.getFileName().toString().startsWith(id.toString()))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        }
    }

    @Override
    public boolean exists(UUID id) {
        try (var stream = Files.list(root)) {
            return stream.anyMatch(p -> p.getFileName().toString().startsWith(id.toString()));
        } catch (IOException e) {
            return false;
        }
    }
}

