package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents an infinite tube (general cylinder) in 3D space.
 * <p>
 * The tube is defined by a central axis ray and a radius.
 * At this stage, the class stores the constructor parameters only.
 * </p>
 *
 * @author Michal Berdugo & Bina Cohen
 */
public class Tube extends RadialGeometry {
    /**
     * Central axis ray
     */
    protected final Ray _axis;

    /**
     * Constructs a tube.
     *
     * @param radius tube radius
     * @param axis   tube axis ray
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        _axis = axis;
    }

    @Override
    public Vector getNormal(Point point) {
        return null;
    }

    @Override
    public String toString() {
        return "Tube(axis=" + _axis + ", radius=" + _radius + ")";
    }
}
