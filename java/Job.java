
/**
 * Job.java
 *
 * Immutable job data:
 * id, arrivalMs, priority, workMs, resource, resourceMs
 *
 * Mutable lifecycle data:
 * state and timestamps.
 *
 * Concurrency note:
 * The mutable fields are written by the Worker thread that currently owns
 * the Job and read by the Monitor thread for reporting.
 *
 * volatile guarantees visibility and ordering between threads, so the
 * Monitor can observe the latest lifecycle values without explicit locking.
 */
public class Job {

    /**
     * Project-level lifecycle states.
     *
     * These states represent the simulation state of a Job and do not map
     * directly to java.lang.Thread.State.
     */
    public enum State {
        ARRIVED,
        READY,
        RUNNING,
        WAITING_RESOURCE,
        COMPLETED
    }

    /**
     * A Job can use at most one shared resource.
     */
    public enum ResourceType {
        NONE,
        PRINTER,
        DATABASE
    }

    // ============================================================
    // Immutable job data
    // ============================================================
    /**
     * Job identifier from the workload file.
     */
    private final String id;

    /**
     * Scheduled arrival offset from simulationStart in milliseconds.
     */
    private final long arrivalMs;

    /**
     * Job priority. 1 means the highest priority.
     */
    private final int priority;

    /**
     * Amount of CPU/work time required by this Job in milliseconds.
     */
    private final long workMs;

    /**
     * Shared resource required by this Job.
     */
    private final ResourceType resource;

    /**
     * Amount of time the Job needs the resource in milliseconds.
     */
    private final long resourceMs;

    /**
     * Monotonically increasing sequence number used for deterministic
     * tie-breaking when two Jobs have the same scheduling attributes.
     */
    private final long sequenceNumber;

    // ============================================================
    // Mutable lifecycle data
    // ============================================================
    /**
     * Current lifecycle state of this Job.
     *
     * ARRIVED is the initial state.
     */
    private volatile State state = State.ARRIVED;

    /**
     * Actual time when the Job was generated/arrived, measured as milliseconds
     * since simulationStart.
     * -1 means timestamp has not been set yet.
     */
    private volatile long actualArrivalTime = -1;

    /**
     * Time when the Worker started processing the Job.
     * -1 means timestamp has not been set yet.
     */
    private volatile long startTime = -1;

    /**
     * Time when the Job started waiting for its shared resource.
     * -1 means timestamp has not been set yet.
     */
    private volatile long resourceWaitStartTime = -1;

    /**
     * Time when the Job successfully acquired its shared resource.
     * -1 means timestamp has not been set yet.
     */
    private volatile long resourceAcquireTime = -1;

    /**
     * Time when the Job completed.
     * -1 means timestamp has not been set yet.
     */
    private volatile long completionTime = -1;

    // ============================================================
    // Constructor
    // ============================================================
    /**
     * Creates a Job using data loaded from the workload file.
     *
     * @param id unique Job identifier
     * @param arrivalMs scheduled arrival offset in milliseconds
     * @param priority Job priority; 1 is highest
     * @param workMs required work time in milliseconds
     * @param resource shared resource required by the Job
     * @param resourceMs resource usage time in milliseconds
     * @param sequenceNumber deterministic tie-break sequence number
     */
    public Job(
            String id,
            long arrivalMs,
            int priority,
            long workMs,
            ResourceType resource,
            long resourceMs,
            long sequenceNumber) {

        this.id = id;
        this.arrivalMs = arrivalMs;
        this.priority = priority;
        this.workMs = workMs;
        this.resource = resource;
        this.resourceMs = resourceMs;
        this.sequenceNumber = sequenceNumber;
    }

    // ============================================================
    // Getters for immutable job data
    // ============================================================
    public String getId() {
        return id;
    }

    public long getArrivalMs() {
        return arrivalMs;
    }

    public int getPriority() {
        return priority;
    }

    public long getWorkMs() {
        return workMs;
    }

    public ResourceType getResource() {
        return resource;
    }

    public long getResourceMs() {
        return resourceMs;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    // ============================================================
    // Lifecycle state
    // ============================================================
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    // ============================================================
    // Lifecycle timestamps
    // ============================================================
    public long getActualArrivalTime() {
        return actualArrivalTime;
    }

    public void setActualArrivalTime(long actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getResourceWaitStartTime() {
        return resourceWaitStartTime;
    }

    public void setResourceWaitStartTime(long resourceWaitStartTime) {
        this.resourceWaitStartTime = resourceWaitStartTime;
    }

    public long getResourceAcquireTime() {
        return resourceAcquireTime;
    }

    public void setResourceAcquireTime(long resourceAcquireTime) {
        this.resourceAcquireTime = resourceAcquireTime;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    // ============================================================
    // Derived metrics
    // ============================================================
    /**
     * Calculates total waiting time before the Job starts running.
     *
     * waiting time = start time - actual arrival time
     *
     * @return waiting time in milliseconds
     */
    public long waitingTime() {
        if (startTime < 0 || actualArrivalTime < 0) {
            return 0;
        }
        return Math.max(0, startTime - actualArrivalTime);
    }

    /**
     * Calculates total turnaround time.
     *
     * turnaround time = completion time - actual arrival time
     *
     * @return turnaround time in milliseconds
     */
    public long turnaroundTime() {
        if (completionTime < 0 || actualArrivalTime < 0) {
            return 0;
        }
        return Math.max(0, completionTime - actualArrivalTime);
    }

    /**
     * Calculates the amount of time spent waiting for the shared resource.
     *
     * Jobs without a resource requirement have zero resource waiting time.
     *
     * @return resource waiting time in milliseconds
     */
    public long resourceWaitTime() {
        if (resource == ResourceType.NONE) {
            return 0;
        }

        if (resourceWaitStartTime < 0 || resourceAcquireTime < 0) {
            return 0;
        }

        return Math.max(0, resourceAcquireTime - resourceWaitStartTime);
    }
    // ============================================================
    // Debug / logging
    // ============================================================

    /**
     * Returns the Job ID when the Job is printed.
     */
    @Override
    public String toString() {
        return id;
    }
}
