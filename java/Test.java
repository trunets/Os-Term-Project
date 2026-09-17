
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test.java
 *
 * Tests Job.java and WorkloadLoader.java together.
 *
 * Test flow: 1. Load Jobs using WorkloadLoader. 2. Verify Job objects created
 * by WorkloadLoader. 3. Verify Job fields. 4. Verify sequence numbers are
 * unique. 5. Verify Jobs are sorted by arrivalMs. 6. Test Job lifecycle state
 * changes. 7. Test Job timestamp methods. 8. Test Job derived metrics.
 *
 * All output uses ProjectLogger.
 */
public class Test {

    /**
     * Main test entry point.
     */
    public static void main(String[] args) {

        // Record the starting time used by ProjectLogger.
        long simulationStart = System.currentTimeMillis();

        // Create the project logger.
        ProjectLogger logger
                = new ProjectLogger(simulationStart);

        // Get the current thread name.
        String threadName
                = Thread.currentThread().getName();

        logger.log(
                threadName,
                "========================================"
        );

        logger.log(
                threadName,
                "        OS TERM PROJECT TEST"
        );

        logger.log(
                threadName,
                "========================================"
        );

        // --------------------------------------------------------
        // Test Job and WorkloadLoader together.
        // --------------------------------------------------------
        testWorkload(
                logger,
                threadName,
                "csv/jobs_db.csv"
        );

        testWorkload(
                logger,
                threadName,
                "csv/jobs_printer.csv"
        );

        testWorkload(
                logger,
                threadName,
                "csv/jobs_same_priority.csv"
        );

        testWorkload(
                logger,
                threadName,
                "csv/jobs_single.csv"
        );

        testWorkload(
                logger,
                threadName,
                "csv/jobs_standard.csv"
        );

        logger.log(
                threadName,
                "========================================"
        );

        logger.log(
                threadName,
                "           ALL TESTS FINISHED"
        );

        logger.log(
                threadName,
                "========================================"
        );
    }

    /**
     * Tests WorkloadLoader and the Job objects it creates.
     *
     * The Jobs are obtained directly from WorkloadLoader, so Test does not
     * manually construct Job objects.
     */
    private static void testWorkload(
            ProjectLogger logger,
            String threadName,
            String csvPath) {

        logger.log(
                threadName,
                ""
        );

        logger.log(
                threadName,
                "========================================"
        );

        logger.log(
                threadName,
                "Testing: " + csvPath
        );

        logger.log(
                threadName,
                "========================================"
        );

        try {

            // ----------------------------------------------------
            // Load Jobs using WorkloadLoader.
            // WorkloadLoader is responsible for constructing Job.
            // ----------------------------------------------------
            List<Job> jobs
                    = WorkloadLoader.load(csvPath);

            logger.log(
                    threadName,
                    "PASS: WorkloadLoader loaded CSV"
            );

            logger.log(
                    threadName,
                    "Number of Jobs = " + jobs.size()
            );

            // ----------------------------------------------------
            // Test that at least one Job exists.
            // ----------------------------------------------------
            if (jobs.isEmpty()) {

                logger.log(
                        threadName,
                        "FAIL: Workload contains no Jobs"
                );

                return;
            }

            logger.log(
                    threadName,
                    "PASS: Job objects exist"
            );

            // ----------------------------------------------------
            // Test Job data.
            // ----------------------------------------------------
            testJobData(
                    logger,
                    threadName,
                    jobs
            );

            // ----------------------------------------------------
            // Test sequence number uniqueness.
            // ----------------------------------------------------
            testSequenceNumbers(
                    logger,
                    threadName,
                    jobs
            );

            // ----------------------------------------------------
            // Test arrival time sorting.
            // ----------------------------------------------------
            testArrivalOrder(
                    logger,
                    threadName,
                    jobs
            );

            // ----------------------------------------------------
            // Test Job lifecycle and timestamps.
            // ----------------------------------------------------
            testJobLifecycle(
                    logger,
                    threadName,
                    jobs.get(0),
                    simulationTime(logger)
            );

            // ----------------------------------------------------
            // Print all Jobs loaded from the CSV.
            // ----------------------------------------------------
            printJobs(
                    logger,
                    threadName,
                    jobs
            );

        } catch (IOException e) {

            // Log the actual error message.
            logger.log(
                    threadName,
                    "FAIL: " + csvPath + " -> " + e.getMessage()
            );
        }
    }

    /**
     * Tests the immutable data stored inside Job.
     */
    private static void testJobData(
            ProjectLogger logger,
            String threadName,
            List<Job> jobs) {

        logger.log(
                threadName,
                "--- Testing Job Data ---"
        );

        boolean valid = true;

        for (Job job : jobs) {

            // ID must exist.
            if (job.getId() == null
                    || job.getId().isEmpty()) {

                logger.log(
                        threadName,
                        "FAIL: Job has invalid ID"
                );

                valid = false;
            }

            // Arrival time cannot be negative.
            if (job.getArrivalMs() < 0) {

                logger.log(
                        threadName,
                        "FAIL: "
                        + job.getId()
                        + " has negative arrivalMs"
                );

                valid = false;
            }

            // Work time cannot be negative.
            if (job.getWorkMs() < 0) {

                logger.log(
                        threadName,
                        "FAIL: "
                        + job.getId()
                        + " has negative workMs"
                );

                valid = false;
            }

            // Resource time cannot be negative.
            if (job.getResourceMs() < 0) {

                logger.log(
                        threadName,
                        "FAIL: "
                        + job.getId()
                        + " has negative resourceMs"
                );

                valid = false;
            }

            // Resource must not be null.
            if (job.getResource() == null) {

                logger.log(
                        threadName,
                        "FAIL: "
                        + job.getId()
                        + " has null resource"
                );

                valid = false;
            }
        }

        if (valid) {

            logger.log(
                    threadName,
                    "PASS: All Job data is valid"
            );
        }
    }

    /**
     * Tests that every Job has a unique sequence number.
     */
    private static void testSequenceNumbers(
            ProjectLogger logger,
            String threadName,
            List<Job> jobs) {

        logger.log(
                threadName,
                "--- Testing Sequence Numbers ---"
        );

        Set<Long> sequenceNumbers
                = new HashSet<>();

        boolean unique = true;

        for (Job job : jobs) {

            // add() returns false if the value already exists.
            if (!sequenceNumbers.add(
                    job.getSequenceNumber())) {

                logger.log(
                        threadName,
                        "FAIL: Duplicate sequenceNumber = "
                        + job.getSequenceNumber()
                );

                unique = false;
            }
        }

        if (unique) {

            logger.log(
                    threadName,
                    "PASS: All sequenceNumbers are unique"
            );
        }
    }

    /**
     * Tests that WorkloadLoader sorted Jobs by arrivalMs.
     */
    private static void testArrivalOrder(
            ProjectLogger logger,
            String threadName,
            List<Job> jobs) {

        logger.log(
                threadName,
                "--- Testing Arrival Order ---"
        );

        boolean sorted = true;

        for (int i = 1; i < jobs.size(); i++) {

            Job previous = jobs.get(i - 1);
            Job current = jobs.get(i);

            // Compare adjacent Jobs by arrivalMs.
            if (previous.getArrivalMs()
                    > current.getArrivalMs()) {

                logger.log(
                        threadName,
                        "FAIL: Jobs are not sorted at index "
                        + i
                );

                sorted = false;

                break;
            }

            // If arrivalMs is equal, sequenceNumber must preserve
            // the original CSV order.
            if (previous.getArrivalMs()
                    == current.getArrivalMs()
                    && previous.getSequenceNumber()
                    > current.getSequenceNumber()) {

                logger.log(
                        threadName,
                        "FAIL: Invalid sequence order at index "
                        + i
                );

                sorted = false;

                break;
            }
        }

        if (sorted) {

            logger.log(
                    threadName,
                    "PASS: Jobs sorted by arrivalMs"
            );
        }
    }

    /**
     * Tests the mutable lifecycle fields of Job.
     *
     * This uses the actual Job object created by WorkloadLoader.
     */
    private static void testJobLifecycle(
            ProjectLogger logger,
            String threadName,
            Job job,
            long simulationStart) {

        logger.log(
                threadName,
                "--- Testing Job Lifecycle ---"
        );

        // --------------------------------------------------------
        // Initial state.
        // --------------------------------------------------------
        if (job.getState() == Job.State.ARRIVED) {

            logger.log(
                    threadName,
                    "PASS: Initial state = ARRIVED"
            );

        } else {

            logger.log(
                    threadName,
                    "FAIL: Initial state = "
                    + job.getState()
            );
        }

        // --------------------------------------------------------
        // Test actual arrival time.
        // --------------------------------------------------------
        job.setActualArrivalTime(10);

        if (job.getActualArrivalTime() == 10) {

            logger.log(
                    threadName,
                    "PASS: actualArrivalTime setter/getter"
            );

        } else {

            logger.log(
                    threadName,
                    "FAIL: actualArrivalTime"
            );
        }

        // --------------------------------------------------------
        // Test state change to READY.
        // --------------------------------------------------------
        job.setState(Job.State.READY);

        if (job.getState() == Job.State.READY) {

            logger.log(
                    threadName,
                    "PASS: State changed to READY"
            );

        } else {

            logger.log(
                    threadName,
                    "FAIL: Could not change state to READY"
            );
        }

        // --------------------------------------------------------
        // Test state change to RUNNING.
        // --------------------------------------------------------
        job.setState(Job.State.RUNNING);

        if (job.getState() == Job.State.RUNNING) {

            logger.log(
                    threadName,
                    "PASS: State changed to RUNNING"
            );

        } else {

            logger.log(
                    threadName,
                    "FAIL: Could not change state to RUNNING"
            );
        }

        // --------------------------------------------------------
        // Test start time.
        // --------------------------------------------------------
        job.setStartTime(20);

        if (job.getStartTime() == 20) {

            logger.log(
                    threadName,
                    "PASS: startTime setter/getter"
            );

        } else {

            logger.log(
                    threadName,
                    "FAIL: startTime"
            );
        }

        // --------------------------------------------------------
        // Test resource timestamps if a resource is required.
        // --------------------------------------------------------
        if (job.getResource()
                != Job.ResourceType.NONE) {

            job.setState(
                    Job.State.WAITING_RESOURCE
            );

            job.setResourceWaitStartTime(30);

            job.setResourceAcquireTime(50);

            if (job.getResourceWaitStartTime() == 30
                    && job.getResourceAcquireTime() == 50) {

                logger.log(
                        threadName,
                        "PASS: Resource timestamps"
                );

            } else {

                logger.log(
                        threadName,
                        "FAIL: Resource timestamps"
                );
            }

            // Test resource waiting time.
            long resourceWait
                    = job.resourceWaitTime();

            if (resourceWait == 20) {

                logger.log(
                        threadName,
                        "PASS: resourceWaitTime() = 20 ms"
                );

            } else {

                logger.log(
                        threadName,
                        "FAIL: resourceWaitTime() = "
                        + resourceWait
                );
            }
        }

        // --------------------------------------------------------
        // Test completion time.
        // --------------------------------------------------------
        job.setCompletionTime(120);

        // Test waitingTime().
        long waitingTime
                = job.waitingTime();

        if (waitingTime == 10) {

            logger.log(
                    threadName,
                    "PASS: waitingTime() = 10 ms"
            );

        } else {

            logger.log(
                    threadName,
                    "FAIL: waitingTime() = "
                    + waitingTime
            );
        }

        // Test turnaroundTime().
        long turnaroundTime
                = job.turnaroundTime();

        if (turnaroundTime == 110) {

            logger.log(
                    threadName,
                    "PASS: turnaroundTime() = 110 ms"
            );

        } else {

            logger.log(
                    threadName,
                    "FAIL: turnaroundTime() = "
                    + turnaroundTime
            );
        }

        // --------------------------------------------------------
        // Test final state.
        // --------------------------------------------------------
        job.setState(Job.State.COMPLETED);

        if (job.getState() == Job.State.COMPLETED) {

            logger.log(
                    threadName,
                    "PASS: State changed to COMPLETED"
            );

        } else {

            logger.log(
                    threadName,
                    "FAIL: Could not change state to COMPLETED"
            );
        }

        logger.log(
                threadName,
                "Job lifecycle test completed for "
                + job.getId()
        );
    }

    /**
     * Prints every Job loaded by WorkloadLoader.
     *
     * Job.toString() currently returns only the Job ID, so the important fields
     * are printed separately.
     */
    private static void printJobs(
            ProjectLogger logger,
            String threadName,
            List<Job> jobs) {

        logger.log(
                threadName,
                "--- Loaded Jobs ---"
        );

        for (Job job : jobs) {

            logger.log(
                    threadName,
                    job.getId()
                    + " | arrivalMs="
                    + job.getArrivalMs()
                    + " | priority="
                    + job.getPriority()
                    + " | workMs="
                    + job.getWorkMs()
                    + " | resource="
                    + job.getResource()
                    + " | resourceMs="
                    + job.getResourceMs()
                    + " | sequence="
                    + job.getSequenceNumber()
            );
        }

        logger.log(
                threadName,
                "--- End Jobs ---"
        );
    }

    /**
     * Returns the current system time.
     *
     * This helper keeps the test code explicit about using milliseconds for
     * simulation-related timestamps.
     */
    private static long simulationTime(
            ProjectLogger logger) {

        return System.currentTimeMillis();
    }
}
