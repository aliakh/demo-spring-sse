package demo.service;

import demo.domain.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.Date;

@Component
public class MessageService {

    private EmitterProcessor<Message> emitter;

    public MessageService() {
        emitter = EmitterProcessor.create();
    }

    public Flux<Message> getMessages() {
        return emitter.log();
    }

    @Scheduled(fixedRate = 1000)
    void someAsyncMethod() {
        emitter.onNext(new Message(new Date().toString()));
    }
}
