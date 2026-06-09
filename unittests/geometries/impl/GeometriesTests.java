package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Unit tests for class {@link Geometries}.
 */
class GeometriesTests {
    /** Basic default constructor to satisfy documentation tools */
    GeometriesTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Error message for wrong geometries composite behavior */
    private static final String ERROR_GEOMETRIES = "ERROR: wrong Geometries intersection result";

    /**
     * Test method for {@link Geometries#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Some geometries are intersected
        {
            Sphere sphere1 = new Sphere(new Point(2, 0, 0), 1d);
            Plane plane = new Plane(new Point(4, 0, 0), new Vector(1, 0, 0));
            Geometries geometries = new Geometries(sphere1, plane,
                new Plane(new Point(0, 0, 2), new Vector(0, 0, 1)));

            var result = geometries.findIntersections(new Ray(new Point(-2, 0, 0), new Vector(1, 0, 0)));
            assertNotNull(result, ERROR_GEOMETRIES);
            assertEquals(3, result.size(), ERROR_GEOMETRIES);
        }

        // =============== Boundary Values Tests ==================
        // BV01: No geometry is intersected
        {
            Sphere sphere1 = new Sphere(new Point(2, 0, 0), 1d);
            Sphere sphere2 = new Sphere(new Point(6, 0, 0), 1d);
            Plane plane = new Plane(new Point(0, 4, 0), new Vector(0, 1, 0));
            Geometries geometries = new Geometries(sphere1, plane, sphere2);

            var result = geometries.findIntersections(new Ray(new Point(-2, 2, 0), new Vector(1, 0, 0)));
            assertNull(result, ERROR_GEOMETRIES);
        }

        // BV02: Only one geometry is intersected
        {
            Sphere sphere1 = new Sphere(new Point(2, 0, 0), 1d);
            Sphere sphere2 = new Sphere(new Point(6, 3, 0), 1d);
            Plane plane = new Plane(new Point(0, 0, 2), new Vector(0, 0, 1));
            Geometries geometries = new Geometries(sphere1, plane, sphere2);

            var result = geometries.findIntersections(new Ray(new Point(-2, 0, 0), new Vector(1, 0, 0)));
            assertNotNull(result, ERROR_GEOMETRIES);
            assertEquals(2, result.size(), ERROR_GEOMETRIES);
        }

        // BV03: All geometries are intersected
        {
            Sphere sphere1 = new Sphere(new Point(2, 0, 0), 1d);
            Sphere sphere2 = new Sphere(new Point(6, 0, 0), 1d);
            Plane plane = new Plane(new Point(4, 0, 0), new Vector(1, 0, 0));
            Geometries geometries = new Geometries(sphere1, plane, sphere2);

            var result = geometries.findIntersections(new Ray(new Point(-2, 0, 0), new Vector(1, 0, 0)));
            assertNotNull(result, ERROR_GEOMETRIES);
            assertEquals(5, result.size(), ERROR_GEOMETRIES);
        }
    }

    /**
     * Test method for {@link geometries.api.Intersectable#calcIntersections(Ray, double)}
     * in {@link Geometries}.
     */
    @Test
    void testCalcIntersectionsWithMaxDistance() {
        Sphere sphere = new Sphere(new Point(2, 0, 0), 1d); // t = 3,5
        Plane plane = new Plane(new Point(4, 0, 0), new Vector(1, 0, 0)); // t = 6
        Geometries geometries = new Geometries(sphere, plane);
        Ray ray = new Ray(new Point(-2, 0, 0), new Vector(1, 0, 0));

        var resultBefore = geometries.calcIntersections(ray, 2.9);
        assertNull(resultBefore, ERROR_GEOMETRIES);

        var resultAtSphereEnter = geometries.calcIntersections(ray, 3.0);
        assertNotNull(resultAtSphereEnter, ERROR_GEOMETRIES);
        assertEquals(1, resultAtSphereEnter.size(), ERROR_GEOMETRIES);

        var resultAtSphereExit = geometries.calcIntersections(ray, 5.0);
        assertNotNull(resultAtSphereExit, ERROR_GEOMETRIES);
        assertEquals(2, resultAtSphereExit.size(), ERROR_GEOMETRIES);

        var resultAtPlane = geometries.calcIntersections(ray, 6.0);
        assertNotNull(resultAtPlane, ERROR_GEOMETRIES);
        assertEquals(3, resultAtPlane.size(), ERROR_GEOMETRIES);
    }
}
