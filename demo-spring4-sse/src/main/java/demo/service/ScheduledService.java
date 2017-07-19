package demo.service;

import demo.domain.Message;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Date;

@Component
public class ScheduledService {

    private final SseEmitter emitter = new SseEmitter();

    public SseEmitter getInfiniteMessages() {
        return emitter;
    }

    @Scheduled(fixedRate = 1000)
    void timerHandler() {
        try {
            emitter.send(new Message(new Date().toString()), MediaType.APPLICATION_JSON);
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}
