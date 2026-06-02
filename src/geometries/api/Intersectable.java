package geometries.api;

import primitives.Point;
import primitives.Ray;
import primitives.Material;
import primitives.Vector;

import lighting.LightSource;

import java.util.List;
import java.util.Objects;

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

        /** Surface normal at the intersection point (computed cache). */
        public Vector normal;
        /** Viewer direction vector (computed cache). */
        public Vector v;
        /** Dot product between v and normal (computed cache). */
        public double vNormal;
        /** Current light source (computed cache). */
        public LightSource light;
        /** Light direction vector from light source to the point (computed cache). */
        public Vector l;
        /** Dot product between l and normal (computed cache). */
        public double lNormal;

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

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Intersection other = (Intersection) obj;
            return geometry == other.geometry && Objects.equals(point, other.point);
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(geometry), point);
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
     * Calculates geometry-aware intersection points using the NVI pattern.
     * <p>
     * Concrete geometries implement this method; callers should always use
     * {@link #calcIntersections(Ray)}.
     * </p>
     *
     * @param ray the intersecting ray
     * @return a list of geometry-point pairs, or {@code null} if there are none
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray);
}
