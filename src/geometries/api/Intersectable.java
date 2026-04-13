package geometries.api;

import primitives.Point;
import primitives.Ray;

import java.util.List;

/**
 * Base abstract class for all intersectable geometric objects.
 * <p>
 * An intersectable object can compute intersection points with a given ray.
 * </p>
 * <p>
 * By project convention, if there are no intersection points, the method
 * returns {@code null} (and not an empty list).
 * </p>
 */
public abstract class Intersectable {
    /**
     * Finds intersection points between a ray and the geometry.
     *
     * @param ray the intersecting ray
     * @return a list of intersection points, or {@code null} if there are none
     */
    public abstract List<Point> findIntersections(Ray ray);
}
