package org.example.util;

public class PerformanceTimer {
    private static final ThreadLocal<StartRecord> current = new ThreadLocal<>();

    private static final class StartRecord {
        final String label;
        final long startNano;
        StartRecord(String label, long startNano) {
            this.label = label;
            this.startNano = startNano;
        }
    }

    private PerformanceTimer() {}

    public static void start(String label) {
        current.set(new StartRecord(label == null ? "timer" : label, System.nanoTime()));
    }

    public static boolean isRunning() {
        return current.get() != null;
    }

    public static void stopAndPrint() {
        StartRecord rec = current.get();
        if (rec == null) return;
        long elapsedMs = (System.nanoTime() - rec.startNano) / 1_000_000;
        System.out.printf("[PERF] %s: %d ms%n", rec.label, elapsedMs);
        current.remove();
    }

    public static void clear() {
        current.remove();
    }
}
