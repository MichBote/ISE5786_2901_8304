package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a finite cylinder in 3D space.
 * <p>
 * A cylinder is defined by an axis ray, a radius and a height.
 * At this stage, the class stores the constructor parameters only.
 * </p>
 *
 * @author Michal Berdugo &amp; Bina Cohen
 */
public final class Cylinder extends Tube {
    /**
     * Deduplication tolerance for intersection points.
     * <p>
     * Unit tests compare points using a distance tolerance around 1e-6, so we
     * treat points closer than that as identical when collecting intersections
     * from multiple surfaces (tube + caps).
     * </p>
     */
    private static final double DEDUP_DISTANCE_SQUARED = 1e-12;

    /**
     * Cylinder height
     */
    private final double _height;

    /**
     * Constructs a cylinder.
     *
     * @param radius cylinder radius
     * @param axis   cylinder axis ray
     * @param height cylinder height
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        _height = height;
    }

    @Override
    public Vector getNormal(Point point) {
        Point p0 = _axis.origin();
        Vector v = _axis.direction();

        if (isZero(point.distanceSquared(p0))) {
            return v.scale(-1);
        }

        Point pTop = p0.add(v.scale(_height));
        if (isZero(point.distanceSquared(pTop))) {
            return v;
        }

        double t = v.dotProduct(point.subtract(p0));
        if (isZero(t)) {
            return v.scale(-1);
        }
        if (isZero(t - _height)) {
            return v;
        }
        return super.getNormal(point);
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        Point pBase = _axis.origin();
        Vector va = _axis.direction();
        Point pTop = pBase.add(va.scale(_height));

        List<Point> intersections = null;

        // ---- Lateral surface (infinite tube) intersections, filtered by height ----
        List<Point> tubePoints = super.findIntersections(ray);
        if (tubePoints != null) {
            for (Point p : tubePoints) {
                double s = alignZero(va.dotProduct(p.subtract(pBase)));
                if (s < 0 || s > _height) continue;
                intersections = addUnique(intersections, p);
            }
        }

        // ---- Caps (disks) intersections ----
        double nv = alignZero(va.dotProduct(ray.direction()));
        if (!isZero(nv)) {
            intersections = addCapIntersection(intersections, ray, pBase, va, nv);
            intersections = addCapIntersection(intersections, ray, pTop, va, nv);
        }

        if (intersections == null) return null;

        intersections.sort(Comparator.comparingDouble(p -> p.distanceSquared(ray.origin())));
        intersections = dedupSorted(intersections);

        if (intersections.isEmpty()) return null;
        // A closed finite cylinder is convex -> at most two intersection points
        if (intersections.size() > 2) {
            return new ArrayList<>(intersections.subList(0, 2));
        }
        return intersections;
    }

    private static List<Point> dedupSorted(List<Point> points) {
        if (points.size() < 2) return points;
        List<Point> unique = new ArrayList<>(points.size());
        Point last = null;
        for (Point p : points) {
            if (last == null || last.distanceSquared(p) > DEDUP_DISTANCE_SQUARED) {
                unique.add(p);
                last = p;
            }
        }
        return unique;
    }

    private List<Point> addCapIntersection(List<Point> intersections, Ray ray, Point center, Vector normal, double nv) {
        if (center.equals(ray.origin())) return intersections;

        double numerator = normal.dotProduct(center.subtract(ray.origin()));
        double t = alignZero(numerator / nv);
        if (t <= 0) return intersections;

        Point p = ray.getPoint(t);
        // Include points on the rim (join between base and shell)
        if (alignZero(p.distanceSquared(center) - _radiusSquared) > 0) return intersections;
        return addUnique(intersections, p);
    }

    private static List<Point> addUnique(List<Point> intersections, Point p) {
        if (intersections == null) {
            intersections = new ArrayList<>();
            intersections.add(p);
            return intersections;
        }

        for (Point existing : intersections) {
            if (existing.distanceSquared(p) <= DEDUP_DISTANCE_SQUARED) return intersections;
        }
        intersections.add(p);
        return intersections;
    }

    @Override
    public String toString() {
        return "Cylinder(axis=" + _axis + ", radius=" + _radius + ", height=" + _height + ")";
    }
}
