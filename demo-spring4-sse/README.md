# demo-spring4-sse

Demo with Spring Boot 1.5 (Spring 4.3) and Server-Sent Events powered by org.springframework.web.servlet.mvc.method.annotation.SseEmitter
  
```
gradlew clean bootRun
```

```
http://localhost:8080
```

```
curl -X GET http://localhost:8080/sse/infinite
curl -X GET http://localhost:8080/sse/finite/12
```
