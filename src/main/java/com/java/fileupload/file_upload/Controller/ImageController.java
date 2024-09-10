package com.java.fileupload.file_upload.Controller;

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

@RestController
public class ImageController {

	@Autowired
	private StorageService stoService;

	@PostMapping("/upload")
	public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file) {
		
		String response = stoService.uploadImage(file);
		
		if (response.contains("Error")) {
			return new ResponseEntity<String>(response, HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<String>(response, HttpStatus.OK);
		}

	}
	@GetMapping("/download/{filename}")
	public ResponseEntity<byte[]> downloadImage(@PathVariable String filename){
		byte[] image=stoService.retrieveImage(filename);
		
		String contentType=stoService.getImageType(filename);
		
		if (image==null|| contentType==null) {

			return ResponseEntity.badRequest().build();
		}
		 return  ResponseEntity.ok().contentType(MediaType.valueOf(contentType)).body(image);
			
		}
		
	
}