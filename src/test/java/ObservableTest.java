import org.junit.jupiter.api.Test;
import rx.core.Disposable;
import rx.core.Observable;
import rx.core.Observer;
import rx.schedulers.IOThreadScheduler;
import rx.schedulers.SingleThreadScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class ObservableTest {

    @Test
    public void shouldEmitAndCompleteSuccessfully() throws InterruptedException {
        List<String> collected = new ArrayList<>();
        CountDownLatch done = new CountDownLatch(1);

        Observable.<String>create(emitter -> {
            emitter.onNext("hello");
            emitter.onComplete();
        }).subscribe(new Observer<>() {
            @Override
            public void onNext(String item) {
                collected.add(item);
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error: " + t);
            }

            @Override
            public void onComplete() {
                done.countDown();
            }
        });

        assertTrue(done.await(1, TimeUnit.SECONDS));
        assertEquals(List.of("hello"), collected);
    }

    @Test
    public void shouldMapValuesCorrectly() throws InterruptedException {
        List<String> output = new ArrayList<>();
        CountDownLatch finish = new CountDownLatch(1);

        Observable.just("x", "y", "z")
                .map(value -> "mapped_" + value)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        output.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t);
                    }

                    @Override
                    public void onComplete() {
                        finish.countDown();
                    }
                });

        assertTrue(finish.await(1, TimeUnit.SECONDS));
        assertEquals(List.of("mapped_x", "mapped_y", "mapped_z"), output);
    }

    @Test
    public void shouldTriggerErrorCorrectly() {
        Observer<String> observer = mock(Observer.class);

        Observable.<String>create(emitter -> {
            emitter.onError(new IllegalStateException("forced error"));
        }).subscribe(observer);

        verify(observer).onError(any(IllegalStateException.class));
        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
    }

    @Test
    public void shouldRunSubscribeOnDifferentThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String originalThread = Thread.currentThread().getName();
        String[] executionThread = new String[1];

        Observable.just(42)
                .subscribeOn(new IOThreadScheduler())
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        executionThread[0] = Thread.currentThread().getName();
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t);
                    }

                    @Override
                    public void onComplete() {}
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertNotNull(executionThread[0]);
        assertNotEquals(originalThread, executionThread[0]);
    }

    @Test
    public void shouldObserveOnDifferentThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String mainThread = Thread.currentThread().getName();
        String[] callbackThread = new String[1];

        Observable.just(99)
                .observeOn(new SingleThreadScheduler())
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        callbackThread[0] = Thread.currentThread().getName();
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t);
                    }

                    @Override
                    public void onComplete() {}
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertNotNull(callbackThread[0]);
        assertNotEquals(mainThread, callbackThread[0]);
    }

    @Test
    public void shouldHandleDisposableCorrectly() {
        Disposable disposable = Observable.just(5)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {}

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t);
                    }

                    @Override
                    public void onComplete() {}
                });

        assertAll(
                () -> assertFalse(disposable.isDisposed()),
                disposable::dispose,
                () -> assertTrue(disposable.isDisposed())
        );
    }

    @Test
    public void shouldFilterValuesProperly() throws InterruptedException {
        List<String> result = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable.just("cat", "elephant", "lion")
                .filter(item -> item.length() > 4)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        result.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t);
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(List.of("elephant"), result);
    }

    @Test
    public void shouldFlatMapCorrectly() throws InterruptedException {
        List<String> result = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable.just(2, 4)
                .flatMap(i -> Observable.just(i + 1, i + 2))
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        result.add(item.toString());
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t);
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(result.containsAll(List.of("3", "4", "5", "6")));
    }

    @Test
    public void shouldSwitchThreadsCorrectly() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String mainThread = Thread.currentThread().getName();
        String[] observedThread = new String[1];

        Observable.just(7)
                .subscribeOn(new IOThreadScheduler())
                .observeOn(new SingleThreadScheduler())
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        observedThread[0] = Thread.currentThread().getName();
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t);
                    }

                    @Override
                    public void onComplete() {}
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertNotNull(observedThread[0]);
        assertNotEquals(mainThread, observedThread[0]);
    }
}