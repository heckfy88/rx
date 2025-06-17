package rx;

import rx.core.Observable;
import rx.core.Observer;
import rx.schedulers.IOThreadScheduler;
import rx.schedulers.SingleThreadScheduler;

public class Main {
    public static void main(String[] args) {
        System.out.println("Program started on thread: " + Thread.currentThread().getName());

        Observable.<String>create(emitter -> {
                    System.out.println("Emitter started on thread: " + Thread.currentThread().getName());
                    emitter.onNext("Orange");
                    emitter.onNext("Strawberry");
                    emitter.onNext("Fig");
                    emitter.onNext("Watermelon");
                    System.out.println("Emitter finished emitting on thread: " + Thread.currentThread().getName());
                    emitter.onComplete();
                })
                .subscribeOn(new IOThreadScheduler())
                .filter(s -> {
                    boolean pass = s.length() > 5;
                    System.out.println("Filtering item: " + s + " | Pass: " + pass + " | Thread: " + Thread.currentThread().getName());
                    return pass;
                })
                .map(s -> {
                    String result = s.toUpperCase();
                    System.out.println("Mapping item: " + s + " to " + result + " | Thread: " + Thread.currentThread().getName());
                    return result;
                })
                .observeOn(new SingleThreadScheduler())
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String s) {
                        System.out.println("Received item: " + s + " | Thread: " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("Error occurred: " + t.getMessage() + " | Thread: " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Processing completed! | Thread: " + Thread.currentThread().getName());
                    }
                });
    }
}