import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main.java
 *
 * Owns: argument parsing/validation, object wiring, starting all Threads,
 * waiting for completion, graceful shutdown, and printing the final summary.
 *
 * Usage:
 *   java Main <workload.csv> <fcfs|priority> <workers> <printerPermits> <databasePermits>
 */
public class Main {

    public static void main(String[] args) {
        // ---- 1. Validate arguments BEFORE starting any Thread ----
        if (args.length != 5) {
            printUsageAndExit();
            return;
        }

        String workloadPath = args[0];
        String policyName = args[1].toLowerCase();
        int workerCount = 0, printerPermits = 0, databasePermits = 0;

        // TODO: parse+validate args[2..4] as positive integers; validate
        // policyName is exactly "fcfs" or "priority"; validate workloadPath
        // exists/is readable. On ANY failure: print usage and return WITHOUT
        // starting any Thread (spec section 12 — "do not silently use defaults").

        // ---- 2. Load workload (instructor-provided WorkloadLoader) ----
        List<Job> workload;
        try {
            workload = WorkloadLoader.load(workloadPath);
        } catch (Exception e) {
            System.err.println("Failed to load workload: " + e.getMessage());
            return;
        }

        // ---- 3. Create shared system objects ----
        long simulationStart = System.currentTimeMillis();
        ProjectLogger logger = new ProjectLogger(simulationStart);

        BlockingQueue<Job> arrivalQueue = new LinkedBlockingQueue<>();
        SchedulingPolicy policy = policyName.equals("priority")
                ? new PriorityPolicy() : new FcfsPolicy();
        ReadyQueue readyQueue = new ReadyQueue(policy);
        ResourceManager resourceManager = new ResourceManager(printerPermits, databasePermits);
        Statistics statistics = new Statistics();

        AtomicInteger runningJobs = new AtomicInteger(0);
        AtomicInteger completedJobs = new AtomicInteger(0);
        int totalJobs = workload.size();

        // ---- 4. Create Threads ----
        Thread generatorThread = new Thread(
                new JobGenerator(workload, arrivalQueue, logger, simulationStart), "JobGenerator");
        Thread schedulerThread = new Thread(
                new Scheduler(arrivalQueue, readyQueue, logger, simulationStart), "Scheduler");

        Thread[] workerThreads = new Thread[workerCount];
        for (int i = 0; i < workerCount; i++) {
            String name = "Worker-" + (i + 1);
            workerThreads[i] = new Thread(
                    new Worker(name, readyQueue, resourceManager, statistics,
                               logger, simulationStart, runningJobs, completedJobs),
                    name);
        }

        Thread monitorThread = new Thread(
                new Monitor(readyQueue, runningJobs, completedJobs, totalJobs,
                            resourceManager, logger, simulationStart, 1000L), "Monitor");

        // ---- 5. Start all Threads ----
        generatorThread.start();
        schedulerThread.start();
        for (Thread t : workerThreads) t.start();
        monitorThread.start();

        // ---- 6. Wait for the system to finish ----
        // TODO: design the graceful shutdown handshake described in spec
        // section 9:
        //   (a) JobGenerator finishes submitting        -> Scheduler knows via poison pill
        //   (b) all Jobs have completed                 -> e.g. a CountDownLatch(totalJobs)
        //       that each Worker counts down after statistics.recordJob(job)
        //   (c) Workers can safely stop                 -> Scheduler sends one poison-pill
        //       Job per Worker into readyQueue once (a) happens
        //   (d) Monitor can safely stop                 -> Main interrupts monitorThread
        //       after the CountDownLatch reaches zero
        // Then join() every thread (generatorThread, schedulerThread, each
        // worker, monitorThread). Do NOT call System.exit().

        // ---- 7. Print final summary ----
        long totalSimulationTimeMs = System.currentTimeMillis() - simulationStart;
        statistics.printSummary(totalSimulationTimeMs);
    }

    private static void printUsageAndExit() {
        System.err.println("Usage: java Main <workload.csv> <fcfs|priority> <workers> <printerPermits> <databasePermits>");
        System.err.println("Example: java Main jobs_standard.csv priority 3 1 2");
    }
}
