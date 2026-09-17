import java.util.Comparator;

/**
 * PriorityPolicy.java
 *
 * Priority scheduling: lower priority number = selected first (1 = highest).
 * When two Jobs share the same priority, fall back to a deterministic
 * tie-break based on Job data (sequenceNumber), never on Thread timing.
 */
public class PriorityPolicy implements SchedulingPolicy {

    @Override
    public Comparator<Job> comparator() {
        // TODO: verify this satisfies "deterministic tie-break based on Job data"
        // — you may swap sequenceNumber for arrival order or Job ID if your
        // design defines the tie-break differently. Keep it consistent with
        // what you write up for the report/Demo.
        return Comparator.comparingInt(Job::getPriority)
                          .thenComparingLong(Job::getSequenceNumber);
    }

    @Override
    public String name() {
        return "PRIORITY";
    }
}
