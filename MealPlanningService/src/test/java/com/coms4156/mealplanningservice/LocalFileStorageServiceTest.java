package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coms4156.mealplanningservice.service.LocalFileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

/** Exercises the real local storage rather than the Spring test double. */
@Timeout(30)
class LocalFileStorageServiceTest {

  /** Isolates every filesystem operation from the repository's seed files. */
  @TempDir
  private Path directory;

  @Test
  void createsAndReplacesTheRequestedFile() throws IOException {
    LocalFileStorageService storage = new LocalFileStorageService(directory.toString());

    storage.writeFile("mealplans.json", "[]");
    assertEquals("[]", storage.readFile("mealplans.json"));
    storage.writeFile("mealplans.json", "[{\"id\":\"new\"}]");

    assertEquals("[{\"id\":\"new\"}]", storage.readFile("mealplans.json"));
    assertEquals(List.of("mealplans.json"), entries());
  }

  @Test
  void concurrentReadersOnlySeeCompleteJsonVersions() throws Exception {
    LocalFileStorageService storage = new LocalFileStorageService(directory.toString());
    String oldJson = "[{\"data\":\"" + "a".repeat(2 * 1024 * 1024) + "\"}]";
    String newJson = "[{\"data\":\"" + "b".repeat(2 * 1024 * 1024) + "\"}]";
    storage.writeFile("mealplans.json", oldJson);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch writing = new CountDownLatch(1);
    CountDownLatch firstRead = new CountDownLatch(1);
    AtomicBoolean finished = new AtomicBoolean();
    AtomicInteger reads = new AtomicInteger();
    try (ExecutorService workers = Executors.newFixedThreadPool(2)) {
      Future<?> writer = workers.submit(() -> {
        try {
          assertTrue(start.await(5, TimeUnit.SECONDS));
          writing.countDown();
          for (int index = 0; index < 40; index++) {
            storage.writeFile("mealplans.json",
                index % 2 == 0 ? newJson : oldJson);
            if (index == 0) {
              assertTrue(firstRead.await(5, TimeUnit.SECONDS));
            }
          }
          return null;
        } finally {
          finished.set(true);
        }
      });
      Future<?> reader = workers.submit(() -> {
        assertTrue(start.await(5, TimeUnit.SECONDS));
        assertTrue(writing.await(5, TimeUnit.SECONDS));
        do {
          String observed = storage.readFile("mealplans.json");
          firstRead.countDown();
          assertTrue(oldJson.equals(observed) || newJson.equals(observed),
              "A reader observed empty, partial, or mixed JSON: "
                  + observed.length() + " characters");
          reads.incrementAndGet();
        } while (!finished.get());
        return null;
      });

      start.countDown();
      reader.get(15, TimeUnit.SECONDS);
      writer.get(15, TimeUnit.SECONDS);
      assertTrue(reads.get() > 0, "A reader must run during replacements");
      assertEquals(oldJson, storage.readFile("mealplans.json"));
      assertEquals(List.of("mealplans.json"), entries());
    } finally {
      start.countDown();
      firstRead.countDown();
    }
  }

  @Test
  void failedReplacementPreservesDestinationAndRemovesTemporaryFile()
      throws IOException {
    LocalFileStorageService storage = new LocalFileStorageService(directory.toString());
    Path destination = Files.createDirectory(directory.resolve("mealplans.json"));
    Path existing = destination.resolve("existing.json");
    Files.writeString(existing, "[{\"id\":\"preserved\"}]");

    assertThrows(IOException.class,
        () -> storage.writeFile("mealplans.json", "[]"));

    assertEquals("[{\"id\":\"preserved\"}]", Files.readString(existing));
    assertEquals(List.of("mealplans.json"), entries());
  }

  /**
   * Lists names so leaked temporary files cannot escape the assertions.
   *
   * @return sorted names in the storage directory
   * @throws IOException when the directory cannot be listed
   */
  private List<String> entries() throws IOException {
    try (var paths = Files.list(directory)) {
      return paths.map(path -> path.getFileName().toString()).sorted().toList();
    }
  }
}
