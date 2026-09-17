import java.util.concurrent.PriorityBlockingQueue;

/**
 * ReadyQueue.java
 *
 * Shared between the Scheduler thread (producer) and all Worker threads
 * (consumers). Backed by a PriorityBlockingQueue, which is already
 * thread-safe internally, so no extra locking is needed here.
 *
 * Ordering comes from the SchedulingPolicy's Comparator (FCFS or Priority).
 *
 * take() blocks the calling Worker thread when the queue is empty instead
 * of busy-waiting/polling, satisfying the "no busy waiting" requirement.
 */
public class ReadyQueue {

    private final PriorityBlockingQueue<Job> queue;

    public ReadyQueue(SchedulingPolicy policy) {
        // Initial capacity is just a hint; the queue grows as needed.
        this.queue = new PriorityBlockingQueue<>(64, policy.comparator());
    }

    /** Called by the Scheduler thread after a Job is accepted into READY. */
    public void put(Job job) {
        // TODO: decide whether job.setState(Job.State.READY) happens here
        // or in Scheduler right before calling put() — pick one place, not both.
        queue.offer(job);
    }

    /** Called by Worker threads. Blocks until a Job is available. */
    public Job take() throws InterruptedException {
        return queue.take();
    }

    /** Used by Monitor for reporting — a snapshot size only, never mutate here. */
    public int size() {
        return queue.size();
    }
}
