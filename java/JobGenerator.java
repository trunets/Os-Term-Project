import java.util.List;
import java.util.concurrent.BlockingQueue;
/** ปล่อยงานตามเวลามาถึงเข้าสู่ arrivalQueue เท่านั้น ไม่ข้ามไปยัง ReadyQueue */
public class JobGenerator implements Runnable {
    private final List<Job> workload;
    private final BlockingQueue<Job> arrivalQueue;
    private final ProjectLogger logger;
    private final long simulationStart;
    public JobGenerator(List<Job> workload, BlockingQueue<Job> arrivalQueue, ProjectLogger logger, long simulationStart) {
        this.workload = workload; this.arrivalQueue = arrivalQueue; this.logger = logger; this.simulationStart = simulationStart;
    }
    @Override public void run() {
        try {
            for (Job job : workload) {
                long sleepMs = simulationStart + job.getArrivalMs() - System.currentTimeMillis();
                if (sleepMs > 0) Thread.sleep(sleepMs);
                job.setActualArrivalTime(System.currentTimeMillis() - simulationStart);
                job.setState(Job.State.ARRIVED);
                logger.log("JobGenerator", job.getId() + " ARRIVED priority=" + job.getPriority());
                arrivalQueue.put(job);
            }
            // แจ้ง Scheduler ว่าไม่มีงานจริงเข้ามาเพิ่มแล้ว
            arrivalQueue.put(Job.poisonPill());
        } catch (InterruptedException exception) {
            // คืนค่าสถานะ interrupt เพื่อให้ผู้เรียกสามารถตรวจพบการยกเลิกได้
            Thread.currentThread().interrupt();
        }
    }
}
