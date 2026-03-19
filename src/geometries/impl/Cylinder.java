package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents a finite cylinder in 3D space.
 * <p>
 * A cylinder is defined by an axis ray, a radius and a height.
 * At this stage, the class stores the constructor parameters only.
 * </p>
 *
 * @author Michal Berdugo & Bina Cohen
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
        return null;
    }

    @Override
    public String toString() {
        return "Cylinder(axis=" + _axis + ", radius=" + _radius + ", height=" + _height + ")";
    }
}
