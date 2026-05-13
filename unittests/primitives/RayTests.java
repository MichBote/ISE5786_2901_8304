package primitives;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for class {@link Ray}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>{@link Ray#Ray(Point, Vector)} normalizes the direction</li>
 * </ul>
 */
class RayTests {
    /** Basic default constructor to satisfy documentation tools */
    RayTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Error message for wrong ray */
    private static final String ERROR_RAY = "ERROR: wrong Ray behavior";

    /**
     * Test method for {@link Ray#Ray(Point, Vector)}.
     */
    @Test
    void testConstructorNormalizesDirection() {
        // Arrange
        Point origin = new Point(1, 2, 3);
        Vector direction = new Vector(2, 4, 6);

        // Act
        Ray ray = new Ray(origin, direction);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Direction vector is normalized
        assertEquals(1d, ray.direction().length(), DELTA, ERROR_RAY);

        // =============== Boundary Values Tests ==================
        // BV01: Normalized direction keeps same orientation
        assertTrue(direction.dotProduct(ray.direction()) > 0, ERROR_RAY);
    }

    /**
     * Test method for {@link Ray#getPoint(double)}.
     */
    @Test
    void testGetPoint() {
        Ray ray = new Ray(new Point(1, 2, 3), new Vector(0, 0, 1));

        // ============ Equivalence Partitions Tests ==============
        // EP01: t > 0
        assertEquals(new Point(1, 2, 5), ray.getPoint(2), ERROR_RAY);

        // EP02: t < 0
        assertEquals(new Point(1, 2, 2), ray.getPoint(-1), ERROR_RAY);

        // =============== Boundary Values Tests ==================
        // BV01: t = 0
        assertEquals(new Point(1, 2, 3), ray.getPoint(0), ERROR_RAY);
    }

    /**
     * Test method for {@link Ray#findClosestPoint(List)}.
     */
    @Test
    void testFindClosestPoint() {
        Ray ray = new Ray(new Point(1, 2, 3), new Vector(1, 0, 0));

        // ============ Equivalence Partitions Tests ==============

        // EP01: Middle point is the closest
        Point p1 = new Point(1, 2, 10);
        Point p2 = new Point(1, 2, 4); // closest
        Point p3 = new Point(1, 2, 6);
        assertEquals(p2, ray.findClosestPoint(List.of(p1, p2, p3)), "findClosestPoint() wrong point (EP)");

        // =============== Boundary Values Tests ==================

        // BV01: Null list (no intersections) -> null
        assertNull(ray.findClosestPoint(null), "findClosestPoint() should return null for null list");

        // BV02: First point is the closest
        Point q1 = new Point(1, 2, 3.5);
        Point q2 = new Point(1, 2, 5);
        Point q3 = new Point(1, 2, 7);
        assertEquals(q1, ray.findClosestPoint(List.of(q1, q2, q3)), "findClosestPoint() wrong point (BV first)");

        // BV03: Last point is the closest
        Point r1 = new Point(1, 2, 9);
        Point r2 = new Point(1, 2, 6);
        Point r3 = new Point(1, 2, 3.25);
        assertEquals(r3, ray.findClosestPoint(List.of(r1, r2, r3)), "findClosestPoint() wrong point (BV last)");
    }
}
