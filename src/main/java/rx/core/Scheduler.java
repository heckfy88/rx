package rx.core;

public interface Scheduler {
    void execute(Runnable task);
}
