package demo.service;

import demo.domain.Message;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.Date;

@Component
public class ScheduledService {

    private final EmitterProcessor<ServerSentEvent<Message>> emitter;

    public ScheduledService() {
        emitter = EmitterProcessor.create();
    }

    public Flux<ServerSentEvent<Message>> getInfiniteMessages() {
        return emitter.log();
    }

    @Scheduled(fixedRate = 1000)
    void timerHandler() {
        try {
            emitter.onNext(
                    ServerSentEvent.builder(
                            new Message(new Date().toString())
                    ).build()
            );
        } catch (Exception e) {
            emitter.onError(e);
        }
    }
}
