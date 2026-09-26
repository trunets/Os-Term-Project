import java.util.concurrent.BlockingQueue;

/**
 * รับงานจาก JobGenerator แล้วจัดเข้า Ready Queue
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ข้อกำหนดจากโจทย์ (หัวข้อ 2 และ 4):
 *   - Scheduler เป็น Thread บังคับ ห้ามให้ JobGenerator ใส่งานลง Ready Queue โดยตรง
 *   - รับผิดชอบการจัดลำดับตามนโยบาย FCFS หรือ Priority
 *
 * ข้อควรคิด:
 *   - Scheduler รับงานจาก JobGenerator ผ่านอะไร และรอโดยไม่กิน CPU อย่างไร
 *   - เมื่อ JobGenerator ปล่อยงานครบแล้ว Scheduler รู้ได้อย่างไรว่าควรหยุด
 * ================================================================
 * ยังขาดช่องทางรับ Job จาก JobGenerator
 * ยังขาดการทำงานเป็น Thread จริง
 * ยังขาดการ take จาก Arrival Queue แล้วใส่ ReadyQueue
 * ยังขาดการหยุดเมื่อ Generator ส่งงานครบ
 * ยังขาดการ log สถานะ READY
 * ห้ามให้ Generator ใส่ ReadyQueue โดยตรง
 */
public class Scheduler extends Thread {
    private final BlockingQueue<Job> arrivalQueue;
    private final ReadyQueue readyQueue;
    private final ProjectLogger logger;


    public Scheduler(BlockingQueue<Job> arrivalQueue, ReadyQueue readyQueue, ProjectLogger logger) {
        super("scheduler");
        this.arrivalQueue = arrivalQueue;
        this.readyQueue = readyQueue;
        this.logger = logger;
    }

    @Override
    public void run() {
        // วนรับงานเข้ามาแล้วใส่ ReadyQueue จนกว่าจะได้รับสัญญาณให้หยุด
        try {
            while(true){
                Job job = arrivalQueue.take();
                if(job == JobGenerator.POISON_PILL){
                    break;
                }
                readyQueue.add(job);
                logger.systemEvent(job.id + " READY");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
