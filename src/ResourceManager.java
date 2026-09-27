import java.util.concurrent.Semaphore;

import javax.swing.plaf.basic.BasicTreeUI.SelectionModelPropertyChangeHandler;

/**
 * ควบคุมสิทธิ์การใช้ทรัพยากรร่วมของทั้งระบบ
 *
 * ===== ไฟล์นี้เป็นโครงเปล่า นักศึกษาต้องเขียนเอง =====
 *
 * ข้อกำหนดจากโจทย์ที่เกี่ยวกับคลาสนี้:
 *   - หัวข้อ 5: ใช้ Semaphore ควบคุม PRINTER และ DATABASE
 *     จำนวน permit มาจาก command line (Config)
 *     ในส่วนบังคับให้สร้าง Semaphore แบบ fair = true
 *   - Worker ทุกตัวต้องใช้ ResourceManager object เดียวกัน
 *   - หัวข้อ 7: permit ต้องไม่สูญหายหรือค้าง แม้เกิด exception
 *     หรือถูก interrupt ระหว่างถือ resource
 *
 * คำถามที่จะถูกถามใน Demo:
 *   - ทำไมต้อง fair = true และถ้าเปลี่ยนเป็น false จะเกิดอะไรขึ้น
 *   - ถ้า Thread ถูก interrupt หลัง acquire สำเร็จแต่ก่อน release
 *     โค้ดของกลุ่มยังคืน permit ได้หรือไม่
 * =============================================================
 * ยังขาด Semaphore ของ PRINTER
 * ยังขาด Semaphore ของ DATABASE
 * ยังขาดการสร้าง Semaphore ด้วย fair=true
 * ยังขาด acquire()
 * ยังขาด release()
 * ยังขาด status() สำหรับ Monitor
 * ยังขาดการออกแบบให้ Worker ทุกตัวใช้ ResourceManager ตัวเดียวกัน
 * ต้องรองรับ permit ที่กำหนดจาก command line
 */
public class ResourceManager {

    // TODO: เก็บ Semaphore ของ PRINTER และ DATABASE
    private final Semaphore printerSemaphore;
    private final Semaphore databaseSemaphore;
    private final int printerPermits;
    private final int databasePermits;

    public ResourceManager(int printerPermits, int databasePermits) {
        this.printerPermits = printerPermits;
        this.databasePermits = databasePermits;
        this.printerSemaphore = new Semaphore(printerPermits, true);
        this.databaseSemaphore = new Semaphore(databasePermits, true);
    }

    /** ขอสิทธิ์ใช้ทรัพยากร จะรอจนกว่าจะได้ */
    public void acquire(ResourceType type) throws InterruptedException {
        switch (type) {
            case PRINTER -> printerSemaphore.acquire();
            case DATABASE -> databaseSemaphore.acquire();
            case NONE -> {}
        }
    }

    /** คืนสิทธิ์ใช้ทรัพยากร */
    public void release(ResourceType type) {
        switch (type) {
            case PRINTER -> printerSemaphore.release();
            case DATABASE -> databaseSemaphore.release();
            case NONE -> {}
        }
    }

    /**
     * ข้อความสั้น ๆ บอกสถานะการใช้ทรัพยากร สำหรับส่งให้ ProjectLogger.monitor()
     * เช่น "printer=1/1 database=0/2"
     */
    public String status() {
        int printerInUse = printerPermits - printerSemaphore.availablePermits();
        int databaseInUse = databasePermits - databaseSemaphore.availablePermits();
        return String.format("printer=%d/%d database=%d/%d",
            printerInUse, printerPermits, databaseInUse, databasePermits
        );
    }
}
