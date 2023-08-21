package org.alpacax.trumpcardgame;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        GameHelper.dropSessionTable();
        GameHelper.createSessionTable();
        SpringApplication.run(Application.class, args);
    }
}
