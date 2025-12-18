package com.agentica.eventsources.facebook.service;

import com.agentica.eventsources.facebook.domain.FacebookCredentials;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FacebookCredentialsProvider {

  public List<FacebookCredentials> findAllActive() {

    return List.of(
        FacebookCredentials.create(
            "tenant-001",
            "450683151453181",
            "RePharma",
            System.getenv("FACEBOOK_PAGE_ACCESS_TOKEN")
        )
    );
  }
}
