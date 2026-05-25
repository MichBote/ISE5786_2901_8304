package geometries.impl;

import geometries.api.Intersectable;
import primitives.Point;
import primitives.Ray;

import java.util.ArrayList;
import java.util.List;

/**
 * A composite of intersectable geometries.
 * <p>
 * The class aggregates multiple {@link Intersectable} objects and delegates
 * intersection computations to them.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public final class Geometries extends Intersectable {
    /** Internal list of geometries */
    private final List<Intersectable> geometries = new ArrayList<>();

    /**
     * Constructs a collection of geometries.
     *
     * @param geometries geometries to add
     */
    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    /**
     * Adds geometries to the collection.
     *
     * @param geometries geometries to add
     */
    public void add(Intersectable... geometries) {
        if (geometries == null || geometries.length == 0) return;
        this.geometries.addAll(List.of(geometries));
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        List<Point> intersections = null;
        for (var geometry : geometries) {
            var points = geometry.findIntersections(ray);
            if (points != null) {
                if (intersections == null) {
                    intersections = new ArrayList<>();
                }
                intersections.addAll(points);
            }
        }
        return intersections;
    }

    @Override
    public List<Intersection> calcIntersections(Ray ray) {
        List<Intersection> intersections = null;
        for (var geometry : geometries) {
            var points = geometry.calcIntersections(ray);
            if (points != null) {
                if (intersections == null) {
                    intersections = new ArrayList<>();
                }
                intersections.addAll(points);
            }
        }
        return intersections;
    }
}
