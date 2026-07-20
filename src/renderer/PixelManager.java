package renderer;

/**
 * Pixel manager for synchronized pixel distribution and optional progress print.
 */
final class PixelManager {
    /** Immutable row/column pixel coordinates. */
    record Pixel(int row, int col) {
    }

    private final int rows;
    private final int cols;
    private final long totalPixels;
    private final double printIntervalSeconds;

    private long nextIndex = 0;
    private long processedPixels = 0;
    private long lastPrintedPercent = -1;
    private long lastPrintNano = System.nanoTime();

    private final Object mutexNext = new Object();
    private final Object mutexPixels = new Object();

    /**
     * Constructs a pixel manager.
     *
     * @param rows                 image rows
     * @param cols                 image columns
     * @param printIntervalSeconds progress print interval in seconds (0 disables)
     */
    PixelManager(int rows, int cols, double printIntervalSeconds) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Image resolution must be positive");
        }
        if (printIntervalSeconds < 0) {
            throw new IllegalArgumentException("Print interval must be non-negative");
        }
        this.rows = rows;
        this.cols = cols;
        this.totalPixels = (long) rows * cols;
        this.printIntervalSeconds = printIntervalSeconds;
    }

    /**
     * Returns the next pixel to process, or {@code null} when all pixels are assigned.
     *
     * @return next pixel or null
     */
    Pixel nextPixel() {
        synchronized (mutexNext) {
            if (nextIndex >= totalPixels) {
                return null;
            }
            long index = nextIndex++;
            int row = (int) (index / cols);
            int col = (int) (index % cols);
            return new Pixel(row, col);
        }
    }

    /**
     * Notify that one pixel was processed; prints progress when enabled.
     */
    void pixelDone() {
        synchronized (mutexPixels) {
            processedPixels++;
            if (printIntervalSeconds == 0) {
                return;
            }

            long percent = (processedPixels * 100) / totalPixels;
            long now = System.nanoTime();
            double elapsedSeconds = (now - lastPrintNano) / 1_000_000_000d;

            boolean shouldPrint = processedPixels == totalPixels
                    || (percent > lastPrintedPercent && elapsedSeconds >= printIntervalSeconds);

            if (shouldPrint) {
                System.out.printf("Rendering progress: %d%%%n", percent);
                lastPrintedPercent = percent;
                lastPrintNano = now;
            }
        }
    }
}
