import java.util.Comparator;

/**
 * SchedulingPolicy.java
 *
 * Strategy interface for FCFS vs Priority scheduling. The Scheduler thread
 * does not implement ordering itself — it delegates to whichever policy
 * was selected on the command line, and hands the resulting Comparator to
 * the ReadyQueue.
 *
 * The comparator is used by a java.util.concurrent.PriorityBlockingQueue
 * (wrapped by ReadyQueue), so it must define a total, deterministic order.
 * Every policy must finish with arrivalMs and then sequenceNumber as the
 * last tie-breaks, so ordering never depends on Thread timing.
 */
public interface SchedulingPolicy {

    /** @return the ordering rule the Ready Queue should use for this policy. */
    Comparator<Job> comparator();

    /** Short label for logging, e.g. "FCFS" or "PRIORITY". */
    String name();
}
