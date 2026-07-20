package renderer;

import geometries.api.Intersectable.Intersection;
import primitives.*;
import scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * A ray tracer with local Phong lighting, shadows, transparency, reflection,
 * glossy reflection and blurry transparency.
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
    /**
     * Default number of rays in a blurred global-effect beam.
     */
    static final int DEFAULT_BLUR_SAMPLES = 9;
    /**
     * Default distance from the hit point to the blur sampling target plane.
     */
    static final double DEFAULT_BLUR_TARGET_DISTANCE = 100d;

    /**
     * Enables blurry reflection and transparency when material blur values are non-zero.
     */
    private boolean blurEnabled = true;

    /**
     * Number of candidate rays in blurred reflection/transparency beams.
     */
    private int blurSamples = DEFAULT_BLUR_SAMPLES;

    /**
     * Sampling pattern for blurred global-effect beams.
     */
    private SamplingPattern blurSamplingPattern = SamplingPattern.JITTERED;

    /**
     * Sampling shape for blurred global-effect beams.
     */
    private SamplingShape blurSamplingShape = SamplingShape.SQUARE;

    /**
     * Base deterministic seed for jittered blurred global-effect beams.
     */
    private long blurSamplingSeed = 0L;

    /**
     * Distance from the hit point to the blur sampling target plane.
     */
    private double blurTargetDistance = DEFAULT_BLUR_TARGET_DISTANCE;

    /**
     * Reusable immutable sampling boards for blurred beams.
     */
    private final ConcurrentMap<BlurSamplingBoardKey, SamplingBoard> blurSamplingBoards = new ConcurrentHashMap<>();

    /**
     * Optional aggregate render profiling counters.
     */
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
     * Enables or disables blurred global effects.
     *
     * @param enabled {@code true} to use material blur values
     * @return this ray tracer
     */
    SimpleRayTracer setBlurEnabled(boolean enabled) {
        blurEnabled = enabled;
        return this;
    }

    /**
     * Sets the number of candidate rays for blurred global effects.
     *
     * @param samples number of beam samples, must be positive
     * @return this ray tracer
     */
    SimpleRayTracer setBlurSamples(int samples) {
        if (samples <= 0) {
            throw new IllegalArgumentException("Blur samples must be positive");
        }
        if (blurSamples != samples) {
            blurSamples = samples;
            clearBlurBoardCache();
        }
        return this;
    }

    /**
     * Sets the sampling pattern for blurred global-effect beams.
     *
     * @param pattern sampling pattern
     * @return this ray tracer
     */
    SimpleRayTracer setBlurSamplingPattern(SamplingPattern pattern) {
        SamplingPattern validated = Objects.requireNonNull(pattern, "Blur sampling pattern must not be null");
        if (blurSamplingPattern != validated) {
            blurSamplingPattern = validated;
            clearBlurBoardCache();
        }
        return this;
    }

    /**
     * Sets the sampling shape for blurred global-effect beams.
     *
     * @param shape sampling shape; must be {@link SamplingShape#SQUARE} or {@link SamplingShape#CIRCLE}
     * @return this ray tracer
     */
    SimpleRayTracer setBlurSamplingShape(SamplingShape shape) {
        SamplingShape validated = Objects.requireNonNull(shape, "Blur sampling shape must not be null");
        if (validated == SamplingShape.RECTANGLE) {
            throw new IllegalArgumentException("Blur sampling shape must be SQUARE or CIRCLE");
        }
        if (blurSamplingShape != validated) {
            blurSamplingShape = validated;
            clearBlurBoardCache();
        }
        return this;
    }

    /**
     * Sets the base deterministic seed for jittered blurred global effects.
     *
     * @param seed base seed
     * @return this ray tracer
     */
    SimpleRayTracer setBlurSamplingSeed(long seed) {
        blurSamplingSeed = seed;
        return this;
    }

    /**
     * Sets the target-plane distance for blurred global effects.
     *
     * @param distance distance from hit point to target plane, must be positive
     * @return this ray tracer
     */
    SimpleRayTracer setBlurTargetDistance(double distance) {
        if (!Double.isFinite(distance) || alignZero(distance) <= 0) {
            throw new IllegalArgumentException("Blur target distance must be positive");
        }
        blurTargetDistance = distance;
        return this;
    }

    /**
     * Attaches aggregate render profiling counters.
     *
     * @param renderStats statistics object, or {@code null} to disable profiling
     * @return this ray tracer
     */
    SimpleRayTracer setRenderStats(RenderStats renderStats) {
        this.renderStats = renderStats;
        return this;
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
        recordIntersectionCalculation();
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
            if (!setLightSource(intersection, lightSource)) continue;

            Double3 ktr = transparency(intersection);
            if (ktr.product(k).isLowerThan(MIN_CALC_COLOR_K)) continue;

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
        recordShadowRay();
        recordIntersectionCalculation();
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
        recordShadowRay();
        recordIntersectionCalculation();
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
        Color color = Color.BLACK;

        Double3 reflectedK = k.product(intersection.material.kR);
        if (!reflectedK.isLowerThan(MIN_CALC_COLOR_K)) {
            color = color.add(calcGlobalEffect(
                    constructReflectedRay(intersection),
                    intersection.point,
                    intersection.normal,
                    intersection.material.glossyBlur,
                    level,
                    reflectedK,
                    intersection.material.kR,
                    GlobalEffectKind.REFLECTION));
        }

        Double3 refractedK = k.product(intersection.material.kT);
        if (!refractedK.isLowerThan(MIN_CALC_COLOR_K)) {
            color = color.add(calcGlobalEffect(
                    constructRefractedRay(intersection),
                    intersection.point,
                    intersection.normal,
                    intersection.material.transparencyBlur,
                    level,
                    refractedK,
                    intersection.material.kT,
                    GlobalEffectKind.TRANSPARENCY));
        }

        return color;
    }

    /**
     * Calculates one recursive global effect from either one ideal ray or a blurred beam.
     *
     * @param idealRay               ideal reflection/transmission ray
     * @param origin                 original intersection point
     * @param normal                 surface normal at the original intersection
     * @param blurSize               physical beam size on the target plane
     * @param level                  remaining recursion depth
     * @param accumulatedCoefficient accumulated coefficient including the current material coefficient
     * @param coefficient            current material reflection/transparency coefficient
     * @return global effect color contribution
     */
    private Color calcGlobalEffect(Ray idealRay, Point origin, Vector normal, double blurSize, int level,
                                   Double3 accumulatedCoefficient, Double3 coefficient,
                                   GlobalEffectKind effectKind) {
        List<Ray> rays = constructBlurredBeam(origin, idealRay, normal, blurSize);
        Double3 perRayCoefficient = accumulatedCoefficient.scale(1d / rays.size());
        recordGlobalRays(effectKind, rays.size());

        Color color = Color.BLACK;
        for (Ray ray : rays) {
            Intersection intersection = findClosestIntersection(ray);
            Color rayColor = intersection == null
                    ? _scene.background
                    : preprocessIntersection(intersection, ray)
                    ? calcColor(intersection, level - 1, perRayCoefficient)
                    : Color.BLACK;
            color = color.add(rayColor);
        }
        return color.scale(1d / rays.size()).scale(coefficient);
    }

    /**
     * Constructs a reflected ideal ray from the current intersection.
     *
     * @param intersection current intersection
     * @return reflected ideal ray
     */
    private Ray constructReflectedRay(Intersection intersection) {
        Vector v = intersection.v;
        Vector n = intersection.normal;
        double vn = intersection.nv;
        Vector r = v.subtract(n.scale(2d * vn));
        return new Ray(intersection.point, r, n);
    }

    /**
     * Air index of refraction.
     */
    private static final double AIR_N = 1.0;

    /**
     * Constructs the ideal transparent/refracted ray.
     * <p>
     * The project uses Snell-law refraction when {@link Material#refractiveIndex}
     * differs from air. If total internal reflection is detected, this falls back
     * to the reflected direction safely.
     * </p>
     *
     * @param intersection current intersection
     * @return ideal transmitted/refracted ray
     */
    private Ray constructRefractedRay(Intersection intersection) {
        Vector i = intersection.v;
        Vector n = intersection.normal;

        boolean entering = i.dotProduct(n) < 0;
        double materialN = intersection.material.refractiveIndex;

        double n1 = entering ? AIR_N : materialN;
        double n2 = entering ? materialN : AIR_N;

        if (!entering) {
            n = n.scale(-1);
        }

        double cosI = -i.dotProduct(n);
        double eta = n1 / n2;
        double k = 1 - eta * eta * (1 - cosI * cosI);

        if (k < 0) {
            return constructReflectedRay(intersection);
        }

        Vector t = i.scale(eta);
        double normalScale = eta * cosI - Math.sqrt(k);
        if (!isZero(normalScale)) {
            t = t.add(n.scale(normalScale));
        }

        return new Ray(intersection.point, t.normalize(), n);
    }

    /**
     * Constructs either one ideal ray or exactly the configured number of blurred
     * candidate rays. The blur value is interpreted as a square side length for
     * {@link SamplingShape#SQUARE}, and as a circle diameter for
     * {@link SamplingShape#CIRCLE}.
     *
     * @param origin   original surface intersection point
     * @param idealRay ideal reflection/transmission ray
     * @param normal   surface normal
     * @param blurSize physical beam size on the target plane
     * @return valid beam rays, or the ideal ray when blur is disabled/zero or all candidates are rejected
     */
    List<Ray> constructBlurredBeam(Point origin, Ray idealRay, Vector normal, double blurSize) {
        validateBlurSize(blurSize);
        if (!blurEnabled || isZero(blurSize)) {
            return List.of(idealRay);
        }

        Vector idealDirection = idealRay.direction().normalize();
        Point targetCenter = origin.add(idealDirection.scale(blurTargetDistance));
        Axes axes = targetPlaneAxes(idealDirection);
        long seed = seedForBeam(origin, idealDirection, normal, blurSize);
        SamplingBoard board = createBlurSamplingBoard(blurSize);
        List<Point> samplePoints = board.sample(targetCenter, axes.right(), axes.up(), seed);

        List<Ray> rays = new ArrayList<>(samplePoints.size());
        for (Point samplePoint : samplePoints) {
            Vector direction = samplePoint.subtract(origin).normalize();
            if (sameNormalHemisphere(direction, idealDirection, normal)) {
                rays.add(new Ray(origin, direction, normal));
            }
        }

        return rays.isEmpty() ? List.of(idealRay) : List.copyOf(rays);
    }

    /**
     * Creates the sampling board for one blurred beam.
     */
    private SamplingBoard createBlurSamplingBoard(double blurSize) {
        BlurSamplingBoardKey key = new BlurSamplingBoardKey(
                blurSize, blurSamples, blurSamplingPattern, blurSamplingShape);
        return blurSamplingBoards.computeIfAbsent(key, SimpleRayTracer::newBlurSamplingBoard);
    }

    /**
     * Creates one immutable cached sampling board for a blur configuration.
     */
    private static SamplingBoard newBlurSamplingBoard(BlurSamplingBoardKey key) {
        return switch (key.shape()) {
            case SQUARE -> SamplingBoard.square(key.blurSize(), key.samples(), key.pattern(), 0L);
            case CIRCLE -> SamplingBoard.circle(key.blurSize() / 2d, key.samples(), key.pattern(), 0L);
            case RECTANGLE -> throw new IllegalArgumentException("Blur sampling shape must be SQUARE or CIRCLE");
        };
    }

    /**
     * Clears reusable blur sampling boards after configuration changes.
     */
    private void clearBlurBoardCache() {
        blurSamplingBoards.clear();
    }

    /**
     * Returns current blur sampling board cache size for focused unit tests.
     */
    int blurSamplingBoardCacheSize() {
        return blurSamplingBoards.size();
    }

    /**
     * Builds normalized target-plane axes perpendicular to the ideal direction.
     */
    private Axes targetPlaneAxes(Vector idealDirection) {
        Vector reference = Math.abs(idealDirection.dotProduct(Vector.AXIS_X)) < 0.9
                ? Vector.AXIS_X
                : Math.abs(idealDirection.dotProduct(Vector.AXIS_Y)) < 0.9
                ? Vector.AXIS_Y
                : Vector.AXIS_Z;
        Vector right = reference.crossProduct(idealDirection).normalize();
        Vector up = idealDirection.crossProduct(right).normalize();
        return new Axes(right, up);
    }

    /**
     * Checks that a candidate ray remains in the same normal hemisphere as the ideal ray.
     */
    private boolean sameNormalHemisphere(Vector direction, Vector idealDirection, Vector normal) {
        double idealSide = alignZero(idealDirection.dotProduct(normal));
        double candidateSide = alignZero(direction.dotProduct(normal));
        return isZero(idealSide)
                ? isZero(candidateSide)
                : alignZero(candidateSide * idealSide) > 0;
    }

    /**
     * Derives a stable per-beam seed from the base seed, hit point, ideal
     * direction, surface normal and blur size so separate surface points do not
     * reuse the same visible jitter pattern.
     */
    private long seedForBeam(Point origin, Vector idealDirection, Vector normal, double blurSize) {
        long value = blurSamplingSeed;
        value ^= (long) origin.hashCode() * 0x9E3779B97F4A7C15L;
        value ^= (long) idealDirection.hashCode() * 0xBF58476D1CE4E5B9L;
        value ^= (long) normal.hashCode() * 0x94D049BB133111EBL;
        value ^= Double.doubleToLongBits(blurSize);
        return mix64(value);
    }

    /**
     * SplitMix64 finalizer used for deterministic seed mixing.
     */
    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    /**
     * Validates a material blur size before beam construction.
     */
    private static void validateBlurSize(double blurSize) {
        if (!Double.isFinite(blurSize) || blurSize < 0) {
            throw new IllegalArgumentException("Blur size must be non-negative");
        }
    }

    /**
     * Records a scene intersection query.
     */
    private void recordIntersectionCalculation() {
        if (renderStats != null) {
            renderStats.addIntersectionCalculations(1);
        }
    }

    /**
     * Records one shadow ray.
     */
    private void recordShadowRay() {
        if (renderStats != null) {
            renderStats.addShadowRays(1);
        }
    }

    /**
     * Records recursive global-effect rays by effect type.
     */
    private void recordGlobalRays(GlobalEffectKind effectKind, int count) {
        if (renderStats == null) {
            return;
        }
        if (effectKind == GlobalEffectKind.REFLECTION) {
            renderStats.addReflectionRays(count);
        } else {
            renderStats.addTransparencyRays(count);
        }
    }

    /**
     * Normalized target-plane axes.
     */
    private record Axes(Vector right, Vector up) {
    }

    /**
     * Immutable key for reusable blur sampling boards.
     */
    private record BlurSamplingBoardKey(double blurSize, int samples,
                                        SamplingPattern pattern, SamplingShape shape) {
    }

    /**
     * Recursive global-effect ray category used for profiling.
     */
    private enum GlobalEffectKind {
        REFLECTION,
        TRANSPARENCY
    }
}
