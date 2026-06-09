package renderer;

import geometries.api.Intersectable.Intersection;
import primitives.Color;
import primitives.Double3;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.List;

import static primitives.Util.alignZero;

/**
 * A ray tracer with local Phong lighting, shadows, transparency and reflection.
 */
class SimpleRayTracer extends RayTracerBase {
    /** Maximum recursion depth for global effects. */
    private static final int MAX_CALC_COLOR_LEVEL = 10;
    /** Minimal meaningful color contribution coefficient. */
    private static final double MIN_CALC_COLOR_K = 0.001;
    /** Initial recursive attenuation coefficient. */
    private static final Double3 INITIAL_K = Double3.ONE;

    /**
     * Constructs a simple ray tracer.
     *
     * @param scene the scene
     */
    SimpleRayTracer(Scene scene) {
        super(scene);
    }

    @Override
    Color traceRay(Ray ray) {
        Intersection closest = findClosestIntersection(ray);
        return closest == null ? _scene.background : calcColor(closest, ray);
    }

    /**
     * Finds the closest intersection of a ray with the scene.
     *
     * @param ray ray to trace
     * @return closest intersection, or {@code null} if there are none
     */
    private Intersection findClosestIntersection(Ray ray) {
        List<Intersection> intersections = _scene.geometries.calcIntersections(ray);
        return ray.findClosestIntersection(intersections);
    }

    /**
     * Computes the final color at an intersection point.
     *
     * @param intersection geometry-aware intersection point
     * @param ray ray that produced the intersection
     * @return computed color
     */
    private Color calcColor(Intersection intersection, Ray ray) {
        return preprocessIntersection(intersection, ray)
                ? _scene.ambientLight.getIntensity().scale(intersection.material.kA)
                    .add(calcColor(intersection, MAX_CALC_COLOR_LEVEL, INITIAL_K))
                : Color.BLACK;
    }

    /**
     * Recursive color calculation without ambient light.
     *
     * @param intersection current intersection
     * @param level remaining recursion depth
     * @param k accumulated attenuation coefficient
     * @return local and global color contribution
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        Color color = calcLocalEffects(intersection, k);
        return level == 1 ? color : color.add(calcGlobalEffects(intersection, level, k));
    }

    /**
     * Calculates local lighting effects (emission + diffuse + specular).
     *
     * @param intersection geometry-aware intersection point
     * @param k accumulated attenuation coefficient
     * @return local lighting color
     */
    private Color calcLocalEffects(Intersection intersection, Double3 k) {
        Color color = intersection.geometry.getEmission();

        for (var lightSource : _scene.lights) {
            if (!setLightSource(intersection, lightSource)) continue;

            Double3 ktr = transparency(intersection);
            if (!ktr.product(k).isGreaterThan(MIN_CALC_COLOR_K)) continue;

            Color lightIntensity = lightSource.getIntensity(intersection.point).scale(ktr);
            Double3 factor = calcDiffuse(intersection).add(calcSpecular(intersection));
            color = color.add(lightIntensity.scale(factor));
        }

        return color;
    }

    /**
     * Initial boolean shadow test kept for direct review of the first shadowing approach.
     *
     * @param intersection current intersection with a light source already set
     * @return {@code true} if no opaque object blocks the light
     */
    @SuppressWarnings("unused")
    private boolean unshaded(Intersection intersection) {
        Vector pointToLight = intersection.l.scale(-1);
        Ray shadowRay = new Ray(intersection.point, pointToLight, intersection.normal);
        double lightDistance = intersection.light.getDistance(intersection.point);
        var shadowIntersections = _scene.geometries.calcIntersections(shadowRay, lightDistance);
        if (shadowIntersections == null) return true;

        for (Intersection shadowIntersection : shadowIntersections) {
            if (shadowIntersection.material.kT.isLowerThan(MIN_CALC_COLOR_K)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates accumulated transparency along the shadow ray to the current light.
     *
     * @param intersection current intersection with a light source already set
     * @return cumulative transparency coefficient
     */
    private Double3 transparency(Intersection intersection) {
        Vector pointToLight = intersection.l.scale(-1);
        Ray shadowRay = new Ray(intersection.point, pointToLight, intersection.normal);
        double lightDistance = intersection.light.getDistance(intersection.point);
        var shadowIntersections = _scene.geometries.calcIntersections(shadowRay, lightDistance);
        if (shadowIntersections == null) return Double3.ONE;

        Double3 ktr = Double3.ONE;
        for (Intersection shadowIntersection : shadowIntersections) {
            ktr = ktr.product(shadowIntersection.material.kT);
            if (ktr.isLowerThan(MIN_CALC_COLOR_K)) return Double3.ZERO;
        }
        return ktr;
    }

    /**
     * Computes the diffuse reflection factor (kD * |l*n|).
     *
     * @param intersection geometry-aware intersection point
     * @return diffuse reflection factor
     */
    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD.scale(Math.abs(intersection.nl));
    }

    /**
     * Computes the specular reflection factor (kS * (max(0, -v*r))^nShininess).
     *
     * @param intersection geometry-aware intersection point
     * @return specular reflection factor
     */
    private Double3 calcSpecular(Intersection intersection) {
        Vector r = intersection.l.subtract(intersection.normal.scale(2d * intersection.nl));
        double minusVR = -alignZero(intersection.v.dotProduct(r));
        if (minusVR <= 0) return Double3.ZERO;

        double factor = Math.pow(minusVR, intersection.material.nShininess);
        return intersection.material.kS.scale(factor);
    }

    /**
     * Calculates recursive reflection and transparency effects.
     *
     * @param intersection current intersection
     * @param level remaining recursion depth
     * @param k accumulated attenuation coefficient
     * @return global effects contribution
     */
    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        return calcGlobalEffect(constructReflectedRay(intersection), level, k, intersection.material.kR)
                .add(calcGlobalEffect(constructRefractedRay(intersection), level, k, intersection.material.kT));
    }

    /**
     * Calculates one recursive global effect.
     *
     * @param ray secondary ray
     * @param level remaining recursion depth
     * @param k accumulated attenuation coefficient
     * @param kx current material global coefficient
     * @return global effect color contribution
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkx = k.product(kx);
        if (!kkx.isGreaterThan(MIN_CALC_COLOR_K)) return Color.BLACK;

        Intersection intersection = findClosestIntersection(ray);
        if (intersection == null) return _scene.background.scale(kx);
        return preprocessIntersection(intersection, ray)
                ? calcColor(intersection, level - 1, kkx).scale(kx)
                : Color.BLACK;
    }

    /**
     * Constructs a reflected ray from the current intersection.
     *
     * @param intersection current intersection
     * @return reflected ray
     */
    private Ray constructReflectedRay(Intersection intersection) {
        Vector v = intersection.v;
        Vector n = intersection.normal;
        double vn = intersection.nv;
        Vector r = v.subtract(n.scale(2d * vn));
        return new Ray(intersection.point, r, n);
    }

    /**
     * Constructs a refracted ray from the current intersection.
     *
     * @param intersection current intersection
     * @return refracted ray
     */
    private Ray constructRefractedRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.normal);
    }
}
