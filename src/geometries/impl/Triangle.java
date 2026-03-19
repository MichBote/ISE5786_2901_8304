package geometries.impl;

import primitives.Point;

/**
 * Represents a triangle in 3D space.
 * <p>
 * A triangle is a specific case of a convex polygon with three vertices.
 * </p>
 *
 * @author Michal Berdugo & Bina Cohen
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
}
