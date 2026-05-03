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

    /** Error message for expected exception */
    private static final String ERROR_EXPECTED_EXCEPTION = "ERROR: expected IllegalArgumentException";

    /**
     * Test method for {@link Point#subtract(Point)}.
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Point p = P246;
        Point q = P123;

        // Act
        Vector result = p.subtract(q);

        // Assert
        assertEquals(V123, result, ERROR_POINT);

        // =============== Boundary Values Tests ==================
        // BV01: subtracting the same point should throw
        assertThrows(IllegalArgumentException.class, () -> q.subtract(q), ERROR_EXPECTED_EXCEPTION);
    }

    /**
     * Test method for {@link Point#add(Vector)}.
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Point p = P123;
        Vector v = V123;

        // Act
        Point result = p.add(v);

        // Assert
        assertEquals(P246, result, ERROR_POINT);

        // =============== Boundary Values Tests ==================
        // BV01: Consistency: p + (q - p) = q
        Point q = P246;
        assertEquals(q, p.add(q.subtract(p)), ERROR_POINT);
    }

    /**
     * Test method for {@link Point#distanceSquared(Point)}.
     */
    @Test
    void testDistanceSquared() {
        Point p = P123;
        Point q = P245;

        // ============ Equivalence Partitions Tests ==============
        assertEquals(9d, p.distanceSquared(q), DELTA, ERROR_POINT);

        // Symmetry
        assertEquals(p.distanceSquared(q), q.distanceSquared(p), DELTA, ERROR_POINT);

        // =============== Boundary Values Tests ==================
        assertEquals(0d, p.distanceSquared(p), DELTA, ERROR_POINT);

        // Consistency with distance
        double d = p.distance(q);
        double d2 = p.distanceSquared(q);
        assertEquals(d2, d * d, DELTA, ERROR_POINT);
    }

    /**
     * Test method for {@link Point#distance(Point)}.
     */
    @Test
    void testDistance() {
        Point p = P123;
        Point q = P245;

        // ============ Equivalence Partitions Tests ==============
        assertEquals(3d, p.distance(q), DELTA, ERROR_POINT);

        // Symmetry
        assertEquals(p.distance(q), q.distance(p), DELTA, ERROR_POINT);

        // =============== Boundary Values Tests ==================
        assertEquals(0d, p.distance(p), DELTA, ERROR_POINT);

        // Consistency with distanceSquared
        double d = p.distance(q);
        double d2 = p.distanceSquared(q);
        assertEquals(d2, d * d, DELTA, ERROR_POINT);
    }
}
