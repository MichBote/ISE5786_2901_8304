package primitives;

import java.util.Objects;

/**
 * Represents a ray (half-line) in 3D space.
 * <p>
 * A ray is defined by an origin point and a direction vector.
 * The direction is stored normalized.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public final class Ray {
    /**
     * Ray origin point
     */
    private final Point _origin;
    /**
     * Ray direction (normalized)
     */
    private final Vector _direction;

    /**
     * Constructs a ray from an origin point and a direction vector.
     * The direction is normalized before being stored.
     *
     * @param origin    ray origin
     * @param direction ray direction
     */
    public Ray(Point origin, Vector direction) {
        _origin = origin;
        _direction = direction.normalize();
    }

    /**
     * Getter for the ray origin.
     *
     * @return the origin point
     */
    public Point origin() {
        return _origin;
    }

    /**
     * Getter for the ray direction.
     *
     * @return the normalized direction vector
     */
    public Vector direction() {
        return _direction;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
                || (obj instanceof Ray other)
                && _origin.equals(other._origin)
                && _direction.equals(other._direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }

    @Override
    public String toString() {
        return "Ray(origin=" + _origin + ", direction=" + _direction + ")";
    }
}
