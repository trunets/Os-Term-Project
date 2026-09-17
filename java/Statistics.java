import java.util.ArrayList;
import java.util.List;
/** เก็บผลของงานที่เสร็จแล้วแบบปลอดภัยสำหรับการเขียนพร้อมกันจาก Worker หลายตัว */
public class Statistics {
    private final List<Job> completedJobs = new ArrayList<>();
    /** บันทึกงานที่ทำเสร็จหนึ่งครั้งต่อหนึ่งงาน */
    public synchronized void recordJob(Job job) { completedJobs.add(job); }
    public synchronized int completedCount() { return completedJobs.size(); }
    /** คำนวณค่าเฉลี่ยเวลารอของงานทุกงาน หน่วยเป็นมิลลิวินาที */
    public synchronized double averageWaitingTime() { return averageOf(Job::waitingTime, completedJobs); }
    /** คำนวณค่าเฉลี่ยเวลาตั้งแต่มาถึงจนเสร็จของงานทุกงาน หน่วยเป็นมิลลิวินาที */
    public synchronized double averageTurnaroundTime() { return averageOf(Job::turnaroundTime, completedJobs); }
    /** คำนวณค่าเฉลี่ยเวลารอทรัพยากรเฉพาะงานที่ร้องขอทรัพยากร */
    public synchronized double averageResourceWaitTime() {
        List<Job> resourceJobs = new ArrayList<>();
        for (Job job : completedJobs) if (job.getResource() != Job.ResourceType.NONE) resourceJobs.add(job);
        return averageOf(Job::resourceWaitTime, resourceJobs);
    }
    /** คำนวณ throughput เป็นจำนวนงานที่เสร็จต่อวินาที */
    public synchronized double throughput(long totalSimulationTimeMs) {
        return totalSimulationTimeMs <= 0 ? 0 : completedJobs.size() * 1000.0 / totalSimulationTimeMs;
    }
    /** แสดงสรุปเมทริกซ์ตามข้อกำหนดของโครงงาน */
    public synchronized void printSummary(long totalSimulationTimeMs) {
        System.out.println("\n===== สรุปผลการจำลอง =====");
        System.out.printf("งานที่เสร็จ: %d%n", completedCount());
        System.out.printf("เวลารอเฉลี่ย: %.0f ms%n", averageWaitingTime());
        System.out.printf("เวลาตั้งแต่มาถึงจนเสร็จเฉลี่ย: %.0f ms%n", averageTurnaroundTime());
        System.out.printf("เวลารอทรัพยากรเฉลี่ย: %.0f ms%n", averageResourceWaitTime());
        System.out.printf("Throughput: %.2f jobs/second%n", throughput(totalSimulationTimeMs));
    }
    /** นิยามฟังก์ชันสำหรับอ่านค่าเวลาจาก Job */
    private interface JobMetric { long value(Job job); }
    /** รวมค่าแล้วหารด้วยจำนวนงาน โดยคืนศูนย์เมื่อลิสต์ว่าง */
    private static double averageOf(JobMetric metric, List<Job> jobs) {
        if (jobs.isEmpty()) return 0;
        long total = 0;
        for (Job job : jobs) total += metric.value(job);
        return (double) total / jobs.size();
    }
}
