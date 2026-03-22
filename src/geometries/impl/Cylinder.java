package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.isZero;

/**
 * Represents a finite cylinder in 3D space.
 * <p>
 * A cylinder is defined by an axis ray, a radius and a height.
 * At this stage, the class stores the constructor parameters only.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public final class Cylinder extends Tube {
    /**
     * Cylinder height
     */
    private final double _height;

    /**
     * Constructs a cylinder.
     *
     * @param radius cylinder radius
     * @param axis   cylinder axis ray
     * @param height cylinder height
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        _height = height;
    }

    @Override
    public Vector getNormal(Point point) {
        Point p0 = _axis.origin();
        Vector v = _axis.direction();

        double t = v.dotProduct(point.subtract(p0));
        if (isZero(t)) {
            return v.scale(-1);
        }
        if (isZero(t - _height)) {
            return v;
        }
        return super.getNormal(point);
    }

    @Override
    public String toString() {
        return "Cylinder(axis=" + _axis + ", radius=" + _radius + ", height=" + _height + ")";
    }
}
