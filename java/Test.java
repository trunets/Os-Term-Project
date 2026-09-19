import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Test {

    public static void main(String[] args) {

        // Record simulation start time.
        long simulationStart = System.currentTimeMillis();

        try {
            // Load jobs from the CSV file.
            List<Job> jobs =
                    WorkloadLoader.load("csv/jobs_db.csv");

            // Create the queue used by JobGenerator.
            BlockingQueue<Job> arrivalQueue =
                    new LinkedBlockingQueue<>();

            // Create the project logger.
            ProjectLogger logger =
                    new ProjectLogger(simulationStart);

            // Log the number of loaded jobs.
            logger.log(
                    "main",
                    "Loaded " + jobs.size() + " jobs"
            );

            // Create the JobGenerator.
            JobGenerator generator =
                    new JobGenerator(
                            jobs,
                            arrivalQueue,
                            logger,
                            simulationStart
                    );

            // Create the generator thread.
            Thread generatorThread =
                    new Thread(
                            generator,
                            "JobGenerator"
                    );

            // Start generating jobs.
            generatorThread.start();

            // Wait until JobGenerator finishes.
            generatorThread.join();

            // Show how many jobs were generated.
            logger.log(
                    "main",
                    "Arrival queue contains "
                            + arrivalQueue.size()
                            + " jobs"
            );

            // Display every job in the arrival queue.
            for (Job job : arrivalQueue) {

                logger.log(
                        "main",
                        "QUEUE: " + job.getId()
                );
            }

            // Test completed.
            logger.log(
                    "main",
                    "Test completed"
            );

        } catch (IOException e) {

            // Handle CSV loading errors.
            System.err.println(
                    "Failed to load workload: "
                            + e.getMessage()
            );

        } catch (InterruptedException e) {

            // Restore interrupted status.
            Thread.currentThread().interrupt();

            System.err.println(
                    "Test was interrupted."
            );
        }
    }
}