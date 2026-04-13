package geometries.api;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Base abstract class for all geometric shapes.
 * <p>
 * At this stage the class exposes only a normal computation API.
 * Intersection-related APIs are introduced via {@link Intersectable}.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public abstract class Geometry extends Intersectable {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    public Geometry() { /* Default constructor to satisfy JavaDoc generator */ }

    /**
     * Returns the normal vector to the geometry at the given point.
     *
     * @param point a point on the geometry
     * @return the geometry normal at {@code point}
     */
    public abstract Vector getNormal(Point point);

    /**
     * Finds intersection points between a ray and the geometry.
     * <p>
     * Concrete geometries should override this method.
     * </p>
     *
     * @param ray the intersecting ray
     * @return a list of intersection points, or {@code null} if there are none
     */
    @Override
    public List<Point> findIntersections(Ray ray) {
        return null;
    }
}


