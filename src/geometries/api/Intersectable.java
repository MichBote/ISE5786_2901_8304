package geometries.api;

import primitives.Point;
import primitives.Ray;
import primitives.Material;
import primitives.Vector;
import lighting.LightSource;

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
    public static final class Intersection {
        /** Intersected geometry. */
        public final Geometry geometry;
        /** Intersection point. */
        public final Point point;
        /** Material of the intersected geometry. */
        public final Material material;
        /** Surface normal at the intersection point. */
        public Vector normal;
        /** Viewer direction vector. */
        public Vector v;
        /** Dot product between viewer direction and normal. */
        public double nv;
        /** Current light direction vector. */
        public Vector l;
        /** Current light source. */
        public LightSource light;
        /** Dot product between light direction and normal. */
        public double nl;

        /**
         * Constructs a geometry-point pair.
         *
         * @param geometry intersected geometry
         * @param point intersection point
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
            this.material = geometry == null ? new Material() : geometry.getMaterial();
        }

        @Override
        public String toString() {
            return "Intersection(geometry=" + geometry + ", point=" + point + ")";
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
    public final List<Point> findIntersections(Ray ray) {
        var intersections = calcIntersections(ray);
        return intersections == null
            ? null
            : intersections.stream().map(intersection -> intersection.point).toList();
    }

    /**
     * Finds geometry-aware intersection points between a ray and this object.
     *
     * @param ray the intersecting ray
     * @return a list of geometry-point pairs, or {@code null} if there are none
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersectionsHelper(ray);
    }

    /**
     * Calculates geometry-aware intersection points.
     *
     * @param ray the intersecting ray
     * @return a list of geometry-point pairs, or {@code null} if there are none
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray);
}
