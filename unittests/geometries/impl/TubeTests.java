package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

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

    /**
     * Test method for {@link Tube#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        Ray axis = new Ray(new Point(0, 0, 0), new Vector(0, 0, 1));
        Tube tube = new Tube(1d, axis);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on tube surface with projection on axis in front of origin
        Vector n1 = tube.getNormal(new Point(1, 0, 5));
        assertEquals(1d, n1.length(), DELTA, ERROR_TUBE);
        assertEquals(new Vector(1, 0, 0), n1, ERROR_TUBE);

        // EP02: Point on tube surface with projection on axis behind origin
        Vector n2 = tube.getNormal(new Point(1, 0, -2));
        assertEquals(1d, n2.length(), DELTA, ERROR_TUBE);
        assertEquals(new Vector(1, 0, 0), n2, ERROR_TUBE);

        // =============== Boundary Values Tests ==================
        // BV01: Point on tube surface with projection exactly at axis origin
        Vector n3 = tube.getNormal(new Point(0, 1, 0));
        assertEquals(1d, n3.length(), DELTA, ERROR_TUBE);
        assertEquals(new Vector(0, 1, 0), n3, ERROR_TUBE);
    }
}
