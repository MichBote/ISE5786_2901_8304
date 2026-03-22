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
 * @author Michal Berdugo &amp; Bina Cohen
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
        Point p0 = _axis.origin();
        Vector v = _axis.direction();

        double t = v.dotProduct(point.subtract(p0));
        Point o = p0.add(v.scale(t));
        return point.subtract(o).normalize();
    }

    @Override
    public String toString() {
        return "Tube(axis=" + _axis + ", radius=" + _radius + ")";
    }
}
