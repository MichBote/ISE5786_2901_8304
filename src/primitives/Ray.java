package primitives;

import geometries.api.Intersectable.Intersection;

import java.util.List;
import java.util.Objects;

import static primitives.Util.isZero;

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
     * Small offset used to move secondary ray heads away from a surface.
     */
    private static final double DELTA = 0.1;

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
     * Constructs a ray whose origin is shifted along a normal to avoid
     * self-intersection with the surface that created the secondary ray.
     *
     * @param origin    original ray origin
     * @param direction ray direction
     * @param normal    surface normal used for the offset
     */
    public Ray(Point origin, Vector direction, Vector normal) {
        _direction = direction.normalize();
        double nv = normal.dotProduct(_direction);
        _origin = isZero(nv) ? origin : origin.add(normal.scale(nv < 0 ? -DELTA : DELTA));
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
        try {
            return _origin.add(_direction.scale(t));
        } catch (IllegalArgumentException ex) {
            return _origin;
        }
    }

    /**
     * Finds the closest point to the ray origin from a given list of points.
     * <p>
     * The method is intended for intersection results. If there are no
     * intersections, the caller is expected to pass {@code null}.
     * </p>
     *
     * @param points list of points (may be {@code null})
     * @return the closest point to the ray origin, or {@code null} if the list
     * is {@code null}
     */
    public Point findClosestPoint(List<Point> points) {
        if (points == null) return null;

        Intersection closest = findClosestIntersection(
                points.stream().map(point -> new Intersection(null, point)).toList()
        );
        return closest == null ? null : closest.point;
    }

    /**
     * Finds the closest geometry-aware intersection to the ray origin.
     *
     * @param intersections list of intersections (may be {@code null})
     * @return closest intersection, or {@code null} when the list is {@code null} or empty
     */
    public Intersection findClosestIntersection(List<Intersection> intersections) {
        if (intersections == null) return null;

        Intersection closestIntersection = null;
        double closestDistanceSquared = Double.POSITIVE_INFINITY;

        for (Intersection intersection : intersections) {
            double distanceSquared = intersection.point.distanceSquared(_origin);
            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closestIntersection = intersection;
            }
        }

        return closestIntersection;
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

