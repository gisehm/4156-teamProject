package com.coms4156.mealplanningservice;

import com.coms4156.mealplanningservice.service.StorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalStorageService implements StorageService {

  private Path directory;

  public void setDirectory(Path directory) {
    this.directory = directory;
  }

  @Override
  public String readFile(String fileName) throws IOException {
    return Files.readString(directory.resolve(fileName));
  }

  @Override
  public void writeFile(String fileName, String content) throws IOException {
    Files.writeString(directory.resolve(fileName), content);
  }
}
