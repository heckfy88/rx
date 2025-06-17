package rx.schedulers;



import rx.core.Scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComputationScheduler implements Scheduler {
    private final ExecutorService executor;

    public ComputationScheduler() {
        int cores = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(cores);
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
}