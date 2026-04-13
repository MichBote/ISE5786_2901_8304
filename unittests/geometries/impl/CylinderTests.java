package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Unit tests for class {@link Cylinder}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>{@link Cylinder#getNormal(Point)}</li>
 * </ul>
 */
class CylinderTests {
    /** Basic default constructor to satisfy documentation tools */
    CylinderTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Error message for wrong cylinder */
    private static final String ERROR_CYLINDER = "ERROR: wrong Cylinder normal";

    @Test
    void testFindIntersectionsSideTwoPointsReverseDirection() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(3, 2, 4), new Vector(-1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(0, 2, 4), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsFromBottomCapInsideToSideOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1.5, 2, 3), new Vector(1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsFromBottomCapInsideUpOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1.5, 2, 3), new Vector(0, 0, 1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1.5, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsFromTopCapInsideDownOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1.5, 2, 5), new Vector(0, 0, -1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1.5, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsFromSideSurfaceOutNoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(2, 2, 4), new Vector(1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsFromSideSurfaceInOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(2, 2, 4), new Vector(-1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(0, 2, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsBottomCapRimTangentOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(2, 1, 2), new Vector(0, 1, 1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsTopCapRimTangentOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(2, 1, 6), new Vector(0, 1, -1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsDiagonalSideTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(-1, 1, 4), new Vector(1, 1, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(0, 2, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 3, 4), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsDiagonalSideReverseTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(2, 4, 4), new Vector(-1, -1, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 3, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(0, 2, 4), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsAxisFromTopCenterOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1, 2, 5), new Vector(0, 0, -1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsInsideDownToBottomCapOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1.5, 2, 4), new Vector(0, 0, -1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1.5, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsOnTopPlaneTwoRimPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1, 0, 5), new Vector(0, 1, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 1, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 3, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsOnTopPlaneStartsOnRimGoesInOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1, 1, 5), new Vector(0, 1, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 3, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsOnTopPlaneStartsOnRimGoesOutNoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1, 1, 5), new Vector(0, -1, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
    }

    /** Error message for wrong cylinder intersection */
    private static final String ERROR_CYLINDER_INTERSECTION = "ERROR: wrong Cylinder intersection result";

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

    private static Cylinder createTestCylinder() {
        Ray axis = new Ray(new Point(1, 2, 3), new Vector(0, 0, 1));
        return new Cylinder(1d, axis, 2d);
    }

    @Test
    void testGetNormalLateralSurface() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Cylinder cylinder = createTestCylinder();

        // Act
        Vector normal = cylinder.getNormal(new Point(2, 2, 4));

        // Assert
        assertEquals(new Vector(1, 0, 0), normal, ERROR_CYLINDER);
    }

    @Test
    void testGetNormalBottomBase() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Cylinder cylinder = createTestCylinder();

        // Act
        Vector nInterior = cylinder.getNormal(new Point(1.5, 2, 3));
        Vector nCenter = cylinder.getNormal(new Point(1, 2, 3));
        Vector nRim = cylinder.getNormal(new Point(2, 2, 3));

        // Assert
        assertEquals(new Vector(0, 0, -1), nInterior, ERROR_CYLINDER);
        assertEquals(1d, nCenter.length(), DELTA, ERROR_CYLINDER);
        assertEquals(new Vector(0, 0, -1), nCenter, ERROR_CYLINDER);
        assertEquals(new Vector(0, 0, -1), nRim, ERROR_CYLINDER);
    }

    @Test
    void testGetNormalTopBase() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Cylinder cylinder = createTestCylinder();

        // Act
        Vector nInterior = cylinder.getNormal(new Point(1, 2.5, 5));
        Vector nCenter = cylinder.getNormal(new Point(1, 2, 5));
        Vector nRim = cylinder.getNormal(new Point(1, 3, 5));

        // Assert
        assertEquals(new Vector(0, 0, 1), nInterior, ERROR_CYLINDER);
        assertEquals(1d, nCenter.length(), DELTA, ERROR_CYLINDER);
        assertEquals(new Vector(0, 0, 1), nCenter, ERROR_CYLINDER);
        assertEquals(new Vector(0, 0, 1), nRim, ERROR_CYLINDER);
    }

    /**
     * Test method for {@link Cylinder#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersectionsSideTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(-1, 2, 4), new Vector(1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(0, 2, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 4), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsSideNoPointsOutsideHeight() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(-1, 2, 6), new Vector(1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsCapsThroughCenterTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1, 2, 0), new Vector(0, 0, 1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsCapsOffsetInsideTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1.5, 2, 0), new Vector(0, 0, 1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1.5, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1.5, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsCapsOutsideRadiusNoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(3, 2, 0), new Vector(0, 0, 1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsStartsInsideToSideOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1.5, 2, 4), new Vector(1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsBottomRimIncludedOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(-1, 2, 3), new Vector(1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        // Join point between base and shell is included
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(0, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 3), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsTopRimIncludedOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(-1, 2, 5), new Vector(1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(0, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsAxisFromInsideOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1, 2, 4), new Vector(0, 0, 1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsAxisFromBottomCenterOnePoint() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1, 2, 3), new Vector(0, 0, 1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        // Base point is at t=0 and excluded; only the top cap intersection remains
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsRimsDedupSideAndCapsTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(-1, 2, 2), new Vector(1, 0, 1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        // Each rim point may be found both by the side and the cap logic; result must be unique
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(0, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsSideAndTopCapTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(-1, 2, 2.5), new Vector(1, 0, 1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(0, 2, 3.5), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1.5, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsOnBottomPlaneTwoRimPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1, 0, 3), new Vector(0, 1, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 1, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1, 3, 3), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsCapsReverseDirectionTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1.5, 2, 6), new Vector(0, 0, -1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1.5, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(1.5, 2, 3), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsCapsRimTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(2, 2, 0), new Vector(0, 0, 1));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        // Rim points are included by the bonus requirement
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsStartsOnRimGoesOutNoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(2, 2, 3), new Vector(1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsStartsOnRimGoesInOnePoint() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(2, 2, 3), new Vector(-1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(0, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsTangentToSideNoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(-1, 3, 4), new Vector(1, 0, 0));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsBottomCapThenSideTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1.5, 2, 2), new Vector(1, 0, 3));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(11d / 6d, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 3.5), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }

    @Test
    void testFindIntersectionsTopCapThenSideTwoPoints() {
        // Arrange
        Cylinder cylinder = createTestCylinder();
        Ray ray = new Ray(new Point(1.5, 2, 6), new Vector(1, 0, -3));

        // Act
        var result = cylinder.findIntersections(ray);

        // Assert
        assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(11d / 6d, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
        assertPointEquals(new Point(2, 2, 4.5), result.get(1), ERROR_CYLINDER_INTERSECTION);
    }
}
