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

    /**
     * Test method for {@link Vector#add(Vector)} and {@link Vector#subtract(Point)}.
     */
    @Test
    void testAddSubtract() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Vector + Vector
        assertEquals(V123_NEG, V123.add(V246_NEG), ERROR_VECTOR);

        // EP02: Vector - Vector
        assertEquals(new Vector(3, 6, 9), V123.subtract(V246_NEG), ERROR_VECTOR);

        // =============== Boundary Values Tests ==================
        // BV01: Add opposite vector should throw (zero vector forbidden)
        assertThrows(IllegalArgumentException.class, () -> V123.add(V123_NEG),
                "ERROR: adding opposite vectors must throw IllegalArgumentException");

        // BV02: Subtract itself should throw (zero vector forbidden)
        assertThrows(IllegalArgumentException.class, () -> V123.subtract(V123),
                "ERROR: subtracting identical vectors must throw IllegalArgumentException");
    }

    /**
     * Test method for {@link Vector#scale(double)}.
     */
    @Test
    void testScale() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Scale by a non-zero scalar
        assertEquals(new Vector(2, 4, 6), V123.scale(2), ERROR_VECTOR);

        // =============== Boundary Values Tests ==================
        // BV01: Scale by zero should throw (zero vector forbidden)
        assertThrows(IllegalArgumentException.class, () -> V123.scale(0),
                "ERROR: scale(0) must throw IllegalArgumentException");
    }

    /**
     * Test method for {@link Vector#dotProduct(Vector)}.
     */
    @Test
    void testDotProduct() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Dot product of orthogonal vectors is zero
        assertEquals(0d, V123.dotProduct(V03_2), DELTA, ERROR_VECTOR);

        // EP02: Dot product value check
        assertEquals(-28d, V123.dotProduct(V246_NEG), DELTA, ERROR_VECTOR);

        // =============== Boundary Values Tests ==================
        // BV01: v · v == |v|^2
        assertEquals(V123.lengthSquared(), V123.dotProduct(V123), DELTA, ERROR_VECTOR);
    }

    /**
     * Test method for {@link Vector#crossProduct(Vector)}.
     */
    @Test
    void testCrossProduct() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Cross product yields a vector orthogonal to both operands
        Vector vr = V123.crossProduct(V03_2);
        assertEquals(0d, vr.dotProduct(V123), DELTA, ERROR_VECTOR);
        assertEquals(0d, vr.dotProduct(V03_2), DELTA, ERROR_VECTOR);
        assertEquals(V123.length() * V03_2.length(), vr.length(), DELTA, ERROR_VECTOR);

        // EP02: Anti-commutativity a x b = -(b x a)
        Vector a = V123.crossProduct(V03_2);
        Vector b = V03_2.crossProduct(V123);
        assertEquals(a, b.scale(-1), ERROR_VECTOR);

        // =============== Boundary Values Tests ==================
        // BV01: Cross product for parallel vectors should throw (zero vector forbidden)
        assertThrows(IllegalArgumentException.class, () -> V123.crossProduct(V246_NEG),
                "ERROR: crossProduct of parallel vectors must throw IllegalArgumentException");
    }

    /**
     * Test method for {@link Vector#lengthSquared()}, {@link Vector#length()} and {@link Vector#normalize()}.
     */
    @Test
    void testLengthAndNormalize() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: length and lengthSquared
        assertEquals(9d, V122.lengthSquared(), DELTA, ERROR_VECTOR);
        assertEquals(3d, V122.length(), DELTA, ERROR_VECTOR);

        // EP02: Normalize a vector
        Vector n = V123.normalize();
        assertEquals(1d, n.length(), DELTA, ERROR_VECTOR);
        assertTrue(V123.dotProduct(n) > 0, ERROR_VECTOR);
        assertThrows(IllegalArgumentException.class, () -> V123.crossProduct(n), ERROR_VECTOR);

        // =============== Boundary Values Tests ==================
        // BV01: Zero vector creation must throw
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0),
                "ERROR: zero vector must throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO),
                "ERROR: zero vector must throw IllegalArgumentException");
    }
}
