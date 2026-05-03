package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Unit tests for class {@link Plane}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>{@link Plane#getNormal(Point)}</li>
 * <li>Constructor {@link Plane#Plane(Point, Vector)} produces a normalized normal</li>
 * <li>Constructor {@link Plane#Plane(Point, Point, Point)} accepts valid points and rejects invalid ones</li>
 * </ul>
 */
class PlaneTests {
    /** Basic default constructor to satisfy documentation tools */
    PlaneTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Error message for wrong plane */
    private static final String ERROR_PLANE = "ERROR: wrong Plane behavior";

    /** Error message for valid constructor not throwing */
    private static final String ERROR_VALID_CONSTRUCTOR_THREW = "ERROR: valid plane constructor must not throw";

    /** Error message for wrong plane intersection */
    private static final String ERROR_PLANE_INTERSECTION = "ERROR: wrong Plane intersection result";

    private static void assertPointEquals(Point expected, Point actual, String message) {
        assertEquals(0d, expected.distance(actual), DELTA, message);
    }

    /**
     * Test method for {@link Plane#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        Point p0 = new Point(0, 0, 0);
        Vector normal = new Vector(0, 0, 5);
        Plane plane = new Plane(p0, normal);

        // ============ Equivalence Partitions Tests ==============
        // EP01: getNormal in a point that is not the reference point
        assertEquals(normal.normalize(), plane.getNormal(new Point(1, 2, 0)), ERROR_PLANE);

        // =============== Boundary Values Tests ==================
        // BV01: getNormal at the reference point
        assertEquals(normal.normalize(), plane.getNormal(p0), ERROR_PLANE);
    }

    /**
     * Test method for constructor {@link Plane#Plane(Point, Vector)}.
     */
    @Test
    void testConstructorPointVectorNormalizes() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Provided normal is normalized in the stored plane
        Plane plane = new Plane(new Point(0, 0, 0), new Vector(0, 0, 2));
        assertEquals(1d, plane.getNormal(new Point(1, 1, 0)).length(), DELTA, ERROR_PLANE);
    }

    /**
     * Test method for constructor {@link Plane#Plane(Point, Point, Point)}.
     */
    @Test
    void testConstructorThreePoints() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Three non-collinear points
        assertDoesNotThrow(() -> new Plane(new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 1, 0)),
            ERROR_VALID_CONSTRUCTOR_THREW);

        // =============== Boundary Values Tests ==================
        // BV01: Two points coincide
        assertThrows(IllegalArgumentException.class,
            () -> new Plane(new Point(0, 0, 1), new Point(0, 0, 1), new Point(0, 1, 0)),
            ERROR_PLANE);

        // BV01.1: Two points coincide (p1 == p3)
        assertThrows(IllegalArgumentException.class,
            () -> new Plane(new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 0, 1)),
            ERROR_PLANE);

        // BV01.2: Two points coincide (p2 == p3)
        assertThrows(IllegalArgumentException.class,
            () -> new Plane(new Point(0, 0, 1), new Point(1, 0, 0), new Point(1, 0, 0)),
            ERROR_PLANE);

        // BV02: All three points coincide
        assertThrows(IllegalArgumentException.class,
            () -> new Plane(new Point(1, 1, 1), new Point(1, 1, 1), new Point(1, 1, 1)),
            ERROR_PLANE);

        // BV03: Three points are collinear
        assertThrows(IllegalArgumentException.class,
            () -> new Plane(new Point(0, 0, 0), new Point(1, 1, 1), new Point(2, 2, 2)),
            ERROR_PLANE);
    }

    /**
     * Test method for {@link Plane#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        Plane plane = new Plane(new Point(0, 0, 1), new Vector(0, 0, 1));

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray intersects the plane (1 point)
        {
            Ray ray = new Ray(new Point(1, 0, 0), new Vector(0, 1, 1));
            var result = plane.findIntersections(ray);
            assertNotNull(result, ERROR_PLANE_INTERSECTION);
            assertEquals(1, result.size(), ERROR_PLANE_INTERSECTION);
            assertPointEquals(new Point(1, 1, 1), result.get(0), ERROR_PLANE_INTERSECTION);
        }

        // EP02: Ray does not intersect the plane (0 points)
        {
            Ray ray = new Ray(new Point(1, 0, 2), new Vector(0, 1, 1));
            var result = plane.findIntersections(ray);
            assertNull(result, ERROR_PLANE_INTERSECTION);
        }

        // =============== Boundary Values Tests ==================
        // BV01: Ray is parallel to the plane and included in the plane
        {
            Ray ray = new Ray(new Point(1, 0, 1), new Vector(1, 0, 0));
            var result = plane.findIntersections(ray);
            assertNull(result, ERROR_PLANE_INTERSECTION);
        }

        // BV02: Ray is parallel to the plane and not included in the plane
        {
            Ray ray = new Ray(new Point(1, 0, 2), new Vector(1, 0, 0));
            var result = plane.findIntersections(ray);
            assertNull(result, ERROR_PLANE_INTERSECTION);
        }

        // BV03: Ray is orthogonal to the plane and starts before the plane (1 point)
        {
            Ray ray = new Ray(new Point(1, 0, 0), new Vector(0, 0, 1));
            var result = plane.findIntersections(ray);
            assertNotNull(result, ERROR_PLANE_INTERSECTION);
            assertEquals(1, result.size(), ERROR_PLANE_INTERSECTION);
            assertPointEquals(new Point(1, 0, 1), result.get(0), ERROR_PLANE_INTERSECTION);
        }

        // BV04: Ray is orthogonal to the plane and starts in the plane (0 points)
        {
            Ray ray = new Ray(new Point(1, 0, 1), new Vector(0, 0, 1));
            var result = plane.findIntersections(ray);
            assertNull(result, ERROR_PLANE_INTERSECTION);
        }

        // BV05: Ray is orthogonal to the plane and starts after the plane (0 points)
        {
            Ray ray = new Ray(new Point(1, 0, 2), new Vector(0, 0, 1));
            var result = plane.findIntersections(ray);
            assertNull(result, ERROR_PLANE_INTERSECTION);
        }

        // BV06: Ray is neither orthogonal nor parallel to the plane and begins on the plane (0 points)
        {
            Ray ray = new Ray(new Point(1, 0, 1), new Vector(0, 1, 1));
            var result = plane.findIntersections(ray);
            assertNull(result, ERROR_PLANE_INTERSECTION);
        }

        // BV07: Ray begins at the reference point of the plane (Q) (0 points)
        {
            Ray ray = new Ray(new Point(0, 0, 1), new Vector(0, 1, 1));
            var result = plane.findIntersections(ray);
            assertNull(result, ERROR_PLANE_INTERSECTION);
        }
    }
}
