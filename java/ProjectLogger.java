/** บันทึกเหตุการณ์ด้วยเวลาอ้างอิงเดียวกันและชื่อเธรด โดย synchronized เพื่อไม่ให้บรรทัด log ปะปนกัน */
public class ProjectLogger {
    private final long simulationStart;

    public ProjectLogger(long simulationStart) {
        this.simulationStart = simulationStart;
    }

    /** แสดงเหตุการณ์หนึ่งบรรทัดในรูปแบบที่กำหนดของโครงงาน */
    public synchronized void log(String threadName, String message) {
        long elapsed = System.currentTimeMillis() - simulationStart;
        System.out.printf("[%04d ms] [%s] %s%n", elapsed, threadName, message);
    }
}
