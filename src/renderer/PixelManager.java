package renderer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Coordinates pixel assignment and optional progress printing.
 */
public final class PixelManager {
    /**
     * Pixel coordinate pair.
     */
    public static final class Pixel {
        /** Row index. */
        public final int row;
        /** Column index. */
        public final int col;

        /**
         * Constructs a pixel coordinate.
         *
         * @param row row index
         * @param col column index
         */
        Pixel(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    /**
     * Total number of rows.
     */
    private final int rows;

    /**
     * Total number of columns.
     */
    private final int cols;

    /**
     * Total number of pixels.
     */
    private final int totalPixels;

    /**
     * Progress print interval in percent.
     */
    private final double printInterval;

    /**
     * Next pixel index to hand out.
     */
    private final AtomicInteger nextIndex = new AtomicInteger();

    /**
     * Number of completed pixels.
     */
    private final AtomicInteger donePixels = new AtomicInteger();

    /**
     * Next progress threshold to print.
     * <p>
     * Read outside the {@link #printProgress} monitor as a fast pre-check, so it
     * must be volatile for that unsynchronized read to see the latest value.
     * </p>
     */
    private volatile double nextPrintThreshold = 0d;

    /**
     * Creates a new pixel manager.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param printInterval progress interval in percent
     */
    public PixelManager(int rows, int cols, double printInterval) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Image resolution must be positive");
        }
        if (printInterval < 0) {
            throw new IllegalArgumentException("Print interval must be non-negative");
        }
        this.rows = rows;
        this.cols = cols;
        this.totalPixels = rows * cols;
        this.printInterval = printInterval;
    }

    /**
     * Returns the next pixel to render, or {@code null} if none remain.
     *
     * @return next pixel, or {@code null}
     */
    public Pixel nextPixel() {
        int index = nextIndex.getAndIncrement();
        if (index >= totalPixels) {
            return null;
        }
        return new Pixel(index / cols, index % cols);
    }

    /**
     * Marks one pixel as completed and optionally prints progress.
     */
    public void pixelDone() {
        int done = donePixels.incrementAndGet();
        if (printInterval == 0) {
            return;
        }

        double progress = 100d * done / totalPixels;
        if (progress >= nextPrintThreshold || done == totalPixels) {
            printProgress(done, progress);
        }
    }

    /**
     * Prints progress from a synchronized section so concurrent workers do not interleave output.
     *
     * @param done     completed pixel count
     * @param progress completed percentage
     */
    private synchronized void printProgress(int done, double progress) {
        if (progress >= nextPrintThreshold || done == totalPixels) {
            System.out.printf("\r%.1f%%", progress);
            if (done == totalPixels) {
                System.out.println();
            }
            nextPrintThreshold += printInterval;
        }
    }
}
