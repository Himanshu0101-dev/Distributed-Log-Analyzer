import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.*;

class LogEntry {
    String timestamp;
    String serverId;
    String message;

    LogEntry(String timestamp, String serverId, String message) {
        this.timestamp = timestamp;
        this.serverId = serverId;
        this.message = message;
    }

    LocalDateTime getTime() {
        return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public String toString() {
        return "(" + timestamp + ", " + serverId + ", " + message + ")";
    }
}

public class DistributedLogAnalyzer {

    // Merge logs using a PriorityQueue (min-heap)
    public static List<LogEntry> mergeLogs(List<List<LogEntry>> logs) {
        List<LogEntry> merged = new ArrayList<>();
        PriorityQueue<Object[]> pq = new PriorityQueue<>(Comparator.comparing(o -> ((LogEntry) o[2]).getTime()));

        // Initialize heap
        for (int i = 0; i < logs.size(); i++) {
            if (!logs.get(i).isEmpty()) {
                pq.add(new Object[]{i, 0, logs.get(i).get(0)});
            }
        }

        while (!pq.isEmpty()) {
            Object[] arr = pq.poll();
            int fileIdx = (int) arr[0];
            int entryIdx = (int) arr[1];
            LogEntry entry = (LogEntry) arr[2];

            merged.add(entry);

            if (entryIdx + 1 < logs.get(fileIdx).size()) {
                pq.add(new Object[]{fileIdx, entryIdx + 1, logs.get(fileIdx).get(entryIdx + 1)});
            }
        }
        return merged;
    }

    // Detect duplicates within 5 minutes
    public static List<LogEntry> detectDuplicates(List<LogEntry> merged) {
        Map<String, Map<String, LocalDateTime>> seen = new HashMap<>();
        List<LogEntry> duplicates = new ArrayList<>();

        for (LogEntry entry : merged) {
            LocalDateTime time = entry.getTime();
            seen.putIfAbsent(entry.serverId, new HashMap<>());

            if (seen.get(entry.serverId).containsKey(entry.message)) {
                LocalDateTime lastTime = seen.get(entry.serverId).get(entry.message);
                if (Duration.between(lastTime, time).toMinutes() <= 5) {
                    duplicates.add(entry);
                }
            }
            seen.get(entry.serverId).put(entry.message, time);
        }
        return duplicates;
    }

    public static void main(String[] args) {
        List<List<LogEntry>> logs = new ArrayList<>();

        logs.add(Arrays.asList(
                new LogEntry("2026-10-14T09:00:00", "S1", "Error A"),
                new LogEntry("2026-10-14T09:03:00", "S1", "Error A")
        ));

        logs.add(Arrays.asList(
                new LogEntry("2026-10-14T09:01:00", "S2", "Error B"),
                new LogEntry("2026-10-14T09:06:00", "S2", "Error B")
        ));

        List<LogEntry> merged = mergeLogs(logs);
        List<LogEntry> duplicates = detectDuplicates(merged);

        System.out.println("Merged Logs:");
        for (LogEntry log : merged) {
            System.out.println(log);
        }

        System.out.println("\nDuplicates Detected:");
        for (LogEntry dup : duplicates) {
            System.out.println(dup);
        }
    }
}
