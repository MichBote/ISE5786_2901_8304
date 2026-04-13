package geometries.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Vector;

/**
 * Unit tests for class {@link Polygon}.
 * <p>
 * The tests verify:
 * </p>
 * <ul>
 * <li>Constructor {@link Polygon#Polygon(Point...)}</li>
 * <li>{@link Polygon#getNormal(Point)}</li>
 * </ul>
 */
class PolygonTests {
    /** Basic default constructor to satisfy documentation tools */
    PolygonTests() { /* Basic default constructor to satisfy documentation tools */ }

    /** Delta value for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Test vertex (0,0,1) */
    private static final Point POINT_Z = new Point(0, 0, 1);
    /** Test vertex (1,0,0) */
    private static final Point POINT_X = new Point(1, 0, 0);
    /** Test vertex (0,1,0) */
    private static final Point POINT_Y = new Point(0, 1, 0);
    /** Additional vertex for valid polygon */
    private static final Point POINT1 = new Point(-1, 1, 1);

    /** Additional vertex for non-coplanar polygon */
    private static final Point POINT_NON_COPLANAR = new Point(0, 2, 2);

    /** Error message for wrong exception */
    private static final String ERROR_EXCEPTION = "ERROR: Exception thrown";
    /** Error message for wrong polygon */
    private static final String ERROR_POLYGON = "ERROR: wrong polygon";
    /** Error message for wrong normal */
    private static final String ERROR_NORMAL = "ERROR: wrong normal";

    /**
     * Test method for {@link Polygon#Polygon(Point...)}.
     */
    @Test
        void testConstructorValidConvexQuadrilateralDoesNotThrow() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct convex quadrilateral with vertices in correct order
        assertDoesNotThrow(() -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT1), ERROR_EXCEPTION);
        }

        @Test
        void testConstructorWrongOrderThrows() {
        // ============ Equivalence Partitions Tests ==============
        // EP02: Wrong vertices order
        assertThrows(IllegalArgumentException.class,
            () -> new Polygon(POINT_Z, POINT_Y, POINT_X, POINT1), ERROR_POLYGON);
        }

        @Test
        void testConstructorNotCoplanarThrows() {
        // ============ Equivalence Partitions Tests ==============
        // EP03: Not in the same plane
        assertThrows(IllegalArgumentException.class,
            () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_NON_COPLANAR), ERROR_POLYGON);
        }

        @Test
        void testConstructorLessThanThreeVerticesThrows() {
        // =============== Boundary Values Tests ==================
        // BV01: Less than 3 vertices
        assertThrows(IllegalArgumentException.class,
            () -> new Polygon(POINT_Z, POINT_X), ERROR_POLYGON);
        }

        @Test
        void testConstructorConsecutiveSamePointThrows() {
        // =============== Boundary Values Tests ==================
        // BV02: Consecutive vertices are the same point
        assertThrows(IllegalArgumentException.class,
            () -> new Polygon(POINT_Z, POINT_X, POINT_X, POINT1), ERROR_POLYGON);
    }

    /**
     * Test method for {@link Polygon#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        // Arrange
        Point[] points = {POINT_Z, POINT_X, POINT_Y, POINT1};
        Polygon polygon = new Polygon(points);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normal exists and is unit-length and orthogonal to edges
        // Act
        Vector result = assertDoesNotThrow(() -> polygon.getNormal(POINT_Z), ERROR_EXCEPTION);

        // Assert
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL);

        Point previous = points[points.length - 1];
        for (Point point : points) {
            Vector edge = point.subtract(previous);
            previous = point;
            assertEquals(0d, result.dotProduct(edge), DELTA, ERROR_NORMAL);
        }
    }
}
