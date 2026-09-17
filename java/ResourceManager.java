import java.util.concurrent.Semaphore;
/** จัดการ Semaphore แบบยุติธรรมสำหรับทรัพยากร PRINTER และ DATABASE ที่ใช้ร่วมกัน */
public class ResourceManager {
    private final Semaphore printer;
    private final Semaphore database;
    public ResourceManager(int printerPermits, int databasePermits) {
        // fair=true ทำให้ผู้รอ permit ได้รับบริการตามลำดับการรอโดยประมาณ
        this.printer = new Semaphore(printerPermits, true);
        this.database = new Semaphore(databasePermits, true);
    }
    /** คืน Semaphore ที่ตรงกับชนิดทรัพยากร หรือ null หากงานไม่ใช้ทรัพยากร */
    public Semaphore getSemaphore(Job.ResourceType type) {
        switch (type) {
            case PRINTER: return printer;
            case DATABASE: return database;
            default: return null;
        }
    }
    /** คืนจำนวน permit ของ Printer ที่ยังว่างสำหรับ Monitor */
    public int availablePrinterPermits() { return printer.availablePermits(); }
    /** คืนจำนวน permit ของ Database ที่ยังว่างสำหรับ Monitor */
    public int availableDatabasePermits() { return database.availablePermits(); }
}
