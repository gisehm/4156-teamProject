package com.coms4156.mealplanningservice;

import com.coms4156.mealplanningservice.service.StorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalStorageService implements StorageService {

  /** Isolated storage location. */
  private Path directory;

  /**
   * Allows you to set the directory.
   * 
   * @param directory directory you're interacting with
   */
  public void setDirectory(final Path directory) {
    this.directory = directory;
  }

  /** Reads a file. */
  @Override
  public String readFile(final String fileName) throws IOException {
    return Files.readString(directory.resolve(fileName));
  }

  /** Writes to a file. */
  @Override
  public void writeFile(final String fileName, final String content)
      throws IOException {
    Files.writeString(directory.resolve(fileName), content);
  }
}
