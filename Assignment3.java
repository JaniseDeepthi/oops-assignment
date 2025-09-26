package assignments;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Assignment3 {

    // Stage 1: Fetch data (simulate I/O)
    static CompletableFuture<String> fetch(String id, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(200);
            System.out.println("Fetched data for " + id);
            return "raw-" + id;
        }, executor);
    }

    // Stage 2: Transform data
    static CompletableFuture<String> transform(String raw, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(150);
            String transformed = raw.toUpperCase();
            System.out.println("Transformed " + raw + " -> " + transformed);
            return transformed;
        }, executor);
    }

    // Stage 3: Save data
    static CompletableFuture<Boolean> save(String transformed, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(100);
            System.out.println("Saved: " + transformed);
            return true;
        }, executor);
    }

    // Utility: Simulate delay
    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        List<String> items = Arrays.asList("a", "b", "c", "d");

        // Build pipeline for each item: fetch -> transform -> save
        List<CompletableFuture<Boolean>> futures = items.stream()
                .map(id -> fetch(id, executor)
                        .thenCompose(raw -> transform(raw, executor))
                        .thenCompose(trans -> save(trans, executor))
                        .exceptionally(ex -> {
                            System.err.println("Error processing " + id + ": " + ex.getMessage());
                            return false; // fallback on error
                        }))
                .collect(Collectors.toList());

        // Wait for all to complete
        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        all.join(); // block until all finished

        // Collect results
        List<Boolean> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        System.out.println("\nFinal Results: " + results);

        executor.shutdown();
    }
}

