package com.youns.portfoliospr;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PortfolioSprApplication {

  public static void main(String[] args) {
    SpringApplication.run(PortfolioSprApplication.class, args);
  }

  @Bean
  Clock applicationClock() {
    return Clock.system(ZoneId.of("Asia/Seoul"));
  }
}
