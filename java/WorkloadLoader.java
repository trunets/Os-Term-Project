import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * WorkloadLoader.java
 *
 * Loads workload jobs from CSV files.
 *
 * Expected format:
 *
 * id,arrivalMs,priority,workMs,resource,resourceMs
 *
 * Example:
 * J1,0,1,100,NONE,0
 * J2,10,2,200,PRINTER,50
 * J3,20,3,150,DATABASE,30
 */
public class WorkloadLoader {

    /**
     * Load jobs from a CSV file.
     *
     * Jobs are sorted by arrivalMs.
     * Each job receives a unique sequenceNumber.
     *
     * @param csvPath path to the CSV workload file
     * @return jobs sorted by arrivalMs
     * @throws IOException if the file cannot be read or contains invalid data
     */
    public static List<Job> load(String csvPath) throws IOException {

        List<Job> jobs = new ArrayList<>();

        Path path = Path.of(csvPath);

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            // Read and validate the CSV header.
            String header = reader.readLine();

            if (header == null) {
                throw new IOException("Workload CSV is empty: " + csvPath);
            }

            String expectedHeader =
                    "id,arrivalMs,priority,workMs,resource,resourceMs";

            if (!header.trim().equalsIgnoreCase(expectedHeader)) {
                throw new IOException(
                        "Invalid CSV header.\n"
                        + "Expected: " + expectedHeader + "\n"
                        + "Found: " + header
                );
            }

            String line;
            long sequenceNumber = 0;
            int lineNumber = 1;

            // Read every job row from the CSV.
            while ((line = reader.readLine()) != null) {

                lineNumber++;

                // Ignore blank lines.
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Split into exactly 6 CSV columns.
                String[] fields = line.split(",", -1);

                if (fields.length != 6) {
                    throw new IOException(
                            "Invalid number of columns at line "
                            + lineNumber
                            + ". Expected 6, found "
                            + fields.length
                    );
                }

                // Remove whitespace around every value.
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].trim();
                }

                // Parse job ID.
                String id = fields[0];

                if (id.isEmpty()) {
                    throw new IOException(
                            "Job ID cannot be empty at line "
                            + lineNumber
                    );
                }

                // Parse numeric fields.
                long arrivalMs = parseLong(
                        fields[1],
                        "arrivalMs",
                        lineNumber
                );

                int priority = parseInt(
                        fields[2],
                        "priority",
                        lineNumber
                );

                long workMs = parseLong(
                        fields[3],
                        "workMs",
                        lineNumber
                );

                String resourceName = fields[4];

                long resourceMs = parseLong(
                        fields[5],
                        "resourceMs",
                        lineNumber
                );

                // Validate numeric values.
                if (arrivalMs < 0) {
                    throw new IOException(
                            "arrivalMs cannot be negative at line "
                            + lineNumber
                    );
                }

                if (workMs < 0) {
                    throw new IOException(
                            "workMs cannot be negative at line "
                            + lineNumber
                    );
                }

                if (resourceMs < 0) {
                    throw new IOException(
                            "resourceMs cannot be negative at line "
                            + lineNumber
                    );
                }

                // ResourceType is defined inside Job.
                Job.ResourceType resource =
                        parseResource(resourceName, lineNumber);

                // Create the Job using the provided constructor.
                Job job = new Job(
                        id,
                        arrivalMs,
                        priority,
                        workMs,
                        resource,
                        resourceMs,
                        sequenceNumber
                );

                jobs.add(job);

                // Give every job a unique sequence number.
                sequenceNumber++;
            }
        }

        // Sort by arrival time.
        // Sequence number preserves CSV order when arrival times are equal.
        jobs.sort(
                Comparator
                        .comparingLong(Job::getArrivalMs)
                        .thenComparingLong(Job::getSequenceNumber)
        );

        return jobs;
    }

    /**
     * Parse a long integer from the CSV.
     */
    private static long parseLong(
            String value,
            String fieldName,
            int lineNumber) throws IOException {

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IOException(
                    "Invalid " + fieldName
                    + " at line "
                    + lineNumber
                    + ": "
                    + value,
                    e
            );
        }
    }

    /**
     * Parse an integer from the CSV.
     */
    private static int parseInt(
            String value,
            String fieldName,
            int lineNumber) throws IOException {

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IOException(
                    "Invalid " + fieldName
                    + " at line "
                    + lineNumber
                    + ": "
                    + value,
                    e
            );
        }
    }

    /**
     * Convert the CSV resource name into Job.ResourceType.
     *
     * Supported values:
     * NONE
     * PRINTER
     * DATABASE
     */
    private static Job.ResourceType parseResource(
            String value,
            int lineNumber) throws IOException {

        if (value.isEmpty()) {
            throw new IOException(
                    "Resource cannot be empty at line "
                    + lineNumber
            );
        }

        try {
            return Job.ResourceType.valueOf(
                    value.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new IOException(
                    "Invalid resource at line "
                    + lineNumber
                    + ": "
                    + value
                    + ". Expected NONE, PRINTER, or DATABASE.",
                    e
            );
        }
    }
}