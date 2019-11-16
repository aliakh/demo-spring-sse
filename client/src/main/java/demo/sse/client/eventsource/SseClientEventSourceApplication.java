package demo.sse.client.eventsource;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class SseClientEventSourceApplication {

    public static void main(String[] args) throws InterruptedException {
        EventHandler eventHandler = new SimpleEventHandler();
        String url = "http://localhost:8080/sse-flux/v1";
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url));

        try (EventSource eventSource = builder.build()) {
            eventSource.start();

            TimeUnit.MINUTES.sleep(10);
        }
    }

}
