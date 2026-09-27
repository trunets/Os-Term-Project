import java.util.ArrayList;
import java.util.List;

/**
 * รวบรวมและคำนวณค่าที่ใช้วัดผลของการรันหนึ่งครั้ง
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ข้อกำหนดจากโจทย์ที่เกี่ยวกับคลาสนี้ (หัวข้อ 8):
 * - Waiting Time, Turnaround Time, Throughput, Resource Wait Time
 * - ต้องถูกอัปเดตจากหลาย Worker พร้อมกันได้อย่างปลอดภัย
 * - ผลต้องสอดคล้องกับสมการตรวจสอบ:
 * Turnaround = Waiting + workMs + Resource Wait + resourceMs
 * ใช้สมการนี้ตรวจงานทีละชิ้นได้ว่าค่าไหนคำนวณผิด
 *
 * ข้อควรระวัง: ค่าเฉลี่ยของ Resource Wait ให้คิดเฉพาะงานที่ใช้ resource
 * ส่วนงานที่ resource = NONE ให้ถือว่า Resource Wait เป็น 0
 * ===============================================================
 * ยังขาดการเก็บผลของ Job ที่เสร็จแล้ว
 * ยังขาด recordCompletion()
 * ยังขาด completedCount()
 * ยังขาดการคำนวณ Waiting Time
 * ยังขาด Turnaround Time
 * ยังขาด Resource Wait Time
 * ยังขาด Throughput
 * ยังขาดการคำนวณค่าเฉลี่ยแบบ thread-safe
 * ยังขาด printSummary() ตามรูปแบบผลลัพธ์ที่โจทย์กำหนด
 */
public class Statistics {

    // TODO: เก็บข้อมูลของงานที่เสร็จแล้ว หรือเก็บผลรวมไว้คำนวณทีหลัง
    private final List<Job> completedJobs = new ArrayList<>();
    private int completedCount = 0;

    /** บันทึกว่างานชิ้นหนึ่งเสร็จแล้ว เรียกโดย Worker หลายตัวพร้อมกันได้ */
    public synchronized void recordCompletion(Job job) {
        completedJobs.add(job);
        completedCount++;
    }

    /** จำนวนงานที่เสร็จแล้ว ใช้โดย Monitor และใช้ตรวจว่างานครบหรือยัง */
    public synchronized int completedCount() {
        return completedCount;
    }

    /**
     * พิมพ์ตารางสรุปผลตอนจบโปรแกรม
     * อย่างน้อยต้องมี avg Waiting Time, avg Turnaround Time,
     * Throughput และ avg Resource Wait Time
     *
     * ตามหัวข้อ 14 ให้รายงานเวลาเป็นจำนวนเต็มหน่วย ms
     * และ Throughput อย่างน้อย 2 ตำแหน่งทศนิยม
     */
    public void printSummary(List<Job> allJobs, long makespanMs) {
        List<Job> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(completedJobs);
        }

        if (snapshot.isEmpty()) {
            System.out.println("No jobs completed — nothing to summarize.");
            return;
        }

        long totalWaiting = 0;
        long totalTurnaround = 0;
        long totalResourceWait = 0;
        int resourceJobCount = 0;

        for (Job job : snapshot) {
            long waiting = job.startTime - job.actualArrivalMs;
            long turnaround = job.completionTime - job.actualArrivalMs;

            totalWaiting += waiting;
            totalTurnaround += turnaround;

            if (job.resource != ResourceType.NONE) {
                totalResourceWait += job.resourceWaitTime;
                resourceJobCount++;
            }

            // Verification equation from section 8:
            // Turnaround = Waiting + workMs + Resource Wait + resourceMs
            long expected = waiting + job.workMs + job.resourceWaitTime + job.resourceMs;
            if (expected != turnaround) {
                System.out.printf("WARNING: %s fails verification: turnaround=%d expected=%d%n",
                        job.id, turnaround, expected);
            }
        }

        double avgWaiting = (double) totalWaiting / snapshot.size();
        double avgTurnaround = (double) totalTurnaround / snapshot.size();
        double avgResourceWait = resourceJobCount == 0
                ? 0.0
                : (double) totalResourceWait / resourceJobCount;
        double throughput = snapshot.size() / (makespanMs / 1000.0);

        System.out.println("---- Summary ----");
        System.out.printf("Completed: %d / %d%n", snapshot.size(), allJobs.size());
        System.out.printf("Avg Waiting Time:     %d ms%n", Math.round(avgWaiting));
        System.out.printf("Avg Turnaround Time:  %d ms%n", Math.round(avgTurnaround));
        System.out.printf("Avg Resource Wait:    %d ms%n", Math.round(avgResourceWait));
        System.out.printf("Throughput:           %.2f jobs/sec%n", throughput);
    }
}
