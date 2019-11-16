package demo.sse.server.web.flux.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;

@RestController
@RequestMapping("/sse/flux")
public class WordsController {

    private static final String[] WORDS = "The quick brown fox jumps over the lazy dog.".split(" ");

    @GetMapping(path = "/words", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> getWords() {
        return Flux
                .zip(Flux.just(WORDS), Flux.interval(Duration.ofSeconds(1)))
                .map(Tuple2::getT1);
    }
}
