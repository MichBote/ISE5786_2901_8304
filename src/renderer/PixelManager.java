package renderer;

/**
 * Pixel manager for synchronized pixel distribution and optional progress print.
 */
final class PixelManager {
    /**
     * Immutable row/column pixel coordinates.
     *
     * @param row pixel row index
     * @param col pixel column index
     */
    record Pixel(int row, int col) {
    }

    /** Number of image rows. */
    private final int rows;
    /** Number of image columns. */
    private final int cols;
    /** Total number of pixels to render. */
    private final long totalPixels;
    /** Minimum progress-print interval in seconds. */
    private final double printIntervalSeconds;

    /** Next linear pixel index to assign. */
    private long nextIndex = 0;
    /** Number of pixels already processed. */
    private long processedPixels = 0;
    /** Last printed integer progress percentage. */
    private long lastPrintedPercent = -1;
    /** Time of the last progress print. */
    private long lastPrintNano = System.nanoTime();

    /** Mutex protecting pixel assignment. */
    private final Object mutexNext = new Object();
    /** Mutex protecting processed-pixel progress state. */
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
