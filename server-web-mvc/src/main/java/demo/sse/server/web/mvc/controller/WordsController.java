package demo.sse.server.web.mvc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/sse/mvc")
public class WordsController {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceController.class);

    private static final String[] WORDS = "The quick brown fox jumps over the lazy dog.".split(" ");

    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @GetMapping(path = "/words", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter getWords(@RequestHeader(name = "Last-Event-ID", required = false) String lastId) {
        logger.info("Id of last received event: {}", lastId);

        SseEmitter emitter = new SseEmitter();
        logger.info("Emitter created: {}", emitter);

        cachedThreadPool.execute(() -> {
            try {
                for (int i = parseLastId(lastId); i < WORDS.length; i++) {
                    emitter.send(
                            SseEmitter.event()
                                    .id(String.valueOf(i))
                                    .data(WORDS[i])
                    );
                    TimeUnit.SECONDS.sleep(5);
                }

                emitter.complete();
                logger.info("Emitter completed: {}", emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
                logger.error("Emitter failed: {}", emitter, e);
            }
        });

        return emitter;
    }

    private int parseLastId(String lastId) {
        try {
            return Integer.parseInt(lastId) + 1;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
