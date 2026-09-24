import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * คิวงานที่พร้อมถูกหยิบไปทำ
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * สิ่งที่คลาสนี้ต้องทำได้: - เก็บงานที่รอ Worker อยู่ -
 * หยิบงานถัดไปตามนโยบายที่เลือก (FCFS หรือ Priority) - ถูกเรียกจากหลาย Thread
 * พร้อมกันได้อย่างปลอดภัย
 *
 * ข้อกำหนดจากโจทย์ที่เกี่ยวกับคลาสนี้: - หัวข้อ 4: priority = 1 สูงสุด
 * เมื่อเท่ากันต้องมีกติกาตัดสินลำดับ (tie-break) ที่ตัดสินจากข้อมูลของ Job
 * ไม่ขึ้นกับว่า Thread ใดเข้าถึงคิวก่อน - หัวข้อ 7: ห้ามวนลูปเช็กแบบกิน CPU
 * (busy waiting) — Worker ที่ไม่มีงานทำ ต้องถูกพักไว้ ไม่ใช่วนถามซ้ำ ๆ
 *
 * จะออกแบบเป็นคลาสเดียวที่รับนโยบายเข้ามา หรือแยกเป็นสองคลาส
 * หรือใช้โครงสร้างข้อมูลสำเร็จรูปของ Java ก็ได้ ขอให้อธิบายเหตุผลได้ใน Demo
 *
 * ================================================================
 * ยังขาดการเก็บและจัดการคิวจริง ยังขาด FCFS ยังขาด Priority Scheduling ยังขาด
 * tie-break เมื่อ priority เท่ากัน โดยต้องไม่ขึ้นกับว่า Thread ไหนเข้าคิวก่อน
 * ยังขาดการรอแบบไม่ใช้ busy waiting ยังขาด size() ที่อ่านได้อย่าง thread-safe
 * เป็นหนึ่งในส่วนหลักที่ต้องออกแบบเองตาม PDF
 * ================================================================
 */
public class ReadyQueue {

    // TODO: เก็บนโยบาย (Config.Policy) และโครงสร้างข้อมูลที่ใช้เก็บงาน
    Config.Policy policy;

    // Queue ของ fcfs
    private Queue<Job> fcfsQueue;

    // Queue ของ Priority
    private PriorityQueue<Job> priorityQueue;

    public ReadyQueue(Config.Policy policy) {
        // TODO
        this.policy = policy;
        if (policy == Config.Policy.FCFS) {
            // สร้าง fcfsQueue
            fcfsQueue = new LinkedList<>();
        } else if (policy == Config.Policy.PRIORITY) {
            // สร้าง priority queue
            priorityQueue = new PriorityQueue<>(
                    (a, b) -> {
                        // เลขน้อย = piority มาก
                        int result = Integer.compare(a.getPriority(), b.getPriority());

                        // กรณีที่ piority เท่ากัน Tie-breaker ให้ใช้ Sequence Number แทน
                        if (result == 0) {
                            result = Long.compare(
                                    a.getSequenceNumber(),
                                    b.getSequenceNumber()
                            );
                        }

                        return result;
                    }
            );
        } else {
            throw new UnsupportedOperationException("TODO: ReadyQueue constructor");
        }
    }

    /**
     * ใส่งานเข้าคิว เรียกโดย Scheduler Thread
     */
    public synchronized void add(Job job) {
        // TODO
        if (policy == Config.Policy.FCFS) {
            fcfsQueue.add(job);
        } else if (policy == Config.Policy.PRIORITY) {
            priorityQueue.add(job);
        } else {
            throw new UnsupportedOperationException("TODO: ReadyQueue.add");
        }

        // Wake up Worker Threads waiting for a job
        notifyAll();
    }

    /**
     * หยิบงานถัดไปตามนโยบาย เรียกโดย Worker Thread
     *
     * ถ้ายังไม่มีงาน ต้องรอโดยไม่กิน CPU ต้องคิดด้วยว่าจะบอก Worker
     * อย่างไรเมื่อไม่มีงานเหลือแล้วและควรหยุดทำงาน
     */
    public synchronized Job take() throws InterruptedException {
        // TODO

        // wait until at least 1 job avaliable
        while (size() == 0) {
            wait();
        }

        if (policy == Config.Policy.FCFS) {
            return fcfsQueue.poll();
        } else if (policy == Config.Policy.PRIORITY) {
            return priorityQueue.poll();
        } else {
            throw new UnsupportedOperationException("TODO: ReadyQueue.take");
        }
    }

    /**
     * จำนวนงานที่รออยู่ตอนนี้ ใช้โดย Monitor — ต้องอ่านได้อย่างปลอดภัย
     */
    public synchronized int size() {
        // TODO
        if (policy == Config.Policy.FCFS) {
            return fcfsQueue.size();
        } else if (policy == Config.Policy.PRIORITY) {
            return priorityQueue.size();
        } else {
            throw new UnsupportedOperationException("TODO: ReadyQueue.size");
        }
    }
}
