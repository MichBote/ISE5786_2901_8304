package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a convex polygon in a 3D Cartesian coordinate system.
 * <p>
 * The polygon is defined by an ordered sequence of vertices.
 * All vertices must lie in the same plane and be arranged along the
 * polygon edge path.
 * </p>
 * <p>
 * The polygon must be convex.
 * </p>
 *
 * @author Dan Zilberstein
 */
public class Polygon extends Geometry {
    /**
     * Ordered list of polygon vertices
     */
    protected final List<Point> _vertices;
    /**
     * Plane containing the polygon
     */
    protected final Plane _plane;
    /**
     * Number of vertices
     */
    private final int _size;

    /**
     * Constructs a convex polygon from ordered vertices.
     * <p>
     * The vertices must:
     * </p>
     * <ul>
     * <li>Contain at least three points</li>
     * <li>Be ordered along the polygon edge path</li>
     * <li>Lie in the same plane</li>
     * <li>Form a convex polygon</li>
     * </ul>
     *
     * @param vertices polygon vertices in edge order
     * @throws IllegalArgumentException if the vertices do not form a valid convex
     *                                  polygon
     */
    public Polygon(Point... vertices) {
        if (vertices.length < 3)
            throw new IllegalArgumentException("A polygon can't have less than 3 vertices");
        _vertices = List.of(vertices);
        _size = vertices.length;

        // Create the supporting plane using the first three vertices.
        // The plane stores the constant normal of the polygon.
        _plane = new Plane(vertices[0], vertices[1], vertices[2]);
        if (_size == 3) return; // no need for more tests for a Triangle

        Vector n = _plane.getNormal(vertices[0]);
        // Subtracting identical vertices would create a zero vector (illegal)
        Vector edge1 = vertices[_size - 1].subtract(vertices[_size - 2]);
        Vector edge2 = vertices[0].subtract(vertices[_size - 1]);

        // Cross product of consecutive edges determines orientation.
        // All edge pairs must produce the same sign relative to the normal,
        // otherwise the polygon is concave or vertices are unordered.
        boolean positive = edge1.crossProduct(edge2).dotProduct(n) > 0;
        for (var i = 1; i < _size; ++i) {
            // Test that the point is in the same plane as calculated originally
            if (!isZero(vertices[i].subtract(vertices[0]).dotProduct(n)))
                throw new IllegalArgumentException("All vertices of a polygon must lay in the same plane");
            // Test the consequent edges have
            edge1 = edge2;
            edge2 = vertices[i].subtract(vertices[i - 1]);
            if (positive != (edge1.crossProduct(edge2).dotProduct(n) > 0))
                throw new IllegalArgumentException("All vertices must be ordered and the polygon must be convex");
        }
    }

    @Override
    public Vector getNormal(Point point) {
        return _plane.getNormal(point);
    }

    /**
     * Calculates intersections between the ray and this polygon.
     *
     * @param ray         ray to intersect with the polygon
     * @param maxDistance maximal allowed distance from the ray origin
     * @return geometry-aware intersections, or {@code null} if there are none
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> planeIntersections = _plane.calcIntersections(ray, maxDistance);
        if (planeIntersections == null) return null;

        Point p = planeIntersections.getFirst().point;
        Vector n = _plane.getNormal(p);

        double sign = 0;
        for (int i = 0; i < _size; i++) {
            Point v1Point = _vertices.get(i);
            Point v2Point = _vertices.get((i + 1) % _size);

            Vector v1;
            Vector v2;
            try {
                v1 = v1Point.subtract(p);
                v2 = v2Point.subtract(p);
            } catch (IllegalArgumentException ex) {
                // Intersection is on a vertex => excluded
                return null;
            }

            double s;
            try {
                s = alignZero(v1.crossProduct(v2).dotProduct(n));
            } catch (IllegalArgumentException ex) {
                // v1 and v2 are collinear => crossProduct is zero => on edge/continuation => excluded
                return null;
            }
            if (isZero(s)) return null; // on an edge or on its continuation => excluded

            if (i == 0) {
                sign = s;
            } else {
                if (sign > 0 != s > 0) return null;
            }
        }

        return List.of(new Intersection(this, p));
    }
}

