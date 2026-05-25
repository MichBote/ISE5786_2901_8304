package geometries.api;

import primitives.Point;
import primitives.Ray;
import primitives.Material;

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
     * Geometry-point pair for intersections that must keep the intersected object.
     */
    public static class Intersection {
        /** Intersected geometry. */
        public final Geometry geometry;
        /** Intersection point. */
        public final Point point;
        /** Material of the intersected geometry. */
        public final Material material;

        /**
         * Constructs a geometry-point pair.
         *
         * @param geometry intersected geometry
         * @param point intersection point
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
            this.material = geometry.getMaterial();
        }
    }

    /**
     * Default constructor.
     * <p>
     * Exists to avoid relying on an undocumented implicit default constructor.
     * </p>
     */
    protected Intersectable() {
    }
    /**
     * Finds intersection points between a ray and the geometry.
     *
     * @param ray the intersecting ray
     * @return a list of intersection points, or {@code null} if there are none
     */
    public abstract List<Point> findIntersections(Ray ray);

    /**
     * Finds geometry-aware intersection points between a ray and this object.
     *
     * @param ray the intersecting ray
     * @return a list of geometry-point pairs, or {@code null} if there are none
     */
    public List<Intersection> calcIntersections(Ray ray) {
        return null;
    }
}
