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

    /** Delta for point comparisons */
    private static void assertPointEquals(Point expected, Point actual, String message) {
        assertEquals(0d, expected.distance(actual), DELTA, message);
    }

    private static void assertIntersectionsCount(List<Point> result, int expectedCount, String message) {
        if (expectedCount == 0) {
            assertNull(result, message);
        } else {
            assertNotNull(result, message);
            assertEquals(expectedCount, result.size(), message);
        }
    }

    private static Tube createTestTube() {
        Ray axis = new Ray(new Point(1, 2, 3), new Vector(0, 0, 1));
        return new Tube(1d, axis);
    }

    @Test
    void testGetNormalProjectionNotAtOrigin() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Tube tube = createTestTube();

        // Act
        Vector nFront = tube.getNormal(new Point(2, 2, 8));
        Vector nBack = tube.getNormal(new Point(2, 2, 1));

        // Assert
        assertEquals(1d, nFront.length(), DELTA, ERROR_TUBE);
        assertEquals(new Vector(1, 0, 0), nFront, ERROR_TUBE);
        assertEquals(1d, nBack.length(), DELTA, ERROR_TUBE);
        assertEquals(new Vector(1, 0, 0), nBack, ERROR_TUBE);
    }

    @Test
    void testGetNormalProjectionAtOrigin() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Tube tube = createTestTube();

        // Act
        Vector n = tube.getNormal(new Point(1, 3, 3));

        // Assert
        assertEquals(1d, n.length(), DELTA, ERROR_TUBE);
        assertEquals(new Vector(0, 1, 0), n, ERROR_TUBE);
    }

    /**
     * Test method for {@link Tube#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersectionsOutside() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(-1, 4, 4), new Vector(1, 0, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsCrossesTwoPoints() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(-1, 2, 4), new Vector(1, 0, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(0, 2, 4), result.get(0), ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(2, 2, 4), result.get(1), ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsStartsInside() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1.5, 2, 4), new Vector(1, 0, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(2, 2, 4), result.get(0), ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsStartsOnSurfaceGoesIn() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(0, 2, 4), new Vector(1, 0, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(2, 2, 4), result.get(0), ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsStartsOnSurfaceGoesOut() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(0, 2, 4), new Vector(-1, 0, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsTangent() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(-1, 3, 4), new Vector(1, 0, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsParallelToAxisOutside() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(3, 2, 0), new Vector(0, 0, 1));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsParallelToAxisInside() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1.5, 2, 0), new Vector(0, 0, 1));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsParallelToAxisOnSurface() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(0, 2, 0), new Vector(0, 0, 1));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        // Ray lies on the tube surface (infinite intersections) => treated as no intersections
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsSkewRayTwoPoints() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(-1, 1, 4), new Vector(1, 1, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsSkewRayNoPoints() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(-1, 4, 4), new Vector(1, 1, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsOrthogonalToAxisTwoPoints() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1, 0, 4), new Vector(0, 1, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(1, 1, 4), result.get(0), ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(1, 3, 4), result.get(1), ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsOrthogonalToAxisTangent() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1, 1, 4), new Vector(1, 0, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsDiagonalThroughTubeTwoPoints() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(-2, -1, 4), new Vector(1, 1, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsDiagonalMissesTube() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(-2, -1, 4), new Vector(-1, -1, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsFromAxisOnePoint() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1, 2, 0), new Vector(1, 0, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(2, 2, 0), result.get(0), ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsFromAxisOriginOnePoint() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1, 2, 3), new Vector(1, 0, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(2, 2, 3), result.get(0), ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsFromAxisSkewOnePoint() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1, 2, 0), new Vector(1, 0, 1));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(2, 2, 1), result.get(0), ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsSkewWithAxisComponentTwoPoints() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(-1, 2, 0), new Vector(1, 0, 1));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(0, 2, 1), result.get(0), ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(2, 2, 3), result.get(1), ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsOrthogonalStartsOnSurfaceGoesIn() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1, 1, 4), new Vector(0, 1, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(1, 3, 4), result.get(0), ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsOrthogonalStartsOnSurfaceGoesOut() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1, 1, 4), new Vector(0, -1, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsOrthogonalOutsideTwoPointsReverseDirection() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1, 4, 4), new Vector(0, -1, 0));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(1, 3, 4), result.get(0), ERROR_TUBE_INTERSECTION);
        assertPointEquals(new Point(1, 1, 4), result.get(1), ERROR_TUBE_INTERSECTION);
    }

    @Test
    void testFindIntersectionsParallelOnAxisNoPoints() {
        // Arrange
        Tube tube = createTestTube();
        Ray ray = new Ray(new Point(1, 2, 0), new Vector(0, 0, 1));

        // Act
        var result = tube.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_TUBE_INTERSECTION);
    }
}
