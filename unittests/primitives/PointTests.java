package primitives;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for class {@link Point}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>{@link Point#subtract(Point)}</li>
 * <li>{@link Point#add(Vector)}</li>
 * <li>{@link Point#distanceSquared(Point)}</li>
 * <li>{@link Point#distance(Point)}</li>
 * </ul>
 */
class PointTests {
    /** Basic default constructor to satisfy documentation tools */
    PointTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Test point (1,2,3) */
    private static final Point P123 = new Point(1, 2, 3);
    /** Test point (2,4,6) */
    private static final Point P246 = new Point(2, 4, 6);
    /** Test point (2,4,5) */
    private static final Point P245 = new Point(2, 4, 5);

    /** Test vector (1,2,3) */
    private static final Vector V123 = new Vector(1, 2, 3);
    /** Test vector (-1,-2,-3) */
    private static final Vector V123_NEG = new Vector(-1, -2, -3);

    /** Error message for wrong point operations */
    private static final String ERROR_POINT = "ERROR: wrong Point operation";

    /**
     * Test method for {@link Point#subtract(Point)} and {@link Point#add(Vector)}.
     */
    @Test
    void testAddSubtract() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Subtract two different points
        assertEquals(V123, P246.subtract(P123), ERROR_POINT);

        // EP02: Add vector to point
        assertEquals(P246, P123.add(V123), ERROR_POINT);

        // EP03: Add opposite vector back to origin
        assertEquals(Point.ZERO, P123.add(V123_NEG), ERROR_POINT);

        // =============== Boundary Values Tests ==================
        // BV01: Subtract a point from itself should throw (zero vector forbidden)
        assertThrows(IllegalArgumentException.class, () -> P123.subtract(P123),
                "ERROR: subtracting identical points must throw IllegalArgumentException");

        // BV02: Consistency: p + (q - p) = q
        assertEquals(P246, P123.add(P246.subtract(P123)), ERROR_POINT);
    }

    /**
     * Test method for {@link Point#distanceSquared(Point)} and {@link Point#distance(Point)}.
     */
    @Test
    void testDistances() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Distance between two different points
        assertEquals(9d, P123.distanceSquared(P245), DELTA, ERROR_POINT);
        assertEquals(3d, P123.distance(P245), DELTA, ERROR_POINT);

        // EP02: Symmetry
        assertEquals(P123.distanceSquared(P245), P245.distanceSquared(P123), DELTA, ERROR_POINT);
        assertEquals(P123.distance(P245), P245.distance(P123), DELTA, ERROR_POINT);

        // =============== Boundary Values Tests ==================
        // BV01: Distance from point to itself
        assertEquals(0d, P123.distanceSquared(P123), DELTA, ERROR_POINT);
        assertEquals(0d, P123.distance(P123), DELTA, ERROR_POINT);

        // BV02: Consistency: distance^2 == distanceSquared
        double d = P123.distance(P245);
        assertEquals(P123.distanceSquared(P245), d * d, DELTA, ERROR_POINT);
    }
}
