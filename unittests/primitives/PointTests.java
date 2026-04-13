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
    void testSubtractDifferentPoints() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Point p = P246;
        Point q = P123;

        // Act
        Vector result = p.subtract(q);

        // Assert
        assertEquals(V123, result, ERROR_POINT);
    }

    /**
     * Test method for {@link Point#add(Vector)}.
     */
    @Test
    void testAddVectorToPoint() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Point p = P123;
        Vector v = V123;

        // Act
        Point result = p.add(v);

        // Assert
        assertEquals(P246, result, ERROR_POINT);
    }

    /**
     * Boundary test for {@link Point#subtract(Point)}.
     */
    @Test
    void testSubtractSamePointThrows() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Point p = P123;

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> p.subtract(p), ERROR_EXPECTED_EXCEPTION);
    }

    /**
     * Consistency test: p + (q - p) = q.
     */
    @Test
    void testAddSubtractConsistency() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Point p = P123;
        Point q = P246;

        // Act
        Point result = p.add(q.subtract(p));

        // Assert
        assertEquals(q, result, ERROR_POINT);
    }

    /**
     * Test method for {@link Point#distanceSquared(Point)} and {@link Point#distance(Point)}.
     */
    @Test
    void testDistanceBetweenDifferentPoints() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Point p = P123;
        Point q = P245;

        // Act
        double d2 = p.distanceSquared(q);
        double d = p.distance(q);

        // Assert
        assertEquals(9d, d2, DELTA, ERROR_POINT);
        assertEquals(3d, d, DELTA, ERROR_POINT);
    }

    /**
     * Symmetry test for distances.
     */
    @Test
    void testDistanceSymmetry() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Point p = P123;
        Point q = P245;

        // Act
        double d2pq = p.distanceSquared(q);
        double d2qp = q.distanceSquared(p);
        double dpq = p.distance(q);
        double dqp = q.distance(p);

        // Assert
        assertEquals(d2pq, d2qp, DELTA, ERROR_POINT);
        assertEquals(dpq, dqp, DELTA, ERROR_POINT);
    }

    /**
     * Boundary test: distance from a point to itself is zero.
     */
    @Test
    void testDistanceToSelfIsZero() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Point p = P123;

        // Act
        double d2 = p.distanceSquared(p);
        double d = p.distance(p);

        // Assert
        assertEquals(0d, d2, DELTA, ERROR_POINT);
        assertEquals(0d, d, DELTA, ERROR_POINT);
    }

    /**
     * Consistency test: distanceSquared == distance^2.
     */
    @Test
    void testDistanceConsistency() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Point p = P123;
        Point q = P245;

        // Act
        double d = p.distance(q);
        double d2 = p.distanceSquared(q);

        // Assert
        assertEquals(d2, d * d, DELTA, ERROR_POINT);
    }
}
