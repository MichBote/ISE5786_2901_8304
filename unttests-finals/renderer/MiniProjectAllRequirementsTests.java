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
 * Mandatory mini-project verification: same rich scene rendered with and without
 * beam-based improvements and with measured runtime.
 */
@SuppressWarnings("java:S109")
class MiniProjectAllRequirementsTests {
    /**
     * Creates the all-requirements mini-project test fixture.
     */
    MiniProjectAllRequirementsTests() {
    }

    /**
     * Renders the requirements scene with improvements disabled.
     */
    @Test
    void renderRequirementsSceneWithoutImprovements() {
        Scene scene = buildScene(false);
        long elapsedMs = render(scene, false, "mp_requirements_off");
        System.out.println("Requirements OFF render time: " + elapsedMs + " ms");
    }

    /**
     * Renders the requirements scene with improvements enabled.
     */
    @Test
    void renderRequirementsSceneWithImprovements() {
        Scene scene = buildScene(true);
        long elapsedMs = render(scene, true, "mp_requirements_on");
        System.out.println("Requirements ON render time: " + elapsedMs + " ms");
    }

    /**
     * Renders the requirements scene and writes the resulting image.
     *
     * @param scene scene to render
     * @param enableImprovements true to enable camera improvements
     * @param outputName output image name
     * @return elapsed render time in milliseconds
     */
    private long render(Scene scene, boolean enableImprovements, String outputName) {
        Camera.Builder builder = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 10, 930))
                .setDirection(new Point(0, -15, -250), Vector.AXIS_Y)
                .setVpDistance(900)
                .setVpSize(300, 300)
                                .setResolution(160, 160)
                .setMultithreading(-2)
                .setDebugPrint(0);

        if (enableImprovements) {
                        builder.setAntiAliasing(4, Blackboard.Shape.SQUARE, Blackboard.Pattern.JITTER, 0.5)
                                        .setDepthOfField(1.0, 900, 4, Blackboard.Shape.CIRCLE, Blackboard.Pattern.JITTER);
        }

        Camera camera = builder.build();
        long start = System.nanoTime();
        camera.renderImage().writeToImage(outputName);
        return (System.nanoTime() - start) / 1_000_000;
    }

    /**
     * Builds the requirements scene.
     *
     * @param enableImprovements true to enable material and light improvements
     * @return configured scene
     */
    private Scene buildScene(boolean enableImprovements) {
        Scene scene = new Scene("Mini project all requirements")
                .setBackground(new Color(14, 15, 21))
                .setAmbientLight(new AmbientLight(new Color(20, 20, 22)));

        Material floor = new Material()
                .setKD(0.5).setKS(0.15).setShininess(70)
                .setKR(0.35);
        if (enableImprovements) {
                        floor.setGlossy(1.0, 3);
        }

        Material glass = new Material()
                .setKD(0.25).setKS(0.45).setShininess(180)
                .setKT(0.75);
        if (enableImprovements) {
                        glass.setDiffuseGlass(1.8, 3);
        }

        scene.geometries.add(
                // 1
                new Plane(new Point(0, -115, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(35, 35, 39))
                        .setMaterial(floor),

                // 2
                new Sphere(new Point(-20, -35, -200), 50)
                        .setEmission(new Color(50, 100, 190))
                        .setMaterial(glass),

                // 3-8: six spheres behind the glass
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

                // 9-11: triangles for edge quality and blur/AA visibility
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

        if (enableImprovements) {
                        key.setSoftShadows(12, 4, Blackboard.Shape.CIRCLE, Blackboard.Pattern.JITTER);
                        fill.setSoftShadows(10, 4, Blackboard.Shape.CIRCLE, Blackboard.Pattern.JITTER);
                        rim.setSoftShadows(7, 4, Blackboard.Shape.CIRCLE, Blackboard.Pattern.JITTER);
        }

        scene.lights.add(key);
        scene.lights.add(fill);
        scene.lights.add(rim);

        return scene;
    }
}
