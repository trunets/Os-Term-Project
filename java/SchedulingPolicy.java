import java.util.Comparator;

/**
 * SchedulingPolicy.java
 *
 * Strategy interface for FCFS vs Priority scheduling. The Scheduler thread
 * does not implement ordering itself — it delegates to whichever policy
 * was selected on the command line, and hands the resulting Comparator to
 * the ReadyQueue.
 *
 * The comparator is used by a java.util.concurrent.PriorityBlockingQueue,
 * so it must define a total, deterministic order. Always finish with
 * sequenceNumber as the last tie-break so ordering never depends on which
 * Thread happened to reach the queue first.
 */
public interface SchedulingPolicy {

    /** @return the ordering rule the Ready Queue should use for this policy. */
    Comparator<Job> comparator();

    /** Short label for logging, e.g. "FCFS" or "PRIORITY". */
    String name();
}
