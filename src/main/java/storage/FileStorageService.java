package storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface FileStorageService {
    UUID save(MultipartFile file) throws IOException;
    Resource loadAsResource(UUID id) throws IOException;
    void delete(UUID id) throws IOException;
    boolean exists(UUID id);
}
