
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * จุดเริ่มต้นของโปรแกรม
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ส่วนที่เขียนไว้ให้แล้วคือการรับค่า การโหลด workload และการแสดง error
 * ซึ่งไม่ใช่สิ่งที่โครงงานนี้วัด ส่วนที่เหลือเป็น TODO ทั้งหมด
 *
 * วิธีรัน: java Main jobs_standard.csv priority 3 1 2
 * ==================================================================
 * ยังขาดการสร้าง ResourceManager ยังขาดการสร้าง ReadyQueue ยังขาดการสร้าง
 * Statistics ยังขาดการสร้าง Worker หลายตัว ยังขาดการสร้างและ start Scheduler
 * ยังขาดการสร้างและ start Monitor ยังขาดการสร้างและ start JobGenerator
 * ยังขาดการรอจน ทุก Job เสร็จ ยังขาด graceful shutdown ยังขาด join() ทุก Thread
 * ยังขาดการพิมพ์ summary ยังขาด SYSTEM_STOP ต้องไม่ใช้ System.exit()
 * เพื่อแก้ปัญหา shutdown
 */
public class Main {

    public static void main(String[] args) {
        // ---------- 1. รับค่าจาก command line ----------

        Config config;
        try {
            config = Config.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("ผิดพลาด: " + e.getMessage());
            System.err.println();
            System.err.println(Config.USAGE);
            System.exit(1);
            return;
        }

        // ---------- 2. เริ่มจับเวลาและโหลด workload ----------
        ProjectLogger logger = new ProjectLogger();
        List<Job> jobs;
        try {
            jobs = WorkloadLoader.load(config.workloadPath);
        } catch (WorkloadFormatException e) {
            System.err.println("ไฟล์ workload ผิดรูปแบบ — " + e.getMessage());
            System.exit(1);
            return;
        } catch (java.io.IOException e) {
            System.err.println("เปิดไฟล์ \"" + config.workloadPath + "\" ไม่ได้");
            System.err.println("ตรวจว่าไฟล์มีอยู่จริงและ path ถูกต้อง (สั่ง java จากโฟลเดอร์ใด)");
            System.exit(1);
            return;
        }
        logger.systemStart(config);
        logger.systemEvent("โหลดงานได้ " + jobs.size() + " ชิ้น");

        // ---------- 3. สร้างส่วนประกอบของระบบ ----------
        // Shared arrival queue between JobGenerator and Scheduler
        BlockingQueue<Job> arrivalQueue = new LinkedBlockingQueue<>();

        // สร้าง ResourceManager จากจำนวน permit ใน config
        ResourceManager resourceManager
                = new ResourceManager(
                        config.printerPermits,
                        config.databasePermits
                );

        // สร้าง ReadyQueue ตามนโยบายใน config
        ReadyQueue readyQueue
                = new ReadyQueue(
                        config.policy
                );

        // สร้าง Statistics
        Statistics statistics = new Statistics();

        // ---------- 4. สร้างและเริ่ม Thread ----------
        Worker[] workers = new Worker[config.workers];
        // สร้าง Worker จำนวน config.workers ตัว แล้ว start
        for (int i = 0; i < config.workers; i++) {
            workers[i]
                    = new Worker(
                            "Worker: " + String.valueOf(i + 1),
                            readyQueue,
                            resourceManager,
                            statistics,
                            logger
                    );
            workers[i].start();
        }

        // สร้างและ start Scheduler
        Scheduler scheduler
                = new Scheduler(
                        arrivalQueue,
                        readyQueue,
                        logger, config.workers
                );
        scheduler.start();

        // สร้างและ start Monitor
        Monitor monitor
                = new Monitor(
                        readyQueue,
                        resourceManager,
                        statistics,
                        logger
                );
        monitor.start();

        // สร้างและ start JobGenerator
        JobGenerator jobGenerator
                = new JobGenerator(
                        jobs,
                        arrivalQueue,
                        logger
                );
        jobGenerator.start();

        // ลำดับการ start มีผลหรือไม่ ให้คิดและอธิบายได้ใน Demo
        // ---------- 5. รอจนงานเสร็จครบ ----------
        // รอจนกว่างานทั้ง jobs.size() ชิ้นจะเสร็จ
        try {
            // Generator must finish sending all jobs first.
            // It sends POISON_PILL after the last job.
            jobGenerator.join();

            // Scheduler consumes all jobs and then puts one POISON_PILL
            // for every Worker.
            scheduler.join();

            // Each Worker processes all jobs before consuming its
            // POISON_PILL, so joining all Workers guarantees that
            // every Job has completed.
            for (Worker worker : workers) {
                worker.join();
            }

        } catch (InterruptedException e) {
            // Restore the interrupt flag before starting shutdown.
            Thread.currentThread().interrupt();

            // Interrupt all running threads so the JVM can shut down cleanly.
            jobGenerator.interrupt();
            scheduler.interrupt();
            monitor.interrupt();

            for (Worker worker : workers) {
                worker.interrupt();
            }

            // Wait for every thread to terminate.
            try {
                jobGenerator.join();
                scheduler.join();
                monitor.join();

                for (Worker worker : workers) {
                    worker.join();
                }
            } catch (InterruptedException shutdownInterrupted) {
                Thread.currentThread().interrupt();
            }

            return;
        }

        //
        // *** นี่คือจุดที่ยากที่สุดของโครงงานนี้ ***
        // Worker ที่กำลังรออยู่ในคิวไม่มีทางรู้ได้เองว่าจะไม่มีงานเข้ามาอีกแล้ว
        // กลุ่มต้องออกแบบวิธีบอก โดยห้ามใช้การเดาเวลา เช่น sleep(10000)
        //
        // เทคนิคที่ไปหาอ่านต่อได้ (เลือกใช้อันใดอันหนึ่งหรือผสมกันก็ได้):
        //   - poison pill
        //   - CountDownLatch
        //   - ตัวนับงานค้างที่ป้องกันด้วย lock
        //
        // อาการผิดที่ต้องไม่เกิด:
        //   1. main จบแล้วแต่ JVM ไม่ปิด เพราะยังมี Thread ค้างอยู่
        //   2. Worker หยุดก่อนที่งานชิ้นสุดท้ายจะทำเสร็จ
        //   3. permit ค้างเพราะถูก interrupt ระหว่างถือ resource
        // ---------- 6. สั่งหยุดทุก Thread ----------
        // หยุด Worker ทุกตัว, Scheduler, Monitor และ JobGenerator
        jobGenerator.interrupt();
        scheduler.interrupt();
        monitor.interrupt();

        for (Worker worker : workers) {
            worker.interrupt();
        }

        try {
            // join ทุก Thread เพื่อยืนยันว่าหยุดจริงก่อนไปขั้นถัดไป
            monitor.join();
            jobGenerator.join();
            scheduler.join();

            for (Worker worker : workers) {
                worker.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // ---------- 7. สรุปผล ----------
        // หา makespan = เวลาที่งานชิ้นสุดท้ายเสร็จ (ใช้ logger.now())
        long makespan = logger.now();

        int completed = statistics.completedCount();

        for (Job job : jobs) {
            if (job.completionTime > makespan) {
                makespan = job.completionTime;
                // เรียก statistics.printSummary(jobs, makespanMs)
                statistics.printSummary(jobs, makespan);
            }
        }
        
        // logger.systemStop(completed, jobs.size())
        logger.systemStop(completed, jobs.size());
    }
}
