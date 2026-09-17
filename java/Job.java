/**
 * Job.java
 *
 * Immutable job data (id, arrivalMs, priority, workMs, resource, resourceMs)
 * plus mutable lifecycle fields (state + timestamps).
 *
 * Concurrency note: the mutable fields below are written by the single
 * Worker thread that owns this Job at any given time, and read by the
 * Monitor thread for reporting. They are declared volatile so the Monitor
 * always sees the latest value without needing a lock (single-writer /
 * multi-reader pattern) — no other synchronization is required for them.
 */
public class Job {

    /** Project-level lifecycle states (do NOT map 1:1 to Thread.State). */
    public enum State {
        ARRIVED, READY, RUNNING, WAITING_RESOURCE, COMPLETED
    }

    /** A Job uses at most one shared resource. */
    public enum ResourceType {
        NONE, PRINTER, DATABASE
    }

    // ---- Immutable job data (from workload file) ----
    private final String id;
    private final long arrivalMs;      // scheduled arrival offset from simulationStart
    private final int priority;        // 1 = highest priority
    private final long workMs;
    private final ResourceType resource;
    private final long resourceMs;

    // Tie-break helper: a monotonically increasing sequence number assigned
    // once (e.g. when the Job is loaded or first arrives). Using this for
    // tie-breaks keeps ordering deterministic and independent of which
    // Thread happens to reach a queue first.
    private final long sequenceNumber;

    // ---- Mutable lifecycle data (single-writer: whichever Worker owns this Job) ----
    private volatile State state = State.ARRIVED;
    private volatile long actualArrivalTime;      // ms since simulationStart, set by JobGenerator
    private volatile long startTime;               // set by Worker at step 1
    private volatile long resourceWaitStartTime;   // set by Worker before acquire()
    private volatile long resourceAcquireTime;     // set by Worker after acquire()
    private volatile long completionTime;          // set by Worker at step 6

    public Job(String id, long arrivalMs, int priority, long workMs,
               ResourceType resource, long resourceMs, long sequenceNumber) {
        this.id = id;
        this.arrivalMs = arrivalMs;
        this.priority = priority;
        this.workMs = workMs;
        this.resource = resource;
        this.resourceMs = resourceMs;
        this.sequenceNumber = sequenceNumber;
    }

    // ---- Getters for immutable fields ----
    public String getId() { return id; }
    public long getArrivalMs() { return arrivalMs; }
    public int getPriority() { return priority; }
    public long getWorkMs() { return workMs; }
    public ResourceType getResource() { return resource; }
    public long getResourceMs() { return resourceMs; }
    public long getSequenceNumber() { return sequenceNumber; }

    // ---- Getters/setters for lifecycle fields ----
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public long getActualArrivalTime() { return actualArrivalTime; }
    public void setActualArrivalTime(long t) { this.actualArrivalTime = t; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long t) { this.startTime = t; }

    public long getResourceWaitStartTime() { return resourceWaitStartTime; }
    public void setResourceWaitStartTime(long t) { this.resourceWaitStartTime = t; }

    public long getResourceAcquireTime() { return resourceAcquireTime; }
    public void setResourceAcquireTime(long t) { this.resourceAcquireTime = t; }

    public long getCompletionTime() { return completionTime; }
    public void setCompletionTime(long t) { this.completionTime = t; }

    // TODO: derived metrics per spec section 10 — implement once all
    // timestamps above are being set correctly by Worker.processJob().
    // public long waitingTime()       { return startTime - actualArrivalTime; }
    // public long turnaroundTime()    { return completionTime - actualArrivalTime; }
    // public long resourceWaitTime()  { return resource == ResourceType.NONE
    //                                        ? 0 : resourceAcquireTime - resourceWaitStartTime; }

    @Override
    public String toString() {
        return id;
    }
}
