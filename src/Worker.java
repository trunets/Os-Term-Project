
/**
 * Thread ที่ดึงงานจาก Ready Queue ไปทำจนเสร็จ
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ลำดับการทำงานของ Job หนึ่งชิ้น บังคับตามหัวข้อ 6 ของโจทย์:
 * 1. รับงานจาก Ready Queue แล้วบันทึกเวลาเริ่ม
 * 2. จำลองงานหลักด้วย Thread.sleep(job.workMs)
 * 3. ถ้า job.resource != NONE ให้บันทึกเวลาเริ่มรอ แล้ว acquire
 * 4. จำลองการถือครองด้วย Thread.sleep(job.resourceMs)
 * 5. release แล้วบันทึกเวลาจบ
 *
 * ห้ามสลับขั้นที่ 2 กับ 3 เพราะจะทำให้ผลของทุกกลุ่มเทียบกันไม่ได้
 *
 * จุดที่มักพลาด:
 * - ถ้า exception หรือ interrupt เกิดขึ้นหลัง acquire แต่ก่อน release
 * permit จะค้างถาวรและระบบจะแขวน ต้องออกแบบให้คืนได้เสมอ
 * - Worker ต้องหยุดเองได้เมื่อไม่มีงานเหลือแล้ว ไม่ใช่วนรอตลอดไป
 * ================================================================
 * ยังขาดการรับ Job จาก ReadyQueue
 * ยังขาด startTime
 * ยังขาด Thread.sleep(workMs)
 * ยังขาดการวัดและบันทึก Resource Wait Time
 * ยังขาด acquire/release
 * ยังขาด try/finally หรือกลไกเทียบเท่าเพื่อไม่ให้ permit หาย
 * ยังขาด completionTime
 * ยังขาดการ update Statistics แบบ thread-safe
 * ยังขาดการจัดการ InterruptedException
 * ยังขาดการหยุด Worker อย่างถูกต้องเมื่อไม่มีงานเหลือ
 */
public class Worker extends Thread {

    // TODO: เก็บ ReadyQueue, ResourceManager, Statistics และ logger
    private final ReadyQueue readyQueue;
    private final ResourceManager resources;
    private final Statistics statistics;
    private final ProjectLogger logger;

    public Worker(String name, ReadyQueue readyQueue, ResourceManager resources,
            Statistics statistics, ProjectLogger logger) {
        super(name);
        this.readyQueue = readyQueue;
        this.resources = resources;
        this.statistics = statistics;
        this.logger = logger;
    }

    @Override
    public void run() {
        // วนรับงานและเรียก processJob จนกว่าจะได้รับสัญญาณให้หยุด
        try {
            while (true) {

                // Wait for next Job from Ready Queue
                Job job = readyQueue.take();

                // Poison pill tell this worker to shutdown
                if (job == JobGenerator.POISON_PILL) {
                    break;
                }

                // This job is now being processed by this worker.
                statistics.jobStarted();

                try {
                    // Process 1 Job
                    processJob(job);
                } finally {
                    // Always decrease runningJobs after processing ends.
                    // This also prevents the counter from remaining incorrect
                    // if processJob() is interrupted or throws an exception.
                    statistics.jobFinished();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * ทำงานหนึ่งชิ้นให้จบตามลำดับ 5 ขั้นด้านบน
     */
    private void processJob(Job job) throws InterruptedException {
        // 1. รับงานจาก Ready Queue แล้วบันทึกเวลาเริ่ม
        job.startTime = logger.now();
        logger.jobStarted(job);

        // 2. จำลองงานหลักด้วย Thread.sleep(job.workMs)
        Thread.sleep(job.workMs);
        logger.workFinished(job);

        // 3. ถ้า job.resource != NONE ให้บันทึกเวลาเริ่มรอ แล้ว acquire
        if (job.resource != ResourceType.NONE) {
            logger.resourceWaitStarted(job);
            long waitStart = logger.now();

            boolean acquired = false;
            try {
                // ให้บันทึกเวลาเริ่มรอ แล้ว acquire
                resources.acquire(job.resource);
                acquired = true;

                job.resourceWaitTime = logger.now() - waitStart;
                logger.resourceAcquired(job, job.resourceWaitTime);

                // 4. จำลองการถือครองด้วย Thread.sleep(job.resourceMs)
                Thread.sleep(job.resourceMs);
            } finally {
                // 5. release แล้วบันทึกเวลาจบ
                if (acquired) {
                    resources.release(job.resource);
                    logger.resourceReleased(job);
                }
            }
        } else {
            // job.resource without resource wait time = 0
            job.resourceWaitTime = 0;
        }

        //record completion time
        job.completionTime = logger.now();
        logger.jobCompleted(job);

        // update statistics
        statistics.recordCompletion(job);
    }
}
