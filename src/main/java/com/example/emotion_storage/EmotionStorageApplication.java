package com.example.emotion_storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class EmotionStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmotionStorageApplication.class, args);
	}

}
