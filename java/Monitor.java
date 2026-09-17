import java.util.concurrent.atomic.AtomicInteger;
/** อ่าน snapshot ของระบบเป็นระยะโดยไม่แก้ไขข้อมูลที่ใช้ร่วมกัน */
public class Monitor implements Runnable {
    private final ReadyQueue readyQueue;
    private final AtomicInteger runningJobs;
    private final AtomicInteger completedJobs;
    private final int totalJobs;
    private final ResourceManager resourceManager;
    private final ProjectLogger logger;
    private final long intervalMs;
    public Monitor(ReadyQueue readyQueue, AtomicInteger runningJobs, AtomicInteger completedJobs, int totalJobs,
                   ResourceManager resourceManager, ProjectLogger logger, long simulationStart, long intervalMs) {
        this.readyQueue = readyQueue; this.runningJobs = runningJobs; this.completedJobs = completedJobs;
        this.totalJobs = totalJobs; this.resourceManager = resourceManager; this.logger = logger; this.intervalMs = intervalMs;
    }
    @Override public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException exception) {
                // interrupt คือสัญญาณหยุด Monitor จึงคืนสถานะแล้วออกจากลูป
                Thread.currentThread().interrupt();
                return;
            }
            logger.log("Monitor", "STATUS ready=" + readyQueue.size()
                    + " running=" + runningJobs.get()
                    + " completed=" + completedJobs.get() + "/" + totalJobs
                    + " printerAvail=" + resourceManager.availablePrinterPermits()
                    + " dbAvail=" + resourceManager.availableDatabasePermits());
        }
    }
}
