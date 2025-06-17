package rx.core;

public interface OnSubscribe<T> {
    /**
     * Метод, вызывающий при подписке для передачи элементов наблюдателю.
     *
     * @param observer целевой наблюдатель
     */
    void subscribe(Observer<? super T> observer);
}
