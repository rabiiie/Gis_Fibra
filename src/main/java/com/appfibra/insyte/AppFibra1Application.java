package com.appfibra.insyte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
// Escanea todos los paquetes "com.appfibra..."
// para detectar @Controller, @Service y @Repository
@ComponentScan(basePackages = {"com.appfibra"})
public class AppFibra1Application {

	public static void main(String[] args) {
		SpringApplication.run(AppFibra1Application.class, args);
	}
	
}
