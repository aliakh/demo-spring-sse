package demo.sse.server.web.mvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "demo.sse.server")
public class SseServerWebMvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(SseServerWebMvcApplication.class, args);
    }
}
