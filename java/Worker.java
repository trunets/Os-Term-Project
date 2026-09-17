import java.util.concurrent.atomic.AtomicInteger;

/**
 * Worker.java (implements Runnable — multiple instances run on their own Threads)
 *
 * Owns: pulling Jobs from readyQueue and executing processJob() per the
 * required 7-step order (spec section 6).
 *
 * Shared data touched:
 *   - readyQueue                        (thread-safe consumer)
 *   - resourceManager                   (Semaphores are thread-safe by design)
 *   - statistics                        (must synchronize internally — many Workers write concurrently)
 *   - runningJobs / completedJobs counters (AtomicInteger, also read by Monitor)
 *
 * Interruption: a Worker can be interrupted while blocked in readyQueue.take(),
 * Thread.sleep(), or semaphore.acquire(). Every acquired Semaphore permit
 * MUST be released even on interruption — use try/finally around the
 * acquire→use→release sequence.
 */
public class Worker implements Runnable {

    private final String name;
    private final ReadyQueue readyQueue;
    private final ResourceManager resourceManager;
    private final Statistics statistics;
    private final ProjectLogger logger;
    private final long simulationStart;
    private final AtomicInteger runningJobs;
    private final AtomicInteger completedJobs;

    public Worker(String name, ReadyQueue readyQueue, ResourceManager resourceManager,
                   Statistics statistics, ProjectLogger logger, long simulationStart,
                   AtomicInteger runningJobs, AtomicInteger completedJobs) {
        this.name = name;
        this.readyQueue = readyQueue;
        this.resourceManager = resourceManager;
        this.statistics = statistics;
        this.logger = logger;
        this.simulationStart = simulationStart;
        this.runningJobs = runningJobs;
        this.completedJobs = completedJobs;
    }

    @Override
    public void run() {
        // TODO:
        // loop:
        //   Job job = readyQueue.take();  // blocks — handle InterruptedException -> break loop
        //   if (job is the poison pill) { return; }  // graceful shutdown signal from Scheduler
        //   processJob(job);
    }

    /**
     * Executes one Job following the required 7-step order (spec section 6):
     *   1. record startTime, state = RUNNING, runningJobs.incrementAndGet(), log START
     *   2. Thread.sleep(job.getWorkMs())
     *   3. if resource != NONE: record resourceWaitStartTime, state = WAITING_RESOURCE,
     *      log WAIT <resource>, acquire the right Semaphore via resourceManager,
     *      record resourceAcquireTime, log ACQUIRE <resource>
     *   4. Thread.sleep(job.getResourceMs())
     *   5. release the Semaphore in a finally block, log RELEASE <resource>
     *   6. record completionTime, state = COMPLETED, log COMPLETE
     *   7. statistics.recordJob(job); runningJobs.decrementAndGet(); completedJobs.incrementAndGet()
     *
     * TODO: implement steps 1-7. Wrap resource acquisition/use in try/finally
     * so the permit is never lost, even if Thread.sleep() throws
     * InterruptedException while the permit is held.
     */
    private void processJob(Job job) {
        // TODO
    }
}
