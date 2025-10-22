/*

package com.paymentgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EntityScan("com.paymentgateway.entity")
@EnableJpaRepositories("com.paymentgateway.repository")
public class KcodersApplication {
 public static void main(String[] args) {
     SpringApplication.run(KcodersApplication.class, args);
 }
}
*/

package com.paymentgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EntityScan("com.paymentgateway.entity")
@EnableJpaRepositories("com.paymentgateway.repository")
public class KcodersApplication {

    public static void main(String[] args) {
        // Force the port and address binding
        System.setProperty("server.port", "10000");
        System.setProperty("server.address", "0.0.0.0");
        
        SpringApplication.run(KcodersApplication.class, args);
    }
}