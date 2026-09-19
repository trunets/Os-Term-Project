/**
 * ProjectLogger.java
 *
 * PLACEHOLDER — the instructor provides a real ProjectLogger.java
 * (spec Appendix B). Replace this file with the instructor's version once
 * you have it. This stub exists only so the rest of the scaffold compiles
 * and runs standalone in the meantime.
 *
 * Required behavior: every log line includes elapsed ms since
 * simulationStart and the calling Thread's name, e.g.
 *   [0012 ms] [Worker-1] J01 START
 *
 * Must be thread-safe: JobGenerator, Scheduler, every Worker, and Monitor
 * all call log() concurrently.
 */
import java.io.FileWriter;
import java.io.IOException;
public class ProjectLogger {

    private final long simulationStart;

    public ProjectLogger(long simulationStart) {
        this.simulationStart = simulationStart;
    }

    /** threadName is passed explicitly so log lines are correct even if
     *  called from a helper method, though Thread.currentThread().getName()
     *  also works when logged directly from the owning thread. */
    public synchronized void log(String threadName, String message) {
        long elapsed = System.currentTimeMillis() - simulationStart;
        System.out.printf("[%04d ms] [%s] %s%n", elapsed, threadName, message);
        try (FileWriter writer = new FileWriter("log.txt", true)) {
            writer.write(String.format("[%04d ms] [%s] %s%n", elapsed, threadName, message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
