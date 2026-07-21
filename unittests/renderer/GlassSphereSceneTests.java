package renderer;

import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Render tests for glass sphere scenes.
 */
@SuppressWarnings("java:S109")
class GlassSphereSceneTests {
    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    GlassSphereSceneTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Produces a picture of a large turquoise-blue glass sphere on a white/gray floor.
     */
    //@Test
//    void testLargeTurquoiseGlassSphereOnWhiteGrayFloor() {
//        Scene scene = new Scene("Large turquoise glass sphere")
//                .setBackground(new Color(150, 170, 190))
//                .setAmbientLight(new AmbientLight(new Color(15, 18, 20)));
//
//        scene.geometries.add(
//                new Plane(new Point(0, -80, 0), new Vector(0, 1, 0))
//                        .setEmission(new Color(170, 170, 170))
//                        .setMaterial(new Material()
//                                .setKD(0.45)
//                                .setKS(0.25)
//                                .setKR(0.15)
//                                .setShininess(80)),
//                new Sphere(new Point(0, -5, -210), 75)
//                        .setEmission(Color.BLACK)
//                        //    .setEmission(new Color(95, 7, 10))
//                        .setMaterial(new Material()
//                                .setKD(0.16)
//                                .setKS(0.88)
//                                .setKR(0.18)
//                                .setShininess(320))
//        );
//
//        scene.lights.add(new SpotLight(new Color(300, 300, 180), new Point(-130, 170, 150),
//                new Vector(1.0, -1.5, -2.2))
//                .setKl(0.00035)
//                .setKq(0.000001));
//     /*   scene.lights.add(new PointLight(new Color(80, 140, 180), new Point(120, 60, 30))
//                .setKl(0.0007)
//                .setKq(0.000002));
//     */
//     /*   scene.lights.add(new PointLight(new Color(80, 140, 180), new Point(-35, 55, 70))
//
//                .setKl(0.0009)
//                .setKq(0.000003));
//*/
//        Camera.getBuilder()
//                .setRayTracer(scene, RayTracerType.SIMPLE)
//                .setLocation(new Point(0, 35, 520))
//                .setDirection(new Point(0, -20, -210), Vector.AXIS_Y)
//                .setVpDistance(520)
//                .setVpSize(240, 240)
//                .setResolution(300, 300)
//                .setBlurEnabled(false)
//                .build()
//                .renderImage()
//                .writeToImage("largeTurquoiseGlassSphere");
//    }
//
//    /**
    /* Produces a dark-blue scene with a glass sphere resting on a blue plane and soft lighting.
     */
    @Test
    void testDarkBlueGlassSphereOnBluePlane() {
        Scene scene = new Scene("Dark blue glass sphere on blue plane")
                .setBackground(new Color(1, 4, 14))
                .setAmbientLight(new AmbientLight(new Color(3, 5, 10)));

        scene.geometries.add(
                new Plane(new Point(0, -50, 0), Vector.AXIS_Y)
                        .setEmission(new Color(6, 20, 48))
                        .setMaterial(new Material()
                                .setKD(0.30)
                                .setKS(0.30)
                                .setKR(0.12)
                                .setShininess(100)
                                .setGlossyBlur(2.5)),
                new Polygon(
                        new Point(-230, -50, -360),
                        new Point(230, -50, -360),
                        new Point(230, 175, -360),
                        new Point(-230, 175, -360))
                        .setEmission(new Color(3, 10, 28))
                        .setMaterial(new Material()
                                .setKD(0.24)
                                .setKS(0.18)
                                .setShininess(70)),
                new Polygon(
                        new Point(-26, -42, -320),
                        new Point(-17, -42, -320),
                        new Point(-17, 62, -320),
                        new Point(-26, 62, -320))
                        .setEmission(new Color(5, 42, 80))
                        .setMaterial(new Material()
                                .setKD(0.22)
                                .setKS(0.12)
                                .setShininess(45)),
                new Polygon(
                        new Point(17, -42, -318),
                        new Point(27, -42, -318),
                        new Point(27, 66, -318),
                        new Point(17, 66, -318))
                        .setEmission(new Color(8, 62, 100))
                        .setMaterial(new Material()
                                .setKD(0.22)
                                .setKS(0.12)
                                .setShininess(45)),
                new Sphere(new Point(0, 0, -180), 50)
                        .setEmission(new Color(1, 5, 12))
                        .setMaterial(new Material()
                                .setKD(0.02)
                                .setKS(0.90)
                                .setKT(0.80)
                                .setKR(0.14)
                                .setShininess(450)
                                .setRefractiveIndex(1.50)
                                .setTransparencyBlur(0.8))
        );

        scene.lights.add(new SpotLight(new Color(650, 700, 850), new Point(-110, 140, 80),
                new Vector(110, -140, -260))
                .setKl(0.00065)
                .setKq(0.000002));
        scene.lights.add(new SpotLight(new Color(180, 300, 520), new Point(130, 70, -260),
                new Vector(-130, -70, 80))
                .setKl(0.00075)
                .setKq(0.0000024));
        scene.lights.add(new SpotLight(new Color(100, 130, 180), new Point(90, 35, 100),
                new Vector(-90, -35, -280))
                .setKl(0.001)
                .setKq(0.0000035));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 28, 430))
                .setDirection(new Point(0, -2, -180), Vector.AXIS_Y)
                .setVpDistance(430)
                .setVpSize(190, 190)
                .setResolution(180, 180)
                .setBlurEnabled(true)
                .setBlurSamples(4)
                .setBlurSamplingPattern(SamplingPattern.JITTERED)
                .setBlurSamplingShape(SamplingShape.CIRCLE)
                .setBlurSamplingSeed(2026L)
                .setBlurTargetDistance(100)
                .setSuperSampling(false)
                .setSamplingPattern(SamplingPattern.JITTERED)
                .setSamplingShape(SamplingShape.CIRCLE)
                .setSamplingSeed(2026L)
                .setMultithreading(-2)
                .build()
                .renderImage()
                .writeToImage("realisticDarkBlueGlassSpherePreview");
    }
}
