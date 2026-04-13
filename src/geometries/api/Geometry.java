package geometries.api;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Base abstract class for all geometric shapes.
 * <p>
 * At this stage the class exposes only a normal computation API.
 * Intersection-related APIs will be added in later stages.
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

    @Override
    public List<Point> findIntersections(Ray ray) {
        return null;
    }
}

