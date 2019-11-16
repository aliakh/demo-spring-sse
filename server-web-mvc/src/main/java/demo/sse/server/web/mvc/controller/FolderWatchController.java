package demo.sse.server.web.mvc.controller;

import demo.sse.server.common.file.FolderChangeEvent;
import demo.sse.server.common.file.FolderWatchService;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/sse/mvc")
public class FolderWatchController implements ApplicationListener<FolderChangeEvent> {

    private final FolderWatchService folderWatchService;

    FolderWatchController(FolderWatchService folderWatchService) {
        this.folderWatchService = folderWatchService;
    }

    private final SseEmitters emitters = new SseEmitters();

    @PostConstruct
    void init() {
        folderWatchService.start(System.getProperty("user.home"));
    }

    @GetMapping(path = "/folder-watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter getFolderWatch() {
        return emitters.add(new SseEmitter(60000L));
    }

    @Override
    public void onApplicationEvent(FolderChangeEvent event) {
        emitters.send(event.getEvent());
    }
}
