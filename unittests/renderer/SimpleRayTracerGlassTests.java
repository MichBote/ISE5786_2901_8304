package renderer;

import geometries.api.Geometry;
import geometries.api.Intersectable.Intersection;
import org.junit.jupiter.api.Test;
import primitives.*;
import scene.Scene;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused tests for glossy reflection and blurry transparency beam handling.
 */
class SimpleRayTracerGlassTests {
    /**
     * Creates the glass ray-tracer test fixture.
     */
    SimpleRayTracerGlassTests() {
    }

    /** Numeric tolerance. */
    private static final double DELTA = 1e-10;
    /** Primary ray origin used by scene-level tests. */
    private static final Point CAMERA_ORIGIN = Point.ZERO;
    /** Primary surface normal. */
    private static final Vector NORMAL = Vector.AXIS_Z;
    /** Beam blur size used by scene-level tests. */
    private static final double BLUR_SIZE = 10d;

    /**
     * Verifies blur zero creates exactly one ideal ray.
     */
    @Test
    void testBlurZeroCreatesOneIdealRay() {
        SimpleRayTracer tracer = tracer()
                .setBlurSamples(5)
                .setBlurSamplingPattern(SamplingPattern.GRID);
        Ray idealRay = new Ray(Point.ZERO, Vector.AXIS_Z, NORMAL);

        List<Ray> rays = tracer.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 0d);

        assertEquals(List.of(idealRay), rays, "Blur zero should use only the ideal ray");
    }

    /**
     * Verifies nonzero blur creates exactly the configured number of valid candidates.
     */
    @Test
    void testNonzeroBlurCreatesConfiguredCandidateCount() {
        SimpleRayTracer tracer = tracer()
                .setBlurSamples(5)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurSamplingShape(SamplingShape.SQUARE);
        Ray idealRay = new Ray(Point.ZERO, Vector.AXIS_Z, NORMAL);

        List<Ray> rays = tracer.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 2d);

        assertEquals(5, rays.size(), "Blurred beam should contain exactly the configured candidate count");
    }

    /**
     * Verifies a sampled beam does not automatically add the central ideal ray.
     */
    @Test
    void testNoExtraCentralRayAddedToBlurredBeam() {
        SimpleRayTracer tracer = tracer()
                .setBlurSamples(4)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurSamplingShape(SamplingShape.SQUARE);
        Ray idealRay = new Ray(Point.ZERO, Vector.AXIS_Z, NORMAL);

        List<Ray> rays = tracer.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 2d);

        assertEquals(4, rays.size(), "Beam should contain exactly N rays");
        assertFalse(rays.contains(idealRay), "Blurred beam should not append the ideal central ray");
    }

    /**
     * Verifies both required blur sampling shapes work.
     */
    @Test
    void testSquareAndCircleBlurSamplingWork() {
        for (SamplingShape shape : List.of(SamplingShape.SQUARE, SamplingShape.CIRCLE)) {
            SimpleRayTracer tracer = tracer()
                    .setBlurSamples(5)
                    .setBlurSamplingPattern(SamplingPattern.GRID)
                    .setBlurSamplingShape(shape);
            Ray idealRay = new Ray(Point.ZERO, Vector.AXIS_Z, NORMAL);

            assertEquals(5, tracer.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 2d).size(),
                    shape + " blur sampling should produce the configured count");
        }
    }

    /**
     * Verifies grid blur sampling is deterministic.
     */
    @Test
    void testGridBlurSamplingIsDeterministic() {
        SimpleRayTracer first = tracer()
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurSamplingSeed(1L);
        SimpleRayTracer second = tracer()
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurSamplingSeed(999L);
        Ray idealRay = new Ray(Point.ZERO, Vector.AXIS_Z, NORMAL);

        assertEquals(first.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 2d),
                second.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 2d),
                "Grid blur sampling should not depend on seed");
    }

    /**
     * Verifies jittered blur sampling is reproducible with the same seed.
     */
    @Test
    void testJitteredBlurSamplingSameSeedIsDeterministic() {
        SimpleRayTracer first = tracer()
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.JITTERED)
                .setBlurSamplingSeed(123L);
        SimpleRayTracer second = tracer()
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.JITTERED)
                .setBlurSamplingSeed(123L);
        Ray idealRay = new Ray(Point.ZERO, Vector.AXIS_Z, NORMAL);

        assertEquals(first.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 2d),
                second.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 2d),
                "Same jitter seed should produce the same blur rays");
    }

    /**
     * Verifies different jitter seeds change sampled beam positions.
     */
    @Test
    void testJitteredBlurSamplingDifferentSeedsDiffer() {
        SimpleRayTracer first = tracer()
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.JITTERED)
                .setBlurSamplingSeed(123L);
        SimpleRayTracer second = tracer()
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.JITTERED)
                .setBlurSamplingSeed(456L);
        Ray idealRay = new Ray(Point.ZERO, Vector.AXIS_Z, NORMAL);

        assertNotEquals(first.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 2d),
                second.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 2d),
                "Different jitter seeds should normally produce different blur rays");
    }

    /**
     * Verifies reflected rays remain on the reflected side of the surface.
     */
    @Test
    void testReflectedRaysRemainOnReflectionSide() {
        SimpleRayTracer tracer = tracer()
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.GRID);
        Ray idealRay = new Ray(Point.ZERO, new Vector(1, 0, 0.4), NORMAL);

        for (Ray ray : tracer.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 0.5d)) {
            assertTrue(ray.direction().dotProduct(NORMAL) > 0,
                    "Reflected candidate crossed to the wrong side");
        }
    }

    /**
     * Verifies transmitted rays remain on the transmission side of the surface.
     */
    @Test
    void testTransmittedRaysRemainOnTransmissionSide() {
        SimpleRayTracer tracer = tracer()
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.GRID);
        Ray idealRay = new Ray(Point.ZERO, new Vector(1, 0, -0.4), NORMAL);

        for (Ray ray : tracer.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 0.5d)) {
            assertTrue(ray.direction().dotProduct(NORMAL) < 0,
                    "Transmitted candidate crossed to the wrong side");
        }
    }

    /**
     * Verifies rejected rays are not included in the averaging divisor.
     */
    @Test
    void testRejectedRaysExcludedFromAveragingDivisor() {
        Ray primaryRay = new Ray(CAMERA_ORIGIN, new Vector(1, 0, -0.1));
        Point hitPoint = primaryRay.getPoint(1d);
        double kr = 0.5d;
        Material material = new Material().setKR(kr).setGlossyBlur(BLUR_SIZE);
        Function<Ray, Color> colorFunction = SimpleRayTracerGlassTests::directionColor;

        SimpleRayTracer tracer = sceneTracer(primaryRay, material, colorFunction)
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurSamplingShape(SamplingShape.SQUARE)
                .setBlurTargetDistance(1d);

        Vector idealDirection = reflected(primaryRay.direction(), NORMAL);
        Ray idealRay = new Ray(hitPoint, idealDirection, NORMAL);
        List<Ray> validRays = tracer.constructBlurredBeam(hitPoint, idealRay, NORMAL, BLUR_SIZE);
        assertTrue(validRays.size() > 0 && validRays.size() < 10,
                "Test setup should produce a partial rejection beam");

        Color expected = average(validRays, colorFunction).scale(kr);

        assertEquals(expected, tracer.traceRay(primaryRay),
                "Rejected rays should not be counted in the averaging divisor");
    }

    /**
     * Verifies no valid sampled rays fall back to the ideal ray safely.
     */
    @Test
    void testNoValidSampledRaysFallbackToIdealRay() {
        SimpleRayTracer tracer = tracer()
                .setBlurSamples(4)
                .setBlurSamplingPattern(SamplingPattern.GRID);
        Ray tangentIdealRay = new Ray(Point.ZERO, Vector.AXIS_X);

        List<Ray> rays = tracer.constructBlurredBeam(Point.ZERO, tangentIdealRay, NORMAL, 4d);

        assertEquals(List.of(tangentIdealRay), rays, "No valid candidates should fall back to the ideal ray");
    }

    /**
     * Verifies reflection averaging and kR scaling are applied exactly once.
     */
    @Test
    void testReflectionAveragingAndScalingOnce() {
        Ray primaryRay = new Ray(CAMERA_ORIGIN, new Vector(1, 0, -0.1));
        Point hitPoint = primaryRay.getPoint(1d);
        double kr = 0.4d;
        Material material = new Material().setKR(kr).setGlossyBlur(BLUR_SIZE);
        Function<Ray, Color> colorFunction = SimpleRayTracerGlassTests::directionColor;

        SimpleRayTracer tracer = sceneTracer(primaryRay, material, colorFunction)
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurTargetDistance(1d);

        Ray idealRay = new Ray(hitPoint, reflected(primaryRay.direction(), NORMAL), NORMAL);
        Color expected = average(tracer.constructBlurredBeam(hitPoint, idealRay, NORMAL, BLUR_SIZE), colorFunction)
                .scale(kr);

        assertEquals(expected, tracer.traceRay(primaryRay),
                "Reflection average should be scaled by kR exactly once");
    }

    /**
     * Verifies transparency averaging and kT scaling are applied exactly once.
     */
    @Test
    void testTransparencyAveragingAndScalingOnce() {
        Ray primaryRay = new Ray(CAMERA_ORIGIN, new Vector(1, 0, -0.1));
        Point hitPoint = primaryRay.getPoint(1d);
        double kt = 0.25d;
        Material material = new Material()
                .setKT(kt)
                .setRefractiveIndex(1d)
                .setTransparencyBlur(BLUR_SIZE);
        Function<Ray, Color> colorFunction = SimpleRayTracerGlassTests::directionColor;

        SimpleRayTracer tracer = sceneTracer(primaryRay, material, colorFunction)
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurTargetDistance(1d);

        Ray idealRay = new Ray(hitPoint, primaryRay.direction(), NORMAL);
        Color expected = average(tracer.constructBlurredBeam(hitPoint, idealRay, NORMAL, BLUR_SIZE), colorFunction)
                .scale(kt);

        assertEquals(expected, tracer.traceRay(primaryRay),
                "Transparency average should be scaled by kT exactly once");
    }

    /**
     * Verifies blur configuration validation.
     */
    @Test
    void testBlurConfigurationValidation() {
        SimpleRayTracer tracer = tracer();

        assertThrows(IllegalArgumentException.class, () -> tracer.setBlurSamples(0),
                "Blur samples must be positive");
        assertThrows(IllegalArgumentException.class, () -> tracer.setBlurTargetDistance(0d),
                "Blur target distance must be positive");
        assertThrows(IllegalArgumentException.class, () -> tracer.setBlurTargetDistance(Double.NaN),
                "Blur target distance must be finite");
        assertThrows(NullPointerException.class, () -> tracer.setBlurSamplingPattern(null),
                "Null blur sampling pattern should be rejected");
        assertThrows(NullPointerException.class, () -> tracer.setBlurSamplingShape(null),
                "Null blur sampling shape should be rejected");
        assertThrows(IllegalArgumentException.class, () -> tracer.setBlurSamplingShape(SamplingShape.RECTANGLE),
                "Rectangular blur sampling should be rejected");
        assertThrows(IllegalArgumentException.class,
                () -> tracer.constructBlurredBeam(Point.ZERO, new Ray(Point.ZERO, Vector.AXIS_Z), NORMAL, -1d),
                "Negative material blur should be rejected during tracing");
    }

    /**
     * Verifies blurred beams reuse cached immutable sampling boards.
     */
    @Test
    void testBlurSamplingBoardCacheIsReused() {
        SimpleRayTracer tracer = tracer()
                .setBlurSamples(5)
                .setBlurSamplingPattern(SamplingPattern.JITTERED)
                .setBlurSamplingShape(SamplingShape.CIRCLE);
        Ray idealRay = new Ray(Point.ZERO, Vector.AXIS_Z, NORMAL);

        assertEquals(0, tracer.blurSamplingBoardCacheSize(), "Cache should start empty");
        tracer.constructBlurredBeam(Point.ZERO, idealRay, NORMAL, 2d);
        assertEquals(1, tracer.blurSamplingBoardCacheSize(), "First blur configuration should create one board");
        tracer.constructBlurredBeam(new Point(1, 0, 0),
                new Ray(new Point(1, 0, 0), Vector.AXIS_Z, NORMAL), NORMAL, 2d);
        assertEquals(1, tracer.blurSamplingBoardCacheSize(), "Same blur configuration should reuse the board");

        tracer.setBlurSamples(6);
        assertEquals(0, tracer.blurSamplingBoardCacheSize(), "Changing blur configuration should clear the cache");
    }

    /**
     * Verifies zero reflection/transparency coefficients avoid beam construction.
     */
    @Test
    void testZeroCoefficientsAvoidBeamConstruction() {
        Ray primaryRay = new Ray(CAMERA_ORIGIN, new Vector(1, 0, -0.1));
        Material material = new Material()
                .setGlossyBlur(BLUR_SIZE)
                .setTransparencyBlur(BLUR_SIZE)
                .setKR(0d)
                .setKT(0d);
        SimpleRayTracer tracer = sceneTracer(primaryRay, material, ray -> Color.BLACK)
                .setBlurSamples(10);

        tracer.traceRay(primaryRay);

        assertEquals(0, tracer.blurSamplingBoardCacheSize(),
                "Zero kR/kT should not construct blurred beams");
    }

    /**
     * Verifies whole-branch contribution pruning avoids beam construction.
     */
    @Test
    void testSubThresholdContributionAvoidsBeamConstruction() {
        Ray primaryRay = new Ray(CAMERA_ORIGIN, new Vector(1, 0, -0.1));
        Material material = new Material()
                .setKR(0.0009)
                .setGlossyBlur(BLUR_SIZE);
        RenderStats stats = new RenderStats();
        SimpleRayTracer tracer = sceneTracer(primaryRay, material, ray -> Color.BLACK)
                .setBlurSamples(10)
                .setRenderStats(stats);

        tracer.traceRay(primaryRay);

        assertEquals(0, tracer.blurSamplingBoardCacheSize(),
                "Sub-threshold branch should not construct a sampling board");
        assertEquals(0, stats.reflectionRays(), "Sub-threshold branch should not trace reflection rays");
    }

    /**
     * Verifies per-ray pruning does not remove the first visible averaged hit.
     */
    @Test
    void testPerRayCoefficientDoesNotRemoveFirstHitAverage() {
        Ray primaryRay = new Ray(CAMERA_ORIGIN, new Vector(1, 0, -0.1));
        Point hitPoint = primaryRay.getPoint(1d);
        double kr = 0.002d;
        Material material = new Material().setKR(kr).setGlossyBlur(BLUR_SIZE);
        Function<Ray, Color> brightColor = ray -> new Color(2000, 1000, 500);

        SimpleRayTracer tracer = sceneTracer(primaryRay, material, brightColor)
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurTargetDistance(1d);

        Ray idealRay = new Ray(hitPoint, reflected(primaryRay.direction(), NORMAL), NORMAL);
        Color expected = average(tracer.constructBlurredBeam(hitPoint, idealRay, NORMAL, BLUR_SIZE), brightColor)
                .scale(kr);

        assertEquals(expected, tracer.traceRay(primaryRay),
                "Averaged first-hit color should not be removed by per-ray recursion pruning");
    }

    /**
     * Verifies per-ray coefficients prune negligible recursive branches.
     */
    @Test
    void testPerRayCoefficientPrunesNegligibleRecursiveBranches() {
        Ray primaryRay = new Ray(CAMERA_ORIGIN, new Vector(0, 0, -1));
        Material material = new Material().setKR(0.009).setGlossyBlur(BLUR_SIZE);
        RenderStats stats = new RenderStats();

        SimpleRayTracer tracer = sceneTracer(primaryRay, material, ray -> Color.BLACK, new Material().setKR(1d))
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurTargetDistance(1d)
                .setRenderStats(stats);

        tracer.traceRay(primaryRay);

        assertEquals(10, stats.reflectionRays(),
                "Per-ray coefficient below threshold should prune each target's recursive reflection");
    }

    /**
     * Verifies visible per-ray recursive branches are not pruned.
     */
    @Test
    void testPerRayCoefficientKeepsVisibleRecursiveBranches() {
        Ray primaryRay = new Ray(CAMERA_ORIGIN, new Vector(0, 0, -1));
        Material material = new Material().setKR(0.02).setGlossyBlur(BLUR_SIZE);
        RenderStats stats = new RenderStats();

        SimpleRayTracer tracer = sceneTracer(primaryRay, material, ray -> Color.BLACK, new Material().setKR(1d))
                .setBlurSamples(10)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurTargetDistance(1d)
                .setRenderStats(stats);

        tracer.traceRay(primaryRay);

        assertTrue(stats.reflectionRays() > 10,
                "Per-ray coefficient above threshold should preserve recursive reflection");
    }

    /**
     * Verifies recursion terminates for mutually recursive reflective hits.
     */
    @Test
    void testRecursionTerminates() {
        Scene scene = new Scene("recursive reflection");
        scene.geometries.add(new AlwaysHitGeometry(new Material().setKR(1d)));
        SimpleRayTracer tracer = new SimpleRayTracer(scene);

        assertDoesNotThrow(() -> tracer.traceRay(new Ray(Point.ZERO, Vector.AXIS_Z)),
                "Recursive reflection should terminate at the configured recursion depth");
    }

    /**
     * Creates a bare tracer.
     *
     * @return bare simple ray tracer
     */
    private SimpleRayTracer tracer() {
        return new SimpleRayTracer(new Scene("glass unit test"));
    }

    /**
     * Creates a ray tracer for scene-level global-effect tests.
     *
     * @param primaryRay primary ray that hits the test surface
     * @param primaryMaterial material assigned to the primary surface
     * @param colorFunction function used to color target hits
     * @return configured scene tracer
     */
    private static SimpleRayTracer sceneTracer(Ray primaryRay, Material primaryMaterial,
                                               Function<Ray, Color> colorFunction) {
        return sceneTracer(primaryRay, primaryMaterial, colorFunction, new Material());
    }

    /**
     * Creates a ray tracer for scene-level global-effect tests with a target material.
     *
     * @param primaryRay primary ray that hits the test surface
     * @param primaryMaterial material assigned to the primary surface
     * @param colorFunction function used to color target hits
     * @param targetMaterial material assigned to the target surface
     * @return configured scene tracer
     */
    private static SimpleRayTracer sceneTracer(Ray primaryRay, Material primaryMaterial,
                                               Function<Ray, Color> colorFunction, Material targetMaterial) {
        Scene scene = new Scene("global effect unit test");
        scene.geometries.add(
                new PrimaryGeometry(primaryRay.origin(), primaryRay.getPoint(1d), NORMAL, primaryMaterial),
                new RayColoredTarget(primaryRay.origin(), colorFunction, targetMaterial)
        );
        return new SimpleRayTracer(scene);
    }

    /**
     * Computes a reflected direction.
     *
     * @param direction incoming direction
     * @param normal surface normal
     * @return reflected direction
     */
    private static Vector reflected(Vector direction, Vector normal) {
        double vn = direction.dotProduct(normal);
        return direction.subtract(normal.scale(2d * vn)).normalize();
    }

    /**
     * Returns a deterministic color from a ray direction.
     *
     * @param ray ray to color
     * @return deterministic direction-based color
     */
    private static Color directionColor(Ray ray) {
        Vector direction = ray.direction();
        return new Color(
                40d + 80d * Math.abs(direction.dotProduct(Vector.AXIS_X)),
                30d + 70d * Math.abs(direction.dotProduct(Vector.AXIS_Y)),
                20d + 60d * Math.abs(direction.dotProduct(Vector.AXIS_Z))
        );
    }

    /**
     * Averages colors returned for the supplied rays.
     *
     * @param rays rays to sample
     * @param colorFunction function used to color each ray
     * @return averaged color
     */
    private static Color average(List<Ray> rays, Function<Ray, Color> colorFunction) {
        assertFalse(rays.isEmpty(), "Test setup must provide at least one ray");
        Color color = Color.BLACK;
        for (Ray ray : rays) {
            color = color.add(colorFunction.apply(ray));
        }
        return color.scale(1d / rays.size());
    }

    /**
     * Geometry representing the first surface hit by a primary test ray only.
     */
    private static final class PrimaryGeometry extends Geometry {
        /** Primary origin accepted by this geometry. */
        private final Point primaryOrigin;
        /** Fixed hit point. */
        private final Point hitPoint;
        /** Fixed surface normal. */
        private final Vector normal;

        /**
         * Creates the primary geometry.
         *
         * @param primaryOrigin primary ray origin accepted by this geometry
         * @param hitPoint fixed hit point
         * @param normal fixed surface normal
         * @param material geometry material
         */
        private PrimaryGeometry(Point primaryOrigin, Point hitPoint, Vector normal, Material material) {
            this.primaryOrigin = primaryOrigin;
            this.hitPoint = hitPoint;
            this.normal = normal;
            setMaterial(material);
        }

        @Override
        public Vector getNormal(Point point) {
            return normal;
        }

        @Override
        protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
            return ray.origin().equals(primaryOrigin)
                    ? List.of(new Intersection(this, hitPoint))
                    : null;
        }
    }

    /**
     * Target geometry whose emission color is derived from the incoming ray.
     */
    private static final class RayColoredTarget extends Geometry {
        /** Primary origin ignored by this target. */
        private final Point primaryOrigin;
        /** Ray-to-color function. */
        private final Function<Ray, Color> colorFunction;
        /** Last ray-specific emission color. */
        private Color color = Color.BLACK;
        /** Last ray-specific valid normal. */
        private Vector normal = Vector.AXIS_Z;

        /**
         * Creates the target geometry.
         *
         * @param primaryOrigin primary ray origin ignored by this target
         * @param colorFunction ray-to-color function
         * @param material target material
         */
        private RayColoredTarget(Point primaryOrigin, Function<Ray, Color> colorFunction, Material material) {
            this.primaryOrigin = primaryOrigin;
            this.colorFunction = colorFunction;
            setMaterial(material);
        }

        @Override
        public Color getEmission() {
            return color;
        }

        @Override
        public Vector getNormal(Point point) {
            return normal;
        }

        @Override
        protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
            if (ray.origin().equals(primaryOrigin)) {
                return null;
            }
            color = colorFunction.apply(ray);
            normal = ray.direction().scale(-1);
            return List.of(new Intersection(this, ray.getPoint(1d)));
        }
    }

    /**
     * Geometry that always returns one hit and reflects recursively.
     */
    private static final class AlwaysHitGeometry extends Geometry {
        /**
         * Creates recursive reflective geometry.
         *
         * @param material geometry material
         */
        private AlwaysHitGeometry(Material material) {
            setMaterial(material);
        }

        @Override
        public Vector getNormal(Point point) {
            return Vector.AXIS_Z;
        }

        @Override
        protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
            return List.of(new Intersection(this, ray.getPoint(1d)));
        }
    }
}
