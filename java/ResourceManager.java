import java.util.concurrent.Semaphore;

/**
 * ResourceManager.java
 *
 * Wraps the two shared resources (PRINTER, DATABASE) as fair Semaphores.
 * All Workers share ONE ResourceManager instance, so the Semaphores are
 * the single source of truth for "how many Jobs are using this resource
 * right now" — no separate counters needed for correctness (Monitor can
 * still read availablePermits() for reporting purposes).
 *
 * new Semaphore(permits, true) uses fair (FIFO) ordering, per spec
 * requirement (section 3).
 */
public class ResourceManager {

    private final Semaphore printer;
    private final Semaphore database;

    public ResourceManager(int printerPermits, int databasePermits) {
        this.printer = new Semaphore(printerPermits, true);
        this.database = new Semaphore(databasePermits, true);
    }

    /** @return the Semaphore matching the Job's resource type, or null for NONE. */
    public Semaphore getSemaphore(Job.ResourceType type) {
        switch (type) {
            case PRINTER: return printer;
            case DATABASE: return database;
            default: return null; // NONE — caller must check before using
        }
    }

    // Used by Monitor for status reporting — read-only, safe to call concurrently.
    public int availablePrinterPermits() { return printer.availablePermits(); }
    public int availableDatabasePermits() { return database.availablePermits(); }
}
