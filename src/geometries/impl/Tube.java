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
        Point p0 = ray.origin();
        Vector v = ray.direction();

        Point pa = _axis.origin();
        Vector va = _axis.direction();

        // Ray direction component orthogonal to the axis
        double vVa = v.dotProduct(va);
        if (isZero(1d - Math.abs(vVa))) {
            // Parallel to axis => 0 intersections or infinite (if on surface). Return null.
            return null;
        }

        Vector vPerp;
        try {
            vPerp = v.add(va.scale(-vVa));
        } catch (IllegalArgumentException ex) {
            return null;
        }

        double a = alignZero(vPerp.lengthSquared());
        if (isZero(a)) {
            // Ray is parallel to axis (or direction projects fully on axis)
            // Either 0 intersections, or infinite intersections (ray on the surface) -> return null
            return null;
        }

        // Vector from axis origin to ray origin: deltaP = P0 - Pa
        Vector deltaP = null;
        if (!p0.equals(pa)) {
            deltaP = p0.subtract(pa);
        }

        // deltaP component orthogonal to the axis
        double b;
        double c;

        if (deltaP == null) {
            // Ray origin is exactly on axis origin => deltaPerp is zero
            b = 0;
            c = -_radiusSquared;
        } else {
            double dpVa = deltaP.dotProduct(va);
            Vector deltaPerp;
            try {
                deltaPerp = deltaP.add(va.scale(-dpVa));
            } catch (IllegalArgumentException ex) {
                // Ray origin is on the axis line => deltaPerp is zero
                deltaPerp = null;
            }

            if (deltaPerp == null) {
                b = 0;
                c = -_radiusSquared;
            } else {
                b = alignZero(2d * vPerp.dotProduct(deltaPerp));
                c = alignZero(deltaPerp.lengthSquared() - _radiusSquared);
            }
        }

        double discriminant = alignZero(b * b - 4d * a * c);

        // Tangency (discriminant == 0) is excluded by project rules
        if (discriminant <= 0) return null;

        double sqrtD = Math.sqrt(discriminant);
        double t1 = alignZero((-b - sqrtD) / (2d * a));
        double t2 = alignZero((-b + sqrtD) / (2d * a));

        boolean t1Positive = t1 > 0;
        boolean t2Positive = t2 > 0;

        if (t1Positive && t2Positive) {
            return t1 < t2
                ? List.of(ray.getPoint(t1), ray.getPoint(t2))
                : List.of(ray.getPoint(t2), ray.getPoint(t1));
        }
        if (t1Positive) return List.of(ray.getPoint(t1));
        if (t2Positive) return List.of(ray.getPoint(t2));
        return null;
    }

    @Override
    public String toString() {
        return "Tube(axis=" + _axis + ", radius=" + _radius + ")";
    }
}

