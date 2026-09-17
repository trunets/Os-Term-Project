import java.util.Comparator;
/** กำหนดลำดับ FCFS โดยเลือกงานที่มี sequenceNumber น้อยที่สุดก่อน */
public class FcfsPolicy implements SchedulingPolicy {
    @Override public Comparator<Job> comparator() { return Comparator.comparingLong(Job::getSequenceNumber); }
    @Override public String name() { return "FCFS"; }
}
