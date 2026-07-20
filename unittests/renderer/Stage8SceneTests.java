package renderer;

import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Own Stage 8 scene combining shadows, transparency and reflection.
 */
@SuppressWarnings("java:S109")
class Stage8SceneTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    Stage8SceneTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Renders a compact scene with 4 geometries and all mandatory Stage 8 effects.
     */
    @org.junit.jupiter.api.Test
    void testStage8CombinedEffectsScene() {
        Scene scene = new Scene("Stage 8 combined effects")
                .setBackground(new Color(15, 18, 22))
                .setAmbientLight(new AmbientLight(new Color(8, 8, 10)));

        //.setAmbientLight(new AmbientLight(new Color(20, 20, 24)));

        scene.geometries.add(
                new Plane(new Point(0, -80, -250), new Vector(0, 1, 0))
                        //  .setEmission(new Color(35, 38, 42))
                        .setEmission(new Color(18, 20, 22))
                        .setMaterial(new Material().setKD(0.45).setKS(0.25).setKR(0.2).setShininess(120)),//KR = 0.35
                new Sphere(new Point(-45, -35, -180), 38)
                        //.setEmission(new Color(30, 70, 170))
                        .setEmission(new Color(15, 35, 90))
                        .setMaterial(new Material().setKD(0.35).setKS(0.45).setKT(0.25).setShininess(160)),//KT = 0.45
                new Sphere(new Point(45, -45, -210), 32)
                        // .setEmission(new Color(170, 45, 35))
                        .setEmission(new Color(90, 25, 18))
                        .setMaterial(new Material().setKD(0.45).setKS(0.35).setShininess(90)),
                new Triangle(new Point(-90, -80, -160), new Point(90, -80, -165), new Point(0, 70, -210))
                        //.setEmission(new Color(20, 110, 70))
                        .setEmission(new Color(10, 55, 35))
                        .setMaterial(new Material().setKD(0.35).setKS(0.45).setKR(0.25).setShininess(180))
        );

        scene.lights.add(new SpotLight(new Color(350, 250, 150), new Point(-120, 120, 120), new Vector(1, -1, -2))//new Color(700, 450, 260), new Point(-120, 120, 120), new Vector(1, -1, -2))
                .setKl(0.0005).setKq(0.000001));
        scene.lights.add(new PointLight(new Color(90, 130, 300), new Point(95, 35, 80))//new Color(160, 220, 500), new Point(95, 35, 80))
                .setKl(0.0007).setKq(0.000002));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 900))
                .setDirection(new Point(0, -20, -200), Vector.AXIS_Y)
                .setVpDistance(900)
                .setVpSize(220, 220)
                .setResolution(500, 500)
                .build()
                .renderImage()
                .writeToImage("stage8CombinedEffects");
    }
}
