package com.java.fileupload.file_upload.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3WebConfig {

	 @Bean
	    public S3Client s3Client() {
	        return S3Client.builder()
	            .region(Region.of(AWS_REGION))
	            .credentialsProvider(StaticCredentialsProvider.create(
	                AwsBasicCredentials.create(AWS_ACCESS_KEY, AWS_SECRET_KEY)))
	            .build();
	    }
}
