package rx.operator;

import rx.core.Observable;
import rx.core.Observer;
import rx.core.OnSubscribe;

import java.util.function.Function;

public class MapOperator<T, R> implements OnSubscribe<R> {
    private final Observable<T> source;
    private final Function<? super T, ? extends R> mapper;

    public MapOperator(Observable<T> source, Function<? super T, ? extends R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public void subscribe(Observer<? super R> observer) {
        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    observer.onNext(mapper.apply(item));
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