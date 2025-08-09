package com.odos.odos_server_v2.domain.shared.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

  // 이미지 업로드 (1장)
  public String uploadFile(MultipartFile file) throws IOException {
    // TODO: AWS S3 업로드 로직 구현 예정
    return UUID.randomUUID() + "_" + file.getOriginalFilename();
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
    return "https://example.com/" + fileName;
  }

  // 이미지 URL 생성 (여러장)
  public List<String> getFileUrls(List<String> fileNames) {
    return fileNames.stream().map(this::getFileUrl).toList();
  }
}
