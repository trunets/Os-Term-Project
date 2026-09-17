import java.util.concurrent.PriorityBlockingQueue;
/** คิวพร้อมทำงานที่ปลอดภัยสำหรับ Scheduler หนึ่งตัวและ Worker หลายตัว */
public class ReadyQueue {
    private final PriorityBlockingQueue<Job> queue;
    public ReadyQueue(SchedulingPolicy policy) {
        // ความจุเริ่มต้นเป็นเพียงค่าประมาณ คิวขยายขนาดได้เมื่อจำเป็น
        this.queue = new PriorityBlockingQueue<>(64, policy.comparator());
    }
    /** เพิ่มงานที่ Scheduler เปลี่ยนสถานะเป็น READY แล้ว */
    public void put(Job job) { queue.offer(job); }
    /** รอจนกว่าจะมีงานให้ Worker รับไปทำ */
    public Job take() throws InterruptedException { return queue.take(); }
    /** คืนจำนวนงานจริงในคิว ณ ขณะเรียก โดยไม่นับค่าสัญญาณหยุด */
    public int size() {
        int count = 0;
        for (Job job : queue) {
            if (!job.isPoisonPill()) count++;
        }
        return count;
    }
}
