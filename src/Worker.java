/**
 * Thread that takes jobs from the Ready Queue and processes them.
 *
 * Required processing order:
 *
 * 1. Take Job from Ready Queue and record startTime.
 * 2. Sleep for job.workMs.
 * 3. If a resource is required, wait and acquire it.
 * 4. Record Resource Wait Time after acquire succeeds.
 * 5. Sleep for job.resourceMs while holding the resource.
 * 6. Release the resource safely.
 * 7. Record completionTime and update Statistics.
 *
 * The Worker must not use busy waiting.
 */
public class Worker extends Thread {

    // Shared objects used by all workers.
    private final ReadyQueue readyQueue;
    private final ResourceManager resources;
    private final Statistics statistics;
    private final ProjectLogger logger;

    /**
     * Create a Worker.
     *
     * The name should be:
     * Worker_1
     * Worker_2
     * Worker_3
     * ...
     */
    public Worker(
            String name,
            ReadyQueue readyQueue,
            ResourceManager resources,
            Statistics statistics,
            ProjectLogger logger
    ) {
        super(name);

        this.readyQueue = readyQueue;
        this.resources = resources;
        this.statistics = statistics;
        this.logger = logger;
    }

    /**
     * Worker main loop.
     *
     * The Worker blocks in readyQueue.take() when there is
     * no job, so it does not busy-wait.
     */
    @Override
    public void run() {

        try {

            while (true) {

                /*
                 * BlockingQueue.take() waits efficiently until
                 * a job becomes available.
                 */
                Job job = readyQueue.take();

                /*
                 * Poison pill means that the system has finished
                 * sending jobs and this Worker can shut down.
                 */
                if (job == JobGenerator.POISON_PILL) {
                    break;
                }

                /*
                 * This job is now being processed by this Worker.
                 */
                statistics.jobStarted();

                try {

                    processJob(job);

                } finally {

                    /*
                     * Always update runningJobs even if processing
                     * is interrupted or another exception occurs.
                     */
                    statistics.jobFinished();
                }
            }

        } catch (InterruptedException e) {

            /*
             * Restore the interrupt flag when the Worker is
             * interrupted while waiting for a job.
             */
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Process one Job according to the exact required order.
     */
    private void processJob(Job job)
            throws InterruptedException {

        // ========================================================
        // 1. Worker receives Job and records startTime
        // ========================================================

        job.startTime = logger.now();

        logger.jobStarted(job);

        // ========================================================
        // 2. Simulate main work
        // ========================================================

        Thread.sleep(job.workMs);

        logger.workFinished(job);

        // ========================================================
        // 3-6. Shared resource processing
        // ========================================================

        if (job.resource != ResourceType.NONE) {

            /*
             * Record the time immediately before attempting
             * to acquire the resource.
             */
            logger.resourceWaitStarted(job);

            long waitStart = logger.now();

            boolean acquired = false;

            try {

                /*
                 * ResourceManager handles the shared Semaphore.
                 *
                 * This call blocks if no permit is available.
                 */
                resources.acquire(job.resource);

                acquired = true;

                // ====================================================
                // 4. Resource acquired -> calculate wait time
                // ====================================================

                job.resourceWaitTime =
                        logger.now() - waitStart;

                logger.resourceAcquired(
                        job,
                        job.resourceWaitTime
                );

                // ====================================================
                // 5. Hold the resource
                // ====================================================

                Thread.sleep(job.resourceMs);

            } finally {

                // ====================================================
                // 6. Always release the resource
                // ====================================================

                /*
                 * Only release if this Worker actually acquired
                 * the permit.
                 *
                 * This prevents releasing a permit that this
                 * Worker does not own.
                 */
                if (acquired) {

                    resources.release(job.resource);

                    logger.resourceReleased(job);
                }
            }

        } else {

            /*
             * A Job without a resource has zero Resource Wait Time.
             */
            job.resourceWaitTime = 0;
        }

        // ========================================================
        // 7. Job completion
        // ========================================================

        job.completionTime = logger.now();

        logger.jobCompleted(job);

        /*
         * Statistics must be thread-safe because multiple
         * Worker threads can update it concurrently.
         */
        statistics.recordCompletion(job);
    }
}