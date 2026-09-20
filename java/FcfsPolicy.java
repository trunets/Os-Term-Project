import java.util.Comparator;

/**
 * FcfsPolicy.java
 *
 * FCFS = the Job that arrived (and so became READY) earliest is selected first.
 *
 * Ordering rule: arrivalMs ascending, then sequenceNumber ascending.
 * Both fields are immutable Job data, so the order never depends on Thread
 * timing. WorkloadLoader assigns sequenceNumber in CSV row order, so it
 * breaks ties between Jobs with the same arrivalMs exactly as they appear
 * in the file.
 *
 * actualArrivalTime is deliberately NOT used: it is volatile, starts at -1,
 * and is set by another thread, so it is unsafe inside a comparator.
 */
public class FcfsPolicy implements SchedulingPolicy {

    @Override
    public Comparator<Job> comparator() {
        return Comparator.comparingLong(Job::getArrivalMs)
                         .thenComparingLong(Job::getSequenceNumber);
    }

    @Override
    public String name() {
        return "FCFS";
    }
}