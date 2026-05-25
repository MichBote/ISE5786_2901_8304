package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Unit tests for class {@link Tube}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>{@link Tube#getNormal(Point)}</li>
 * </ul>
 */
class TubeTests {
    /** Basic default constructor to satisfy documentation tools */
    TubeTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Error message for wrong tube */
    private static final String ERROR_TUBE = "ERROR: wrong Tube normal";

    /** Error message for wrong tube intersection */
    private static final String ERROR_TUBE_INTERSECTION = "ERROR: wrong Tube intersection result";

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
     * Asserts the expected number of intersection points.
     *
     * @param result actual intersection list
     * @param expectedCount expected number of intersections
     * @param message assertion message
     */
    private static void assertIntersectionsCount(List<Point> result, int expectedCount, String message) {
        if (expectedCount == 0) {
            assertNull(result, message);
        } else {
            assertNotNull(result, message);
            assertEquals(expectedCount, result.size(), message);
        }
    }

    /**
     * Creates the tube shared by tube tests.
     *
     * @return test tube
     */
    private static Tube createTestTube() {
        Ray axis = new Ray(new Point(1, 2, 3), new Vector(0, 0, 1));
        return new Tube(1d, axis);
    }

    /**
     * Test method for {@link Tube#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        {
            Tube tube = createTestTube();

            Vector nFront = tube.getNormal(new Point(2, 2, 8));
            Vector nBack = tube.getNormal(new Point(2, 2, 1));

            assertEquals(1d, nFront.length(), DELTA, ERROR_TUBE);
            assertEquals(new Vector(1, 0, 0), nFront, ERROR_TUBE);
            assertEquals(1d, nBack.length(), DELTA, ERROR_TUBE);
            assertEquals(new Vector(1, 0, 0), nBack, ERROR_TUBE);
        }

        // =============== Boundary Values Tests ==================
        {
            Tube tube = createTestTube();

            Vector n = tube.getNormal(new Point(1, 3, 3));

            assertEquals(1d, n.length(), DELTA, ERROR_TUBE);
            assertEquals(new Vector(0, 1, 0), n, ERROR_TUBE);
        }
    }

    /**
     * Test method for {@link Tube#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {
        // Outside (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(-1, 4, 4), new Vector(1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Crosses (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(-1, 2, 4), new Vector(1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(0, 2, 4), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 4), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // Starts inside (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1.5, 2, 4), new Vector(1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 4), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // Starts on surface -> in (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(0, 2, 4), new Vector(1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 4), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // Starts on surface -> out (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(0, 2, 4), new Vector(-1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Tangent (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(-1, 3, 4), new Vector(1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Parallel to axis - outside (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(3, 2, 0), new Vector(0, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Parallel to axis - inside (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1.5, 2, 0), new Vector(0, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Parallel to axis - on surface (treated as 0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(0, 2, 0), new Vector(0, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Skew ray (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(-1, 1, 4), new Vector(1, 1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
        }

        // Skew ray (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(-1, 4, 4), new Vector(1, 1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Orthogonal to axis (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 0, 4), new Vector(0, 1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(1, 1, 4), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(1, 3, 4), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // Orthogonal to axis tangent (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 1, 4), new Vector(1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Diagonal through tube (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(-2, -1, 4), new Vector(1, 1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
        }

        // Diagonal misses (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(-2, -1, 4), new Vector(-1, -1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // From axis (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 2, 0), new Vector(1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 0), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // From axis origin (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 2, 3), new Vector(1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 3), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // From axis skew (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 2, 0), new Vector(1, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 1), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // Skew with axis component (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(-1, 2, 0), new Vector(1, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(0, 2, 1), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 3), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // Orthogonal starts on surface -> in (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 1, 4), new Vector(0, 1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(1, 3, 4), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // Orthogonal starts on surface -> out (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 1, 4), new Vector(0, -1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Orthogonal outside (2) reverse direction
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 4, 4), new Vector(0, -1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(1, 3, 4), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(1, 1, 4), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // Parallel on axis (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 2, 0), new Vector(0, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Skew XZ (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(-1, 2, 10), new Vector(1, 0, -1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(0, 2, 9), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 7), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // Skew YZ (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 0, 10), new Vector(0, 1, -1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(1, 1, 9), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(1, 3, 7), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // Starts inside skew (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1.5, 2, 10), new Vector(1, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 10.5), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // Starts inside skew other side (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1.5, 2, 10), new Vector(-1, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(0, 2, 11.5), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // Starts on surface skew -> in (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(0, 2, 10), new Vector(1, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 12), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // Starts on surface skew -> out (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(0, 2, 10), new Vector(-1, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Starts on surface tangent with axis component (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(0, 2, 10), new Vector(0, 1, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Skew outside (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(-1, 4, 10), new Vector(1, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Skew from outside (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(3, 2, 10), new Vector(-1, 0, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 11), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(0, 2, 13), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // Crosses (2) reverse direction
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(3, 2, 4), new Vector(-1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 4), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(0, 2, 4), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // From axis reverse (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 2, 0), new Vector(-1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(0, 2, 0), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // From axis origin reverse (1)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(1, 2, 3), new Vector(-1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(0, 2, 3), result.get(0), ERROR_TUBE_INTERSECTION);
        }

        // Starts on other surface -> out (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(2, 2, 4), new Vector(1, 0, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }

        // Diagonal XY (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(0, 0, 4), new Vector(1, 1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(1, 1, 4), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 4), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // Diagonal XY reverse (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(3, 3, 4), new Vector(-1, -1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 4), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(1, 1, 4), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // Diagonal XYZ (2)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(0, 0, 0), new Vector(1, 1, 1));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(1, 1, 1), result.get(0), ERROR_TUBE_INTERSECTION);
            assertPointEquals(new Point(2, 2, 2), result.get(1), ERROR_TUBE_INTERSECTION);
        }

        // Tangent in Y direction (0)
        {
            Tube tube = createTestTube();
            Ray ray = new Ray(new Point(2, 0, 4), new Vector(0, 1, 0));
            var result = tube.findIntersections(ray);
            assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
        }
    }
}
