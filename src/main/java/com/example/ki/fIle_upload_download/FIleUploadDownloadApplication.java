package com.example.ki.fIle_upload_download;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"api.dto","storage"})
public class FIleUploadDownloadApplication {

	public static void main(String[] args) {
		SpringApplication.run(FIleUploadDownloadApplication.class, args);
	}

}
