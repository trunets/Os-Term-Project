import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monitor.java (implements Runnable — runs on its own Thread)
 *
 * Owns: periodically (~1000ms) reading shared state and printing a status
 * report. Never mutates shared state — it is a read-only observer.
 *
 * Shared data read: readyQueue.size(), runningJobs, completedJobs,
 * resourceManager's available permits. All of these are safe to read
 * concurrently (PriorityBlockingQueue.size(), AtomicInteger.get(),
 * Semaphore.availablePermits() are all thread-safe reads).
 *
 * Shutdown: the Monitor loop must check an interruption signal between
 * sleeps, and must NOT be killed with Thread.stop(). Main should interrupt
 * this thread (Thread.sleep() will throw InterruptedException) once all
 * Jobs are complete, then join() it.
 */
public class Monitor implements Runnable {

    private final ReadyQueue readyQueue;
    private final AtomicInteger runningJobs;
    private final AtomicInteger completedJobs;
    private final int totalJobs;
    private final ResourceManager resourceManager;
    private final ProjectLogger logger;
    private final long simulationStart;
    private final long intervalMs;

    public Monitor(ReadyQueue readyQueue, AtomicInteger runningJobs, AtomicInteger completedJobs,
                    int totalJobs, ResourceManager resourceManager, ProjectLogger logger,
                    long simulationStart, long intervalMs) {
        this.readyQueue = readyQueue;
        this.runningJobs = runningJobs;
        this.completedJobs = completedJobs;
        this.totalJobs = totalJobs;
        this.resourceManager = resourceManager;
        this.logger = logger;
        this.simulationStart = simulationStart;
        this.intervalMs = intervalMs;
    }

    @Override
    public void run() {
        // TODO:
        // while (!Thread.currentThread().isInterrupted()) {
        //     try {
        //         Thread.sleep(intervalMs);
        //     } catch (InterruptedException e) {
        //         Thread.currentThread().interrupt();
        //         break;   // this IS the stop signal — do not swallow it
        //     }
        //     logger.log("Monitor", "STATUS ready=" + readyQueue.size()
        //         + " running=" + runningJobs.get()
        //         + " completed=" + completedJobs.get() + "/" + totalJobs
        //         + " printerAvail=" + resourceManager.availablePrinterPermits()
        //         + " dbAvail=" + resourceManager.availableDatabasePermits());
        // }
    }
}
