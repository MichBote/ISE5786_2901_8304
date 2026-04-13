package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

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

    private static Cylinder createTestCylinder() {
        Ray axis = new Ray(new Point(0, 0, 0), new Vector(0, 0, 1));
        return new Cylinder(1d, axis, 2d);
    }

    @Test
    void testGetNormalLateralSurface() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Cylinder cylinder = createTestCylinder();

        // Act
        Vector normal = cylinder.getNormal(new Point(1, 0, 1));

        // Assert
        assertEquals(new Vector(1, 0, 0), normal, ERROR_CYLINDER);
    }

    @Test
    void testGetNormalBottomBase() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Cylinder cylinder = createTestCylinder();

        // Act
        Vector nInterior = cylinder.getNormal(new Point(0.5, 0, 0));
        Vector nCenter = cylinder.getNormal(new Point(0, 0, 0));
        Vector nRim = cylinder.getNormal(new Point(1, 0, 0));

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
        Vector nInterior = cylinder.getNormal(new Point(0, 0.5, 2));
        Vector nCenter = cylinder.getNormal(new Point(0, 0, 2));
        Vector nRim = cylinder.getNormal(new Point(0, 1, 2));

        // Assert
        assertEquals(new Vector(0, 0, 1), nInterior, ERROR_CYLINDER);
        assertEquals(1d, nCenter.length(), DELTA, ERROR_CYLINDER);
        assertEquals(new Vector(0, 0, 1), nCenter, ERROR_CYLINDER);
        assertEquals(new Vector(0, 0, 1), nRim, ERROR_CYLINDER);
    }
}
