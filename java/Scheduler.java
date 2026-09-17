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

    public Scheduler(BlockingQueue<Job> arrivalQueue, ReadyQueue readyQueue,
                      ProjectLogger logger, long simulationStart) {
        this.arrivalQueue = arrivalQueue;
        this.readyQueue = readyQueue;
        this.logger = logger;
        this.simulationStart = simulationStart;
    }

    @Override
    public void run() {
        // TODO:
        // loop:
        //   Job job = arrivalQueue.take();   // blocks — handle InterruptedException
        //   if (job is the poison pill signalling "JobGenerator done") {
        //       // forward the shutdown signal onward, e.g. put one poison-pill
        //       // Job into readyQueue per Worker thread so each Worker's
        //       // take() unblocks and can exit its loop.
        //       return;
        //   }
        //   job.setState(Job.State.READY);
        //   logger.log("Scheduler", job.getId() + " READY");
        //   readyQueue.put(job);
    }
}
