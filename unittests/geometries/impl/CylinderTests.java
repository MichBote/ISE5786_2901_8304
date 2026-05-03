package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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

    /**
     * Single test method for {@link Cylinder#getNormal(Point)}.
     * All test cases are grouped here by requirement.
     */
    @Test
    void testGetNormal() {
        assertAll(
                "getNormal",
                // ============ Equivalence Partitions Tests ==============
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Vector normal = cylinder.getNormal(new Point(2, 2, 4));
                    assertEquals(new Vector(1, 0, 0), normal, ERROR_CYLINDER);
                },
                // =============== Boundary Values Tests ==================
                () -> {
                    Cylinder cylinder = createTestCylinder();

                    Vector nInterior = cylinder.getNormal(new Point(1.5, 2, 3));
                    Vector nCenter = cylinder.getNormal(new Point(1, 2, 3));
                    Vector nRim = cylinder.getNormal(new Point(2, 2, 3));

                    assertEquals(new Vector(0, 0, -1), nInterior, ERROR_CYLINDER);
                    assertEquals(1d, nCenter.length(), DELTA, ERROR_CYLINDER);
                    assertEquals(new Vector(0, 0, -1), nCenter, ERROR_CYLINDER);
                    assertEquals(new Vector(0, 0, -1), nRim, ERROR_CYLINDER);
                },
                () -> {
                    Cylinder cylinder = createTestCylinder();

                    Vector nInterior = cylinder.getNormal(new Point(1, 2.5, 5));
                    Vector nCenter = cylinder.getNormal(new Point(1, 2, 5));
                    Vector nRim = cylinder.getNormal(new Point(1, 3, 5));

                    assertEquals(new Vector(0, 0, 1), nInterior, ERROR_CYLINDER);
                    assertEquals(1d, nCenter.length(), DELTA, ERROR_CYLINDER);
                    assertEquals(new Vector(0, 0, 1), nCenter, ERROR_CYLINDER);
                    assertEquals(new Vector(0, 0, 1), nRim, ERROR_CYLINDER);
                }
        );
    }

    /**
     * Single test method for {@link Cylinder#findIntersections(Ray)}.
     * All test cases are grouped here by requirement.
     */
    @Test
    void testFindIntersections() {
        assertAll(
                "findIntersections",
                // Side (two points), reverse direction
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(3, 2, 4), new Vector(-1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(0, 2, 4), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // From bottom cap inside -> side (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1.5, 2, 3), new Vector(1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // From bottom cap inside -> up (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1.5, 2, 3), new Vector(0, 0, 1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1.5, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // From top cap inside -> down (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1.5, 2, 5), new Vector(0, 0, -1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1.5, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // From side surface -> out (no points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(2, 2, 4), new Vector(1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
                },
                // From side surface -> in (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(2, 2, 4), new Vector(-1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(0, 2, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // Bottom cap rim tangent (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(2, 1, 2), new Vector(0, 1, 1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // Top cap rim tangent (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(2, 1, 6), new Vector(0, 1, -1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // Diagonal side (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(-1, 1, 4), new Vector(1, 1, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(0, 2, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 3, 4), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Diagonal side, reverse (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(2, 4, 4), new Vector(-1, -1, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 3, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(0, 2, 4), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Axis from top center (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1, 2, 5), new Vector(0, 0, -1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // Inside -> down to bottom cap (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1.5, 2, 4), new Vector(0, 0, -1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1.5, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // On top plane, two rim points
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1, 0, 5), new Vector(0, 1, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 1, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 3, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // On top plane, starts on rim, goes in (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1, 1, 5), new Vector(0, 1, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 3, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // On top plane, starts on rim, goes out (no points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1, 1, 5), new Vector(0, -1, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
                },
                // Side (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(-1, 2, 4), new Vector(1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(0, 2, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 4), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Side outside height (no points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(-1, 2, 6), new Vector(1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
                },
                // Caps through center (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1, 2, 0), new Vector(0, 0, 1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Caps offset inside (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1.5, 2, 0), new Vector(0, 0, 1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1.5, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1.5, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Caps outside radius (no points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(3, 2, 0), new Vector(0, 0, 1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
                },
                // Starts inside -> side (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1.5, 2, 4), new Vector(1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 4), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // Bottom rim is included (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(-1, 2, 3), new Vector(1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(0, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 3), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Top rim is included (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(-1, 2, 5), new Vector(1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(0, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Axis from inside (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1, 2, 4), new Vector(0, 0, 1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // Axis from bottom center (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1, 2, 3), new Vector(0, 0, 1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // Rims: dedup side and caps (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(-1, 2, 2), new Vector(1, 0, 1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(0, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Side and top cap (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(-1, 2, 2.5), new Vector(1, 0, 1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(0, 2, 3.5), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1.5, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // On bottom plane: two rim points
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1, 0, 3), new Vector(0, 1, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 1, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1, 3, 3), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Caps reverse direction (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1.5, 2, 6), new Vector(0, 0, -1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1.5, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(1.5, 2, 3), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Caps rim (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(2, 2, 0), new Vector(0, 0, 1));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 5), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Starts on rim, goes out (no points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(2, 2, 3), new Vector(1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
                },
                // Starts on rim, goes in (one point)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(2, 2, 3), new Vector(-1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 1, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(0, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                },
                // Tangent to side (no points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(-1, 3, 4), new Vector(1, 0, 0));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 0, ERROR_CYLINDER_INTERSECTION);
                },
                // Bottom cap then side (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1.5, 2, 2), new Vector(1, 0, 3));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(11d / 6d, 2, 3), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 3.5), result.get(1), ERROR_CYLINDER_INTERSECTION);
                },
                // Top cap then side (two points)
                () -> {
                    Cylinder cylinder = createTestCylinder();
                    Ray ray = new Ray(new Point(1.5, 2, 6), new Vector(1, 0, -3));
                    var result = cylinder.findIntersections(ray);
                    assertIntersectionsCount(result, 2, ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(11d / 6d, 2, 5), result.get(0), ERROR_CYLINDER_INTERSECTION);
                    assertPointEquals(new Point(2, 2, 4.5), result.get(1), ERROR_CYLINDER_INTERSECTION);
                }
        );
    }
}