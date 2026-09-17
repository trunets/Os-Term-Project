import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * JobGenerator.java (implements Runnable — run on its own Thread)
 *
 * Owns: reading the pre-loaded workload list and releasing each Job into
 * arrivalQueue at (simulationStart + arrivalMs), sleeping between releases.
 *
 * Shared data touched: arrivalQueue (a BlockingQueue, thread-safe by design)
 * and the ProjectLogger (must be thread-safe internally).
 *
 * IMPORTANT (spec section 2): JobGenerator must NEVER put Jobs into the
 * Ready Queue directly — only into arrivalQueue. The Scheduler thread is
 * the only thing allowed to move Jobs into the Ready Queue.
 */
public class JobGenerator implements Runnable {

    private final List<Job> workload;
    private final BlockingQueue<Job> arrivalQueue;
    private final ProjectLogger logger;
    private final long simulationStart;

    public JobGenerator(List<Job> workload, BlockingQueue<Job> arrivalQueue,
                         ProjectLogger logger, long simulationStart) {
        this.workload = workload;
        this.arrivalQueue = arrivalQueue;
        this.logger = logger;
        this.simulationStart = simulationStart;
    }

    @Override
    public void run() {
        // TODO:
        // for each job in workload (WorkloadLoader should return them sorted by arrivalMs):
        //   1. sleep until simulationStart + job.getArrivalMs() (compute a positive delta, don't
        //      sleep a negative amount)
        //   2. job.setActualArrivalTime(System.currentTimeMillis() - simulationStart)
        //   3. job.setState(Job.State.ARRIVED)
        //   4. logger.log("JobGenerator", job.getId() + " ARRIVED priority=" + job.getPriority())
        //   5. arrivalQueue.put(job)   // handle InterruptedException
        //
        // After the loop: signal "no more jobs" to the Scheduler — e.g. put a
        // poison-pill Job into arrivalQueue so the Scheduler thread knows to
        // stop waiting for new arrivals once it's drained everything real.
    }
}
