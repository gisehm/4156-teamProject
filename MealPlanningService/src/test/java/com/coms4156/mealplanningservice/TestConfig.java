package com.coms4156.mealplanningservice;

import com.coms4156.mealplanningservice.service.StorageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

  @Bean
  @Primary
  public StorageService storageService() {
    return new LocalStorageService();
  }
}
