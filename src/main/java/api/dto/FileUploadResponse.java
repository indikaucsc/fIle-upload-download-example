package api.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private UUID id;
    private String downloadUrl;
    private long size;
}

