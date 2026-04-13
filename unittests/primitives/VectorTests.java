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
    void testAddVectors() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Vector v1 = V123;
        Vector v2 = V246_NEG;

        // Act
        Vector result = v1.add(v2);

        // Assert
        assertEquals(V123_NEG, result, ERROR_VECTOR);
    }

    @Test
    void testAddOppositeVectorsThrows() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Vector v = V123;

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> v.add(V123_NEG), ERROR_EXPECTED_EXCEPTION);
    }

    @Test
    void testSubtractVectors() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Vector v1 = V123;
        Vector v2 = V246_NEG;

        // Act
        Vector result = v1.subtract(v2);

        // Assert
        assertEquals(new Vector(3, 6, 9), result, ERROR_VECTOR);
    }

    @Test
    void testSubtractSameVectorThrows() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Vector v = V123;

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> v.subtract(v), ERROR_EXPECTED_EXCEPTION);
    }

    /**
     * Test method for {@link Vector#scale(double)}.
     */
    @Test
    void testScale() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Vector v = V123;

        // Act
        Vector result = v.scale(2);

        // Assert
        assertEquals(new Vector(2, 4, 6), result, ERROR_VECTOR);
    }

    @Test
    void testScaleByZeroThrows() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Vector v = V123;

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> v.scale(0), ERROR_EXPECTED_EXCEPTION);
    }

    /**
     * Test method for {@link Vector#dotProduct(Vector)}.
     */
    @Test
    void testDotProductOrthogonalIsZero() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Vector v1 = V123;
        Vector v2 = V03_2;

        // Act
        double result = v1.dotProduct(v2);

        // Assert
        assertEquals(0d, result, DELTA, ERROR_VECTOR);
    }

    @Test
    void testDotProductValue() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Vector v1 = V123;
        Vector v2 = V246_NEG;

        // Act
        double result = v1.dotProduct(v2);

        // Assert
        assertEquals(-28d, result, DELTA, ERROR_VECTOR);
    }

    @Test
    void testDotProductWithSelfEqualsLengthSquared() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Vector v = V123;

        // Act
        double dot = v.dotProduct(v);
        double len2 = v.lengthSquared();

        // Assert
        assertEquals(len2, dot, DELTA, ERROR_VECTOR);
    }

    /**
     * Test method for {@link Vector#crossProduct(Vector)}.
     */
    @Test
    void testCrossProductOrthogonalityAndLength() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Vector v1 = V123;
        Vector v2 = V03_2;

        // Act
        Vector result = v1.crossProduct(v2);

        // Assert
        assertEquals(0d, result.dotProduct(v1), DELTA, ERROR_VECTOR);
        assertEquals(0d, result.dotProduct(v2), DELTA, ERROR_VECTOR);
        assertEquals(v1.length() * v2.length(), result.length(), DELTA, ERROR_VECTOR);
    }

    @Test
    void testCrossProductAntiCommutativity() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Vector v1 = V123;
        Vector v2 = V03_2;

        // Act
        Vector a = v1.crossProduct(v2);
        Vector b = v2.crossProduct(v1);

        // Assert
        assertEquals(a, b.scale(-1), ERROR_VECTOR);
    }

    @Test
    void testCrossProductParallelVectorsThrows() {
        // =============== Boundary Values Tests ==================
        // Arrange
        Vector v1 = V123;

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> v1.crossProduct(V246_NEG), ERROR_EXPECTED_EXCEPTION);
    }

    /**
     * Test method for {@link Vector#lengthSquared()}, {@link Vector#length()} and {@link Vector#normalize()}.
     */
    @Test
    void testLengthAndLengthSquared() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Vector v = V122;

        // Act
        double len2 = v.lengthSquared();
        double len = v.length();

        // Assert
        assertEquals(9d, len2, DELTA, ERROR_VECTOR);
        assertEquals(3d, len, DELTA, ERROR_VECTOR);
    }

    @Test
    void testNormalizeReturnsUnitVectorSameDirection() {
        // ============ Equivalence Partitions Tests ==============
        // Arrange
        Vector v = V123;

        // Act
        Vector n = v.normalize();

        // Assert
        assertEquals(1d, n.length(), DELTA, ERROR_VECTOR);
        assertTrue(v.dotProduct(n) > 0, ERROR_VECTOR);
    }

    @Test
    void testZeroVectorCreationThrows() {
        // =============== Boundary Values Tests ==================
        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0), ERROR_EXPECTED_EXCEPTION);
        assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO), ERROR_EXPECTED_EXCEPTION);
    }
}
