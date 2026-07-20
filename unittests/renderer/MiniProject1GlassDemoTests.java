package renderer;

import geometries.api.Intersectable;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.SpotLight;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mini-Project 1 glossy surface / diffuse glass demonstration and benchmarks.
 */
@SuppressWarnings("java:S109")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MiniProject1GlassDemoTests {
    /** Shared jitter seed used by camera and glass sampling in comparison renders. */
    private static final long MP1_SEED = 2026L;
    /** Last no-blur preview render time, used to print the blur multiplier. */
    private static double lastWithoutBlurSeconds;

    /**
     * Verifies the shared MP1 scene satisfies the instructor's stricter object count.
     */
    @Test
    @Order(1)
    void testGlassDemoSceneContainsAtLeast100Objects() {
        SceneBuild build = createGlassDemoScene();

        assertTrue(build.objectCount() >= 100,
                "MP1 glass demo must contain at least 100 meaningful visible geometry objects");
        assertEquals(3, build.lightPositions().size(), "MP1 glass demo should use exactly three lights");
    }

    /**
     * Required comparison render: same scene with blur disabled.
     */
    @Test
    @Order(2)
    void testGlassSceneWithoutBlur() {
        RenderResult result = renderGlassScene(false, RenderProfile.PREVIEW, "mp1_glass_without_blur");
        lastWithoutBlurSeconds = result.seconds();

        System.out.printf("MP1 glass WITHOUT blur: %.3f seconds%n", result.seconds());
        System.out.printf("MP1 glass WITHOUT blur stats: %s%n", result.stats().summary());
    }

    /**
     * Required comparison render: same scene with blur enabled.
     */
    @Test
    @Order(3)
    void testGlassSceneWithBlur() {
        RenderResult result = renderGlassScene(true, RenderProfile.PREVIEW, "mp1_glass_with_blur");

        System.out.printf("MP1 glass WITH blur: %.3f seconds%n", result.seconds());
        System.out.printf("MP1 glass WITH blur stats: %s%n", result.stats().summary());
        if (lastWithoutBlurSeconds > 0d) {
            System.out.printf("Performance multiplier: %.2fx%n", result.seconds() / lastWithoutBlurSeconds);
        }
    }

    /**
     * Benchmarks course threading modes on the same preview scene.
     */
    @Test
    @Order(4)
    void testPreviewThreadingBenchmarkModes() {
        for (int mode : new int[]{0, -1, -2}) {
            RenderResult result = renderGlassScene(true, RenderProfile.PREVIEW.withThreads(mode), null);
            System.out.printf("MP1 threading mode %d: %.3f seconds; %s%n",
                    mode, result.seconds(), result.stats().summary());
        }
    }

    /**
     * Small visual comparison for the anti-aliasing bonus.
     */
    @Test
    @Order(5)
    void testAntiAliasingComparison() {
        Scene scene = createAntiAliasingScene();

        renderAntiAliasingScene(scene, false, "mp1_aa_off");
        renderAntiAliasingScene(scene, true, "mp1_aa_on");
    }

    /**
     * Creates the shared dark-blue glass demo scene.
     */
    private static SceneBuild createGlassDemoScene() {
        Scene scene = new Scene("MP1 dark-blue celestial glass garden")
                .setBackground(new Color(1, 4, 14))
                .setAmbientLight(new AmbientLight(new Color(3, 5, 10)));

        List<Intersectable> geometries = new ArrayList<>();
        addGeometry(geometries, new Plane(new Point(0, -50, 0), Vector.AXIS_Y)
                .setEmission(new Color(6, 20, 48))
                .setMaterial(new Material()
                        .setKD(0.30)
                        .setKS(0.30)
                        .setKR(0.12)
                        .setShininess(100)
                        .setGlossyBlur(2.5)));
        addGeometry(geometries, new Polygon(
                new Point(-260, -50, -380),
                new Point(260, -50, -380),
                new Point(260, 190, -380),
                new Point(-260, 190, -380))
                .setEmission(new Color(3, 10, 28))
                .setMaterial(new Material()
                        .setKD(0.28)
                        .setKS(0.14)
                        .setShininess(70)));

        for (int i = 0; i < 6; i++) {
            double x = -52 + i * 20d;
            double z = -328 - (i % 2) * 4d;
            addGeometry(geometries, new Polygon(
                    new Point(x, -44, z),
                    new Point(x + 5, -44, z),
                    new Point(x + 5, 70, z),
                    new Point(x, 70, z))
                    .setEmission(i % 2 == 0 ? new Color(4, 55, 100) : new Color(8, 75, 120))
                    .setMaterial(new Material()
                            .setKD(0.24)
                            .setKS(0.12)
                            .setShininess(45)));
        }

        addSpiralGardenSpheres(geometries);

        addGeometry(geometries, new Sphere(new Point(-82, -35, -205), 15)
                .setEmission(new Color(4, 14, 36))
                .setMaterial(new Material()
                        .setKD(0.08)
                        .setKS(0.70)
                        .setKR(0.30)
                        .setShininess(220)
                        .setGlossyBlur(2.2)));
        addGeometry(geometries, new Sphere(new Point(78, -37, -255), 13)
                .setEmission(new Color(5, 18, 44))
                .setMaterial(new Material()
                        .setKD(0.09)
                        .setKS(0.65)
                        .setKR(0.24)
                        .setShininess(190)
                        .setGlossyBlur(2.8)));
        addGeometry(geometries, new Sphere(new Point(0, -41, -305), 9)
                .setEmission(new Color(6, 22, 50))
                .setMaterial(new Material()
                        .setKD(0.10)
                        .setKS(0.55)
                        .setKR(0.20)
                        .setShininess(160)
                        .setGlossyBlur(1.8)));

        addGeometry(geometries, new Polygon(
                new Point(78, -48, -248),
                new Point(120, -48, -248),
                new Point(120, 58, -248),
                new Point(78, 58, -248))
                .setEmission(new Color(2, 12, 24))
                .setMaterial(new Material()
                        .setKD(0.04)
                        .setKS(0.65)
                        .setKT(0.62)
                        .setKR(0.08)
                        .setShininess(280)
                        .setRefractiveIndex(1.30)
                        .setTransparencyBlur(3.2)));

        addGeometry(geometries, new Sphere(new Point(0, 0, -180), 50)
                .setEmission(new Color(1, 5, 12))
                .setMaterial(new Material()
                        .setKD(0.02)
                        .setKS(0.90)
                        .setKT(0.80)
                        .setKR(0.14)
                        .setShininess(450)
                        .setRefractiveIndex(1.50)
                        .setTransparencyBlur(0.8)));

        scene.geometries.add(geometries.toArray(new Intersectable[0]));

        List<Point> lightPositions = List.of(
                new Point(-120, 160, 85),
                new Point(145, 82, -310),
                new Point(95, 45, 120));
        scene.lights.add(new SpotLight(new Color(620, 690, 850), lightPositions.get(0),
                new Vector(120, -160, -300))
                .setKl(0.00065)
                .setKq(0.0000022));
        scene.lights.add(new SpotLight(new Color(160, 290, 520), lightPositions.get(1),
                new Vector(-145, -82, 125))
                .setKl(0.0008)
                .setKq(0.0000026));
        scene.lights.add(new SpotLight(new Color(95, 125, 175), lightPositions.get(2),
                new Vector(-95, -45, -310))
                .setKl(0.0010)
                .setKq(0.0000035));

        return new SceneBuild(scene, geometries.size(), lightPositions);
    }

    /**
     * Adds many inexpensive opaque spheres so the glass has rich geometry to distort.
     */
    private static void addSpiralGardenSpheres(List<Intersectable> geometries) {
        Color[] colors = {
                new Color(8, 44, 86),
                new Color(10, 68, 110),
                new Color(24, 36, 100),
                new Color(32, 22, 78),
                new Color(4, 82, 112)
        };

        for (int i = 0; i < 96; i++) {
            double angle = i * 0.47d;
            double radiusFromCenter = Math.min(145d, 22d + i * 1.35d);
            double x = Math.cos(angle) * radiusFromCenter;
            double z = -120d - i * 2.1d + Math.sin(angle) * 18d;
            double sphereRadius = 3.0d + (i % 5) * 0.65d;
            double y = -50d + sphereRadius;

            addGeometry(geometries, new Sphere(new Point(x, y, z), sphereRadius)
                    .setEmission(colors[i % colors.length])
                    .setMaterial(new Material()
                            .setKD(0.48)
                            .setKS(0.20)
                            .setShininess(60 + (i % 4) * 20)));
        }
    }

    /**
     * Adds one geometry to the scene list.
     */
    private static void addGeometry(List<Intersectable> geometries, Intersectable geometry) {
        geometries.add(geometry);
    }

    /**
     * Renders the shared glass scene with the requested blur setting.
     */
    private static RenderResult renderGlassScene(boolean blurEnabled, RenderProfile profile, String fileName) {
        SceneBuild build = createGlassDemoScene();
        assertTrue(build.objectCount() >= 100, "MP1 scene object count regressed");

        RenderStats stats = new RenderStats();
        Camera camera = glassCameraBuilder(build.scene(), profile, blurEnabled, stats).build();

        long start = System.nanoTime();
        camera.renderImage();
        long nanos = System.nanoTime() - start;

        if (fileName != null) {
            camera.writeToImage(fileName);
        }
        return new RenderResult(nanos / 1_000_000_000d, stats, build.objectCount(), build.lightPositions());
    }

    /**
     * Creates the shared glass demo camera configuration.
     */
    private static Camera.Builder glassCameraBuilder(Scene scene, RenderProfile profile,
                                                     boolean blurEnabled, RenderStats stats) {
        Camera.Builder builder = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 42, 430))
                .setDirection(new Point(0, -5, -225), Vector.AXIS_Y)
                .setVpDistance(430)
                .setVpSize(230, 230)
                .setResolution(profile.resolution(), profile.resolution())
                .setBlurEnabled(blurEnabled)
                .setBlurSamples(profile.blurSamples())
                .setBlurSamplingPattern(SamplingPattern.JITTERED)
                .setBlurSamplingShape(SamplingShape.CIRCLE)
                .setBlurSamplingSeed(MP1_SEED)
                .setBlurTargetDistance(100)
                .setSamplingPattern(SamplingPattern.JITTERED)
                .setSamplingShape(SamplingShape.CIRCLE)
                .setSamplingSeed(MP1_SEED)
                .setMultithreading(profile.threads())
                .setRenderStats(stats);

        if (profile.cameraSamples() > 1) {
            builder.setSuperSampling(profile.cameraSamples());
        } else {
            builder.setSuperSampling(false);
        }
        return builder;
    }

    /**
     * Creates a small scene for AA-on versus AA-off comparison.
     */
    private static Scene createAntiAliasingScene() {
        Scene scene = new Scene("MP1 anti-aliasing comparison")
                .setBackground(new Color(2, 5, 15))
                .setAmbientLight(new AmbientLight(new Color(8, 8, 12)));

        scene.geometries.add(
                new Polygon(
                        new Point(-80, -45, -180),
                        new Point(80, -45, -180),
                        new Point(80, 70, -180),
                        new Point(-80, 70, -180))
                        .setEmission(new Color(6, 18, 46))
                        .setMaterial(new Material().setKD(0.45).setKS(0.20).setShininess(80)),
                new Polygon(
                        new Point(-55, -35, -150),
                        new Point(45, 55, -150),
                        new Point(60, -40, -150))
                        .setEmission(new Color(8, 90, 135))
                        .setMaterial(new Material().setKD(0.40).setKS(0.25).setShininess(90)),
                new Sphere(new Point(22, -8, -110), 24)
                        .setEmission(new Color(24, 30, 95))
                        .setMaterial(new Material().setKD(0.30).setKS(0.55).setShininess(140))
        );
        scene.lights.add(new SpotLight(new Color(280, 330, 460), new Point(-70, 90, 80),
                new Vector(70, -90, -250))
                .setKl(0.0008)
                .setKq(0.000003));
        return scene;
    }

    /**
     * Renders the anti-aliasing comparison scene.
     */
    private static void renderAntiAliasingScene(Scene scene, boolean aaEnabled, String fileName) {
        Camera.Builder builder = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 10, 300))
                .setDirection(new Point(0, 0, -150), Vector.AXIS_Y)
                .setVpDistance(300)
                .setVpSize(150, 150)
                .setResolution(140, 140)
                .setBlurEnabled(false)
                .setMultithreading(-2);
        if (aaEnabled) {
            builder.setSuperSampling(9)
                    .setSamplingPattern(SamplingPattern.JITTERED)
                    .setSamplingShape(SamplingShape.CIRCLE)
                    .setSamplingSeed(MP1_SEED);
        }

        builder.build()
                .renderImage()
                .writeToImage(fileName);
    }

    /**
     * Shared scene construction result.
     */
    private record SceneBuild(Scene scene, int objectCount, List<Point> lightPositions) {
    }

    /**
     * Test-level quality profiles. The final profile is defined for manual use,
     * but preview/demo profiles are used for routine tests and benchmarking.
     */
    private record RenderProfile(String name, int resolution, int cameraSamples, int blurSamples, int threads) {
        /** Low-cost development profile. */
        private static final RenderProfile PREVIEW = new RenderProfile("PREVIEW", 160, 1, 4, -2);
        /** Medium demonstration profile. */
        @SuppressWarnings("unused")
        private static final RenderProfile DEMO = new RenderProfile("DEMO", 320, 1, 16, -2);
        /** Final-quality profile for a separate manual render after review. */
        @SuppressWarnings("unused")
        private static final RenderProfile FINAL = new RenderProfile("FINAL", 500, 1, 25, -2);

        /**
         * Returns this profile with a different threading mode.
         */
        private RenderProfile withThreads(int newThreads) {
            return new RenderProfile(name, resolution, cameraSamples, blurSamples, newThreads);
        }
    }

    /**
     * Render measurement result.
     */
    private record RenderResult(double seconds, RenderStats stats, int objectCount, List<Point> lightPositions) {
    }
}
