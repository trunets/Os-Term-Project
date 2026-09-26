import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.*;


// ยังไม่ได้ Test JobGen, Scheduler เพราะแม่งerrorอะไรก็ไม่รู็เยอะแยะฝาก test ในเครื่องมึงหน่อย
public class SchedulerTest {
    public static void main(String[] args) throws InterruptedException {
        ProjectLogger logger = new ProjectLogger();
        BlockingQueue<Job> arrivalQueue = new LinkedBlockingQueue<>();
        ReadyQueue readyQueue = new ReadyQueue(Config.Policy.PRIORITY);

        Scheduler scheduler = new Scheduler(arrivalQueue, readyQueue, logger);
        scheduler.start();

        arrivalQueue.put(new Job("A", 0, 3, 100, ResourceType.NONE, 0, 0));
        arrivalQueue.put(new Job("B", 0, 1, 100, ResourceType.NONE, 0, 1));
        arrivalQueue.put(JobGenerator.POISON_PILL);

        scheduler.join();

        System.out.println("B first? " + readyQueue.take().id);
    }
}