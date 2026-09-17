import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
/** สร้างส่วนประกอบของระบบ เริ่มเธด รอให้จบ และปิดระบบอย่างปลอดภัย */
public class Main {
    public static void main(String[] args) {
        if (args.length != 5) { printUsage(); return; }
        String workloadPath = args[0];
        String policyName = args[1].toLowerCase();
        if (!policyName.equals("fcfs") && !policyName.equals("priority")) { printUsage(); return; }
        int workerCount, printerPermits, databasePermits;
        try {
            workerCount = parsePositiveInt(args[2], "workers");
            printerPermits = parsePositiveInt(args[3], "printerPermits");
            databasePermits = parsePositiveInt(args[4], "databasePermits");
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage()); printUsage(); return;
        }
        if (!Files.isRegularFile(Path.of(workloadPath)) || !Files.isReadable(Path.of(workloadPath))) {
            System.err.println("ไม่พบไฟล์ workload หรือไม่มีสิทธิ์อ่าน: " + workloadPath); return;
        }
        List<Job> workload;
        try { workload = WorkloadLoader.load(workloadPath); }
        catch (Exception exception) { System.err.println("โหลด workload ไม่สำเร็จ: " + exception.getMessage()); return; }
        long simulationStart = System.currentTimeMillis();
        ProjectLogger logger = new ProjectLogger(simulationStart);
        BlockingQueue<Job> arrivalQueue = new LinkedBlockingQueue<>();
        SchedulingPolicy policy = policyName.equals("priority") ? new PriorityPolicy() : new FcfsPolicy();
        ReadyQueue readyQueue = new ReadyQueue(policy);
        ResourceManager resourceManager = new ResourceManager(printerPermits, databasePermits);
        Statistics statistics = new Statistics();
        AtomicInteger runningJobs = new AtomicInteger();
        AtomicInteger completedJobs = new AtomicInteger();
        Thread generatorThread = new Thread(new JobGenerator(workload, arrivalQueue, logger, simulationStart), "JobGenerator");
        Thread schedulerThread = new Thread(new Scheduler(arrivalQueue, readyQueue, logger, workerCount), "Scheduler");
        Thread[] workerThreads = new Thread[workerCount];
        for (int index = 0; index < workerCount; index++) {
            String name = "Worker-" + (index + 1);
            workerThreads[index] = new Thread(new Worker(name, readyQueue, resourceManager, statistics, logger,
                    simulationStart, runningJobs, completedJobs), name);
        }
        Thread monitorThread = new Thread(new Monitor(readyQueue, runningJobs, completedJobs, workload.size(),
                resourceManager, logger, simulationStart, 1000L), "Monitor");
        generatorThread.start(); schedulerThread.start();
        for (Thread workerThread : workerThreads) workerThread.start();
        monitorThread.start();
        try {
            // Scheduler ส่ง poison pill หลังรับงานจริงครบ และ Worker จะจบหลังประมวลผลงานทั้งหมด
            generatorThread.join(); schedulerThread.join();
            for (Thread workerThread : workerThreads) workerThread.join();
            monitorThread.interrupt(); monitorThread.join();
        } catch (InterruptedException exception) {
            // หาก Main ถูก interrupt ให้ส่งต่อสัญญาณหยุดแก่เธรดทั้งหมดและคืนสถานะ interrupt
            generatorThread.interrupt(); schedulerThread.interrupt(); monitorThread.interrupt();
            for (Thread workerThread : workerThreads) workerThread.interrupt();
            Thread.currentThread().interrupt(); return;
        }
        statistics.printSummary(System.currentTimeMillis() - simulationStart);
    }
    /** แปลงค่าจำนวนเต็มบวกจาก argument และแจ้งชื่อค่าที่ผิด */
    private static int parsePositiveInt(String value, String label) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException exception) { throw new IllegalArgumentException(label + " ต้องเป็นจำนวนเต็มบวก"); }
    }
    /** แสดงรูปแบบคำสั่งตามข้อกำหนดของโครงงาน */
    private static void printUsage() {
        System.err.println("Usage: java Main <workload.csv> <fcfs|priority> <workers> <printerPermits> <databasePermits>");
        System.err.println("Example: java Main csv/jobs_standard.csv priority 3 1 2");
    }
}
