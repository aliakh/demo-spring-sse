package demo.sse.server.web.flux.controller;

import demo.sse.server.common.file.FolderChangeEvent;
import demo.sse.server.common.file.FolderWatchService;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/sse/flux")
public class FolderWatchController implements ApplicationListener<FolderChangeEvent> {

    private final FolderWatchService folderWatchService;

    FolderWatchController(FolderWatchService folderWatchService) {
        this.folderWatchService = folderWatchService;
    }

    private final SubscribableChannel subscribableChannel = MessageChannels.publishSubscribe().get();

    @PostConstruct
    void init() {
        folderWatchService.start(System.getProperty("user.home"));
    }

    @GetMapping(path = "/folder-watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<FolderChangeEvent.Event> getFolderWatch() {
        return Flux.create(sink -> {
            MessageHandler handler = message -> sink.next(FolderChangeEvent.class.cast(message.getPayload()).getEvent());
            sink.onCancel(() -> subscribableChannel.unsubscribe(handler));
            subscribableChannel.subscribe(handler);
        }, FluxSink.OverflowStrategy.LATEST);
    }

    @Override
    public void onApplicationEvent(FolderChangeEvent event) {
        subscribableChannel.send(new GenericMessage<>(event));
    }
}
