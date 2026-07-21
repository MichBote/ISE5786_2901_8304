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
 * Mini-project validation test for diffuse glass: render same 10+ body scene
 * once without blur and once with blur.
 */
@SuppressWarnings("java:S109")
class DiffuseGlassMiniProjectTests {
    /**
     * Creates the diffuse-glass mini-project test fixture.
     */
    DiffuseGlassMiniProjectTests() {
    }

    /**
     * Renders the 10+ body scene without diffuse-glass blur.
     */
    @Test
    void renderTenBodiesWithoutDiffuseGlass() {
        Scene scene = buildScene(false);
        long ms = renderScene(scene, "mp1_diffuse_glass_off");
        System.out.println("Diffuse glass OFF render time: " + ms + " ms");
    }

    /**
     * Renders the 10+ body scene with diffuse-glass blur.
     */
    @Test
    void renderTenBodiesWithDiffuseGlass() {
        Scene scene = buildScene(true);
        long ms = renderScene(scene, "mp1_diffuse_glass_on");
        System.out.println("Diffuse glass ON render time: " + ms + " ms");
    }

    /**
     * Renders a scene and writes the resulting image.
     *
     * @param scene      scene to render
     * @param outputName output image name
     * @return elapsed render time in milliseconds
     */
    private long renderScene(Scene scene, String outputName) {
        Camera camera = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 15, 900))
                .setDirection(new Point(0, -20, -200), Vector.AXIS_Y)
                .setVpDistance(900)
                .setVpSize(280, 280)
                .setResolution(500, 500)
                .setMultithreading(-2)
                .setDebugPrint(0)
                .build();

        long start = System.nanoTime();
        camera.renderImage().writeToImage(outputName);
        return (System.nanoTime() - start) / 1_000_000;
    }

    /**
     * Builds the diffuse-glass comparison scene.
     *
     * @param withDiffuseGlass true to enable diffuse-glass blur on the main sphere
     * @return configured scene
     */
    private Scene buildScene(boolean withDiffuseGlass) {
        Scene scene = new Scene("Mini project diffuse glass")
                .setBackground(new Color(14, 16, 22))
                .setAmbientLight(new AmbientLight(new Color(26, 26, 28)));

        Material floorMaterial = new Material()
                .setKD(0.55).setKS(0.2).setShininess(60)
                .setKR(0.25);

        Material glassMaterial = new Material()
                .setKD(0.2).setKS(0.5).setShininess(180)
                .setKT(0.75);

        if (withDiffuseGlass) {
            glassMaterial.setDiffuseGlass(3.0, 81);
        }

        scene.geometries.add(
                new Plane(new Point(0, -110, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(34, 35, 38))
                        .setMaterial(floorMaterial),

                // 1) Main diffuse glass sphere
                new Sphere(new Point(-20, -40, -180), 50)
                        .setEmission(new Color(45, 90, 170))
                        .setMaterial(glassMaterial),

                // 2-7) Six colored spheres behind glass
                new Sphere(new Point(-95, -58, -305), 18)
                        .setEmission(new Color(190, 40, 40))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(40)),
                new Sphere(new Point(-55, -62, -305), 18)
                        .setEmission(new Color(195, 135, 45))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(40)),
                new Sphere(new Point(-15, -62, -305), 18)
                        .setEmission(new Color(180, 180, 55))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(40)),
                new Sphere(new Point(25, -62, -305), 18)
                        .setEmission(new Color(70, 170, 80))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(40)),
                new Sphere(new Point(65, -62, -305), 18)
                        .setEmission(new Color(70, 130, 195))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(40)),
                new Sphere(new Point(105, -58, -305), 18)
                        .setEmission(new Color(160, 80, 170))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(40)),

                // 8-10) Three triangles for sharp edges in blur comparison
                new Triangle(new Point(-145, -110, -360), new Point(-40, -110, -250), new Point(-120, 20, -330))
                        .setEmission(new Color(32, 145, 125))
                        .setMaterial(new Material().setKD(0.5).setKS(0.3).setShininess(80)),
                new Triangle(new Point(130, -110, -260), new Point(35, -110, -365), new Point(105, 35, -325))
                        .setEmission(new Color(140, 85, 35))
                        .setMaterial(new Material().setKD(0.5).setKS(0.35).setShininess(110)),
                new Triangle(new Point(-30, -95, -250), new Point(40, -95, -245), new Point(0, -15, -210))
                        .setEmission(new Color(95, 60, 160))
                        .setMaterial(new Material().setKD(0.5).setKS(0.45).setShininess(130))
        );

        scene.lights.add(new SpotLight(new Color(700, 420, 260), new Point(-160, 170, 130), new Vector(1, -1.2, -2))
                .setKl(0.00045).setKq(0.000001));
        scene.lights.add(new PointLight(new Color(260, 320, 620), new Point(155, 30, 90))
                .setKl(0.0007).setKq(0.000002));
        scene.lights.add(new PointLight(new Color(220, 100, 90), new Point(0, 80, -40))
                .setKl(0.0009).setKq(0.000004));

        return scene;
    }
}
