package primitives;

import static primitives.Util.alignZero;

/**
 * Represents a 3D vector in a Cartesian coordinate system.
 * <p>
 * A vector has direction and magnitude. It is implemented as an extension of
 * {@link Point} and reuses the protected {@link Point#_xyz} coordinates.
 * The vector is immutable.
 * </p>
 * <p>
 * Zero vectors are not allowed.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public final class Vector extends Point {
    /**
     * Unit vector on X axis
     */
    public static final Vector AXIS_X = new Vector(1, 0, 0);
    /**
     * Unit vector on Y axis
     */
    public static final Vector AXIS_Y = new Vector(0, 1, 0);
    /**
     * Unit vector on Z axis
     */
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /**
     * Constructs a vector from its coordinate values.
     *
     * @param x x component
     * @param y y component
     * @param z z component
     * @throws IllegalArgumentException if the vector is a zero vector
     */
    public Vector(double x, double y, double z) {
        this(new Double3(x, y, z));
    }

    /**
     * Constructs a vector from a {@link Double3} container.
     *
     * @param xyz the vector components
     * @throws IllegalArgumentException if the vector is a zero vector
     */
    public Vector(Double3 xyz) {
        if (Double3.ZERO.equals(xyz))
            throw new IllegalArgumentException("Zero vector is not allowed");
        super(xyz);
    }

    /**
     * Adds another vector to this vector.
     *
     * @param other another vector
     * @return the resulting vector
     */
    public Vector add(Vector other) {
        return new Vector(_xyz.add(other._xyz));
    }

    /**
     * Scales this vector by a scalar.
     *
     * @param scalar scaling factor
     * @return the scaled vector
     */
    public Vector scale(double scalar) {
        return new Vector(_xyz.scale(scalar));
    }

    /**
     * Computes the dot (scalar) product of this vector with another vector.
     *
     * @param other another vector
     * @return dot product
     */
    public double dotProduct(Vector other) {
        return _xyz._d1() * other._xyz._d1() + _xyz._d2() * other._xyz._d2() + _xyz._d3() * other._xyz._d3();
    }

    /**
     * Computes the cross (vector) product of this vector with another vector.
     *
     * @param other another vector
     * @return cross product vector
     */
    public Vector crossProduct(Vector other) {
        double x = _xyz._d2() * other._xyz._d3() - _xyz._d3() * other._xyz._d2();
        double y = _xyz._d3() * other._xyz._d1() - _xyz._d1() * other._xyz._d3();
        double z = _xyz._d1() * other._xyz._d2() - _xyz._d2() * other._xyz._d1();
        return new Vector(x, y, z);
    }

    /**
     * Returns the squared length of the vector.
     *
     * @return squared length
     */
    public double lengthSquared() {
        double x = _xyz._d1();
        double y = _xyz._d2();
        double z = _xyz._d3();
        return x * x + y * y + z * z;
    }

    /**
     * Returns the length (magnitude) of the vector.
     *
     * @return length
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Normalizes this vector.
     *
     * @return a unit vector in the same direction
     */
    public Vector normalize() {
        return scale(1d / length());
    }

    /**
     * Rotates this vector around the given axis by the given angle (in radians),
     * using Rodrigues' rotation formula.
     *
     * @param axis         rotation axis (does not have to be normalized)
     * @param angleRadians rotation angle in radians (right-hand rule)
     * @return rotated vector
     */
    public Vector rotateAround(Vector axis, double angleRadians) {
        // Implement using component arithmetic to avoid creating forbidden zero vectors
        // for intermediate terms (e.g., v*cos when cos=0, or k x v when parallel).

        Vector k = axis.normalize();
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);

        double vx = _xyz._d1();
        double vy = _xyz._d2();
        double vz = _xyz._d3();

        double kx = k._xyz._d1();
        double ky = k._xyz._d2();
        double kz = k._xyz._d3();

        double kDotV = kx * vx + ky * vy + kz * vz;

        // cross = k x v
        double cx = ky * vz - kz * vy;
        double cy = kz * vx - kx * vz;
        double cz = kx * vy - ky * vx;

        double oneMinusCos = 1d - cos;
        double rx = vx * cos + cx * sin + kx * kDotV * oneMinusCos;
        double ry = vy * cos + cy * sin + ky * kDotV * oneMinusCos;
        double rz = vz * cos + cz * sin + kz * kDotV * oneMinusCos;

        return new Vector(alignZero(rx), alignZero(ry), alignZero(rz));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "->" + super.toString();
    }
}
