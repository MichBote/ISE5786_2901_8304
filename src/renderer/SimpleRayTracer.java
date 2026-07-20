package renderer;

import geometries.api.Intersectable.Intersection;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Ray;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

import java.util.ArrayList;
import java.util.List;

import static primitives.Util.alignZero;

/**
 * A ray tracer with local Phong lighting, shadows, transparency and reflection.
 */
class SimpleRayTracer extends RayTracerBase {
    /**
     * Maximum recursion depth for global effects.
     */
    private static final int MAX_CALC_COLOR_LEVEL = 10;
    /**
     * Minimal meaningful color contribution coefficient.
     */
    private static final double MIN_CALC_COLOR_K = 0.001;
    /**
     * Initial recursive attenuation coefficient.
     */
    private static final Double3 INITIAL_K = Double3.ONE;

    /** Golden angle in radians for deterministic spiral disk sampling. */
    private static final double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));

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
     * @param ray          ray that produced the intersection
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
     * @param level        remaining recursion depth
     * @param k            accumulated attenuation coefficient
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
     * @param k            accumulated attenuation coefficient
     * @return local lighting color
     */
    private Color calcLocalEffects(Intersection intersection, Double3 k) {
        Color color = intersection.geometry.getEmission();

        for (var lightSource : _scene.lights) {
            List<Vector> sampledDirections = lightSource.getLs(intersection.point);
            Color sampleAccumulated = Color.BLACK;

            for (Vector sampledL : sampledDirections) {
                if (!setLightDirection(intersection, lightSource, sampledL)) {
                    continue;
                }

                Double3 ktr = transparency(intersection);
                if (ktr.product(k).isLowerThan(MIN_CALC_COLOR_K)) {
                    continue;
                }

                Color lightIntensity = lightSource.getIntensity(intersection.point).scale(ktr);
                Double3 factor = calcDiffuse(intersection).add(calcSpecular(intersection));
                sampleAccumulated = sampleAccumulated.add(lightIntensity.scale(factor));
            }

            color = color.add(sampleAccumulated.scale(1d / sampledDirections.size()));
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
     * @param level        remaining recursion depth
     * @param k            accumulated attenuation coefficient
     * @return global effects contribution
     */
    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        Color reflection = calcGlossyReflectionEffect(intersection, level, k);
        Color refraction = calcDiffuseGlassEffect(intersection, level, k);
        return reflection.add(refraction);
    }

    /**
     * Calculates reflection contribution, optionally using glossy beam tracing.
     */
    private Color calcGlossyReflectionEffect(Intersection intersection, int level, Double3 k) {
        Material material = intersection.material;
        Ray centerRay = constructReflectedRay(intersection);

        if (material.glossyRays <= 1 || material.glossyRadius == 0) {
            return calcGlobalEffect(centerRay, level, k, material.kR);
        }

        List<Ray> beam = constructBeam(
                centerRay,
                intersection.normal,
                material.glossyRadius,
                material.glossyDistance,
                material.glossyRays
        );

        Color color = Color.BLACK;
        for (Ray ray : beam) {
            color = color.add(calcGlobalEffect(ray, level, k, material.kR));
        }
        return color.scale(1d / beam.size());
    }

    /**
     * Calculates transparency contribution, optionally using a diffuse-glass beam.
     *
     * @param intersection current intersection
     * @param level        recursion depth
     * @param k            accumulated attenuation
     * @return transparency contribution
     */
    private Color calcDiffuseGlassEffect(Intersection intersection, int level, Double3 k) {
        Material material = intersection.material;
        Ray centerRay = constructRefractedRay(intersection);

        if (material.diffuseGlassRays <= 1 || material.diffuseGlassRadius == 0) {
            return calcGlobalEffect(centerRay, level, k, material.kT);
        }

        List<Ray> beam = constructBeam(
                centerRay,
                intersection.normal,
                material.diffuseGlassRadius,
                material.diffuseGlassDistance,
                material.diffuseGlassRays
        );

        Color color = Color.BLACK;
        for (Ray ray : beam) {
            color = color.add(calcGlobalEffect(ray, level, k, material.kT));
        }
        return color.scale(1d / beam.size());
    }

    /**
     * Calculates one recursive global effect.
     *
     * @param ray   secondary ray
     * @param level remaining recursion depth
     * @param k     accumulated attenuation coefficient
     * @param kx    current material global coefficient
     * @return global effect color contribution
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkx = k.product(kx);
        if (kkx.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;

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

    /**
     * Constructs a deterministic beam around a central ray using disk sampling.
     *
     * @param centerRay central ray
     * @param normal    surface normal at hit point
     * @param radius    disk radius at target plane
     * @param distance  target plane distance from ray origin
     * @param rays      requested number of rays
     * @return sampled beam containing at least the central ray
     */
    private List<Ray> constructBeam(Ray centerRay, Vector normal, double radius, double distance, int rays) {
        List<Ray> beam = new ArrayList<>(Math.max(1, rays));
        beam.add(centerRay);
        if (rays <= 1 || radius == 0) {
            return beam;
        }

        Vector centerDir = centerRay.direction();
        Point origin = centerRay.origin();
        Point centerTarget = origin.add(centerDir.scale(distance));
        Vector[] basis = buildOrthonormalBasis(centerDir);
        Vector u = basis[0];
        Vector v = basis[1];

        double centerSign = alignZero(centerDir.dotProduct(normal));
        int candidateCount = rays - 1;
        for (int i = 1; i <= candidateCount; i++) {
            double t = i / (double) candidateCount;
            double r = radius * Math.sqrt(t);
            double theta = i * GOLDEN_ANGLE;

            Vector offset = u.scale(r * Math.cos(theta)).add(v.scale(r * Math.sin(theta)));
            Point target = centerTarget.add(offset);
            Vector dir = target.subtract(origin);

            // Keep rays on the same side of the tangent plane as the central ray.
            if (centerSign != 0 && alignZero(dir.dotProduct(normal)) * centerSign <= 0) {
                continue;
            }

            beam.add(new Ray(origin, dir, normal));
        }

        return beam;
    }

    /**
     * Builds two normalized vectors orthogonal to the given direction.
     *
     * @param direction normalized center direction
     * @return orthonormal basis {u, v}
     */
    private Vector[] buildOrthonormalBasis(Vector direction) {
        Vector helper = Math.abs(direction.dotProduct(Vector.AXIS_Y)) < 0.9 ? Vector.AXIS_Y : Vector.AXIS_X;
        Vector u = direction.crossProduct(helper).normalize();
        Vector v = direction.crossProduct(u).normalize();
        return new Vector[]{u, v};
    }
}
