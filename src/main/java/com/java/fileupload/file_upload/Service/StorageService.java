package com.java.fileupload.file_upload.Service;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

import com.java.fileupload.file_upload.Config.S3WebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.java.fileupload.file_upload.Repo.StorageRepo;
import com.java.fileupload.file_upload.Util.ImageUtil;
import com.java.fileupload.file_upload.model.ImageData;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class StorageService {

	@Value("${aws.region}")
	private String region;

	@Value("${aws.access-key-id}")
	private String accessKey;

	@Value("${aws.secret-access-key}")
	private String secretKey;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	@Autowired
	private StorageRepo storageRepo;

	@Autowired
	private S3WebConfig s3WebConfig;

	public String uploadImage(MultipartFile file) {
		try {
			String fileName = file.getOriginalFilename();
			S3Client s3Client = s3WebConfig.getS3Client();

			// Create a PutObjectRequest
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(bucketName)
					.key(fileName)
					.contentType(file.getContentType())
					.build();

			// Upload to S3
			try (InputStream inputStream = file.getInputStream()) {
				s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
			}

			// Generate the URL
			String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);

			// Save the metadata in the database
			storageRepo.save(ImageData.builder()
					.name(fileName)
					.type(file.getContentType())
					.url(imageUrl)
					.build());

			return "File uploaded successfully: " + fileName;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error while uploading file: " + e.getMessage();
		}
	}

	public String retrieveUrl(String name) {
		Optional<ImageData> imageData = storageRepo.findByName(name);
		return imageData.map(ImageData::getUrl).orElseThrow(() -> new RuntimeException("URL not found"));
	}

	public URL retrieveImageFromUrl(String fileName) {
		try (S3Presigner presigner = S3Presigner.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
				.build()) {

			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
					.bucket(bucketName)
					.key(fileName)
					.build();

			GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(10))
					.getObjectRequest(getObjectRequest)
					.build();

			// Generate a presigned URL for secure access to the file
			return presigner.presignGetObject(getObjectPresignRequest).url();
		} catch (Exception e) {
			throw new RuntimeException("Error fetching image from S3: " + e.getMessage());
		}
	}
	public String getImageType(String filename) {
		Optional<ImageData> imageData = storageRepo.findByName(filename);
		return imageData.map(ImageData::getType).orElse("application/octet-stream"); // Default MIME type
	}
}
