package com.odos.odos_server_v2.domain.shared.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class ImageService {
  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;
  @Value("${cloud.aws.region.static}")
  private String region;

  // 이미지 업로드 (1장)
  public String uploadFile(MultipartFile file) throws IOException {
    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

    PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .contentType(file.getContentType())
            //.acl("public-read")
            .build();

    s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

    return fileName;
  }

  // 이미지 업로드 (여러장)
  public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
    return files.stream()
        .map(
            file -> {
              try {
                return uploadFile(file);
              } catch (IOException e) {
                throw new RuntimeException("파일 업로드 실패", e);
              }
            })
        .toList();
  }

  // 이미지 URL 생성 (1장)
  public String getFileUrl(String fileName) {
    // TODO: AWS S3 URL 생성 로직 구현 예정
    return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;
  }

  // 이미지 URL 생성 (여러장)
  public List<String> getFileUrls(List<String> fileNames) {
    return fileNames.stream().map(this::getFileUrl).toList();
  }
}
