package com.scriptuit.ConfigClient3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ConfigClient3Application {

	public static void main(String[] args) {
		SpringApplication.run(ConfigClient3Application.class, args);
	}
}

@RefreshScope
@RestController
class MessageRestController {

	@Value("${message}")
    private String message;

    @RequestMapping(value = "/message", method = RequestMethod.GET)
    String getMessage() {
        return this.message;
    }
}