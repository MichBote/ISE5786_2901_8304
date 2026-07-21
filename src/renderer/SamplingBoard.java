package renderer;

import primitives.Point;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Immutable reusable sampler for local 2D offsets and their mapping onto a 3D plane.
 * <p>
 * Samples are generated as local offsets first, then mapped using:
 * </p>
 * <pre>
 * point = center + right * dx + up * dy
 * </pre>
 */
public final class SamplingBoard {
    /**
     * Local 2D sample offset from the sampling center.
     *
     * @param dx offset along the local right axis
     * @param dy offset along the local up axis
     */
    public record Offset(double dx, double dy) {
    }

    /**
     * Sampling area width for square/rectangle shapes, or circle diameter.
     */
    private final double width;

    /**
     * Sampling area height for square/rectangle shapes, or circle diameter.
     */
    private final double height;

    /**
     * Circle radius. Zero for non-circular shapes.
     */
    private final double radius;

    /**
     * Exact number of samples.
     */
    private final int samples;

    /**
     * Sampling pattern.
     */
    private final SamplingPattern pattern;

    /**
     * Sampling shape.
     */
    private final SamplingShape shape;

    /**
     * Seed used for configured jittered sampling.
     */
    private final long jitterSeed;

    /**
     * Immutable canonical cells used by both grid and jittered sampling.
     */
    private final List<Cell> cells;

    /**
     * Immutable grid offsets. Grid sampling is deterministic, so these are reused
     * for every call regardless of seed.
     */
    private final List<Offset> gridOffsets;

    /**
     * Immutable offsets generated from the configured pattern, shape and seed.
     */
    private final List<Offset> offsets;

    /**
     * Constructs a rectangular sampling board for backward compatibility.
     *
     * @param width   sampling area width
     * @param height  sampling area height
     * @param samples exact number of samples
     * @param pattern sampling pattern
     */
    public SamplingBoard(double width, double height, int samples, SamplingPattern pattern) {
        this(width, height, samples, pattern, SamplingShape.RECTANGLE, 0L);
    }

    /**
     * Constructs a square or circular sampling board.
     * <p>
     * For {@link SamplingShape#SQUARE}, {@code sizeOrRadius} is the square side length.
     * For {@link SamplingShape#CIRCLE}, {@code sizeOrRadius} is the circle radius.
     * </p>
     *
     * @param sizeOrRadius square side length or circle radius
     * @param samples      exact number of samples
     * @param pattern      sampling pattern
     * @param shape        sampling shape; must be {@code SQUARE} or {@code CIRCLE}
     * @param jitterSeed   deterministic seed used for jittered sampling
     */
    public SamplingBoard(double sizeOrRadius, int samples, SamplingPattern pattern,
                         SamplingShape shape, long jitterSeed) {
        this(shape == SamplingShape.CIRCLE ? sizeOrRadius * 2d : sizeOrRadius,
                shape == SamplingShape.CIRCLE ? sizeOrRadius * 2d : sizeOrRadius,
                shape == SamplingShape.CIRCLE ? sizeOrRadius : 0d,
                samples, pattern, shape, jitterSeed);
        if (shape == SamplingShape.RECTANGLE) {
            throw new IllegalArgumentException("Use the width/height constructor for rectangular sampling");
        }
    }

    /**
     * Constructs a sampling board with explicit dimensions and shape.
     * <p>
     * {@link SamplingShape#SQUARE} and {@link SamplingShape#CIRCLE} require equal
     * width and height. For circles these dimensions are interpreted as the circle
     * diameter; use {@link #circle(double, int, SamplingPattern, long)} when the
     * natural input is a radius.
     * </p>
     *
     * @param width      sampling area width
     * @param height     sampling area height
     * @param samples    exact number of samples
     * @param pattern    sampling pattern
     * @param shape      sampling shape
     * @param jitterSeed deterministic seed used for jittered sampling
     */
    public SamplingBoard(double width, double height, int samples, SamplingPattern pattern,
                         SamplingShape shape, long jitterSeed) {
        this(width, height, shape == SamplingShape.CIRCLE ? width / 2d : 0d,
                samples, pattern, shape, jitterSeed);
    }

    /**
     * Private canonical constructor.
     *
     * @param width sampling area width
     * @param height sampling area height
     * @param radius circle radius, or zero for non-circular boards
     * @param samples exact sample count
     * @param pattern sampling pattern
     * @param shape sampling shape
     * @param jitterSeed deterministic jitter seed
     */
    private SamplingBoard(double width, double height, double radius, int samples,
                          SamplingPattern pattern, SamplingShape shape, long jitterSeed) {
        validatePositive(width, "Sampling width");
        validatePositive(height, "Sampling height");
        if (samples <= 0) {
            throw new IllegalArgumentException("Samples must be positive");
        }

        this.pattern = Objects.requireNonNull(pattern, "Sampling pattern must not be null");
        this.shape = Objects.requireNonNull(shape, "Sampling shape must not be null");
        if ((shape == SamplingShape.SQUARE || shape == SamplingShape.CIRCLE) && !isZero(width - height)) {
            throw new IllegalArgumentException(shape + " sampling requires equal width and height");
        }
        if (shape == SamplingShape.CIRCLE) {
            validatePositive(radius, "Sampling radius");
        }

        this.width = width;
        this.height = height;
        this.radius = radius;
        this.samples = samples;
        this.jitterSeed = jitterSeed;
        this.cells = List.copyOf(createCells());
        this.gridOffsets = List.copyOf(generateGridOffsets());
        this.offsets = pattern == SamplingPattern.GRID
                ? gridOffsets
                : List.copyOf(generateJitteredOffsets(jitterSeed));
    }

    /**
     * Creates a square sampling board.
     *
     * @param size       square side length
     * @param samples    exact number of samples
     * @param pattern    sampling pattern
     * @param jitterSeed deterministic seed used for jittered sampling
     * @return square sampling board
     */
    public static SamplingBoard square(double size, int samples, SamplingPattern pattern, long jitterSeed) {
        return new SamplingBoard(size, samples, pattern, SamplingShape.SQUARE, jitterSeed);
    }

    /**
     * Creates a circular sampling board.
     *
     * @param radius     circle radius
     * @param samples    exact number of samples
     * @param pattern    sampling pattern
     * @param jitterSeed deterministic seed used for jittered sampling
     * @return circular sampling board
     */
    public static SamplingBoard circle(double radius, int samples, SamplingPattern pattern, long jitterSeed) {
        return new SamplingBoard(radius * 2d, radius * 2d, radius, samples, pattern,
                SamplingShape.CIRCLE, jitterSeed);
    }

    /**
     * Creates a rectangular sampling board.
     *
     * @param width      sampling area width
     * @param height     sampling area height
     * @param samples    exact number of samples
     * @param pattern    sampling pattern
     * @param jitterSeed deterministic seed used for jittered sampling
     * @return rectangular sampling board
     */
    public static SamplingBoard rectangle(double width, double height, int samples,
                                          SamplingPattern pattern, long jitterSeed) {
        return new SamplingBoard(width, height, samples, pattern, SamplingShape.RECTANGLE, jitterSeed);
    }

    /**
     * Returns the sampling area width.
     *
     * @return sampling area width for square/rectangle shapes, or circle diameter
     */
    public double width() {
        return width;
    }

    /**
     * Returns the sampling area height.
     *
     * @return sampling area height for square/rectangle shapes, or circle diameter
     */
    public double height() {
        return height;
    }

    /**
     * Returns the circle radius.
     *
     * @return circle radius, or zero for non-circular shapes
     */
    public double radius() {
        return radius;
    }

    /**
     * Returns the configured exact sample count.
     *
     * @return exact configured sample count
     */
    public int samples() {
        return samples;
    }

    /**
     * Returns the configured sampling pattern.
     *
     * @return configured sampling pattern
     */
    public SamplingPattern pattern() {
        return pattern;
    }

    /**
     * Returns the configured sampling shape.
     *
     * @return configured sampling shape
     */
    public SamplingShape shape() {
        return shape;
    }

    /**
     * Returns the configured jitter seed.
     *
     * @return configured jitter seed
     */
    public long jitterSeed() {
        return jitterSeed;
    }

    /**
     * Returns the immutable configured local 2D offsets.
     *
     * @return local 2D offsets
     */
    public List<Offset> sampleOffsets() {
        return offsets;
    }

    /**
     * Returns local 2D offsets generated with the supplied jitter seed.
     * <p>
     * For {@link SamplingPattern#GRID}, the seed has no effect.
     * </p>
     *
     * @param seed deterministic seed used for jittered sampling
     * @return local 2D offsets
     */
    public List<Offset> sampleOffsets(long seed) {
        return pattern == SamplingPattern.GRID
                ? gridOffsets
                : List.copyOf(generateJitteredOffsets(seed));
    }

    /**
     * Creates sample points around a center point using the configured jitter seed.
     *
     * @param center sampling center
     * @param right  local right axis, normalized by this method
     * @param up     local up axis, normalized by this method
     * @return sample points
     */
    public List<Point> sample(Point center, Vector right, Vector up) {
        return mapToPlane(offsets, center, right, up);
    }

    /**
     * Creates sample points around a center point using an explicit jitter seed.
     *
     * @param center sampling center
     * @param right  local right axis, normalized by this method
     * @param up     local up axis, normalized by this method
     * @param seed   deterministic seed used for jittering
     * @return sample points
     */
    public List<Point> sample(Point center, Vector right, Vector up, long seed) {
        return mapToPlane(sampleOffsets(seed), center, right, up);
    }

    /**
     * Generates deterministic grid offsets from cached cells.
     *
     * @return deterministic grid offsets
     */
    private List<Offset> generateGridOffsets() {
        List<Offset> result = new ArrayList<>(samples);

        for (Cell cell : cells) {
            double u = (cell.uMin() + cell.uMax()) / 2d;
            double v = (cell.vMin() + cell.vMax()) / 2d;
            result.add(toShapeOffset(u, v));
        }

        return result;
    }

    /**
     * Generates jittered offsets from cached cells and a local random source.
     *
     * @param seed deterministic jitter seed
     * @return jittered offsets
     */
    private List<Offset> generateJitteredOffsets(long seed) {
        List<Offset> result = new ArrayList<>(samples);
        SplittableRandom random = new SplittableRandom(seed);

        for (Cell cell : cells) {
            double u = random.nextDouble(cell.uMin(), cell.uMax());
            double v = random.nextDouble(cell.vMin(), cell.vMax());
            result.add(toShapeOffset(u, v));
        }

        return result;
    }

    /**
     * Creates exactly {@link #samples} equal-area cells in canonical square coordinates.
     *
     * @return canonical sampling cells
     */
    private List<Cell> createCells() {
        int rows = rowCount();
        int baseColumns = samples / rows;
        int rowsWithExtraColumn = samples % rows;

        List<Cell> cells = new ArrayList<>(samples);
        int assignedSamples = 0;
        double vMin = 0d;

        for (int row = 0; row < rows; row++) {
            int rowSamples = baseColumns + (row < rowsWithExtraColumn ? 1 : 0);
            double vMax = row == rows - 1 ? 1d : (assignedSamples + rowSamples) / (double) samples;

            for (int col = 0; col < rowSamples; col++) {
                double uMin = col / (double) rowSamples;
                double uMax = (col + 1) / (double) rowSamples;
                cells.add(new Cell(uMin, uMax, vMin, vMax));
            }

            assignedSamples += rowSamples;
            vMin = vMax;
        }

        return cells;
    }

    /**
     * Chooses a row count that keeps cells close to the sampling area's aspect ratio.
     *
     * @return row count for the sampling grid
     */
    private int rowCount() {
        double aspect = width / height;
        int rows = (int) Math.round(Math.sqrt(samples / aspect));
        if (rows < 1) {
            return 1;
        }
        return Math.min(rows, samples);
    }

    /**
     * Converts canonical unit-square coordinates to a local offset in the configured shape.
     *
     * @param u horizontal canonical coordinate in {@code [0,1]}
     * @param v vertical canonical coordinate in {@code [0,1]}
     * @return local sampling offset
     */
    private Offset toShapeOffset(double u, double v) {
        double squareX = 2d * u - 1d;
        double squareY = 2d * v - 1d;

        return switch (shape) {
            case RECTANGLE, SQUARE -> new Offset(squareX * width / 2d, squareY * height / 2d);
            case CIRCLE -> toDiskOffset(squareX, squareY);
        };
    }

    /**
     * Maps a square point in [-1, 1]^2 to the configured circle without rejection.
     *
     * @param squareX square-space x-coordinate
     * @param squareY square-space y-coordinate
     * @return disk-space offset
     */
    private Offset toDiskOffset(double squareX, double squareY) {
        if (isZero(squareX) && isZero(squareY)) {
            return new Offset(0d, 0d);
        }

        double r;
        double theta;
        if (Math.abs(squareX) > Math.abs(squareY)) {
            r = squareX;
            theta = Math.PI / 4d * (squareY / squareX);
        } else {
            r = squareY;
            theta = Math.PI / 2d - Math.PI / 4d * (squareX / squareY);
        }

        return new Offset(radius * r * Math.cos(theta), radius * r * Math.sin(theta));
    }

    /**
     * Maps local offsets to points on a 3D plane.
     *
     * @param offsets local offsets to map
     * @param center sampling center
     * @param right local right axis
     * @param up local up axis
     * @return mapped sample points
     */
    private List<Point> mapToPlane(List<Offset> offsets, Point center, Vector right, Vector up) {
        Objects.requireNonNull(center, "Sampling center must not be null");
        Axes axes = normalizeAndValidateAxes(right, up);

        List<Point> points = new ArrayList<>(offsets.size());
        for (Offset offset : offsets) {
            Point point = center;
            if (!isZero(offset.dx())) {
                point = point.add(axes.right().scale(offset.dx()));
            }
            if (!isZero(offset.dy())) {
                point = point.add(axes.up().scale(offset.dy()));
            }
            points.add(point);
        }

        return List.copyOf(points);
    }

    /**
     * Normalizes supplied axes and verifies they form a valid orthogonal basis for a plane.
     *
     * @param right local right axis
     * @param up local up axis
     * @return normalized orthogonal axes
     */
    private Axes normalizeAndValidateAxes(Vector right, Vector up) {
        Vector normalizedRight = normalizeAxis(Objects.requireNonNull(right, "Right axis must not be null"),
                "Right axis");
        Vector normalizedUp = normalizeAxis(Objects.requireNonNull(up, "Up axis must not be null"),
                "Up axis");

        if (!isZero(normalizedRight.dotProduct(normalizedUp))) {
            throw new IllegalArgumentException("Sampling axes must be orthogonal");
        }
        return new Axes(normalizedRight, normalizedUp);
    }

    /**
     * Normalizes one axis after rejecting invalid values.
     *
     * @param axis axis to normalize
     * @param name diagnostic axis name
     * @return normalized axis
     */
    private Vector normalizeAxis(Vector axis, String name) {
        double lengthSquared = axis.lengthSquared();
        if (!Double.isFinite(lengthSquared) || alignZero(lengthSquared) <= 0d) {
            throw new IllegalArgumentException(name + " must be a valid non-zero vector");
        }
        return axis.normalize();
    }

    /**
     * Validates positive finite dimensions.
     *
     * @param value value to validate
     * @param name diagnostic parameter name
     */
    private static void validatePositive(double value, String name) {
        if (!Double.isFinite(value) || alignZero(value) <= 0d) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    /**
     * Canonical sampling cell in unit-square coordinates.
     *
     * @param uMin minimum horizontal coordinate
     * @param uMax maximum horizontal coordinate
     * @param vMin minimum vertical coordinate
     * @param vMax maximum vertical coordinate
     */
    private record Cell(double uMin, double uMax, double vMin, double vMax) {
    }

    /**
     * Normalized local axes.
     *
     * @param right normalized right axis
     * @param up normalized up axis
     */
    private record Axes(Vector right, Vector up) {
    }
}
