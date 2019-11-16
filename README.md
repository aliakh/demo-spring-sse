# Server-Sent Events (SSE) in Spring 5 with Web MVC and Web Flux

## Introduction

There are no simple, general-purpose methods to implement asynchronous _server-to-client_ communication in web applications with acceptable performance. 

HTTP is a request-response protocol in the _client-server_ computing model. To start an exchange, a client submits a request to a server. To finish the exchange, the server returns a response to the client. The server can send a response to only _one_ client - the one that made the request. In the HTTP protocol, _a client_ is the initiator of messages exchange. 

There are cases when _a server_ should be the initiator of exchange. One of the methods to implement this is to allow the server to push messages to clients in the _publish/subscribe_ computing model. To start an exchange, a client subscribes to messages from the server. During the exchange, the server sends messages (as soon as they become available) to _many_ subscribed clients. To finish the exchange, the client cancels the subscription.

Server-Sent Events (SSE) is a _simple_ technology to implement asynchronous _server-to-client_ communication for _specific_ web applications.

## Overview

There are several technologies that allow a client to receive messages about asynchronous updates from a server. They can be divided into two categories: _client pull_ and _server push_. 

### Client pull

In _client pull_ technologies, a client periodically requests a server for updates. The server can respond with updates or with a special response that it has not yet been updated. There are two types of _client pull_: _short polling_ and _long polling_.

#### Short polling

A client periodically sends requests to a server. If the server has updates, it sends a response to the client and closes the connection. If the server has no updates, it sends a special response to the client and also closes the connection.

#### Long polling

A client sends a request to a server. If the server has updates, it sends a response to the client and closes the connection. If the server has no updates, it holds the connection until updates become available. When updates are available, the server sends a response to the client and closes the connection. If updates are not available for some timeout, the server sends a special response to the client and also closes the connection.

### Server push

In _server push_ technologies, a server proactively sends messages to clients immediately after they are available. Among others, there are two types of _server push_: Server-Sent Events and WebSocket.

#### Server-Sent Events

Server-Sent Events is a technology to send text messages only from a server to clients in browser-based web applications. Server-Sent Events is based on _persistent connections_ in the HTTP protocol. Server-Sent Events has the network protocol and the EventSource client interface [standardized](https://html.spec.whatwg.org/multipage/server-sent-events.html) by W3C as part of HTML5 standards suite.

#### WebSocket

WebSocket is a technology to implement simultaneous, bi-directional, real-time communication in web applications. WebSocket is based on a protocol other than HTTP, so it can require additional setup of network infrastructure (proxy servers, NATs, firewalls, etc). However, WebSocket can provide performance that is difficult to achieve with HTTP-based technologies.

## SSE network protocol

To subscribe to server events, a client should make a `GET` request with the headers:

*   `Accept: text/event-stream` indicates _media type_ of events required by the standard
*   `Cache-Control: no-cache` disables any events caching
*   `Connection: keep-alive` indicates that a _persistent connection_ is being used

```
GET /sse HTTP/1.1
Accept: text/event-stream
Cache-Control: no-cache
Connection: keep-alive
```

A server should confirm the subscription with a response with the headers: 

*   `Content-Type: text/event-stream;charset=UTF-8` indicates _media type_ and _encoding_ of events required by the standard
*   `Transfer-Encoding: chunked` indicates that the server streams dynamically generated content and therefore the content size is not known in advance

```
HTTP/1.1 200
Content-Type: text/event-stream;charset=UTF-8
Transfer-Encoding: chunked
```

After subscribing, the server sends messages as soon as they become available. Events are text messages in `UTF-8` encoding. Events are separated one from another by two newline characters `\n\n`. Each event consists of one or many `name: value` fields, separated by a single newline character `\n`.

In the `data` field, the server can send event data. 

```
data: The first event.

data: The second event.
```

The server can split the `data` field into several lines by a single newline character `\n`.

```
data: The third
data: event.
```

In the `id` field the server can send a unique event identifier. If a connection is broken, the client should automatically reconnect and send the last received event `id` with the header `Last-Event-ID`.

```
id: 1
data: The first event.

id: 2 
data: The second event.
```

In the `event` field the server can send event type. The server can send events of different types, as well as without any type, in the same subscription.

```
event: type1
data: An event of type1.

event: type2
data: An event of type2.

data: An event without any type.
```

In the `retry` field the server can send timeout (in milliseconds), after which the client should automatically reconnect when a connection is broken. If this field is not specified, by the standard it should be 3000 milliseconds.

```
retry: 1000
```

If a line begins with a colon character `:`, it should be ignored by the client. This can be used to send comments from the server or to prevent some proxy servers from closing the connection by timeout.

```
: ping
```

## SSE client: EventSource interface

To open a connection, it should be created an `EventSource` object.

```
var eventSource = new EventSource('/sse);
```

Despite Server-Sent Events is designed to send events _from server to client_ the it’s possible to use `GET` query parameters to pass data _from client to server_.

```
var eventSource = new EventSource('/sse?event=type1); 
...
eventSource.close();
eventSource = new EventSource('/sse?event=type1&event=type2);
...
```

To close the connection, it should be called method `close()`.

```
eventSource.close();
```

There is the `readyState` attribute that represents the state of the connection:

*   `EventSource.CONNECTING = 0` - the connection has not yet been established, or it was closed and the client is reconnecting
*   `EventSource.OPEN = 1` - the client has an open connection and is handling events as it receives them
*   `EventSource.CLOSED = 2`- the connection is not open, and the client is not trying to reconnect either there was a fatal error or the `close()` method was called

To handle an establishment of a connection, it should be subscribed to the `onopen` event handler. 

```
eventSource.onopen = function () {
   console.log('connection is established');
};
```

To handle _some_ changes in the connection state _or_ fatal errors, it should be subscribed to the `onerrror` event handler.

```
eventSource.onerror = function (event) {
    console.log('connection state: ' + eventSource.readyState + ', error: ' + event);
};
```

To handle receiving events without the `event` field, it should be subscribed to the `onmessage` event handler.

```
eventSource.onmessage = function (event) {
    console.log('id: ' + event.lastEventId + ', data: ' + event.data);
};
```

To handle receiving events with the `event` field, it should be subscribed to an event handler for such an event.

```
eventSource.addEventListener('type1', function (event) {
    console.log('id: ' + event.lastEventId + ', data: ' + event.data);
}, false);
```

EventSource client interface is implemented in [most modern browsers](https://caniuse.com/#feat=eventsource).

![https://caniuse.com/#feat=eventsource](/.images/caniuse.com-eventsource.png)

## SSE Java server: Spring Web MVC

### Introduction

Spring Web MVC framework 5.2.0 is based on Servlet 3.1 API and uses _thread pools_ to implement asynchronous Java web applications. Such applications can be run on Servlet 3.1+ containers such as Tomcat 8.5 and Jetty 9.3.

### Overview

To implement sending events with Spring Web MVC framework:

1. create a controller class and mark it with the `@RestController` annotation
2. create a method to create a client connection, that returns a [SseEmitter](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/SseEmitter.html), handles `GET` requests and produces `text/event-stream`
    1. create a new `SseEmitter`, to save it and to return it from the method
3. send events asynchronously, in another thread, get the saved `SseEmitter` and call a `SseEmitter.send` method as many times as necessary
    1. to finish sending events, call the [SseEmitter.complete()](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/ResponseBodyEmitter.html#complete--) method
    2. to finish sending events exceptionally, call the [SseEmitter.completeWithError()](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/ResponseBodyEmitter.html#completeWithError-java.lang.Throwable-) method

A simplified controller source:

```
@RestController
public class SseWebMvcController

    private SseEmitter emitter;

    @GetMapping(path="/sse", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter createConnection() {
        emitter = new SseEmitter();
        return emitter;
    }

    // in another thread
    void sendEvents() {
        try {
            emitter.send("Alpha");
            emitter.send("Omega");

            emitter.complete();
        } catch(Exception e) {
            emitter.completeWithError(e);
        }
    }
}
```

To send events with only the `data` field, it should be used the [SseEmitter.send(Object object)](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/SseEmitter.html#send-java.lang.Object-) method. To send events with the fields `data`, `id`, `event`, `retry` and comments, it should be used the  [SseEmitter.send(SseEmitter.SseEventBuilder builder)](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/SseEmitter.html#send-org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder-) method.

In the examples below, to send the same events to many clients, the [SseEmitters](https://github.com/aliakh/demo-spring-sse/blob/master/server-mvc/src/main/java/demo/sse/server/mvc/controller/SseEmitters.java) class was implemented. To create a client connection, there is the `add(SseEmitter emitter)` method that saves a `SseEmitter` in a thread-safe container. To send events asynchronously, there is the `send(Object obj)` method that sends the same event to all connected clients.

A simplified class source:

```
class SseEmitters {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    SseEmitter add(SseEmitter emitter) {
        this.emitters.add(emitter);

        emitter.onCompletion(() -> {
            this.emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            emitter.complete();
            this.emitters.remove(emitter);
        });

        return emitter;
    }

    void send(Object obj) {
        List<SseEmitter> failedEmitters = new ArrayList<>();

        this.emitters.forEach(emitter -> {
            try {
                emitter.send(obj);
            } catch (Exception e) {
                emitter.completeWithError(e);
                failedEmitters.add(emitter);
            }
        });

        this.emitters.removeAll(failedEmitters);
    }
}
```

### Handling short-lasting periodic events stream

In this example, a server sends a _short-lasting periodic events_ stream - a finite stream of words (_[The quick brown fox jumps over the lazy dog](https://en.wikipedia.org/wiki/The_quick_brown_fox_jumps_over_the_lazy_dog)_ pangram) every second, until the words are finished. 

To implement this, the mentioned [SseEmitters](https://github.com/aliakh/demo-spring-sse/blob/master/server-mvc/src/main/java/demo/sse/server/mvc/controller/SseEmitters.java) class was used. To send events asynchronously and periodically, a _cached thread pool_ has been created. Because the events stream is short-lasting, _each client connection_ submits a _separate task_ to the thread pool, right inside the controller method. 

A simplified controller source:

```
@Controller
@RequestMapping("/sse/mvc")
public class WordsController {

   private static final String[] WORDS = "The quick brown fox jumps over the lazy dog.".split(" ");

   private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

   @GetMapping(path = "/words", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   SseEmitter getWords() {
       SseEmitter emitter = new SseEmitter();

       cachedThreadPool.execute(() -> {
           try {
               for (int i = 0; i < WORDS.length; i++) {
                   emitter.send(WORDS[i]);
                   TimeUnit.SECONDS.sleep(1);
               }

               emitter.complete();
           } catch (Exception e) {
               emitter.completeWithError(e);
           }
       });

       return emitter;
   }
}
```

An events client example with `curl` command-line tool.

```
curl -v http://localhost:8080/sse/mvc/words
```

![An events client example with curl command-line tool](/.images/words-curl.png)

An events client example with SSE URL in a browser. 

```
http://localhost:8080/sse/mvc/words
```

![An events client example with SSE URL in a browser](/.images/words-browser.png)

An events client source with `EventSource` JavaScript client.

```
<!DOCTYPE html>
<html lang="en">
<head>
   <meta charset="UTF-8">
   <title>Server-Sent Events client example with EventSource</title>
</head>
<body>
<script>
   if (window.EventSource == null) {
       alert('The browser does not support Server-Sent Events');
   } else {
       var eventSource = new EventSource('/sse/mvc/words');

       eventSource.onopen = function () {
           console.log('connection is established');
       };

       eventSource.onerror = function (error) {
           console.log('connection state: ' + eventSource.readyState + ', error: ' + event);
       };

       eventSource.onmessage = function (event) {
           console.log('id: ' + event.lastEventId + ', data: ' + event.data);

           if (event.data.endsWith('.')) {
               eventSource.close();
               console.log('connection is closed');
           }
       };
   }
</script>
</body>
</html>
```

An events client example with `EventSource` JavaScript client in a browser. There is used automatic reconnect on the client-side and the [implemented reconnect](https://github.com/aliakh/demo-spring-sse/blob/master/server-web-mvc/src/main/java/demo/sse/server/web/mvc/controller/WordsController.java) on the server-side.

![An events client example with EventSource JavaScript client in a browser](/.images/words-eventsource.png)

### Handling long-lasting periodic events

In this example, a server sends _long-lasting periodic events_ stream - a potentially infinite stream of server performance information every second: 

*   committed virtual memory size
*   total swap space size
*   free swap space size
*   total physical memory size
*   free physical memory size
*   system CPU load
*   process CPU load

To implement this the [PerformanceService](https://github.com/aliakh/demo-spring-sse/blob/master/server-common/src/main/java/demo/sse/server/common/management/PerformanceService.java) class was implemented which uses the [OperatingSystemMXBean](https://docs.oracle.com/en/java/javase/12/docs/api/jdk.management/com/sun/management/OperatingSystemMXBean.html) class to read performance information from an operations system. Also was used the mentioned [SseEmitters](https://github.com/aliakh/demo-spring-sse/blob/master/server-mvc/src/main/java/demo/sse/server/mvc/controller/SseEmitters.java) class. To send events asynchronously and periodically, a _scheduled thread pool_ has been created. Because the events stream is long-lasting, _a single task_ is submitted to the thread pool to send events to _all clients_ simultaneously. 

A simplified controller example:

```
@RestController
@RequestMapping("/sse/mvc")
public class PerformanceController {

   private final PerformanceService performanceService;

   PerformanceController(PerformanceService performanceService) {
       this.performanceService = performanceService;
   }

   private final AtomicInteger id = new AtomicInteger();

   private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

   private final SseEmitters emitters = new SseEmitters();

   @PostConstruct
   void init() {
       scheduledThreadPool.scheduleAtFixedRate(() -> {
           emitters.send(performanceService.getPerformance());
       }, 0, 1, TimeUnit.SECONDS);
   }

   @GetMapping(path = "/performance", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   SseEmitter getPerformance() {
       return emitters.add();
   }
}
```

An events client example with [Highcharts](https://www.highcharts.com/) JavaScript library to draw charts of server performance.

![An events client example with Highcharts JavaScript library](/.images/performance-highcharts.png)

### Handling aperiodic events

In this example, a server sends aperiodic events stream about changes of files (create, modify, delete) in a folder being watched. As the folder is used the current user’s home folder available by the `System.getProperty("user.home")` property. 

To implement this the [FolderWatchService](https://github.com/aliakh/demo-spring-sse/blob/master/server-common/src/main/java/demo/sse/server/common/file/FolderWatchService.java) class was implemented which uses Java NIO files watch features. Also was used the mentioned [SseEmitters](https://github.com/aliakh/demo-spring-sse/blob/master/server-mvc/src/main/java/demo/sse/server/mvc/controller/SseEmitters.java) class. To send events asynchronously and aperiodically, the [FolderWatchService](https://github.com/aliakh/demo-spring-sse/blob/master/server-common/src/main/java/demo/sse/server/common/file/FolderWatchService.java) class produces Spring applications events, that are consumed by the controller (by implementing a listener method).

A simplified server example:

```
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
       return emitters.add(new SseEmitter());
   }

   @Override
   public void onApplicationEvent(FolderChangeEvent event) {
       emitters.send(event.getEvent());
   }
}
```

An events client example using `EventSource` JavaScript client.

![An events client example using EventSource JavaScript client](/.images/folder-watch-eventsource.png)

## SSE Java server: Spring Web Flux

### Introduction

Spring Web Flux framework 5.2.0 is based on Reactive Streams API and uses the _event-loop_ computing model to implement asynchronous Java web applications. Such applications can be run on non-blocking web servers such as Netty 4.1 and Undertow 1.4 _and_ on Servlet 3.1+ containers such as Tomcat 8.5 and Jetty 9.3.

### Overview

To implement sending events with Spring Web Flux framework:

1. create a controller class and mark it with the `@RestController` annotation
2. create a method to create a client connection and to send events, that returns a [Flux](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html), handles `GET` requests and produces `text/event-stream`
    1. create a new `Flux` and return it from the method

A simplified controller source:

```
@RestController
public class ExampleController

    @GetMapping(path="/sse", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> createConnectionAndSendEvents() {
        return Flux.just("Alpha", "Omega");
    }
}
```

To send events with only the `data` field, it should be used the `Flux<T>` type. To send events with the fields `data`, `id`, `event`, `retry` and comments, it should be used the `Flux<ServerSentEvent<T>>` type.

### Handling short-lasting periodic events stream

In this example, a server sends a _short-lasting periodic events_ stream - a finite stream of words (_[The quick brown fox jumps over the lazy dog](https://en.wikipedia.org/wiki/The_quick_brown_fox_jumps_over_the_lazy_dog)_ pangram) every second, until the words are finished. 

To implement this:

*   create a `Flux` of the words `Flux.just(WORDS)` of type `Flux<String>`
*   create a `Flux` that emits incrementing `long` values every second `Flux.interval(Duration.ofSeconds(1))`  of type `Flux<Long>`
*   combine them together by `zip` method to type `Flux<Tuple2<String,Long>>`
*   extract the first element of the tuple by `map(Tuple2::getT1)` of type `Flux<String>`

A simplified controller source:

```
@RestController
@RequestMapping("/sse/flux")
public class WordsController {

   private static final String[] WORDS = "The quick brown fox jumps over the lazy dog.".split(" ");

   @GetMapping(path = "/words", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   Flux<String> getWords() {
       return Flux
               .zip(Flux.just(WORDS), Flux.interval(Duration.ofSeconds(1)))
               .map(Tuple2::getT1);
   }
}
```

The event clients for this example are identical to those used in the Web MVC example.

### Handling long-lasting periodic events

In this example, a server sends _long-lasting periodic events_ stream - a potentially infinite stream of server performance information every second.

To implement this:

*   create a `Flux` that emits incrementing `long` values every second `Flux.interval(Duration.ofSeconds(1))` of type `Flux<Long>`
*   convert it by `map(sequence -> performanceService.getPerformance())` method to type `Flux<Performance>`

A simplified controller example:

```
@RestController
@RequestMapping("/sse/flux")
public class PerformanceController {

   private final PerformanceService performanceService;

   PerformanceController(PerformanceService performanceService) {
       this.performanceService = performanceService;
   }

   @GetMapping(path = "/performance", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   Flux<Performance> getPerformance() {
       return Flux
               .interval(Duration.ofSeconds(1))
               .map(sequence -> performanceService.getPerformance());
   }
}
```

The event client for this example is identical to that used in the Web MVC example.

### Handling aperiodic events

In this example, a server sends aperiodic events stream about changes of files (create, modify, delete) in a folder being watched. As the folder is used the current user’s home folder available by the `System.getProperty("user.home")` property. 

To implement this the [FolderWatchService](https://github.com/aliakh/demo-spring-sse/blob/master/server-common/src/main/java/demo/sse/server/common/file/FolderWatchService.java) class was implemented which uses Java NIO files watch features. To send events asynchronously and aperiodically, the [FolderWatchService](https://github.com/aliakh/demo-spring-sse/blob/master/server-common/src/main/java/demo/sse/server/common/file/FolderWatchService.java) class produces Spring applications events, that are consumed by the controller (by implementing a listener method). The controller listener method sends events to a `SubscribableChannel`, that is subscribed in a controller method to produce `Flux` of events.

A simplified controller example:

```
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
```

The event client for this example is identical to that used in the Web MVC example.

## SSE limitations

There are limitations of SSE _by design_:

*   it's possible to send messages in only one direction, from server to clients
*   it's possible to send only text messages; despite it’s possible to use `Base64` encoding and `gzip` compression to send binary messages, it can be inefficient.

But there are also limitations of SSE  _by implementation_:

*   Internet Explorer/Edge and many mobile browsers don’t support SSE; despite it’s possible to use _polyfills_, they can be inefficient
*   many browsers allow opening a very limited number of SSE connections (up to 6 connections _per browser_ for Chrome, Firefox)

## Conclusion

Complete code examples are available in the [GitHub repository](https://github.com/aliakh/demo-spring-sse).
