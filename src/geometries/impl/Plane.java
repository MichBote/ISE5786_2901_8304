package geometries.impl;

import geometries.api.Geometry;
import geometries.api.Intersectable.Intersection;
import primitives.Point;
import primitives.Vector;
import primitives.Ray;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a plane in 3D space.
 * <p>
 * A plane is represented by a reference point and a constant normal vector.
 * The normal is stored normalized.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public final class Plane extends Geometry {
    /**
     * A reference point on the plane
     */
    private final Point _point;
    /**
     * The plane normal (normalized)
     */
    private final Vector _normal;

    /**
     * Constructs a plane from three points.
     *
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @throws IllegalArgumentException if the points are not suitable to define a plane
     */
    public Plane(Point p1, Point p2, Point p3) {
        _point = p1;
        Vector u = p2.subtract(p1);
        Vector v = p3.subtract(p1);
        _normal = u.crossProduct(v).normalize();
    }

    /**
     * Constructs a plane from a point and a normal vector.
     * The normal is stored normalized.
     *
     * @param point  a point on the plane
     * @param normal plane normal
     */
    public Plane(Point point, Vector normal) {
        _point = point;
        _normal = normal.normalize();
    }

    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }

    /**
     * Calculates intersections between the ray and this plane.
     *
     * @param ray ray to intersect with the plane
     * @return geometry-aware intersections, or {@code null} if there are none
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        Point p0 = ray.origin();

        // Ray starts at the plane reference point => intersection at t=0 (excluded)
        if (_point.equals(p0)) return null;

        Vector v = ray.direction();
        double nv = _normal.dotProduct(v);

        // no intersection – the ray is parallel to the plane
        if (isZero(nv)) return null;

        double nQMinusP0 = _normal.dotProduct(_point.subtract(p0));
        double t = alignZero(nQMinusP0 / nv);

        // intersection must be in the ray direction and must not include the origin
        return t <= 0 ? null : List.of(new Intersection(this, ray.getPoint(t)));
    }

    @Override
    public String toString() {
        return "Plane(point=" + _point + ", normal=" + _normal + ")";
    }
}

