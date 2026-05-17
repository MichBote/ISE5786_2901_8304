package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;
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
        // Möller–Trumbore algorithm (direct ray-triangle intersection)
        Point v0 = _vertices.get(0);
        Point v1 = _vertices.get(1);
        Point v2 = _vertices.get(2);

        // If ray starts at a vertex, the only intersection is at t=0 (excluded)
        Point p0 = ray.origin();
        if (p0.equals(v0) || p0.equals(v1) || p0.equals(v2)) return null;

        Vector dir = ray.direction();

        Vector edge1 = v1.subtract(v0);
        Vector edge2 = v2.subtract(v0);

        Vector pVec = dir.crossProduct(edge2);
        double det = alignZero(edge1.dotProduct(pVec));
        if (isZero(det)) return null; // ray is parallel to triangle plane

        double invDet = 1d / det;

        Vector tvec;
        try {
            tvec = p0.subtract(v0);
        } catch (IllegalArgumentException ex) {
            return null;
        }

        double u = alignZero(tvec.dotProduct(pVec) * invDet);
        if (u <= 0 || u >= 1) return null; // exclude edges/vertices

        Vector qVec = tvec.crossProduct(edge1);
        double v = alignZero(dir.dotProduct(qVec) * invDet);
        if (v <= 0 || u + v >= 1) return null; // exclude edges/vertices

        double t = alignZero(edge2.dotProduct(qVec) * invDet);
        return t <= 0 ? null : List.of(ray.getPoint(t));
    }
}

