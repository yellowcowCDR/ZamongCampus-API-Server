package com.litCitrus.zamongcampusServer;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
public class ZamongcampusServerApplication {

	// 이 아래 코드는 config로 따로 빼도 괜찮을듯.
	@Bean
	FirebaseMessaging firebaseMessaging() throws IOException {
		GoogleCredentials googleCredentials = GoogleCredentials
				.fromStream(new FileInputStream("/app/config/zamongcampus-server/mate-campus-firebase-adminsdk-fjhtq-2d31a08d5e.json"));
		FirebaseOptions firebaseOptions = FirebaseOptions
				.builder()
				.setCredentials(googleCredentials)
				.build();
		FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, "mate-campus");
		return FirebaseMessaging.getInstance(app);
	}

	@PostConstruct
	public void started(){
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
//		System.out.println("현재시각 : " + new Date());
//		System.out.println(LocalDateTime.now());

	}

	public static final String APPLICATION_LOCATIONS = "spring.config.location="
			+ "classpath:application.yml,"
			+ "/app/config/zamongcampus-server/application.yml,"
			+ "/app/config/zamongcampus-server/application-dev.yml,"
			+ "/app/config/zamongcampus-server/application-prod.yml,"
			+ "/app/config/zamongcampus-server/dummy-data.yml,"
			+ "/app/config/zamongcampus-server/aws.yml";

	public static void main(String[] args) {
		new SpringApplicationBuilder(ZamongcampusServerApplication.class)
				.properties(APPLICATION_LOCATIONS)
				.run(args);
	}



}

