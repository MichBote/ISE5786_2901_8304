package geometries.impl;

import geometries.api.Intersectable;
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

    /**
     * Aggregates geometry-aware intersections from all contained objects.
     *
     * @param ray the intersecting ray
     * @return a list of geometry-point pairs, or {@code null} if there are none
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        List<Intersection> intersections = null;
        for (var geometry : geometries) {
            var geoIntersections = geometry.calcIntersections(ray);
            if (geoIntersections != null) {
                if (intersections == null) {
                    intersections = new ArrayList<>();
                }
                intersections.addAll(geoIntersections);
            }
        }
        return intersections;
    }
}
