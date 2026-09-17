/**
 * แทนข้อมูลงานจาก workload และสถานะการทำงานของงานนั้น
 * ข้อมูลที่อ่านจาก CSV เปลี่ยนแปลงไม่ได้ ส่วนเวลาและสถานะเป็น volatile เพื่อให้
 * Monitor อ่านค่าล่าสุดได้อย่างปลอดภัยในรูปแบบผู้เขียนหนึ่งราย/ผู้อ่านหลายราย
 */
public class Job {
    /** สถานะระดับโปรเจกต์ ไม่ได้เทียบแบบหนึ่งต่อหนึ่งกับ Thread.State */
    public enum State { ARRIVED, READY, RUNNING, WAITING_RESOURCE, COMPLETED }
    /** งานหนึ่งงานขอทรัพยากรที่ใช้ร่วมกันได้สูงสุดหนึ่งชนิด */
    public enum ResourceType { NONE, PRINTER, DATABASE }
    private final String id;
    private final long arrivalMs;
    private final int priority;
    private final long workMs;
    private final ResourceType resource;
    private final long resourceMs;
    private final long sequenceNumber;
    private final boolean poisonPill;
    private volatile State state = State.ARRIVED;
    private volatile long actualArrivalTime;
    private volatile long startTime;
    private volatile long resourceWaitStartTime;
    private volatile long resourceAcquireTime;
    private volatile long completionTime;
    public Job(String id, long arrivalMs, int priority, long workMs, ResourceType resource, long resourceMs, long sequenceNumber) {
        this(id, arrivalMs, priority, workMs, resource, resourceMs, sequenceNumber, false);
    }
    private Job(String id, long arrivalMs, int priority, long workMs, ResourceType resource, long resourceMs, long sequenceNumber, boolean poisonPill) {
        this.id = id; this.arrivalMs = arrivalMs; this.priority = priority; this.workMs = workMs;
        this.resource = resource; this.resourceMs = resourceMs; this.sequenceNumber = sequenceNumber; this.poisonPill = poisonPill;
    }
    /** สร้างค่างานพิเศษสำหรับบอกเธรดให้หยุดหลังงานจริงทั้งหมดในคิว */
    public static Job poisonPill() {
        return new Job("__POISON_PILL__", Long.MAX_VALUE, Integer.MAX_VALUE, 0, ResourceType.NONE, 0, Long.MAX_VALUE, true);
    }
    public String getId() { return id; }
    public long getArrivalMs() { return arrivalMs; }
    public int getPriority() { return priority; }
    public long getWorkMs() { return workMs; }
    public ResourceType getResource() { return resource; }
    public long getResourceMs() { return resourceMs; }
    public long getSequenceNumber() { return sequenceNumber; }
    public boolean isPoisonPill() { return poisonPill; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public long getActualArrivalTime() { return actualArrivalTime; }
    public void setActualArrivalTime(long time) { this.actualArrivalTime = time; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long time) { this.startTime = time; }
    public long getResourceWaitStartTime() { return resourceWaitStartTime; }
    public void setResourceWaitStartTime(long time) { this.resourceWaitStartTime = time; }
    public long getResourceAcquireTime() { return resourceAcquireTime; }
    public void setResourceAcquireTime(long time) { this.resourceAcquireTime = time; }
    public long getCompletionTime() { return completionTime; }
    public void setCompletionTime(long time) { this.completionTime = time; }
    /** เวลารอตั้งแต่มาถึงจน Worker เริ่มทำงาน */
    public long waitingTime() { return startTime - actualArrivalTime; }
    /** เวลาตั้งแต่มาถึงจนงานเสร็จ */
    public long turnaroundTime() { return completionTime - actualArrivalTime; }
    /** เวลาที่รอ Semaphore; งานที่ไม่ใช้ทรัพยากรมีค่าเป็นศูนย์ */
    public long resourceWaitTime() { return resource == ResourceType.NONE ? 0 : resourceAcquireTime - resourceWaitStartTime; }
    @Override public String toString() { return id; }
}
