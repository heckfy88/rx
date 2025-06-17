package rx.operator;

import rx.core.Observable;
import rx.core.Observer;
import rx.core.OnSubscribe;

import java.util.function.Predicate;

public class FilterOperator<T> implements OnSubscribe<T> {
    private final Observable<T> source;
    private final Predicate<? super T> predicate;

    public FilterOperator(Observable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public void subscribe(Observer<? super T> observer) {
        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    if (predicate.test(item)) {
                        observer.onNext(item);
                    }
                } catch (Exception e) {
                    observer.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        });
    }
}