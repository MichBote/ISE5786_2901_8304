package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused unit tests for {@link SamplingBoard}.
 */
class SamplingBoardTests {
    /** Numeric comparison tolerance. */
    private static final double DELTA = 1e-10;
    /** Sample counts that cover perfect and non-perfect square layouts. */
    private static final int[] COUNTS = {1, 4, 5, 9, 10, 25};
    /** Patterns explicitly required by the sampling infrastructure. */
    private static final SamplingPattern[] PATTERNS = {SamplingPattern.GRID, SamplingPattern.JITTERED};
    /** Shapes explicitly required by the sampling infrastructure. */
    private static final SamplingShape[] SHAPES = {SamplingShape.SQUARE, SamplingShape.CIRCLE};

    /**
     * Verifies exact count for all required counts, patterns and shapes.
     */
    @Test
    void testExactSampleCount() {
        for (int count : COUNTS) {
            for (SamplingPattern pattern : PATTERNS) {
                for (SamplingShape shape : SHAPES) {
                    SamplingBoard board = createBoard(shape, count, pattern, 11L);

                    assertEquals(count, board.sampleOffsets().size(), "Wrong offset count");
                    assertEquals(count, board.sample(Point.ZERO, Vector.AXIS_X, Vector.AXIS_Y).size(),
                            "Wrong mapped point count");
                }
            }
        }
    }

    /**
     * Verifies square samples remain inside square bounds.
     */
    @Test
    void testSquareSamplesStayWithinBounds() {
        for (int count : COUNTS) {
            for (SamplingPattern pattern : PATTERNS) {
                SamplingBoard board = SamplingBoard.square(2d, count, pattern, 17L);

                for (SamplingBoard.Offset offset : board.sampleOffsets()) {
                    assertTrue(offset.dx() >= -1d - DELTA && offset.dx() <= 1d + DELTA,
                            "Square sample dx is out of bounds");
                    assertTrue(offset.dy() >= -1d - DELTA && offset.dy() <= 1d + DELTA,
                            "Square sample dy is out of bounds");
                }
            }
        }
    }

    /**
     * Verifies circular samples remain inside the requested radius.
     */
    @Test
    void testCircleSamplesStayWithinRadius() {
        double radius = 2d;
        for (int count : COUNTS) {
            for (SamplingPattern pattern : PATTERNS) {
                SamplingBoard board = SamplingBoard.circle(radius, count, pattern, 23L);

                for (SamplingBoard.Offset offset : board.sampleOffsets()) {
                    double distanceSquared = offset.dx() * offset.dx() + offset.dy() * offset.dy();
                    assertTrue(distanceSquared <= radius * radius + DELTA,
                            "Circle sample is outside the radius");
                }
            }
        }
    }

    /**
     * Verifies that jittered sampling is reproducible with the same seed.
     */
    @Test
    void testSameJitterSeedProducesSamePositions() {
        SamplingBoard first = SamplingBoard.square(2d, 10, SamplingPattern.JITTERED, 101L);
        SamplingBoard second = SamplingBoard.square(2d, 10, SamplingPattern.JITTERED, 101L);

        assertEquals(first.sampleOffsets(), second.sampleOffsets(), "Same configured seed should match");
        assertEquals(first.sampleOffsets(202L), second.sampleOffsets(202L), "Same explicit seed should match");
    }

    /**
     * Verifies that different jitter seeds affect jittered positions.
     */
    @Test
    void testDifferentJitterSeedsProduceDifferentPositions() {
        SamplingBoard first = SamplingBoard.square(2d, 10, SamplingPattern.JITTERED, 101L);
        SamplingBoard second = SamplingBoard.square(2d, 10, SamplingPattern.JITTERED, 102L);

        assertNotEquals(first.sampleOffsets(), second.sampleOffsets(), "Different configured seeds should differ");
        assertNotEquals(first.sampleOffsets(202L), first.sampleOffsets(203L), "Different explicit seeds should differ");
    }

    /**
     * Verifies cached grid offsets are reused and remain seed-independent.
     */
    @Test
    void testGridOffsetsAreCachedAndSeedIndependent() {
        SamplingBoard board = SamplingBoard.circle(2d, 10, SamplingPattern.GRID, 101L);
        SamplingBoard sameConfiguration = SamplingBoard.circle(2d, 10, SamplingPattern.GRID, 999L);

        assertSame(board.sampleOffsets(), board.sampleOffsets(202L),
                "Grid sampling should reuse the cached offsets for explicit seeds");
        assertEquals(sameConfiguration.sampleOffsets(), board.sampleOffsets(303L),
                "Grid caching must not change generated positions");
    }

    /**
     * Verifies that local 2D offsets are mapped onto normalized right/up axes.
     */
    @Test
    void testAxisMapping() {
        SamplingBoard board = SamplingBoard.square(2d, 4, SamplingPattern.GRID, 0L);
        Point center = new Point(1, 2, 3);

        List<Point> points = board.sample(center, new Vector(0, 0, 5), new Vector(0, 2, 0));

        assertEquals(List.of(
                new Point(1, 1.5, 2.5),
                new Point(1, 1.5, 3.5),
                new Point(1, 2.5, 2.5),
                new Point(1, 2.5, 3.5)
        ), points, "Wrong 2D-to-3D mapping");
    }

    /**
     * Verifies that the sampler does not append an additional center point.
     */
    @Test
    void testNoExtraCentralSample() {
        SamplingBoard board = SamplingBoard.square(2d, 4, SamplingPattern.GRID, 0L);
        List<Point> points = board.sample(Point.ZERO, Vector.AXIS_X, Vector.AXIS_Y);

        assertEquals(4, points.size(), "Sampler must return exactly the configured count");
        assertFalse(points.contains(Point.ZERO), "Sampler added an extra central point");
    }

    /**
     * Verifies invalid board configuration is rejected.
     */
    @Test
    void testInvalidConfigurationRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SamplingBoard.square(0d, 1, SamplingPattern.GRID, 1L),
                "Zero square size should be rejected");
        assertThrows(IllegalArgumentException.class,
                () -> SamplingBoard.circle(-1d, 1, SamplingPattern.GRID, 1L),
                "Negative circle radius should be rejected");
        assertThrows(IllegalArgumentException.class,
                () -> SamplingBoard.square(Double.NaN, 1, SamplingPattern.GRID, 1L),
                "NaN dimensions should be rejected");
        assertThrows(IllegalArgumentException.class,
                () -> SamplingBoard.square(1d, 0, SamplingPattern.GRID, 1L),
                "Non-positive sample count should be rejected");
        assertThrows(NullPointerException.class,
                () -> SamplingBoard.square(1d, 1, null, 1L),
                "Null pattern should be rejected");
        assertThrows(NullPointerException.class,
                () -> new SamplingBoard(1d, 1d, 1, SamplingPattern.GRID, null, 1L),
                "Null shape should be rejected");
        assertThrows(IllegalArgumentException.class,
                () -> new SamplingBoard(2d, 3d, 4, SamplingPattern.GRID, SamplingShape.SQUARE, 1L),
                "Non-square dimensions should be rejected for square sampling");
    }

    /**
     * Verifies invalid mapping arguments are rejected.
     */
    @Test
    void testInvalidMappingArgumentsRejected() {
        SamplingBoard board = SamplingBoard.square(1d, 1, SamplingPattern.GRID, 0L);

        assertThrows(NullPointerException.class,
                () -> board.sample(null, Vector.AXIS_X, Vector.AXIS_Y),
                "Null center should be rejected");
        assertThrows(NullPointerException.class,
                () -> board.sample(Point.ZERO, null, Vector.AXIS_Y),
                "Null right axis should be rejected");
        assertThrows(NullPointerException.class,
                () -> board.sample(Point.ZERO, Vector.AXIS_X, null),
                "Null up axis should be rejected");
        assertThrows(IllegalArgumentException.class,
                () -> board.sample(Point.ZERO, Vector.AXIS_X, new Vector(1, 1, 0)),
                "Non-orthogonal axes should be rejected");
        assertThrows(IllegalArgumentException.class,
                () -> board.sample(Point.ZERO, new Vector(Double.NaN, 1, 0), Vector.AXIS_Y),
                "Invalid axis values should be rejected");
    }

    /**
     * Verifies returned lists cannot be mutated.
     */
    @Test
    void testImmutableResults() {
        SamplingBoard board = SamplingBoard.square(1d, 1, SamplingPattern.GRID, 0L);

        assertThrows(UnsupportedOperationException.class,
                () -> board.sampleOffsets().add(new SamplingBoard.Offset(0d, 0d)),
                "Offset result should be immutable");
        assertThrows(UnsupportedOperationException.class,
                () -> board.sample(Point.ZERO, Vector.AXIS_X, Vector.AXIS_Y).add(Point.ZERO),
                "Point result should be immutable");
    }

    /**
     * Verifies concurrent calls on the same board produce stable results without shared random state.
     */
    @Test
    void testConcurrentCallsAreSafe() throws Exception {
        SamplingBoard board = SamplingBoard.circle(2d, 25, SamplingPattern.JITTERED, 909L);
        Point center = new Point(1, 2, 3);
        Vector right = Vector.AXIS_X;
        Vector up = Vector.AXIS_Y;
        List<Point> expected = board.sample(center, right, up, 333L);

        AtomicReference<Throwable> failure = new AtomicReference<>();
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Thread worker = new Thread(() -> {
                try {
                    for (int call = 0; call < 8; call++) {
                        assertEquals(expected, board.sample(center, right, up, 333L),
                                "Concurrent sampling call changed result");
                    }
                } catch (Throwable ex) {
                    failure.compareAndSet(null, ex);
                }
            }, "sampling-board-test-worker");
            workers.add(worker);
            worker.start();
        }

        for (Thread worker : workers) {
            worker.join();
        }

        if (failure.get() != null) {
            if (failure.get() instanceof AssertionError assertionError) {
                throw assertionError;
            }
            throw new AssertionError("Concurrent sampling worker failed", failure.get());
        }
    }

    /**
     * Creates a required-shape sampling board.
     */
    private SamplingBoard createBoard(SamplingShape shape, int count, SamplingPattern pattern, long seed) {
        return switch (shape) {
            case SQUARE -> SamplingBoard.square(2d, count, pattern, seed);
            case CIRCLE -> SamplingBoard.circle(1d, count, pattern, seed);
            case RECTANGLE -> SamplingBoard.rectangle(2d, 1d, count, pattern, seed);
        };
    }
}
