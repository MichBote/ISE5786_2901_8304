package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
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

    /** Error message for wrong triangle intersection */
    private static final String ERROR_TRIANGLE_INTERSECTION = "ERROR: wrong Triangle intersection result";

    private static void assertPointEquals(Point expected, Point actual, String message) {
        assertEquals(0d, expected.distance(actual), DELTA, message);
    }

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

        Vector e1 = new Point(1, 0, 0).subtract(new Point(0, 0, 1));
        Vector e2 = new Point(0, 1, 0).subtract(new Point(0, 0, 1));
        assertEquals(0d, n.dotProduct(e1), DELTA, ERROR_TRIANGLE);
        assertEquals(0d, n.dotProduct(e2), DELTA, ERROR_TRIANGLE);
    }

    /**
     * Test method for {@link Triangle#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        Triangle triangle = new Triangle(new Point(1, 0, 1), new Point(0, 2, 1), new Point(2, 2, 1));

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray intersects inside the triangle (1 point)
        {
            Ray ray = new Ray(new Point(1, 1.5, 0), new Vector(0, 0, 1));
            var result = triangle.findIntersections(ray);
            assertNotNull(result, ERROR_TRIANGLE_INTERSECTION);
            assertEquals(1, result.size(), ERROR_TRIANGLE_INTERSECTION);
            assertPointEquals(new Point(1, 1.5, 1), result.get(0), ERROR_TRIANGLE_INTERSECTION);
        }

        // EP02: Ray intersects the plane outside the triangle against an edge (0 points)
        {
            Ray ray = new Ray(new Point(1, -0.5, 0), new Vector(0, 0, 1));
            var result = triangle.findIntersections(ray);
            assertNull(result, ERROR_TRIANGLE_INTERSECTION);
        }

        // EP03: Ray intersects the plane outside the triangle against a vertex (0 points)
        {
            Ray ray = new Ray(new Point(-1, 2, 0), new Vector(0, 0, 1));
            var result = triangle.findIntersections(ray);
            assertNull(result, ERROR_TRIANGLE_INTERSECTION);
        }

        // =============== Boundary Values Tests ==================
        // BV01: Intersection point is on an edge (0 points)
        {
            Ray ray = new Ray(new Point(0.5, 1, 0), new Vector(0, 0, 1));
            var result = triangle.findIntersections(ray);
            assertNull(result, ERROR_TRIANGLE_INTERSECTION);
        }

        // BV02: Intersection point is in a vertex (0 points)
        {
            Ray ray = new Ray(new Point(1, 0, 0), new Vector(0, 0, 1));
            var result = triangle.findIntersections(ray);
            assertNull(result, ERROR_TRIANGLE_INTERSECTION);
        }

        // BV03: Intersection point is on an edge continuation (0 points)
        {
            Ray ray = new Ray(new Point(1.5, -1, 0), new Vector(0, 0, 1));
            var result = triangle.findIntersections(ray);
            assertNull(result, ERROR_TRIANGLE_INTERSECTION);
        }

        // ---- Plane-related cases (must include plane "no intersection" cases) ----
        // BV04: Ray is parallel to the triangle plane and included in the plane
        {
            Ray ray = new Ray(new Point(1, 1, 1), new Vector(1, 0, 0));
            var result = triangle.findIntersections(ray);
            assertNull(result, ERROR_TRIANGLE_INTERSECTION);
        }

        // BV05: Ray is parallel to the triangle plane and not included in the plane
        {
            Ray ray = new Ray(new Point(1, 1, 2), new Vector(1, 0, 0));
            var result = triangle.findIntersections(ray);
            assertNull(result, ERROR_TRIANGLE_INTERSECTION);
        }

        // BV06: Ray begins on the plane (0 points)
        {
            Ray ray = new Ray(new Point(1, 1.5, 1), new Vector(0, 0, 1));
            var result = triangle.findIntersections(ray);
            assertNull(result, ERROR_TRIANGLE_INTERSECTION);
        }
    }
}

