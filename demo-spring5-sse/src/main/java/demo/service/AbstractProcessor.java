package demo.service;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractProcessor<T> {

    public Flux<T> processMany(int count, ExecutorService executorService) {
        final List<Flux<T>> observables = IntStream
                .range(0, count)
                .mapToObj(i -> processOneAsync(executorService))
                .collect(Collectors.toList());
        return Flux.merge(observables);
    }

    private Flux<T> processOneAsync(ExecutorService executorService) {
        return Flux.<T>create(s -> {
            s.next(processOne());
            s.complete();
        }).subscribeOn(Schedulers.fromExecutor(executorService));
    }

    public abstract T processOne();
}
