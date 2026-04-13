package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.compareSign;
import static primitives.Util.isZero;

/**
 * Represents a triangle in 3D space.
 * <p>
 * A triangle is a specific case of a convex polygon with three vertices.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public final class Triangle extends Polygon {

    /**
     * Constructs a triangle from three vertices.
     *
     * @param p1 first vertex
     * @param p2 second vertex
     * @param p3 third vertex
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        List<Point> planeIntersections = _plane.findIntersections(ray);
        if (planeIntersections == null) return null;

        Point p0 = ray.origin();
        Vector v = ray.direction();

        Vector v1;
        Vector v2;
        Vector v3;
        try {
            v1 = _vertices.get(0).subtract(p0);
            v2 = _vertices.get(1).subtract(p0);
            v3 = _vertices.get(2).subtract(p0);
        } catch (IllegalArgumentException ex) {
            // Ray starts at a vertex => intersection at origin is excluded
            return null;
        }

        double s1 = alignZero(v.dotProduct(v1.crossProduct(v2)));
        if (isZero(s1)) return null;

        double s2 = alignZero(v.dotProduct(v2.crossProduct(v3)));
        if (isZero(s2)) return null;

        double s3 = alignZero(v.dotProduct(v3.crossProduct(v1)));
        if (isZero(s3)) return null;

        return compareSign(s1, s2) && compareSign(s1, s3) ? planeIntersections : null;
    }
}

