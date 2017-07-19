package demo.service;

import demo.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ReactiveService {

    @Autowired
    private Processor processor;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public Flux<Message> getFiniteMessages(int count) {
        return processor.processMany(count, executorService);
    }
}
