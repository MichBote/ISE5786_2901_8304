package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for class {@link Sphere}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>{@link Sphere#getNormal(Point)}</li>
 * </ul>
 */
class SphereTests {
    /** Basic default constructor to satisfy documentation tools */
    SphereTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Error message for wrong sphere */
    private static final String ERROR_SPHERE = "ERROR: wrong Sphere normal";

    /**
     * Test method for {@link Sphere#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        Sphere sphere = new Sphere(new Point(0, 0, 0), 1d);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normal at point (0,0,1) should be (0,0,1)
        Vector n = sphere.getNormal(new Point(0, 0, 1));
        assertEquals(1d, n.length(), DELTA, ERROR_SPHERE);
        assertEquals(new Vector(0, 0, 1), n, ERROR_SPHERE);
    }
}
