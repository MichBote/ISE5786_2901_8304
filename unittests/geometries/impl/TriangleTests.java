package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for class {@link Triangle}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>{@link Triangle#getNormal(Point)} (inherited from {@link Polygon})</li>
 * </ul>
 */
class TriangleTests {
    /** Basic default constructor to satisfy documentation tools */
    TriangleTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Error message for wrong triangle */
    private static final String ERROR_TRIANGLE = "ERROR: wrong Triangle normal";

    /**
     * Test method for {@link Triangle#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        Triangle triangle = new Triangle(
                new Point(0, 0, 1),
                new Point(1, 0, 0),
                new Point(0, 1, 0));

        // Choose a point inside the triangle (and on its plane)
        Point p = new Point(0.2, 0.3, 0.5);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normal is unit-length
        Vector n = triangle.getNormal(p);
        assertEquals(1d, n.length(), DELTA, ERROR_TRIANGLE);

        // EP02: Normal is orthogonal to triangle edges
        Vector e1 = new Point(1, 0, 0).subtract(new Point(0, 0, 1));
        Vector e2 = new Point(0, 1, 0).subtract(new Point(0, 0, 1));
        assertEquals(0d, n.dotProduct(e1), DELTA, ERROR_TRIANGLE);
        assertEquals(0d, n.dotProduct(e2), DELTA, ERROR_TRIANGLE);
    }
}
