package com.java.fileupload.file_upload.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.java.fileupload.file_upload.Repo.StorageRepo;
import com.java.fileupload.file_upload.Util.ImageUtil;
import com.java.fileupload.file_upload.model.ImageData;

@Service
public class StorageService {

	@Autowired
	private StorageRepo storageRepo;

	public String uploadImage(MultipartFile file) {
		try {

			byte[] compressedImage = ImageUtil.compressImage(file.getBytes());

			storageRepo.save(ImageData.builder().name(file.getOriginalFilename()).type(file.getContentType())
					.imagedata(compressedImage).build());

			return "file uploaded succesfully:" + file.getOriginalFilename();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error while uploading file:" + e.getMessage();
		}

	}

	public byte[] retrieveImage(String name) {

		ImageData imageData = storageRepo.findByName(name).orElseThrow(() -> new RuntimeException("Image not found"));

		// Decompress the image bytes before returning
		return ImageUtil.decompressImage(imageData.getImagedata());

	}

	public String getImageType(String filename) {
		Optional<ImageData> imageData = storageRepo.findByName(filename);
		return imageData.map(ImageData::getType).orElse(null);
	}

}