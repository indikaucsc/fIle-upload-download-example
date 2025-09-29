package api.dto;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import storage.FileStorageService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v8.4/files")
@Tag(name = "Files")
public class FileController {

    private final FileStorageService storage;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file")
    public ResponseEntity<FileUploadResponse> upload(
            @RequestPart("file") @NotNull MultipartFile file) throws IOException {

        UUID id = storage.save(file);
        String downloadUrl = "/api/v8.4/files/" + id;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FileUploadResponse(id, downloadUrl, file.getSize()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Download a file by ID")
    public ResponseEntity<Resource> download(@PathVariable UUID id) throws IOException {
        Resource resource = storage.loadAsResource(id);

        ContentDisposition cd = ContentDisposition.attachment()
                .filename(resource.getFilename() != null ? resource.getFilename() : id.toString())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file by ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) throws IOException {
        storage.delete(id);
        return ResponseEntity.noContent().build();
    }
}

