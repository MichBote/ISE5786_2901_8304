package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

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

        double t = alignZero(v.dotProduct(point.subtract(p0)));
        Point o = isZero(t) ? p0 : p0.add(v.scale(t));
        return point.subtract(o).normalize();
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        return null;
    }

    @Override
    public String toString() {
        return "Tube(axis=" + _axis + ", radius=" + _radius + ")";
    }
}

