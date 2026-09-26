import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * ปล่อยงานเข้าสู่ระบบตามเวลา arrivalMs ของแต่ละ Job
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * หน้าที่ (หัวข้อ 3 ของโจทย์):
 *   - รอจนถึงเวลา arrivalMs ของแต่ละงาน แล้วส่งงานต่อไปยัง Scheduler
 *   - บันทึกเวลาที่งานเข้าสู่ระบบ "จริง" ลงใน Job
 *     (อาจไม่ตรงกับ arrivalMs เป๊ะ เพราะ Thread ถูกปลุกช้าได้)
 *   - เรียก logger.jobArrived(job) ทุกครั้งที่ปล่อยงาน
 *
 * ข้อควรคิด:
 *   - รายการงานที่ได้จาก WorkloadLoader เรียงตามลำดับในไฟล์ ไม่ได้เรียงตามเวลา
 *   - เมื่อปล่อยงานครบทุกชิ้นแล้ว ต้องมีวิธีบอกระบบว่า "จะไม่มีงานเข้ามาอีก"
 *     ดู TODO เรื่องการปิดระบบใน Main
 * 
 * ===============================================================
 * ยังขาดการเก็บ jobs, ช่องทางส่ง Job ไป Scheduler และ logger  
 * ยังขาดการปล่อย Job ตาม arrivalMs
 * ยังขาดการบันทึก actual arrival time
 * ยังขาดการส่ง Job ผ่าน Scheduler โดยไม่ข้ามไป ReadyQueue
 * ยังขาดกลไกบอกว่า Generator ส่ง Job ครบแล้ว
 * ต้องรองรับกรณี workload ไม่ได้เรียงตาม arrivalMs
 */
public class JobGenerator extends Thread {

    // TODO: เก็บรายการงาน, ช่องทางส่งงานไปยัง Scheduler และ logger
    //
    // หมายเหตุ: constructor ด้านล่างยังไม่มี parameter สำหรับ "ช่องทางส่งงาน"
    // เพราะเป็นสิ่งที่กลุ่มต้องออกแบบเอง (หัวข้อ 2 ห้ามให้ JobGenerator
    // ใส่งานลง ReadyQueue โดยตรง ต้องผ่าน Scheduler เสมอ)
    // ให้เพิ่ม parameter เข้าไปตามที่ออกแบบ เช่น BlockingQueue<Job>
    // หรือคลาสของกลุ่มเอง — เพิ่ม parameter ได้ แต่อย่าเปลี่ยนชื่อคลาส

    public static final Job POISON_PILL = new Job("POISON", -1, Integer.MAX_VALUE, 0, ResourceType.NONE, 0, -1);

    private final List<Job> jobs;
    private final BlockingQueue<Job> arrivalQueue;
    private final ProjectLogger logger;

    public JobGenerator(List<Job> jobs,BlockingQueue<Job> arrivalQueue, ProjectLogger logger) {
        super("generator");
        this.jobs = new ArrayList<>(jobs);
        this.jobs.sort(Comparator.comparingLong(j -> j.arrivalMs)); //handle unsorted workload
        this.arrivalQueue = arrivalQueue;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            for (Job job : jobs){
                long now = logger.now();
                long delay = job.arrivalMs - now;
                if(delay > 0){
                    Thread.sleep(delay);
                }
                job.actualArrivalMs = logger.now();
                arrivalQueue.put(job);
                logger.jobArrived(job);
            }
            logger.systemEvent("JobGenerator finished, sent poison pill");
            arrivalQueue.put(POISON_PILL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
