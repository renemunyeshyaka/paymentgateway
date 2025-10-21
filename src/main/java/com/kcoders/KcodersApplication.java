/*

package com.kcoders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EntityScan("com.kcoders.entity")
@EnableJpaRepositories("com.kcoders.repository")
public class KcodersApplication {
 public static void main(String[] args) {
     SpringApplication.run(KcodersApplication.class, args);
 }
}*/

@SpringBootApplication
public class KcodersApplication {
    public static void main(String[] args) {
        String port = System.getenv("PORT");
        if (port == null) {
            port = "8080"; // default for local development
        }
        SpringApplication app = new SpringApplication(KcodersApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", port));
        app.run(args);
    }
}

