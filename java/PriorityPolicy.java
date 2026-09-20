import java.util.Comparator;

/**
 * PriorityPolicy.java
 *
 * Priority scheduling (non-preemptive): a lower priority number is selected
 * first (1 = highest).
 *
 * Tie-break chain when priorities are equal:
 *   1. arrivalMs ascending        (earlier arrival first)
 *   2. sequenceNumber ascending   (CSV row order, unique per Job)
 *
 * All three keys are immutable Job data and sequenceNumber is unique, so the
 * order is total and never depends on which Thread reached the queue first.
 */
public class PriorityPolicy implements SchedulingPolicy {

    @Override
    public Comparator<Job> comparator() {
        return Comparator.comparingInt(Job::getPriority)
                         .thenComparingLong(Job::getArrivalMs)
                         .thenComparingLong(Job::getSequenceNumber);
    }

    @Override
    public String name() {
        return "PRIORITY";
    }
}