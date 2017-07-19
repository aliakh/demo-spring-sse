package demo.controller;

import demo.domain.Message;
import demo.service.ReactiveService;
import demo.service.ScheduledService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SseController {

    @Autowired
    private ScheduledService scheduledService;

    @Autowired
    private ReactiveService reactiveService;

    @GetMapping(name = "/sse/infinite", produces = "text/event-stream")
    public Flux<ServerSentEvent<Message>> getInfiniteMessages() {
        return scheduledService.getInfiniteMessages();
    }

    @GetMapping(path = "/sse/finite/{count}", produces = "text/event-stream")
    public Flux<Message> getFiniteMessages(@PathVariable int count) {
        return reactiveService.getFiniteMessages(count);
    }
}
