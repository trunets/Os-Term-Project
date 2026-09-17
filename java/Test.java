import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test.java
 *
 * Tests the project components in sequence.
 *
 * Test flow:
 *
 * WorkloadLoader
 *      ↓
 *    List<Job>
 *      ↓
 *     Job
 *      ↓
 * JobGenerator
 *
 * Each test uses the result from the previous test instead of creating
 * independent test data.
 */
public class Test {

    /**
     * Workload files provided by the project.
     */
    private static final String BASE_PATH = "csv/";

    private static final String[] FILES = {
        "jobs_db.csv",
        "jobs_printer.csv",
        "jobs_same_priority.csv",
        "jobs_single.csv",
        "jobs_standard.csv"
    };

    /**
     * Test WorkloadLoader.
     *
     * This is the first stage of the test chain.
     *
     * It loads the CSV files and returns the actual List<Job> that will
     * be passed to the next test.
     *
     * @return loaded Jobs
     */
    public static List<Job> TestWorkloadLoader() {

        System.out.println();
        System.out.println("========================================");
        System.out.println("TestWorkloadLoader");
        System.out.println("========================================");

        List<Job> allJobs = new java.util.ArrayList<>();

        for (String fileName : FILES) {

            String csvPath = BASE_PATH + fileName;

            System.out.println();
            System.out.println("Testing: " + fileName);

            try {

                // Load Jobs directly from WorkloadLoader.
                List<Job> jobs = WorkloadLoader.load(csvPath);

                System.out.println("Load: PASS");
                System.out.println("Number of jobs: " + jobs.size());

                // Check arrivalMs ordering.
                boolean sorted = true;

                for (int i = 1; i < jobs.size(); i++) {

                    if (jobs.get(i).getArrivalMs()
                            < jobs.get(i - 1).getArrivalMs()) {

                        sorted = false;
                        break;
                    }
                }

                System.out.println(
                        "Arrival time sorted: "
                        + (sorted ? "PASS" : "FAIL")
                );

                if (!sorted) {
                    throw new AssertionError(
                            "Jobs are not sorted by arrivalMs"
                    );
                }

                // Check sequenceNumber uniqueness.
                Set<Long> sequenceNumbers = new HashSet<>();

                boolean uniqueSequence = true;

                for (Job job : jobs) {

                    if (!sequenceNumbers.add(
                            job.getSequenceNumber())) {

                        uniqueSequence = false;
                        break;
                    }
                }

                System.out.println(
                        "Sequence numbers unique: "
                        + (uniqueSequence ? "PASS" : "FAIL")
                );

                if (!uniqueSequence) {
                    throw new AssertionError(
                            "Duplicate sequenceNumber found"
                    );
                }

                // Pass the exact same Job objects to the next stage.
                allJobs.addAll(jobs);

                // Print loaded Jobs.
                for (Job job : jobs) {
                    System.out.println("  " + job);
                }

            } catch (IOException e) {

                System.out.println("Load: FAIL");
                System.out.println("Error: " + e.getMessage());

                throw new RuntimeException(
                        "WorkloadLoader test failed for "
                        + fileName,
                        e
                );

            } catch (Exception e) {

                System.out.println("Load: ERROR");
                System.out.println(
                        e.getClass().getSimpleName()
                        + ": "
                        + e.getMessage()
                );

                throw e;
            }
        }

        System.out.println();
        System.out.println(
                "Total Jobs loaded: " + allJobs.size()
        );

        return allJobs;
    }

    /**
     * Test Job.
     *
     * This test DOES NOT load CSV files itself.
     *
     * It receives the List<Job> produced by TestWorkloadLoader().
     * Therefore both tests operate on the same Job objects.
     *
     * @param jobs Jobs returned from TestWorkloadLoader()
     * @return the same List<Job> after Job tests
     */
    public static List<Job> TestJob(List<Job> jobs) {

        System.out.println();
        System.out.println("========================================");
        System.out.println("TestJob");
        System.out.println("========================================");

        if (jobs == null) {
            throw new IllegalArgumentException(
                    "jobs cannot be null"
            );
        }

        if (jobs.isEmpty()) {
            throw new AssertionError(
                    "No Jobs were provided by WorkloadLoader"
            );
        }

        for (Job job : jobs) {

            System.out.println();
            System.out.println("Testing Job: " + job.getId());

            // Verify immutable data exists.
            if (job.getId() == null
                    || job.getId().isEmpty()) {

                throw new AssertionError(
                        "Job ID is invalid"
                );
            }

            // A newly loaded Job must start as ARRIVED.
            if (job.getState() != Job.State.ARRIVED) {

                throw new AssertionError(
                        "Initial Job state must be ARRIVED"
                );
            }

            // Verify the Job lifecycle fields using the same Job object.
            job.setActualArrivalTime(job.getArrivalMs());

            job.setStartTime(
                    job.getActualArrivalTime() + 10
            );

            job.setResourceWaitStartTime(
                    job.getStartTime()
            );

            job.setResourceAcquireTime(
                    job.getStartTime() + 5
            );

            job.setCompletionTime(
                    job.getStartTime() + job.getWorkMs()
            );

            // Change state to READY.
            job.setState(Job.State.READY);

            if (job.getState() != Job.State.READY) {

                throw new AssertionError(
                        "Job state change failed"
                );
            }

            // Verify waiting time.
            long expectedWaitingTime =
                    job.getStartTime()
                    - job.getActualArrivalTime();

            if (job.waitingTime() != expectedWaitingTime) {

                throw new AssertionError(
                        "Waiting time calculation failed"
                );
            }

            // Verify turnaround time.
            long expectedTurnaroundTime =
                    job.getCompletionTime()
                    - job.getActualArrivalTime();

            if (job.turnaroundTime()
                    != expectedTurnaroundTime) {

                throw new AssertionError(
                        "Turnaround time calculation failed"
                );
            }

            // Verify resource wait time.
            long expectedResourceWaitTime = 0;

            if (job.getResource()
                    != Job.ResourceType.NONE) {

                expectedResourceWaitTime =
                        job.getResourceAcquireTime()
                        - job.getResourceWaitStartTime();
            }

            if (job.resourceWaitTime()
                    != expectedResourceWaitTime) {

                throw new AssertionError(
                        "Resource wait time calculation failed"
                );
            }

            System.out.println("Job: PASS");
        }

        System.out.println();
        System.out.println("TestJob: PASS");

        // Return the SAME Job objects to the next test.
        return jobs;
    }

    /**
     * Main test chain.
     *
     * Each stage receives the result of the previous stage.
     */
    public static void main(String[] args) {

        // Stage 1:
        // CSV → WorkloadLoader → List<Job>
        List<Job> jobs = TestWorkloadLoader();

        // Stage 2:
        // List<Job> → Job tests → same List<Job>
        jobs = TestJob(jobs);

        // Stage 3 will be added after JobGenerator.java is implemented.
        //
        // jobs = TestJobGenerator(jobs);

        System.out.println();
        System.out.println("========================================");
        System.out.println("All available tests completed.");
        System.out.println("========================================");
    }
}