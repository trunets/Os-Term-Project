import java.util.ArrayList;
import java.util.List;

/**
 * Statistics.java
 *
 * Shared by all Worker threads — each Worker calls recordJob() once, after
 * a Job completes. Must be safe under concurrent writes from multiple
 * Workers at once, so every method that touches the internal list is
 * synchronized on this object.
 *
 * Metric formulas (spec section 10):
 *   Waiting Time        = startTime - actualArrivalTime
 *   Turnaround Time      = completionTime - actualArrivalTime
 *   Resource Wait Time   = resourceAcquireTime - resourceWaitStartTime (0 if resource == NONE)
 *   Throughput            = completedJobs / totalSimulationTimeMs
 *
 * Validation: turnaroundTime == waitingTime + workMs + resourceWaitTime + resourceMs
 * for every individual Job — check this per-Job, not just on the averages.
 */
public class Statistics {

    private final List<Job> completedJobs = new ArrayList<>();

    /** Called once per Job, by whichever Worker finished it. */
    public synchronized void recordJob(Job job) {
        // TODO: optionally validate the TAT equation here before adding
        // (useful during testing — e.g. assert or log a warning on mismatch).
        completedJobs.add(job);
    }

    public synchronized int completedCount() {
        return completedJobs.size();
    }

    // TODO: implement using the formulas above, averaged over completedJobs.
    public synchronized double averageWaitingTime() { return 0; }
    public synchronized double averageTurnaroundTime() { return 0; }

    /** Average over ONLY Jobs whose resource != NONE (per spec section 10). */
    public synchronized double averageResourceWaitTime() { return 0; }

    public synchronized double throughput(long totalSimulationTimeMs) {
        // TODO: completedCount() / totalSimulationTimeMs, with consistent units
        // (report with at least two decimal places per spec section 14).
        return 0;
    }

    /** Prints the final summary table required by spec section 14. */
    public synchronized void printSummary(long totalSimulationTimeMs) {
        // TODO
    }
}
