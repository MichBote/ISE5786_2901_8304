package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;

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

    /**
     * Calculates intersections between the ray and this sphere.
     *
     * @param ray         ray to intersect with the sphere
     * @param maxDistance maximal allowed distance from the ray origin
     * @return geometry-aware intersections, or {@code null} if there are none
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        Point p0 = ray.origin();
        Vector v = ray.direction();

        Vector u;
        try {
            u = _center.subtract(p0);
        } catch (IllegalArgumentException ex) {
            // Ray starts at the sphere center
        return alignZero(_radius - maxDistance) <= 0
                    ? List.of(new Intersection(this, ray.getPoint(_radius)))
                    : null;
        }

        double tm = v.dotProduct(u);
        double dSquared = u.lengthSquared() - tm * tm;
        double thSquared = alignZero(_radiusSquared - dSquared);
        if (thSquared <= 0) return null;

        double th = Math.sqrt(thSquared);
        double t2 = alignZero(tm + th);
        if (t2 <= 0) return null;
        double t1 = alignZero(tm - th);
        if (alignZero(t1 - maxDistance) > 0) return null;

        if (alignZero(t2 - maxDistance) > 0) {
            return t1 <= 0 ? null : List.of(new Intersection(this, ray.getPoint(t1)));
        } else {
            return t1 <= 0 ? List.of(new Intersection(this, ray.getPoint(t2)))
                    : List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)));
        }
    }

    @Override
    public String toString() {
        return "Sphere(center=" + _center + ", radius=" + _radius + ")";
    }
}

