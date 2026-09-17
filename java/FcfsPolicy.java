import java.util.Comparator;

/**
 * FcfsPolicy.java
 *
 * FCFS = the Job that became READY earliest is selected first.
 * TODO: confirm sequenceNumber reflects arrival order the way your
 * JobGenerator/Scheduler assign it — it's the safest tie-break because
 * it's assigned exactly once and never depends on Thread timing.
 */
public class FcfsPolicy implements SchedulingPolicy {

    @Override
    public Comparator<Job> comparator() {
        // TODO: order by sequenceNumber ascending (earliest arrival first).
        return Comparator.comparingLong(Job::getSequenceNumber);
    }

    @Override
    public String name() {
        return "FCFS";
    }
}
