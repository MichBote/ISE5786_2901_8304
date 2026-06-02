package geometries.impl;

import geometries.api.Intersectable.Intersection;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

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
        return point.subtract(_center).normalize();
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        Point p0 = ray.origin();
        Vector v = ray.direction();

        Vector u;
        try {
            u = _center.subtract(p0);
        } catch (IllegalArgumentException ex) {
            // Ray starts at the sphere center
            return List.of(new Intersection(this, ray.getPoint(_radius)));
        }

        double tm = alignZero(v.dotProduct(u));
        double dSquared = alignZero(u.lengthSquared() - tm * tm);

        // No intersections if the ray misses the sphere or is tangent (tangent excluded)
        if (alignZero(dSquared - _radiusSquared) >= 0) return null;

        double thSquared = alignZero(_radiusSquared - dSquared);
        if (thSquared <= 0) return null;
        double th = Math.sqrt(thSquared);

        double t1 = alignZero(tm - th);
        double t2 = alignZero(tm + th);

        boolean t1Positive = t1 > 0;
        boolean t2Positive = t2 > 0;

        if (t1Positive && t2Positive) {
            // order by distance from ray origin
            return t1 < t2
                ? List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)))
                : List.of(new Intersection(this, ray.getPoint(t2)), new Intersection(this, ray.getPoint(t1)));
        }

        if (t1Positive) return List.of(new Intersection(this, ray.getPoint(t1)));
        if (t2Positive) return List.of(new Intersection(this, ray.getPoint(t2)));

        return null;
    }

    @Override
    public String toString() {
        return "Sphere(center=" + _center + ", radius=" + _radius + ")";
    }
}

