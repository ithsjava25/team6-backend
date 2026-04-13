package org.example.team6backend.document.dto;

import lombok.Data;

@Data
public class DocumentDTO {
	private String fileName;
	private String fileKey;
	private String fileUrl;
	private boolean image;
}
