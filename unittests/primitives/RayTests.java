package primitives;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for class {@link Ray}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>{@link Ray#Ray(Point, Vector)} normalizes the direction</li>
 * </ul>
 */
class RayTests {
    /** Basic default constructor to satisfy documentation tools */
    RayTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Error message for wrong ray */
    private static final String ERROR_RAY = "ERROR: wrong Ray behavior";

    /**
     * Test method for {@link Ray#Ray(Point, Vector)}.
     */
    @Test
    void testConstructorNormalizesDirection() {
        // Arrange
        Point origin = new Point(1, 2, 3);
        Vector direction = new Vector(2, 4, 6);

        // Act
        Ray ray = new Ray(origin, direction);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Direction vector is normalized
        assertEquals(1d, ray.direction().length(), DELTA, ERROR_RAY);

        // =============== Boundary Values Tests ==================
        // BV01: Normalized direction keeps same orientation
        assertTrue(direction.dotProduct(ray.direction()) > 0, ERROR_RAY);
    }
}
