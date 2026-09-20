import java.util.Comparator;
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
 *
 * Shutdown: the Scheduler calls putPoisonPills(workerCount) after the last
 * real Job is forwarded. The comparator always sorts POISON_PILL after every
 * real Job, so Workers drain all real work before any Worker sees a pill.
 * Each Worker exits its loop when take() returns the pill.
 */
public class ReadyQueue {

    /**
     * Sentinel Job used as a shutdown signal. It is detected by identity (==)
     * via isPoisonPill(); its field values are dummy and never read.
     */
    public static final Job POISON_PILL = new Job("POISON_PILL", 0, 0, 0, Job.ResourceType.NONE, 0, 0);

    private final PriorityBlockingQueue<Job> queue;

    /**
     * @param policy scheduling policy (FCFS or Priority) that decides the
     *               order of real Jobs
     */
    public ReadyQueue(SchedulingPolicy policy) {
        Comparator<Job> base = policy.comparator();

        // Wraps the policy comparator so the poison pill always sorts LAST.
        // The pill check runs first, so the policy never sees the pill's
        // dummy priority/sequenceNumber values.
        Comparator<Job> pillLast = (a, b) -> {
            boolean aPill = isPoisonPill(a);
            boolean bPill = isPoisonPill(b);

            if (aPill && bPill)
                return 0; // two pills are equal
            if (aPill)
                return 1; // pill goes after a real Job
            if (bPill)
                return -1; // real Job goes before the pill
            return base.compare(a, b); // real Jobs: FCFS or Priority rule
        };

        // Initial capacity is just a hint; the queue grows as needed.
        this.queue = new PriorityBlockingQueue<>(64, pillLast);
    }

    /**
     * Called by the Scheduler thread. The Scheduler sets state = READY BEFORE
     * calling this, so a fast Worker cannot set RUNNING and then have it
     * overwritten by READY. This method only stores the Job.
     *
     * offer() never blocks on an unbounded queue.
     */
    public void put(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null");
        }
        queue.offer(job);
    }

    /** Called by Worker threads. Blocks until a Job (or a pill) is available. */
    public Job take() throws InterruptedException {
        return queue.take();
    }

    /**
     * Used by Monitor for reporting. Counts only real Jobs, excluding poison
     * pills. The iterator works on a snapshot, so this is safe while other
     * threads modify the queue; the result is a point-in-time count.
     */
    public int size() {
        int count = 0;
        for (Job job : queue) {
            if (!isPoisonPill(job)) {
                count++;
            }
        }
        return count;
    }

    /** @return true if the Job is the shutdown sentinel (identity check). */
    public static boolean isPoisonPill(Job job) {
        return job == POISON_PILL;
    }

    /**
     * Called by the Scheduler once, after the last real Job has been put.
     * Adds one pill per Worker so every Worker's take() unblocks and exits.
     *
     * offer() never blocks or throws InterruptedException, so this is safe
     * to call from a finally block.
     *
     * @param count number of Worker threads
     */
    public void putPoisonPills(int count) {
        for (int i = 0; i < count; i++) {
            queue.offer(POISON_PILL);
        }
    }
}