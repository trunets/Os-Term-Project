import java.util.concurrent.BlockingQueue;

/**
 * Scheduler.java (implements Runnable — run on its own Thread)
 *
 * Owns: draining arrivalQueue and handing each Job to the ReadyQueue
 * (which applies the FCFS/Priority ordering via its Comparator).
 *
 * Shared data touched: arrivalQueue (consumer side), readyQueue (producer
 * side). Both are internally thread-safe BlockingQueue-based structures,
 * so the Scheduler needs no extra synchronization of its own.
 *
 * This thread blocks on arrivalQueue.take() rather than polling, so it
 * never busy-waits while there is nothing to schedule.
 */
public class Scheduler implements Runnable {

    private final BlockingQueue<Job> arrivalQueue;
    private final ReadyQueue readyQueue;
    private final ProjectLogger logger;
    private final long simulationStart;
    private final int totalJobs;
    private final int workerCount;

    public Scheduler(BlockingQueue<Job> arrivalQueue, ReadyQueue readyQueue,
                      ProjectLogger logger, long simulationStart,int totalJobs,int workerCount) {
        this.arrivalQueue = arrivalQueue;
        this.readyQueue = readyQueue;
        this.logger = logger;
        this.simulationStart = simulationStart;
        this.totalJobs = totalJobs;
        this.workerCount = workerCount;
    }

    @Override
    public void run() {
        int forwarded = 0;
        try {
            while (forwarded < totalJobs){
                Job job = arrivalQueue.take(); // blocks, no busy waiting
                job.setState(Job.State.READY); // BEFORE put()
                logger.log("Scheduler", job.getId() + " READY");
                readyQueue.put(job);
                forwarded++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore the flag
            logger.log("Scheduler", "Interrupted; stopping");
        } finally{
            readyQueue.putPoisonPills(workerCount); // always release the Workers
        }

    }
}
