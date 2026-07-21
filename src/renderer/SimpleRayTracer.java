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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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

    /** Whether material blur values should generate multi-ray beams. */
    private boolean blurEnabled = true;

    /** Exact number of rays in a glossy/transparency blur beam. */
    private int blurSamples = 1;

    /** Distance from the hit point to the blur target area. */
    private double blurTargetDistance = 100d;

    /** Sampling pattern for blur target areas. */
    private SamplingPattern blurSamplingPattern = SamplingPattern.GRID;

    /** Sampling shape for blur target areas. */
    private SamplingShape blurSamplingShape = SamplingShape.CIRCLE;

    /** Base seed for deterministic jittered blur samples. */
    private long blurSamplingSeed = 0L;

    /** Cached local sampling boards reused across blur beams. */
    private final Map<BlurBoardKey, SamplingBoard> blurSamplingBoards = new ConcurrentHashMap<>();

    /** Optional aggregate render statistics. */
    private RenderStats renderStats;

    /**
     * Constructs a simple ray tracer.
     *
     * @param scene the scene
     */
    SimpleRayTracer(Scene scene) {
        super(scene);
    }

    /**
     * Enables or disables glossy/refraction blur beams.
     *
     * @param enabled true to use blur beams, false to force ideal rays
     * @return this ray tracer, for chaining
     */
    SimpleRayTracer setBlurEnabled(boolean enabled) {
        blurEnabled = enabled;
        return this;
    }

    /**
     * Sets the exact number of blur beam samples.
     *
     * @param samples sample count, must be positive
     * @return this ray tracer, for chaining
     */
    SimpleRayTracer setBlurSamples(int samples) {
        if (samples < 1) {
            throw new IllegalArgumentException("blur samples must be at least 1");
        }
        blurSamples = samples;
        clearBlurSamplingBoardCache();
        return this;
    }

    /**
     * Sets the distance from the hit point to the blur sampling target plane.
     *
     * @param distance target distance, must be positive and finite
     * @return this ray tracer, for chaining
     */
    SimpleRayTracer setBlurTargetDistance(double distance) {
        validatePositiveFinite(distance, "blur target distance");
        blurTargetDistance = distance;
        return this;
    }

    /**
     * Sets the blur sampling pattern.
     *
     * @param pattern sampling pattern
     * @return this ray tracer, for chaining
     */
    SimpleRayTracer setBlurSamplingPattern(SamplingPattern pattern) {
        blurSamplingPattern = Objects.requireNonNull(pattern, "blur sampling pattern must not be null");
        clearBlurSamplingBoardCache();
        return this;
    }

    /**
     * Sets the blur sampling shape.
     *
     * @param shape sampling shape
     * @return this ray tracer, for chaining
     */
    SimpleRayTracer setBlurSamplingShape(SamplingShape shape) {
        Objects.requireNonNull(shape, "blur sampling shape must not be null");
        if (shape == SamplingShape.RECTANGLE) {
            throw new IllegalArgumentException("Blur sampling supports square or circle only");
        }
        blurSamplingShape = shape;
        clearBlurSamplingBoardCache();
        return this;
    }

    /**
     * Sets the base seed for deterministic jittered blur samples.
     *
     * @param seed base seed
     * @return this ray tracer, for chaining
     */
    SimpleRayTracer setBlurSamplingSeed(long seed) {
        blurSamplingSeed = seed;
        clearBlurSamplingBoardCache();
        return this;
    }

    /**
     * Installs an optional statistics collector.
     *
     * @param renderStats statistics collector
     * @return this ray tracer, for chaining
     */
    SimpleRayTracer setRenderStats(RenderStats renderStats) {
        this.renderStats = renderStats;
        return this;
    }

    /**
     * Returns the current blur sampling-board cache size.
     *
     * @return number of cached sampling boards
     */
    int blurSamplingBoardCacheSize() {
        return blurSamplingBoards.size();
    }

    @Override
    Color traceRay(Ray ray) {
        if (renderStats != null) {
            renderStats.addPrimaryRays(1);
        }
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
        if (renderStats != null) {
            renderStats.addIntersectionCalculations(1);
        }
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
        if (renderStats != null) {
            renderStats.addShadowRays(1);
            renderStats.addIntersectionCalculations(1);
        }
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
     *
     * @param intersection current intersection
     * @param level remaining recursion depth
     * @param k accumulated attenuation coefficient
     * @return reflection contribution
     */
    private Color calcGlossyReflectionEffect(Intersection intersection, int level, Double3 k) {
        Material material = intersection.material;
        if (k.product(material.kR).isLowerThan(MIN_CALC_COLOR_K)) {
            return Color.BLACK;
        }
        Ray centerRay = constructReflectedRay(intersection);
        double blur = effectiveGlossyBlur(material);
        int samples = effectiveGlossySamples(material);

        if (!blurEnabled || samples <= 1 || blur == 0) {
            return calcGlobalEffect(centerRay, level, k, material.kR, true, 1);
        }

        List<Ray> beam = constructBlurredBeam(intersection.point, centerRay, intersection.normal, blur, samples);

        Color color = Color.BLACK;
        for (Ray ray : beam) {
            color = color.add(calcGlobalEffect(ray, level, k, material.kR, true, beam.size()));
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
        if (k.product(material.kT).isLowerThan(MIN_CALC_COLOR_K)) {
            return Color.BLACK;
        }
        Ray centerRay = constructRefractedRay(intersection);
        double blur = effectiveTransparencyBlur(material);
        int samples = effectiveTransparencySamples(material);

        if (!blurEnabled || samples <= 1 || blur == 0) {
            return calcGlobalEffect(centerRay, level, k, material.kT, false, 1);
        }

        List<Ray> beam = constructBlurredBeam(intersection.point, centerRay, intersection.normal, blur, samples);

        Color color = Color.BLACK;
        for (Ray ray : beam) {
            color = color.add(calcGlobalEffect(ray, level, k, material.kT, false, beam.size()));
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
     * @param reflection true for reflected rays, false for transparent rays
     * @param sampleDivisor beam size used for recursive contribution pruning
     * @return global effect color contribution
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx, boolean reflection, int sampleDivisor) {
        Double3 kkx = k.product(kx);
        Double3 recursiveK = sampleDivisor <= 1 ? kkx : kkx.scale(1d / sampleDivisor);
        if (kkx.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;

        if (renderStats != null) {
            if (reflection) {
                renderStats.addReflectionRays(1);
            } else {
                renderStats.addTransparencyRays(1);
            }
        }
        Intersection intersection = findClosestIntersection(ray);
        if (intersection == null) return _scene.background.scale(kx);
        return preprocessIntersection(intersection, ray)
                ? calcColor(intersection, level - 1, recursiveK).scale(kx)
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
     * Constructs a blurred beam around an ideal secondary ray.
     *
     * @param origin       nominal hit point that produced the ideal ray
     * @param idealRay     ideal reflected/refracted ray
     * @param normal       surface normal at the hit point
     * @param materialBlur blur radius configured on the material
     * @return valid sampled rays, or the ideal ray when blur is disabled/no samples are valid
     */
    List<Ray> constructBlurredBeam(Point origin, Ray idealRay, Vector normal, double materialBlur) {
        return constructBlurredBeam(origin, idealRay, normal, materialBlur, blurSamples);
    }

    /**
     * Constructs a blurred beam with an explicit sample count.
     *
     * @param origin nominal hit point that produced the ideal ray
     * @param idealRay ideal reflected/refracted ray
     * @param normal surface normal at the hit point
     * @param materialBlur material blur radius
     * @param samples requested sample count
     * @return valid sampled rays, or the ideal ray when no sampled ray is valid
     */
    private List<Ray> constructBlurredBeam(Point origin, Ray idealRay, Vector normal,
                                           double materialBlur, int samples) {
        validateNonNegativeFinite(materialBlur, "material blur");
        Objects.requireNonNull(origin, "origin must not be null");
        Objects.requireNonNull(idealRay, "ideal ray must not be null");
        Objects.requireNonNull(normal, "normal must not be null");

        if (!blurEnabled || samples <= 1 || materialBlur == 0) {
            return List.of(idealRay);
        }

        Vector idealDirection = idealRay.direction();
        double centerSign = alignZero(idealDirection.dotProduct(normal));
        if (centerSign == 0) {
            return List.of(idealRay);
        }

        Point rayOrigin = idealRay.origin();
        Point targetCenter = rayOrigin.add(idealDirection.scale(blurTargetDistance));
        Vector[] basis = buildOrthonormalBasis(idealDirection);
        SamplingBoard board = blurSamplingBoard(materialBlur, samples);

        List<Point> targetPoints = board.sample(targetCenter, basis[0], basis[1]);
        List<Ray> rays = new ArrayList<>(samples);
        for (Point target : targetPoints) {
            Vector direction = target.subtract(rayOrigin);
            if (alignZero(direction.dotProduct(normal)) * centerSign > 0) {
                rays.add(new Ray(rayOrigin, direction));
            }
        }

        return rays.isEmpty() ? List.of(idealRay) : List.copyOf(rays);
    }

    /**
     * Returns a cached sampling board for the current blur configuration.
     *
     * @param materialBlur material blur radius
     * @param samples requested sample count
     * @return cached sampling board
     */
    private SamplingBoard blurSamplingBoard(double materialBlur, int samples) {
        BlurBoardKey key = new BlurBoardKey(materialBlur, samples, blurSamplingPattern,
                blurSamplingShape, blurSamplingPattern == SamplingPattern.GRID ? 0L : blurSamplingSeed);
        return blurSamplingBoards.computeIfAbsent(key, this::createBlurSamplingBoard);
    }

    /**
     * Creates a blur sampling board from a cache key.
     *
     * @param key cache key
     * @return new sampling board
     */
    private SamplingBoard createBlurSamplingBoard(BlurBoardKey key) {
        return switch (key.shape()) {
            case SQUARE -> SamplingBoard.square(key.materialBlur() * 2d, key.samples(), key.pattern(), key.seed());
            case CIRCLE -> SamplingBoard.circle(key.materialBlur(), key.samples(), key.pattern(), key.seed());
            case RECTANGLE -> throw new IllegalArgumentException("Blur sampling supports square or circle only");
        };
    }

    /**
     * Clears cached blur boards after configuration changes.
     */
    private void clearBlurSamplingBoardCache() {
        blurSamplingBoards.clear();
    }

    /**
     * Returns the material glossy blur radius, considering legacy fields.
     *
     * @param material material to inspect
     * @return glossy blur radius
     */
    private double effectiveGlossyBlur(Material material) {
        return material.glossyBlur > 0 ? material.glossyBlur : material.glossyRadius;
    }

    /**
     * Returns the material transparency blur radius, considering legacy fields.
     *
     * @param material material to inspect
     * @return transparency blur radius
     */
    private double effectiveTransparencyBlur(Material material) {
        return material.transparencyBlur > 0 ? material.transparencyBlur : material.diffuseGlassRadius;
    }

    /**
     * Returns the effective glossy sample count.
     *
     * @param material material to inspect
     * @return effective glossy sample count
     */
    private int effectiveGlossySamples(Material material) {
        return blurSamples > 1 ? blurSamples : material.glossyRays;
    }

    /**
     * Returns the effective transparency sample count.
     *
     * @param material material to inspect
     * @return effective transparency sample count
     */
    private int effectiveTransparencySamples(Material material) {
        return blurSamples > 1 ? blurSamples : material.diffuseGlassRays;
    }

    /**
     * Validates a finite non-negative scalar.
     *
     * @param value value to validate
     * @param name diagnostic parameter name
     */
    private static void validateNonNegativeFinite(double value, String name) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(name + " must be non-negative and finite");
        }
    }

    /**
     * Validates a finite positive scalar.
     *
     * @param value value to validate
     * @param name diagnostic parameter name
     */
    private static void validatePositiveFinite(double value, String name) {
        if (!Double.isFinite(value) || alignZero(value) <= 0) {
            throw new IllegalArgumentException(name + " must be positive and finite");
        }
    }

    /**
     * Sampling-board cache key.
     *
     * @param materialBlur material blur radius
     * @param samples sample count
     * @param pattern sampling pattern
     * @param shape sampling shape
     * @param seed deterministic jitter seed
     */
    private record BlurBoardKey(double materialBlur, int samples,
                                SamplingPattern pattern, SamplingShape shape, long seed) {
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
