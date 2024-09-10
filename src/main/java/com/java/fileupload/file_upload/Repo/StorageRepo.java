package com.java.fileupload.file_upload.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.java.fileupload.file_upload.model.ImageData;

@Repository
public interface StorageRepo extends JpaRepository<ImageData, Long> {

	Optional<ImageData> findByName(String name);
}