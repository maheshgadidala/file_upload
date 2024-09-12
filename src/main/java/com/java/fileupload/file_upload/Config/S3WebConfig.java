package com.java.fileupload.file_upload.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import static software.amazon.awssdk.awscore.client.config.AwsClientOption.AWS_REGION;

@Configuration
public class S3WebConfig {
	@Value("${aws.region}")
	private String region;

	@Value("${aws.access-key-id}")
	private String accessKey;

	@Value("${aws.secret-access-key}")
	private String secretKey;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	 @Bean
	    public S3Client s3Client() {
	        return S3Client.builder()
	            .region(Region.of(region))
	            .credentialsProvider(StaticCredentialsProvider.create(
	                AwsBasicCredentials.create(accessKey, secretKey)))
	            .build();
	    }
}
