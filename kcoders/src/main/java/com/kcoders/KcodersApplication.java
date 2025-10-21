

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
}



