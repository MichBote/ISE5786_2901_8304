package primitives;

/**
 * Represents a point in a 3D Cartesian coordinate system.
 * <p>
 * The point is immutable.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public class Point {
    /**
     * Point coordinates
     */
    protected final Double3 _xyz;

    /**
     * The origin (0,0,0)
     */
    public static final Point ZERO = new Point(Double3.ZERO);

    /**
     * Constructs a point from its coordinate values.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public Point(double x, double y, double z) {
        this(new Double3(x, y, z));
    }

    /**
     * Constructs a point from a {@link Double3} container.
     *
     * @param xyz point coordinates
     */
    public Point(Double3 xyz) {
        _xyz = xyz;
    }

    /**
     * Subtracts another point from this point.
     * <p>
     * The result is the vector that starts at {@code other} and ends at {@code this}.
     * </p>
     *
     * @param other another point
     * @return the vector from {@code other} to {@code this}
     */
    public Vector subtract(Point other) {
        return new Vector(_xyz.subtract(other._xyz));
    }

    /**
     * Adds a vector to this point.
     *
     * @param vector translation vector
     * @return a new translated point
     */
    public Point add(Vector vector) {
        return new Point(_xyz.add(vector._xyz));
    }

    /**
     * Computes the squared distance between this point and another point.
     *
     * @param other another point
     * @return squared distance
     */
    public double distanceSquared(Point other) {
        double dx = _xyz._d1() - other._xyz._d1();
        double dy = _xyz._d2() - other._xyz._d2();
        double dz = _xyz._d3() - other._xyz._d3();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Computes the distance between this point and another point.
     * <p>
     * The implementation uses {@link #distanceSquared(Point)}.
     * </p>
     *
     * @param other another point
     * @return distance
     */
    public double distance(Point other) {
        return Math.sqrt(distanceSquared(other));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return _xyz.equals(((Point) obj)._xyz);
    }

    @Override
    public int hashCode() {
        return _xyz.hashCode();
    }

    @Override
    public String toString() {
        return "" + _xyz;
    }
}
