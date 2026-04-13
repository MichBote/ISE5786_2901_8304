package geometries.impl;

import geometries.api.Intersectable;
import primitives.Point;
import primitives.Ray;

import java.util.ArrayList;
import java.util.List;

public final class Geometries extends Intersectable {
    private final List<Intersectable> geometries = new ArrayList<>();

    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    public void add(Intersectable... geometries) {
        if (geometries == null || geometries.length == 0) return;
        this.geometries.addAll(List.of(geometries));
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        return null;
    }
}
