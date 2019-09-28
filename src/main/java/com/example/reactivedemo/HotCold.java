package com.example.reactivedemo;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

public class HotCold {

    public static void main(String[] args) throws Exception{
        Flux<Object> fibonacciGenerator = Flux.generate(
                () -> Tuples.<Long, Long>of(0L, 1L),
                (state, sink) -> {
                    System.out.println("calling");
                    sink.next(state.getT1());
                    return Tuples.of(state.getT2(), state.getT1() + state.getT2());
                }).take(10);

        ConnectableFlux<Object> connectable = fibonacciGenerator.publish();
        connectable.subscribe(t -> System.out.println("1. "+t));
        connectable.subscribe(t -> System.out.println("2. "+t));

        connectable.connect();
    }
}
