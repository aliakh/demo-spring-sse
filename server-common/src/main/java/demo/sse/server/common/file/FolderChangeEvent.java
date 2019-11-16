package demo.sse.server.common.file;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.StringJoiner;

public class FolderChangeEvent extends ApplicationEvent {

    private final Event event;

    public FolderChangeEvent(Object source, WatchEvent<Path> pathEvent, Path path) {
        super(source);
        this.event = new Event(pathEvent, path);
    }

    public Event getEvent() {
        return event;
    }

    public static class Event {

        private final String action;
        private final String path;

        public Event(WatchEvent<Path> event, Path path) {
            this.action = event.kind().toString();
            this.path = path.toString();
        }

        public String getAction() {
            return action;
        }

        public String getPath() {
            return path;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Event.class.getSimpleName() + "[", "]")
                    .add("action='" + action + "'")
                    .add("path='" + path + "'")
                    .toString();
        }
    }
}
