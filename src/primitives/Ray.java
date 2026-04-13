package primitives;

import static primitives.Util.isZero;

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

    /**
     * Computes a point on the ray's line at distance {@code t} from the origin:
     * {@code P = P0 + t * v}.
     * <p>
     * Note: the method accepts any {@code t} (positive/negative/zero). For
     * intersection computations we typically use only {@code t > 0}.
     * </p>
     * <p>
     * If scaling the direction by {@code t} creates a near-zero vector (which is
     * forbidden by {@link Vector}), the method returns the ray origin.
     * </p>
     *
     * @param t distance parameter
     * @return point on the ray's line
     */
    public Point getPoint(double t) {
        if (isZero(t)) return _origin;
        try {
            return _origin.add(_direction.scale(t));
        } catch (IllegalArgumentException ex) {
            return _origin;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ray other = (Ray) obj;
        return _origin.equals(other._origin) && _direction.equals(other._direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }

    @Override
    public String toString() {
        return "Ray:" + _origin + _direction;
    }
}

