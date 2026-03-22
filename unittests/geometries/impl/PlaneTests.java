package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
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
                "ERROR: valid plane constructor must not throw");

        // =============== Boundary Values Tests ==================
        // BV01: Two points coincide
        assertThrows(IllegalArgumentException.class,
                () -> new Plane(new Point(0, 0, 1), new Point(0, 0, 1), new Point(0, 1, 0)),
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
}
