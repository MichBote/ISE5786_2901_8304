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
    void testFindIntersectionsSome() {
        // Arrange
        Sphere sphere1 = new Sphere(new Point(2, 0, 0), 1d);
        Plane plane = new Plane(new Point(4, 0, 0), new Vector(1, 0, 0));

        Geometries geometries = new Geometries(sphere1, plane, new Plane(new Point(0, 0, 2), new Vector(0, 0, 1)));

        // Act
        var result = geometries.findIntersections(new Ray(new Point(-2, 0, 0), new Vector(1, 0, 0)));

        // Assert
        // ============ Equivalence Partitions Tests ==============
        // EP01: Some geometries are intersected
        assertNotNull(result, ERROR_GEOMETRIES);
        assertEquals(3, result.size(), ERROR_GEOMETRIES);
    }

    /**
     * Test method for {@link Geometries#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersectionsNone() {
        // Arrange
        Sphere sphere1 = new Sphere(new Point(2, 0, 0), 1d);
        Sphere sphere2 = new Sphere(new Point(6, 0, 0), 1d);
        Plane plane = new Plane(new Point(0, 4, 0), new Vector(0, 1, 0));
        Geometries geometries = new Geometries(sphere1, plane, sphere2);

        // Act
        var result = geometries.findIntersections(new Ray(new Point(-2, 2, 0), new Vector(1, 0, 0)));

        // Assert
        // =============== Boundary Values Tests ==================
        // BV01: No geometry is intersected
        assertNull(result, ERROR_GEOMETRIES);
    }

    /**
     * Test method for {@link Geometries#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersectionsOne() {
        // Arrange
        Sphere sphere1 = new Sphere(new Point(2, 0, 0), 1d);
        Sphere sphere2 = new Sphere(new Point(6, 3, 0), 1d);
        Plane plane = new Plane(new Point(0, 0, 2), new Vector(0, 0, 1));
        Geometries geometries = new Geometries(sphere1, plane, sphere2);

        // Act
        var result = geometries.findIntersections(new Ray(new Point(-2, 0, 0), new Vector(1, 0, 0)));

        // Assert
        // =============== Boundary Values Tests ==================
        // BV02: Only one geometry is intersected
        assertNotNull(result, ERROR_GEOMETRIES);
        assertEquals(2, result.size(), ERROR_GEOMETRIES);
    }

    /**
     * Test method for {@link Geometries#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersectionsAll() {
        // Arrange
        Sphere sphere1 = new Sphere(new Point(2, 0, 0), 1d);
        Sphere sphere2 = new Sphere(new Point(6, 0, 0), 1d);
        Plane plane = new Plane(new Point(4, 0, 0), new Vector(1, 0, 0));
        Geometries geometries = new Geometries(sphere1, plane, sphere2);

        // Act
        var result = geometries.findIntersections(new Ray(new Point(-2, 0, 0), new Vector(1, 0, 0)));

        // Assert
        // =============== Boundary Values Tests ==================
        // BV03: All geometries are intersected
        assertNotNull(result, ERROR_GEOMETRIES);
        assertEquals(5, result.size(), ERROR_GEOMETRIES);
    }
}
