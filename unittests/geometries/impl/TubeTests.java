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

    private static Tube createTestTube() {
        Ray axis = new Ray(new Point(0, 0, 0), new Vector(0, 0, 1));
        return new Tube(1d, axis);
    }

    @Test
    void testGetNormalProjectionNotAtOrigin() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Tube tube = createTestTube();

        // Act
        Vector nFront = tube.getNormal(new Point(1, 0, 5));
        Vector nBack = tube.getNormal(new Point(1, 0, -2));

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
        Vector n = tube.getNormal(new Point(0, 1, 0));

        // Assert
        assertEquals(1d, n.length(), DELTA, ERROR_TUBE);
        assertEquals(new Vector(0, 1, 0), n, ERROR_TUBE);
    }
}
