package demo.controller;

import demo.service.ReactiveService;
import demo.service.ScheduledService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {

    @Autowired
    private ScheduledService scheduledService;

    @Autowired
    private ReactiveService reactiveService;

    @RequestMapping(path = "/sse/infinite", method = RequestMethod.GET)
    public SseEmitter getInfiniteMessages() {
        return scheduledService.getInfiniteMessages();
    }

    @RequestMapping(path = "/sse/finite/{count}", method = RequestMethod.GET)
    public SseEmitter getFiniteMessages(@PathVariable int count) {
        return reactiveService.getFiniteMessages(count);
    }
}
