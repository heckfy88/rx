# Библиотека для реактивной обработки данных с поддержкой многопоточности

## Общая информация

Разработана легкая библиотека для организации реактивных потоков в Java.   
В основе лежит концепция обработки последовательностей данных с возможностью их преобразования и переключения между потоками.  
Библиотека предоставляет инструменты для создания асинхронных решений с поддержкой многопоточности,  
а также управление временем жизни подписок.

Основные возможности:
- Создание потоков данных (Observable)
- Поддержка трансформаций через `map`, `filter`, `flatMap`
- Гибкое управление потоками выполнения (Schedulers)
- Безопасная отмена подписок (Disposable)

## Структура решения

В библиотеке определены следующие ключевые элементы:

- **Observable\<T>** — источник событий. Позволяет создавать поток данных и применять к нему последовательность операций.

- **Observer\<T>** — подписчик, который принимает данные, обрабатывает ошибки и отслеживает завершение потока.

- **Disposable** — объект, отвечающий за корректное завершение подписки и освобождение ресурсов.

- **Schedulers** — абстракция для управления потоками, предоставляющая гибкость в выборе, где и как будет выполняться обработка данных.

- **Операторы** — вспомогательные инструменты для изменения, фильтрации и объединения элементов в потоке.

## Планировщики и работа с потоками

Для управления многопоточностью в библиотеке предусмотрено несколько типов Schedulers:

- `IOThreadScheduler` — выполняет задачи в пуле потоков, рассчитанном на IO-нагрузку. Актуален для операций с задержками (сеть, файлы).

- `ComputationScheduler` — использует фиксированный пул потоков, соответствующий количеству доступных ядер. Подходит для интенсивных вычислений.

- `SingleThreadScheduler` — выполняет все задачи последовательно в одном потоке. Полезен в случаях, когда требуется строгий порядок выполнения.

## Проверка работоспособности

Функциональность библиотеки протестирована с использованием JUnit и Mockito. Были проверены следующие сценарии:

- Последовательная передача элементов от источника к подписчику.
- Корректная работа операторов `map`, `filter` и `flatMap`.
- Обработка исключений в потоке.
- Корректное переключение между потоками с использованием Schedulers.
- Реакция на отмену подписки и освобождение ресурсов.

## Примеры

### Создание потока и получение данных
```java
Observable.<String>create(emitter -> {
    emitter.onNext("Data 1");
    emitter.onNext("Data 2");
    emitter.onComplete();
}).subscribe(new Observer<>() {
    @Override
    public void onNext(String item) {
        System.out.println("Received: " + item);
    }

    @Override
    public void onError(Throwable t) {
        System.err.println("Stream error: " + t.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("Stream finished.");
    }
});
```

### Применение операторов
```java
Observable.just("one", "two", "three")
    .filter(s -> s.contains("o"))
    .map(String::toUpperCase)
    .subscribe(new Observer<>() {
        @Override
        public void onNext(String item) {
            System.out.println("Processed: " + item);
        }

        @Override
        public void onError(Throwable t) {
            System.err.println("Error occurred: " + t.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("Processing complete.");
        }
    });
```

### Управление потоками с помощью Schedulers
```java
Observable.just(100)
    .subscribeOn(new IOThreadScheduler())
    .observeOn(new SingleThreadScheduler())
    .subscribe(new Observer<>() {
        @Override
        public void onNext(Integer item) {
            System.out.println("Item: " + item);
        }

        @Override
        public void onError(Throwable t) {
            System.err.println("Error: " + t.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("Stream completed on scheduler.");
        }
    });
```
