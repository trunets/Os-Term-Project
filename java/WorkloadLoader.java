import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/** อ่านและตรวจสอบไฟล์ CSV ของ workload แล้วกำหนด sequenceNumber ที่แน่นอนให้แต่ละ Job */
public class WorkloadLoader {
    /** อ่าน CSV ที่มีคอลัมน์ id,arrivalMs,priority,workMs,resource,resourceMs */
    public static List<Job> load(String csvPath) throws IOException {
        List<Job> jobs = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(csvPath))) {
            String header = reader.readLine();
            if (header == null || !header.trim().equalsIgnoreCase("id,arrivalMs,priority,workMs,resource,resourceMs")) {
                throw new IOException("หัวตาราง CSV ต้องเป็น id,arrivalMs,priority,workMs,resource,resourceMs");
            }
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split(",", -1);
                if (fields.length != 6) throw new IOException("แถว " + lineNumber + " ต้องมี 6 คอลัมน์");
                String id = fields[0].trim();
                if (id.isEmpty() || !ids.add(id)) throw new IOException("รหัส Job ซ้ำหรือว่างที่แถว " + lineNumber);
                long arrivalMs = parseNonNegative(fields[1], "arrivalMs", lineNumber);
                int priority = parsePositive(fields[2], "priority", lineNumber);
                long workMs = parseNonNegative(fields[3], "workMs", lineNumber);
                Job.ResourceType resource;
                try { resource = Job.ResourceType.valueOf(fields[4].trim().toUpperCase()); }
                catch (IllegalArgumentException exception) { throw new IOException("resource ไม่ถูกต้องที่แถว " + lineNumber); }
                long resourceMs = parseNonNegative(fields[5], "resourceMs", lineNumber);
                if (resource == Job.ResourceType.NONE && resourceMs != 0) {
                    throw new IOException("resourceMs ต้องเป็น 0 เมื่อ resource เป็น NONE ที่แถว " + lineNumber);
                }
                jobs.add(new Job(id, arrivalMs, priority, workMs, resource, resourceMs, 0));
            }
        }
        // เรียงตามเวลามาถึงและ id เพื่อให้ sequenceNumber ไม่ขึ้นกับจังหวะการทำงานของเธรด
        jobs.sort(Comparator.comparingLong(Job::getArrivalMs).thenComparing(Job::getId));
        List<Job> sequencedJobs = new ArrayList<>();
        for (int index = 0; index < jobs.size(); index++) {
            Job job = jobs.get(index);
            sequencedJobs.add(new Job(job.getId(), job.getArrivalMs(), job.getPriority(), job.getWorkMs(),
                    job.getResource(), job.getResourceMs(), index));
        }
        return sequencedJobs;
    }
    /** แปลงจำนวนเต็มที่มีค่าตั้งแต่ศูนย์ขึ้นไป */
    private static long parseNonNegative(String value, String field, int lineNumber) throws IOException {
        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed < 0) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException exception) { throw new IOException(field + " ต้องเป็นจำนวนเต็มไม่ติดลบที่แถว " + lineNumber); }
    }
    /** แปลงจำนวนเต็มบวก */
    private static int parsePositive(String value, String field, int lineNumber) throws IOException {
        long parsed = parseNonNegative(value, field, lineNumber);
        if (parsed <= 0 || parsed > Integer.MAX_VALUE) throw new IOException(field + " ต้องเป็นจำนวนเต็มบวกที่แถว " + lineNumber);
        return (int) parsed;
    }
}
