import java.io.IOException;
import java.util.List;

/**
 * WorkloadLoader.java
 *
 * PLACEHOLDER — the instructor provides a real WorkloadLoader.java for
 * reading/validating the workload CSV files (spec Appendix B: no credit is
 * given for writing a CSV parser). Replace this file with the instructor's
 * version. This stub exists only so the rest of the scaffold compiles
 * standalone; it does NOT actually parse CSV.
 *
 * Expected columns: id,arrivalMs,priority,workMs,resource,resourceMs
 */
public class WorkloadLoader {

    /** @return Jobs sorted by arrivalMs, each with a unique sequenceNumber assigned. */
    public static List<Job> load(String csvPath) throws IOException {
        throw new UnsupportedOperationException(
            "Replace this stub with the instructor-provided WorkloadLoader.java");
    }
}
