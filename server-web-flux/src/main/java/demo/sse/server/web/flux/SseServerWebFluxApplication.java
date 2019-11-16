package demo.sse.server.web.flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "demo.sse.server")
public class SseServerWebFluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SseServerWebFluxApplication.class, args);
    }
}


