package kr.hhplus.be.commerce;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommerceApplication {
	public static void main(String[] args) {
		setUpTimeZone();
		SpringApplication.run(CommerceApplication.class, args);
	}

	private static void setUpTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

}
