package geometries.impl;

import primitives.Point;
import primitives.Vector;

/**
 * Represents a sphere in 3D space.
 * <p>
 * At this stage, the class stores the center and radius.
 * The normal computation will be implemented in a later stage.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public final class Sphere extends RadialGeometry {
    /**
     * Sphere center
     */
    private final Point _center;

    /**
     * Constructs a sphere.
     *
     * @param center sphere center
     * @param radius sphere radius
     */
    public Sphere(Point center, double radius) {
        super(radius);
        _center = center;
    }

    @Override
    public Vector getNormal(Point point) {
        return null;
    }

    @Override
    public String toString() {
        return "Sphere(center=" + _center + ", radius=" + _radius + ")";
    }
}
