package demo.sse.server.web.flux.controller;

import demo.sse.domain.common.Performance;
import demo.sse.server.common.management.PerformanceService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/sse/flux")
public class PerformanceController {

    private final PerformanceService performanceService;

    PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping(path = "/performance", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Performance> getPerformance(@RequestParam int seconds) {
        return Flux
                .interval(Duration.ofSeconds(seconds))
                .map(sequence -> performanceService.getPerformance());
    }
}
