package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Unit tests for class {@link Sphere}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>{@link Sphere#getNormal(Point)}</li>
 * </ul>
 */
class SphereTests {
    /** Basic default constructor to satisfy documentation tools */
    SphereTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Error message for wrong sphere */
    private static final String ERROR_SPHERE = "ERROR: wrong Sphere normal";

    /** Error message for wrong sphere intersection */
    private static final String ERROR_SPHERE_INTERSECTION = "ERROR: wrong Sphere intersection result";

    /** Sphere used in intersection tests (avoid Point.ZERO in test data) */
    private static final Sphere SPHERE = new Sphere(new Point(2, 0, 0), 1d);

    /**
     * Test method for {@link Sphere#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        Sphere sphere = new Sphere(new Point(0, 0, 0), 1d);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normal at point (0,0,1) should be (0,0,1)
        Vector n = sphere.getNormal(new Point(0, 0, 1));
        assertEquals(1d, n.length(), DELTA, ERROR_SPHERE);
        assertEquals(new Vector(0, 0, 1), n, ERROR_SPHERE);
    }

    /**
     * Asserts that two points are equal within the test tolerance.
     *
     * @param expected expected point
     * @param actual actual point
     * @param message assertion message
     */
    private static void assertPointEquals(Point expected, Point actual, String message) {
        assertEquals(0d, expected.distance(actual), DELTA, message);
    }

    /**
     * Test method for {@link Sphere#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray's line is outside the sphere (0 points)
        {
            Ray ray = new Ray(new Point(-2, 2, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNull(result, ERROR_SPHERE_INTERSECTION);
        }

        // EP02: Ray starts before and crosses the sphere (2 points)
        {
            Ray ray = new Ray(new Point(-2, 0, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNotNull(result, ERROR_SPHERE_INTERSECTION);
            assertEquals(2, result.size(), ERROR_SPHERE_INTERSECTION);
            assertPointEquals(new Point(1, 0, 0), result.get(0), ERROR_SPHERE_INTERSECTION);
            assertPointEquals(new Point(3, 0, 0), result.get(1), ERROR_SPHERE_INTERSECTION);
        }

        // EP03: Ray starts inside the sphere (1 point)
        {
            Ray ray = new Ray(new Point(2.5, 0, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNotNull(result, ERROR_SPHERE_INTERSECTION);
            assertEquals(1, result.size(), ERROR_SPHERE_INTERSECTION);
            assertPointEquals(new Point(3, 0, 0), result.get(0), ERROR_SPHERE_INTERSECTION);
        }

        // EP04: Ray starts after the sphere (0 points)
        {
            Ray ray = new Ray(new Point(4, 0, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNull(result, ERROR_SPHERE_INTERSECTION);
        }

        // =============== Boundary Values Tests ==================
        // BV11: Ray starts at sphere and goes inside (1 point)
        {
            Point p1 = new Point(2 - Math.sqrt(0.75), 0, 0.5);
            Point p2 = new Point(2 + Math.sqrt(0.75), 0, 0.5);
            Ray ray = new Ray(p1, new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNotNull(result, ERROR_SPHERE_INTERSECTION);
            assertEquals(1, result.size(), ERROR_SPHERE_INTERSECTION);
            assertPointEquals(p2, result.get(0), ERROR_SPHERE_INTERSECTION);
        }

        // BV12: Ray starts at sphere and goes outside (0 points)
        {
            Point p1 = new Point(2 - Math.sqrt(0.75), 0, 0.5);
            Ray ray = new Ray(p1, new Vector(-1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNull(result, ERROR_SPHERE_INTERSECTION);
        }

        // BV21: Ray starts before the sphere and goes through center (2 points)
        {
            Ray ray = new Ray(new Point(-2, 0, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNotNull(result, ERROR_SPHERE_INTERSECTION);
            assertEquals(2, result.size(), ERROR_SPHERE_INTERSECTION);
            assertPointEquals(new Point(1, 0, 0), result.get(0), ERROR_SPHERE_INTERSECTION);
            assertPointEquals(new Point(3, 0, 0), result.get(1), ERROR_SPHERE_INTERSECTION);
        }

        // BV22: Ray starts at sphere and goes inside through center (1 point)
        {
            Ray ray = new Ray(new Point(1, 0, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNotNull(result, ERROR_SPHERE_INTERSECTION);
            assertEquals(1, result.size(), ERROR_SPHERE_INTERSECTION);
            assertPointEquals(new Point(3, 0, 0), result.get(0), ERROR_SPHERE_INTERSECTION);
        }

        // BV23: Ray starts inside through center (1 point)
        {
            Ray ray = new Ray(new Point(2.5, 0, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNotNull(result, ERROR_SPHERE_INTERSECTION);
            assertEquals(1, result.size(), ERROR_SPHERE_INTERSECTION);
            assertPointEquals(new Point(3, 0, 0), result.get(0), ERROR_SPHERE_INTERSECTION);
        }

        // BV24: Ray starts at the center (1 point)
        {
            Ray ray = new Ray(new Point(2, 0, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNotNull(result, ERROR_SPHERE_INTERSECTION);
            assertEquals(1, result.size(), ERROR_SPHERE_INTERSECTION);
            assertPointEquals(new Point(3, 0, 0), result.get(0), ERROR_SPHERE_INTERSECTION);
        }

        // BV25: Ray starts at sphere and goes outside through center (0 points)
        {
            Ray ray = new Ray(new Point(3, 0, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNull(result, ERROR_SPHERE_INTERSECTION);
        }

        // BV26: Ray starts after sphere through center (0 points)
        {
            Ray ray = new Ray(new Point(4, 0, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNull(result, ERROR_SPHERE_INTERSECTION);
        }

        // BV31: Ray starts before the tangent point (0 points)
        {
            Ray ray = new Ray(new Point(0, 1, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNull(result, ERROR_SPHERE_INTERSECTION);
        }

        // BV32: Ray starts at the tangent point (0 points)
        {
            Ray ray = new Ray(new Point(2, 1, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNull(result, ERROR_SPHERE_INTERSECTION);
        }

        // BV33: Ray starts after the tangent point (0 points)
        {
            Ray ray = new Ray(new Point(3, 1, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNull(result, ERROR_SPHERE_INTERSECTION);
        }

        // BV41: Ray's line is outside sphere, ray is orthogonal to start-to-center line (0 points)
        {
            Ray ray = new Ray(new Point(2, 2, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNull(result, ERROR_SPHERE_INTERSECTION);
        }

        // BV42: Ray starts inside, ray is orthogonal to start-to-center line (1 point)
        {
            Ray ray = new Ray(new Point(2, 0.5, 0), new Vector(1, 0, 0));
            var result = SPHERE.findIntersections(ray);
            assertNotNull(result, ERROR_SPHERE_INTERSECTION);
            assertEquals(1, result.size(), ERROR_SPHERE_INTERSECTION);
            assertPointEquals(new Point(2 + Math.sqrt(0.75), 0.5, 0), result.get(0), ERROR_SPHERE_INTERSECTION);
        }
    }
}
