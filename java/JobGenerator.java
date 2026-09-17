import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * JobGenerator.java
 *
 * Generates jobs according to their scheduled arrival time.
 *
 * This class runs on its own thread and is responsible for:
 * 1. Waiting until each job's arrival time.
 * 2. Setting the actual arrival time.
 * 3. Changing the job state to ARRIVED.
 * 4. Logging the arrival.
 * 5. Putting the job into arrivalQueue.
 *
 * IMPORTANT:
 * JobGenerator must NEVER put jobs directly into the Ready Queue.
 * Only the Scheduler is responsible for moving jobs from arrivalQueue
 * to the Ready Queue.
 */
public class JobGenerator implements Runnable {

    // Pre-loaded and sorted workload.
    private final List<Job> workload;

    // Queue used to transfer arrived jobs to the Scheduler.
    private final BlockingQueue<Job> arrivalQueue;

    // Thread-safe project logger.
    private final ProjectLogger logger;

    // Absolute system time when the simulation started.
    private final long simulationStart;

    /**
     * Creates a new JobGenerator.
     *
     * @param workload pre-loaded workload
     * @param arrivalQueue queue for arrived jobs
     * @param logger project logger
     * @param simulationStart simulation start time in milliseconds
     */
    public JobGenerator(
            List<Job> workload,
            BlockingQueue<Job> arrivalQueue,
            ProjectLogger logger,
            long simulationStart) {

        this.workload = workload;
        this.arrivalQueue = arrivalQueue;
        this.logger = logger;
        this.simulationStart = simulationStart;
    }

    /**
     * Releases each job at its scheduled arrival time.
     */
    @Override
    public void run() {

        // Process jobs in workload order.
        for (Job job : workload) {

            // Calculate the absolute time when this job should arrive.
            long targetTimeMs =
                    simulationStart + job.getArrivalMs();

            // Calculate how long the generator needs to wait.
            long delayMs =
                    targetTimeMs - System.currentTimeMillis();

            // Wait until the scheduled arrival time.
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);

                } catch (InterruptedException e) {

                    // Restore the interrupted status.
                    Thread.currentThread().interrupt();

                    // Log the interruption.
                    logger.log(
                            "JobGenerator",
                            "Interrupted while waiting for "
                                    + job.getId()
                                    + "; stopping generation."
                    );

                    // Stop generating remaining jobs.
                    return;
                }
            }

            // Record the actual arrival time relative to simulation start.
            long actualArrivalTime =
                    System.currentTimeMillis() - simulationStart;

            job.setActualArrivalTime(actualArrivalTime);

            // Mark the job as arrived.
            job.setState(Job.State.ARRIVED);

            // Log the job arrival.
            logger.log(
                    "JobGenerator",
                    job.getId()
                            + " ARRIVED priority="
                            + job.getPriority()
            );

            // Put the arrived job into arrivalQueue.
            // JobGenerator must NOT access the Ready Queue directly.
            try {
                arrivalQueue.put(job);

            } catch (InterruptedException e) {

                // Restore the interrupted status.
                Thread.currentThread().interrupt();

                // Log the interruption.
                logger.log(
                        "JobGenerator",
                        "Interrupted while enqueuing "
                                + job.getId()
                                + "; stopping generation."
                );

                // Stop generating remaining jobs.
                return;
            }
        }

        // All jobs have been released.
        logger.log(
                "JobGenerator",
                "All jobs released."
        );
    }
}