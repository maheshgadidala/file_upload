package com.java.fileupload.file_upload.Controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.ILoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.java.fileupload.file_upload.Service.StorageService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@RestController
public class ImageController {

	@Autowired
	private StorageService stoService;

	@PostMapping("/upload")
	public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {

		InputStream inputStream = file.getInputStream();
		String fileName = file.getOriginalFilename();

		String response = stoService.uploadImage(file);

		if (response.contains("Error")) {
			return new ResponseEntity<String>(response, HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<String>(response, HttpStatus.OK);
		}

	}

	// Endpoint to get file URL
	@GetMapping("/url/{filename}")
	public ResponseEntity<String> getFileUrl(@PathVariable("filename") String filename) {
		try {
			String url = stoService.retrieveUrl(filename);
			return ResponseEntity.ok(url);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + e.getMessage());
		}
	}

	@GetMapping("/download/{filename}")
	public ResponseEntity<byte[]> downloadImage(@PathVariable String filename) {
		try {
			// Get the presigned URL for the file
			URL presignedUrl = stoService.retrieveImageFromUrl(filename);

			// Fetch the file content using the presigned URL
			HttpURLConnection connection = (HttpURLConnection) presignedUrl.openConnection();
			connection.setRequestMethod("GET");
			InputStream inputStream = connection.getInputStream();

			// Read the input stream to byte array
			byte[] fileContent = inputStream.readAllBytes();
			inputStream.close();

			// Determine the file type from the URL or metadata
			String contentType = stoService.getImageType(filename);

			return ResponseEntity.ok()
					.header("Content-Type", contentType)
					.header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
					.body(fileContent);

		} catch (Exception e) {
			log.error("Error downloading file: " + filename, e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
}