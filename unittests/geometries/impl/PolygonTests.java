package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Unit tests for class {@link Polygon}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>Constructor {@link Polygon#Polygon(Point...)}</li>
 * <li>{@link Polygon#getNormal(Point)}</li>
 * </ul>
 */
class PolygonTests {
    /** Basic default constructor to satisfy documentation tools */
    PolygonTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Test vertex (0,0,1) */
    private static final Point POINT_Z = new Point(0, 0, 1);
    /** Test vertex (1,0,0) */
    private static final Point POINT_X = new Point(1, 0, 0);
    /** Test vertex (0,1,0) */
    private static final Point POINT_Y = new Point(0, 1, 0);
    /** Additional vertex for valid polygon */
    private static final Point POINT1 = new Point(-1, 1, 1);

    /** Additional vertex for non-coplanar polygon */
    private static final Point POINT_NON_COPLANAR = new Point(0, 2, 2);

    /** Error message for wrong exception */
    private static final String ERROR_EXCEPTION = "ERROR: Exception thrown";
    /** Error message for wrong polygon */
    private static final String ERROR_POLYGON = "ERROR: wrong polygon";
    /** Error message for wrong normal */
    private static final String ERROR_NORMAL = "ERROR: wrong normal";

    /** Error message for wrong polygon intersection */
    private static final String ERROR_POLYGON_INTERSECTION = "ERROR: wrong Polygon intersection result";

    /** Polygon used in intersection tests */
    private static final Polygon POLYGON_INTERSECTIONS = new Polygon(
        new Point(1, 1, 1),
        new Point(3, 1, 1),
        new Point(3, 3, 1),
        new Point(1, 3, 1));

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
     * Test method for {@link Polygon#Polygon(Point...)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct convex quadrilateral with vertices in correct order
        assertDoesNotThrow(() -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT1), ERROR_EXCEPTION);

        // EP02: Wrong vertices order
        assertThrows(IllegalArgumentException.class,
            () -> new Polygon(POINT_Z, POINT_Y, POINT_X, POINT1), ERROR_POLYGON);

        // EP03: Not in the same plane
        assertThrows(IllegalArgumentException.class,
            () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_NON_COPLANAR), ERROR_POLYGON);

        // =============== Boundary Values Tests ==================
        // BV01: Less than 3 vertices
        assertThrows(IllegalArgumentException.class,
            () -> new Polygon(POINT_Z, POINT_X), ERROR_POLYGON);

        // BV02: Consecutive vertices are the same point
        assertThrows(IllegalArgumentException.class,
            () -> new Polygon(POINT_Z, POINT_X, POINT_X, POINT1), ERROR_POLYGON);
    }

    /**
     * Test method for {@link Polygon#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        // Arrange
        Point[] points = {POINT_Z, POINT_X, POINT_Y, POINT1};
        Polygon polygon = new Polygon(points);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normal exists and is unit-length and orthogonal to edges
        // Act
        Vector result = assertDoesNotThrow(() -> polygon.getNormal(POINT_Z), ERROR_EXCEPTION);

        // Assert
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL);

        Point previous = points[points.length - 1];
        for (Point point : points) {
            Vector edge = point.subtract(previous);
            previous = point;
            assertEquals(0d, result.dotProduct(edge), DELTA, ERROR_NORMAL);
        }
    }

    /**
     * Test method for {@link Polygon#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Intersection point is inside the polygon (1 point)
        {
            Ray ray = new Ray(new Point(2, 2, 0), new Vector(0, 0, 1));
            var result = POLYGON_INTERSECTIONS.findIntersections(ray);
            assertNotNull(result, ERROR_POLYGON_INTERSECTION);
            assertEquals(1, result.size(), ERROR_POLYGON_INTERSECTION);
            assertPointEquals(new Point(2, 2, 1), result.get(0), ERROR_POLYGON_INTERSECTION);
        }

        // EP02: Intersection point is outside the polygon against an edge (0 points)
        {
            Ray ray = new Ray(new Point(0.5, 2, 0), new Vector(0, 0, 1));
            var result = POLYGON_INTERSECTIONS.findIntersections(ray);
            assertNull(result, ERROR_POLYGON_INTERSECTION);
        }

        // EP03: Intersection point is outside the polygon against a vertex (0 points)
        {
            Ray ray = new Ray(new Point(0.5, 0.5, 0), new Vector(0, 0, 1));
            var result = POLYGON_INTERSECTIONS.findIntersections(ray);
            assertNull(result, ERROR_POLYGON_INTERSECTION);
        }

        // =============== Boundary Values Tests ==================
        // BV01: Intersection point is on an edge (0 points)
        {
            Ray ray = new Ray(new Point(1, 2, 0), new Vector(0, 0, 1));
            var result = POLYGON_INTERSECTIONS.findIntersections(ray);
            assertNull(result, ERROR_POLYGON_INTERSECTION);
        }

        // BV02: Intersection point is on a vertex (0 points)
        {
            Ray ray = new Ray(new Point(1, 1, 0), new Vector(0, 0, 1));
            var result = POLYGON_INTERSECTIONS.findIntersections(ray);
            assertNull(result, ERROR_POLYGON_INTERSECTION);
        }

        // BV03: Intersection point is on an edge continuation (0 points)
        {
            Ray ray = new Ray(new Point(1, 4, 0), new Vector(0, 0, 1));
            var result = POLYGON_INTERSECTIONS.findIntersections(ray);
            assertNull(result, ERROR_POLYGON_INTERSECTION);
        }

        // Plane-related no-intersection case: parallel and included in the plane
        {
            Ray ray = new Ray(new Point(2, 2, 1), new Vector(1, 0, 0));
            var result = POLYGON_INTERSECTIONS.findIntersections(ray);
            assertNull(result, ERROR_POLYGON_INTERSECTION);
        }

        // Plane-related no-intersection case: parallel and not included in the plane
        {
            Ray ray = new Ray(new Point(2, 2, 2), new Vector(1, 0, 0));
            var result = POLYGON_INTERSECTIONS.findIntersections(ray);
            assertNull(result, ERROR_POLYGON_INTERSECTION);
        }

        // Plane-related no-intersection case: ray begins in the plane (t=0 excluded)
        {
            Ray ray = new Ray(new Point(2, 2, 1), new Vector(0, 0, 1));
            var result = POLYGON_INTERSECTIONS.findIntersections(ray);
            assertNull(result, ERROR_POLYGON_INTERSECTION);
        }
    }

    /**
     * Test method for {@link geometries.api.Intersectable#calcIntersections(Ray, double)}
     * in {@link Polygon}.
     */
    @Test
    void testCalcIntersectionsWithMaxDistance() {
        Ray ray = new Ray(new Point(2, 2, 0), new Vector(0, 0, 1)); // t = 1 inside polygon

        var resultBefore = POLYGON_INTERSECTIONS.calcIntersections(ray, 0.9);
        assertNull(resultBefore, ERROR_POLYGON_INTERSECTION);

        var resultAt = POLYGON_INTERSECTIONS.calcIntersections(ray, 1.0);
        assertNotNull(resultAt, ERROR_POLYGON_INTERSECTION);
        assertEquals(1, resultAt.size(), ERROR_POLYGON_INTERSECTION);
        assertPointEquals(new Point(2, 2, 1), resultAt.get(0).point, ERROR_POLYGON_INTERSECTION);

        var resultAfter = POLYGON_INTERSECTIONS.calcIntersections(ray, 2.0);
        assertNotNull(resultAfter, ERROR_POLYGON_INTERSECTION);
        assertEquals(1, resultAfter.size(), ERROR_POLYGON_INTERSECTION);
        assertPointEquals(new Point(2, 2, 1), resultAfter.get(0).point, ERROR_POLYGON_INTERSECTION);
    }
}
