package renderer;

import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Per-feature with/without comparison tests on one rich scene.
 */
@SuppressWarnings("java:S109")
class MiniProjectFeatureComparisonTests {

    private enum Feature {
        ANTI_ALIASING,
        DEPTH_OF_FIELD,
        SOFT_SHADOWS,
        GLOSSY,
        DIFFUSE_GLASS
    }

    @Test
    void renderAntiAliasingOff() {
        renderFeature(Feature.ANTI_ALIASING, false, "mp_aa_off");
    }

    @Test
    void renderAntiAliasingOn() {
        renderFeature(Feature.ANTI_ALIASING, true, "mp_aa_on");
    }

    @Test
    void renderDepthOfFieldOff() {
        renderFeature(Feature.DEPTH_OF_FIELD, false, "mp_dof_off");
    }

    @Test
    void renderDepthOfFieldOn() {
        renderFeature(Feature.DEPTH_OF_FIELD, true, "mp_dof_on");
    }

    @Test
    void renderSoftShadowsOff() {
        renderFeature(Feature.SOFT_SHADOWS, false, "mp_soft_shadows_off");
    }

    @Test
    void renderSoftShadowsOn() {
        renderFeature(Feature.SOFT_SHADOWS, true, "mp_soft_shadows_on");
    }

    @Test
    void renderGlossyOff() {
        renderFeature(Feature.GLOSSY, false, "mp_glossy_off");
    }

    @Test
    void renderGlossyOn() {
        renderFeature(Feature.GLOSSY, true, "mp_glossy_on");
    }

    @Test
    void renderDiffuseGlassOff() {
        renderFeature(Feature.DIFFUSE_GLASS, false, "mp_diffuse_glass_feature_off");
    }

    @Test
    void renderDiffuseGlassOn() {
        renderFeature(Feature.DIFFUSE_GLASS, true, "mp_diffuse_glass_feature_on");
    }

    private void renderFeature(Feature feature, boolean enabled, String outputName) {
        Scene scene = buildScene(feature, enabled);
        Camera.Builder builder = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 10, 930))
                .setDirection(new Point(0, -15, -250), Vector.AXIS_Y)
                .setVpDistance(900)
                .setVpSize(300, 300)
                .setResolution(180, 180)
                .setMultithreading(-2)
                .setDebugPrint(0);

        if (feature == Feature.ANTI_ALIASING && enabled) {
            builder.setAntiAliasing(9, Blackboard.Shape.SQUARE, Blackboard.Pattern.JITTER, 0.5);
        }
        if (feature == Feature.DEPTH_OF_FIELD && enabled) {
            builder.setDepthOfField(1.2, 900, 9, Blackboard.Shape.CIRCLE, Blackboard.Pattern.JITTER);
        }

        long start = System.nanoTime();
        builder.build().renderImage().writeToImage(outputName);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println(feature + " " + (enabled ? "ON" : "OFF") + " render time: " + elapsedMs + " ms");
    }

    private Scene buildScene(Feature feature, boolean enabled) {
        Scene scene = new Scene("Mini project feature comparison")
                .setBackground(new Color(14, 15, 21))
                .setAmbientLight(new AmbientLight(new Color(20, 20, 22)));

        Material floor = new Material()
                .setKD(0.5).setKS(0.15).setShininess(70)
                .setKR(0.35);
        if (feature == Feature.GLOSSY && enabled) {
            floor.setGlossy(1.0, 5);
        }

        Material glass = new Material()
                .setKD(0.25).setKS(0.45).setShininess(180)
                .setKT(0.75);
        if (feature == Feature.DIFFUSE_GLASS && enabled) {
            glass.setDiffuseGlass(1.8, 5);
        }

        scene.geometries.add(
                new Plane(new Point(0, -115, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(35, 35, 39))
                        .setMaterial(floor),
                new Sphere(new Point(-20, -35, -200), 50)
                        .setEmission(new Color(50, 100, 190))
                        .setMaterial(glass),
                new Sphere(new Point(-118, -56, -340), 18).setEmission(new Color(200, 52, 52))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(45)),
                new Sphere(new Point(-78, -58, -330), 18).setEmission(new Color(210, 150, 40))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(45)),
                new Sphere(new Point(-38, -58, -320), 18).setEmission(new Color(215, 215, 55))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(45)),
                new Sphere(new Point(2, -58, -310), 18).setEmission(new Color(65, 175, 78))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(45)),
                new Sphere(new Point(42, -58, -320), 18).setEmission(new Color(62, 120, 200))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(45)),
                new Sphere(new Point(82, -56, -340), 18).setEmission(new Color(170, 72, 180))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(45)),
                new Triangle(new Point(-150, -115, -390), new Point(-30, -115, -250), new Point(-120, 36, -350))
                        .setEmission(new Color(28, 150, 130))
                        .setMaterial(new Material().setKD(0.5).setKS(0.35).setShininess(120)),
                new Triangle(new Point(145, -115, -250), new Point(25, -115, -390), new Point(105, 38, -340))
                        .setEmission(new Color(145, 90, 40))
                        .setMaterial(new Material().setKD(0.5).setKS(0.35).setShininess(120)),
                new Triangle(new Point(-45, -100, -255), new Point(45, -100, -250), new Point(0, -15, -210))
                        .setEmission(new Color(95, 60, 165))
                        .setMaterial(new Material().setKD(0.5).setKS(0.45).setShininess(130))
        );

        SpotLight key = new SpotLight(new Color(700, 420, 250), new Point(-180, 180, 140), new Vector(1, -1.2, -2))
                .setKl(0.00045).setKq(0.000001);
        PointLight fill = new PointLight(new Color(280, 320, 620), new Point(165, 28, 80))
                .setKl(0.0007).setKq(0.000002);
        PointLight rim = new PointLight(new Color(220, 120, 100), new Point(0, 85, -20))
                .setKl(0.0009).setKq(0.000004);

        if (feature == Feature.SOFT_SHADOWS && enabled) {
            key.setSoftShadows(12, 9, Blackboard.Shape.CIRCLE, Blackboard.Pattern.JITTER);
            fill.setSoftShadows(10, 9, Blackboard.Shape.CIRCLE, Blackboard.Pattern.JITTER);
            rim.setSoftShadows(7, 9, Blackboard.Shape.CIRCLE, Blackboard.Pattern.JITTER);
        }

        scene.lights.add(key);
        scene.lights.add(fill);
        scene.lights.add(rim);

        return scene;
    }
}