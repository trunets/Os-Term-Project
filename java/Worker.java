import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
/** รับงานจาก ReadyQueue และประมวลผลงานพร้อมจัดการทรัพยากรที่ใช้ร่วมกันอย่างปลอดภัย */
public class Worker implements Runnable {
    private final String name;
    private final ReadyQueue readyQueue;
    private final ResourceManager resourceManager;
    private final Statistics statistics;
    private final ProjectLogger logger;
    private final long simulationStart;
    private final AtomicInteger runningJobs;
    private final AtomicInteger completedJobs;
    public Worker(String name, ReadyQueue readyQueue, ResourceManager resourceManager, Statistics statistics,
                  ProjectLogger logger, long simulationStart, AtomicInteger runningJobs, AtomicInteger completedJobs) {
        this.name = name; this.readyQueue = readyQueue; this.resourceManager = resourceManager;
        this.statistics = statistics; this.logger = logger; this.simulationStart = simulationStart;
        this.runningJobs = runningJobs; this.completedJobs = completedJobs;
    }
    @Override public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Job job = readyQueue.take();
                if (job.isPoisonPill()) return;
                if (!processJob(job)) return;
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
    /** ทำงานตามลำดับ: งานหลัก รอ/ใช้ทรัพยากร แล้วบันทึกผลเมื่อเสร็จ */
    private boolean processJob(Job job) {
        boolean countedAsRunning = false;
        Semaphore semaphore = null;
        boolean acquired = false;
        try {
            job.setStartTime(elapsedMs());
            job.setState(Job.State.RUNNING);
            runningJobs.incrementAndGet();
            countedAsRunning = true;
            logger.log(name, job.getId() + " START");
            Thread.sleep(job.getWorkMs());
            if (job.getResource() != Job.ResourceType.NONE) {
                semaphore = resourceManager.getSemaphore(job.getResource());
                job.setResourceWaitStartTime(elapsedMs());
                job.setState(Job.State.WAITING_RESOURCE);
                logger.log(name, job.getId() + " WAIT " + job.getResource());
                semaphore.acquire();
                acquired = true;
                job.setResourceAcquireTime(elapsedMs());
                logger.log(name, job.getId() + " ACQUIRE " + job.getResource());
                try {
                    Thread.sleep(job.getResourceMs());
                } finally {
                    semaphore.release();
                    acquired = false;
                    logger.log(name, job.getId() + " RELEASE " + job.getResource());
                }
            }
            job.setCompletionTime(elapsedMs());
            job.setState(Job.State.COMPLETED);
            logger.log(name, job.getId() + " COMPLETE");
            statistics.recordJob(job);
            completedJobs.incrementAndGet();
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (acquired) {
                semaphore.release();
                logger.log(name, job.getId() + " RELEASE " + job.getResource());
            }
            if (countedAsRunning) runningJobs.decrementAndGet();
        }
    }
    /** คืนเวลาที่ผ่านไปนับจากจุดเริ่มต้นการจำลอง */
    private long elapsedMs() { return System.currentTimeMillis() - simulationStart; }
}
