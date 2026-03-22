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

    /**
     * Test method for {@link Cylinder#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        Ray axis = new Ray(new Point(0, 0, 0), new Vector(0, 0, 1));
        Cylinder cylinder = new Cylinder(1d, axis, 2d);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normal on the lateral surface
        assertEquals(new Vector(1, 0, 0), cylinder.getNormal(new Point(1, 0, 1)), ERROR_CYLINDER);

        // EP02: Normal on the bottom base (interior point)
        assertEquals(new Vector(0, 0, -1), cylinder.getNormal(new Point(0.5, 0, 0)), ERROR_CYLINDER);

        // EP03: Normal on the top base (interior point)
        assertEquals(new Vector(0, 0, 1), cylinder.getNormal(new Point(0, 0.5, 2)), ERROR_CYLINDER);

        // =============== Boundary Values Tests ==================
        // BV01: Normal on the bottom base at the center
        Vector nBottomCenter = cylinder.getNormal(new Point(0, 0, 0));
        assertEquals(1d, nBottomCenter.length(), DELTA, ERROR_CYLINDER);
        assertEquals(new Vector(0, 0, -1), nBottomCenter, ERROR_CYLINDER);

        // BV02: Normal on the top base at the center
        Vector nTopCenter = cylinder.getNormal(new Point(0, 0, 2));
        assertEquals(1d, nTopCenter.length(), DELTA, ERROR_CYLINDER);
        assertEquals(new Vector(0, 0, 1), nTopCenter, ERROR_CYLINDER);

        // BV03: Normal on the bottom rim (classified as base)
        assertEquals(new Vector(0, 0, -1), cylinder.getNormal(new Point(1, 0, 0)), ERROR_CYLINDER);

        // BV04: Normal on the top rim (classified as base)
        assertEquals(new Vector(0, 0, 1), cylinder.getNormal(new Point(0, 1, 2)), ERROR_CYLINDER);
    }
}
