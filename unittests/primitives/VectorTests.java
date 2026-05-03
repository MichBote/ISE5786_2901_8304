package primitives;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for class {@link Vector}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>{@link Vector#add(Vector)}</li>
 * <li>{@link Vector#subtract(Point)} (inherited behavior must be tested)</li>
 * <li>{@link Vector#scale(double)}</li>
 * <li>{@link Vector#dotProduct(Vector)}</li>
 * <li>{@link Vector#crossProduct(Vector)}</li>
 * <li>{@link Vector#lengthSquared()}</li>
 * <li>{@link Vector#length()}</li>
 * <li>{@link Vector#normalize()}</li>
 * </ul>
 */
class VectorTests {
    /** Basic default constructor to satisfy documentation tools */
    VectorTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Test vector (1,2,3) */
    private static final Vector V123 = new Vector(1, 2, 3);
    /** Test vector (-1,-2,-3) */
    private static final Vector V123_NEG = new Vector(-1, -2, -3);
    /** Test vector (-2,-4,-6) */
    private static final Vector V246_NEG = new Vector(-2, -4, -6);
    /** Test vector (0,3,-2) */
    private static final Vector V03_2 = new Vector(0, 3, -2);
    /** Test vector (1,2,2) */
    private static final Vector V122 = new Vector(1, 2, 2);

    /** Error message for wrong vector */
    private static final String ERROR_VECTOR = "ERROR: wrong Vector operation";

    /** Error message for expected exception */
    private static final String ERROR_EXPECTED_EXCEPTION = "ERROR: expected IllegalArgumentException";

    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============
        {
            Vector v1 = V123;
            Vector v2 = V246_NEG;
            Vector result = v1.add(v2);
            assertEquals(V123_NEG, result, ERROR_VECTOR);
        }

        // =============== Boundary Values Tests ==================
        {
            Vector v = V123;
            assertThrows(IllegalArgumentException.class, () -> v.add(V123_NEG), ERROR_EXPECTED_EXCEPTION);
        }
    }

    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============
        {
            Vector v1 = V123;
            Vector v2 = V246_NEG;
            Vector result = v1.subtract(v2);
            assertEquals(new Vector(3, 6, 9), result, ERROR_VECTOR);
        }

        // =============== Boundary Values Tests ==================
        {
            Vector v = V123;
            assertThrows(IllegalArgumentException.class, () -> v.subtract(v), ERROR_EXPECTED_EXCEPTION);
        }
    }

    /**
     * Test method for {@link Vector#scale(double)}.
     */
    @Test
    void testScale() {
        // ============ Equivalence Partitions Tests ==============
        {
            Vector v = V123;
            Vector result = v.scale(2);
            assertEquals(new Vector(2, 4, 6), result, ERROR_VECTOR);
        }

        // =============== Boundary Values Tests ==================
        {
            Vector v = V123;
            assertThrows(IllegalArgumentException.class, () -> v.scale(0), ERROR_EXPECTED_EXCEPTION);
        }
    }

    /**
     * Test method for {@link Vector#dotProduct(Vector)}.
     */
    @Test
    void testDotProduct() {
        // ============ Equivalence Partitions Tests ==============
        {
            Vector v1 = V123;
            Vector v2 = V03_2;
            assertEquals(0d, v1.dotProduct(v2), DELTA, ERROR_VECTOR);
        }

        {
            Vector v1 = V123;
            Vector v2 = V246_NEG;
            assertEquals(-28d, v1.dotProduct(v2), DELTA, ERROR_VECTOR);
        }

        // =============== Boundary Values Tests ==================
        {
            Vector v = V123;
            assertEquals(v.lengthSquared(), v.dotProduct(v), DELTA, ERROR_VECTOR);
        }
    }

    /**
     * Test method for {@link Vector#crossProduct(Vector)}.
     */
    @Test
    void testCrossProduct() {
        // ============ Equivalence Partitions Tests ==============
        {
            Vector v1 = V123;
            Vector v2 = V03_2;
            Vector result = v1.crossProduct(v2);

            assertEquals(0d, result.dotProduct(v1), DELTA, ERROR_VECTOR);
            assertEquals(0d, result.dotProduct(v2), DELTA, ERROR_VECTOR);
            assertEquals(v1.length() * v2.length(), result.length(), DELTA, ERROR_VECTOR);
        }

        {
            Vector v1 = V123;
            Vector v2 = V03_2;
            Vector a = v1.crossProduct(v2);
            Vector b = v2.crossProduct(v1);
            assertEquals(a, b.scale(-1), ERROR_VECTOR);
        }

        // =============== Boundary Values Tests ==================
        {
            Vector v1 = V123;
            assertThrows(IllegalArgumentException.class, () -> v1.crossProduct(V246_NEG), ERROR_EXPECTED_EXCEPTION);
        }
    }

    /**
     * Test method for {@link Vector#lengthSquared()}.
     */
    @Test
    void testLengthSquared() {
        // ============ Equivalence Partitions Tests ==============
        Vector v = V122;
        assertEquals(9d, v.lengthSquared(), DELTA, ERROR_VECTOR);
    }

    @Test
    void testLength() {
        // ============ Equivalence Partitions Tests ==============
        Vector v = V122;
        assertEquals(3d, v.length(), DELTA, ERROR_VECTOR);
    }

    @Test
    void testNormalize() {
        // ============ Equivalence Partitions Tests ==============
        Vector v = V123;
        Vector n = v.normalize();
        assertEquals(1d, n.length(), DELTA, ERROR_VECTOR);
        assertTrue(v.dotProduct(n) > 0, ERROR_VECTOR);
    }

    @Test
    void testConstructor() {
        // =============== Boundary Values Tests ==================
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0), ERROR_EXPECTED_EXCEPTION);
        assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO), ERROR_EXPECTED_EXCEPTION);
    }
}
