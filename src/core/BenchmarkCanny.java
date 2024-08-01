package core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import ui.BenchmarkDialog;

public class BenchmarkCanny {
    private BufferedImage source;
    private float highThreshold;
    private float lowThreshold;
    private int steps;
    private int repetitions;

    public BenchmarkCanny(BufferedImage source, float highThreshold, float lowThreshold, int steps, int repetitions) {
        this.source = source;
        this.highThreshold = highThreshold;
        this.lowThreshold = lowThreshold;
        this.steps = steps;
        this.repetitions = repetitions;
    }

    public List<BenchmarkResult> runBenchmark(BenchmarkDialog dialog) {
        List<BenchmarkResult> results = new ArrayList<>();
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();

        for (int i = 0; i < steps; i++) {
            float percent = 1 - (i * (1.0f / steps));
            int newW = (int) (originalWidth * percent);
            int newH = (int) (originalHeight * percent);

            long bestTime = Long.MAX_VALUE;

            for (int j = 0; j < repetitions; j++) {
                TileWorker canny = new TileWorker(source, newW, newH, highThreshold, lowThreshold);

                long start = System.currentTimeMillis();
                canny.processcanny();
                long end = System.currentTimeMillis();
                long time = end - start;

                if (time < bestTime) {
                    bestTime = time;
                }

                dialog.addResult(String.format("Paso %d/%d, Repetición %d/%d - Ancho: %d Alto: %d Hilos: %d Tiempo: %dms",
                        i + 1, steps, j + 1, repetitions, newW, newH, canny.getThreadcount(), time));
            }

            BenchmarkResult result = new BenchmarkResult(newW, newH, bestTime);
            results.add(result);
            dialog.addResult(String.format("Mejor tiempo para Ancho: %d Alto: %d - %dms", newW, newH, bestTime));
        }

        return results;
    }

    public static class BenchmarkResult {
        public final int width;
        public final int height;
        public final long bestTime;

        public BenchmarkResult(int width, int height, long bestTime) {
            this.width = width;
            this.height = height;
            this.bestTime = bestTime;
        }
    }
}