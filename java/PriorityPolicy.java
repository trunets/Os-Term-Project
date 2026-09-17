import java.util.Comparator;
/** กำหนดลำดับ Priority โดยเลข priority น้อยกว่าจะมาก่อน แล้วตัดสินกรณีเท่ากันด้วย sequenceNumber */
public class PriorityPolicy implements SchedulingPolicy {
    @Override public Comparator<Job> comparator() {
        return Comparator.comparingInt(Job::getPriority).thenComparingLong(Job::getSequenceNumber);
    }
    @Override public String name() { return "PRIORITY"; }
}
