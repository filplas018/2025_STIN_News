package com.example.stin_news;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@ComponentScan(basePackages = {"clients", "controllers", "config", "services", "models", "repositories"})
@SpringBootApplication(scanBasePackages = "com.example.stin_news")
@EnableJpaRepositories(basePackages = "repositories")
@EntityScan(basePackages = {"models"})
public class StinNewsApplication {

    public static void main(String[] args) {

        /*Dotenv dotenv = Dotenv.configure()
                .directory("./.azure/default") // Nastavte cestu k adresáři, kde se nachází .env
                .load();*/
        SpringApplication.run(StinNewsApplication.class, args);
    }

}
