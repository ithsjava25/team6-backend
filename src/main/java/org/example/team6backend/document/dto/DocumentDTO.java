package org.example.team6backend.document.dto;

import lombok.Data;

@Data
public class DocumentDTO {
    private String fileName;
    private String fileKey;
    private String contentType;
    private Long fileSize;
    private boolean image;
    private String fileUrl;
}