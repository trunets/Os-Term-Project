import java.util.concurrent.BlockingQueue;
/** รับงานจาก arrivalQueue จัดเป็น READY แล้วส่งต่อไปยัง ReadyQueue ตามนโยบายที่เลือก */
public class Scheduler implements Runnable {
    private final BlockingQueue<Job> arrivalQueue;
    private final ReadyQueue readyQueue;
    private final ProjectLogger logger;
    private final int workerCount;
    public Scheduler(BlockingQueue<Job> arrivalQueue, ReadyQueue readyQueue, ProjectLogger logger, int workerCount) {
        this.arrivalQueue = arrivalQueue; this.readyQueue = readyQueue; this.logger = logger; this.workerCount = workerCount;
    }
    @Override public void run() {
        try {
            while (true) {
                Job job = arrivalQueue.take();
                if (job.isPoisonPill()) {
                    // งานจริงถูกเพิ่มเข้า ReadyQueue ครบแล้ว จึงส่งสัญญาณหยุดให้ Worker ทุกตัว
                    for (int index = 0; index < workerCount; index++) readyQueue.put(Job.poisonPill());
                    return;
                }
                job.setState(Job.State.READY);
                logger.log("Scheduler", job.getId() + " READY");
                readyQueue.put(job);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
