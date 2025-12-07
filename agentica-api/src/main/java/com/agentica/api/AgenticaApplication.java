package com.agentica.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.agentica")
@ConfigurationPropertiesScan(basePackages = "com.agentica")
@EnableAsync
@EnableScheduling
public class AgenticaApplication {

  public static void main(final String[] args) {

    SpringApplication.run(AgenticaApplication.class, args);
  }
}
