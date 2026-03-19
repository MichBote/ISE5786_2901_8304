package primitives;

import java.util.Objects;

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
    protected final Double3 xyz_;

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
        xyz_ = xyz;
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
        return new Vector(xyz_.subtract(other.xyz_));
    }

    /**
     * Adds a vector to this point.
     *
     * @param vector translation vector
     * @return a new translated point
     */
    public Point add(Vector vector) {
        return new Point(xyz_.add(vector.xyz_));
    }

    /**
     * Computes the squared distance between this point and another point.
     *
     * @param other another point
     * @return squared distance
     */
    public double distanceSquared(Point other) {
        double dx = xyz_._d1() - other.xyz_._d1();
        double dy = xyz_._d2() - other.xyz_._d2();
        double dz = xyz_._d3() - other.xyz_._d3();
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
        return this == obj || (obj instanceof Point other) && xyz_.equals(other.xyz_);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xyz_);
    }

    @Override
    public String toString() {
        return "Point" + xyz_;
    }
}
