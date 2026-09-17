import java.util.Comparator;
/** กติกาการเรียง Job ที่ ReadyQueue ใช้ร่วมกันได้ทั้ง FCFS และ Priority */
public interface SchedulingPolicy {
    /** คืน comparator ที่ให้ลำดับแน่นอนสำหรับทุก Job */
    Comparator<Job> comparator();
    /** คืนชื่อสั้นสำหรับระบุนโยบาย */
    String name();
}
