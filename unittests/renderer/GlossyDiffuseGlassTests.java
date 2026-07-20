/// **package renderer;
//
//import geometries.impl.Plane;
//import geometries.impl.Polygon;
//import geometries.impl.Sphere;
//import geometries.impl.Triangle;
//import lighting.AmbientLight;
//import lighting.PointLight;
//import lighting.SpotLight;
//import org.junit.jupiter.api.Test;
//import primitives.Color;
//import primitives.Material;
//import primitives.Point;
//import primitives.Vector;
//import scene.Scene;
//
///**
// * High-resolution demonstration scenes for glossy reflection and diffuse glass.
// */
//@SuppressWarnings("java:S109")
//class GlossyDiffuseGlassTests {
//    /**
//     * Output image resolution for the demonstration images.
//     */
//    private static final int RESOLUTION = 800;
//
//    /**
//     * Default constructor to satisfy JavaDoc generator.
//     */
//    GlossyDiffuseGlassTests() { /* to satisfy JavaDoc generator */ }
//
//    /**
//     * Renders three mirror panels with identical red spheres and increasing glossy blur.
//     */
//    @Test
//    void testGlossyMirrorComparison() {
//        Scene scene = new Scene("Glossy mirror comparison")
//                .setBackground(new Color(8, 16, 28))
//                .setAmbientLight(new AmbientLight(new Color(8, 8, 10)));
//
//        scene.geometries.add(
//                new Plane(new Point(0, -78, -170), Vector.AXIS_Y)
//                        .setEmission(new Color(10, 24, 32))
//                        .setMaterial(new Material().setKD(0.28).setKS(0.18).setShininess(80)),
//                mirrorPanel(-175, -58, 0),
//                mirrorPanel(-56, 56, 5),
//                mirrorPanel(58, 175, 10)
//        );
//
//        addGlossySphereGroup(scene, -116);
//        addGlossySphereGroup(scene, 0);
//        addGlossySphereGroup(scene, 116);
//        addGlossyLights(scene);
//
//        Camera.getBuilder()
//                .setRayTracer(scene, RayTracerType.SIMPLE)
//                .setLocation(new Point(0, 18, 370))
//                .setDirection(new Point(0, -55, -140), Vector.AXIS_Y)
//                .setVpDistance(360)
//                .setVpSize(270, 270)
//                .setResolution(RESOLUTION, RESOLUTION)
//                .setBlurSamples(9)
//                .setBlurTargetDistance(100)
//                .setMultithreading(-2)
//                .build()
//                .renderImage()
//                .writeToImage("glossyMirrorComparison");
//    }
//
//    /**
//     * Renders side-by-side glass panels with increasing diffuse blur.
//     */
//    @Test
//    void testDiffuseGlassComparison() {
//        Scene scene = new Scene("Diffuse glass comparison")
//                .setBackground(new Color(120, 170, 210))
//                .setAmbientLight(new AmbientLight(new Color(14, 16, 18)));
//
//        scene.geometries.add(
//                new Plane(new Point(0, -80, -220), Vector.AXIS_Y)
//                        .setEmission(new Color(28, 52, 64))
//                        .setMaterial(new Material().setKD(0.18).setKS(0.04).setShininess(45)),
//                glassPanel(-180, -62, 0),
//                glassPanel(-58, 58, 3),
//                glassPanel(62, 180, 7)
//        );
//
//        addGlassObjects(scene, -120);
//        addGlassObjects(scene, 0);
//        addGlassObjects(scene, 120);
//        addGlassLights(scene);
//
//        Camera.getBuilder()
//                .setRayTracer(scene, RayTracerType.SIMPLE)
//                .setLocation(new Point(0, 0, 410))
//                .setDirection(new Point(0, -8, -185), Vector.AXIS_Y)
//                .setVpDistance(410)
//                .setVpSize(300, 300)
//                .setResolution(RESOLUTION, RESOLUTION)
//                .setBlurSamples(9)
//                .setBlurTargetDistance(100)
//                .setMultithreading(-2)
//                .build()
//                .renderImage()
//                .writeToImage("diffuseGlassComparison");
//    }
//
//    /**
//     * Renders a single red glossy sphere above a blue reflective plane, matching
//     * the provided assignment-style reference image.
//     */
//    @Test
//    void testReferenceGlossySphereMirror() {
//        Scene scene = new Scene("Reference glossy sphere mirror")
//                .setBackground(new Color(5, 32, 55))
//                .setAmbientLight(new AmbientLight(new Color(10, 12, 14)));
//
//        scene.geometries.add(
//                new Plane(new Point(0, -78, -170), Vector.AXIS_Y)
//                        .setEmission(new Color(24, 92, 105))
//                        .setMaterial(new Material()
//                                .setKD(0.10)
//                                .setKS(0.28)
//                                .setKR(0.78)
//                                .setShininess(180)
//                                .setGlossyBlur(4)),
//                new Sphere(new Point(0, -6, -135), 54)
//                        .setEmission(new Color(90, 25, 30))
//                        .setMaterial(new Material()
//                                .setKD(0.08)
//                                .setKS(0.95)
//                                .setKT(0.18)
//                                .setKR(0.35)
//                                .setShininess(280)
//                                .setDiffuseBlur(0.2)
//                                .setGlossyBlur(0.2)),
//                new Sphere(new Point(18, 13, -86), 4.2)
//                        .setEmission(new Color(255, 245, 230))
//                        .setMaterial(new Material().setKD(0.12).setKS(0.35).setShininess(120))
//        );
//
//        scene.lights.add(new SpotLight(new Color(1050, 820, 640), new Point(95, 115, 75),
//                new Vector(-1.3, -1.2, -2.6)).setKl(0.00025).setKq(0.0000008).setNarrowBeam(3));
//        scene.lights.add(new PointLight(new Color(105, 170, 240), new Point(-150, 35, 100))
//                .setKl(0.0005).setKq(0.0000014));
//
//        Camera.getBuilder()
//                // Inside your Camera.getBuilder() chain in the test file:
//
//                .setRayTracer(scene, RayTracerType.SIMPLE)
//                .setLocation(new Point(0, 10, 410))
//                .setDirection(new Point(0, -25, -145), Vector.AXIS_Y)
//                .setVpDistance(410)
//                .setVpSize(260, 200)
//                .setResolution(700, 540)
//                .setSuperSampling(true)
//                .setSamples(2)
//                .setBlurSamples(5)
//                .setBlurTargetDistance(-2)
//                .setMultithreading(Runtime.getRuntime().availableProcessors())
//
//                //.setMultithreading(-2)
//
//                .build()
//                .renderImage()
//                .writeToImage("referenceGlossySphereMirror");
//    }
//**/
//    /**
//     * Creates one rectangular mirror panel on the floor.
//     *
//     * @param xMin minimum x coordinate
//     * @param xMax maximum x coordinate
//     * @param blur glossy blur size
//     * @return mirror panel
//     */
//    private Polygon mirrorPanel(double xMin, double xMax, double blur) {
//        return (Polygon) new Polygon(
//                new Point(xMin, -75, -250),
//                new Point(xMax, -75, -250),
//                new Point(xMax, -75, 20),
//                new Point(xMin, -75, 20))
//                .setEmission(new Color(8, 32, 38))
//                .setMaterial(new Material()
//                        .setKD(0.04)
//                        .setKS(0.35)
//                        .setKR(0.82)
//                        .setShininess(220)
//                        .setGlossyBlur(blur));
//    }
//
//    /**
//     * Adds a large colored sphere and highlight objects above one mirror panel.
//     *
//     * @param scene   scene to update
//     * @param xOffset group x offset
//     */
//    private void addGlossySphereGroup(Scene scene, double xOffset) {
//        scene.geometries.add(
//                new Sphere(new Point(xOffset, -4, -150), 42)
//                        .setEmission(new Color(105, 12, 14))
//                        .setMaterial(new Material().setKD(0.18).setKS(0.75).setShininess(260)),
//                new Sphere(new Point(xOffset + 13, 16, -116), 5)
//                        .setEmission(new Color(235, 220, 210))
//                        .setMaterial(new Material()),
//                new Sphere(new Point(xOffset - 34, -47, -215), 12)
//                        .setEmission(new Color(220, 72, 28))
//                        .setMaterial(new Material().setKD(0.22).setKS(0.28).setShininess(90)),
//                new Sphere(new Point(xOffset + 35, -45, -215), 12)
//                        .setEmission(new Color(35, 115, 220))
//                        .setMaterial(new Material().setKD(0.22).setKS(0.28).setShininess(90))
//        );
//    }
//
//    /**
//     * Creates one frosted glass panel.
//     *
//     * @param xMin minimum x coordinate
//     * @param xMax maximum x coordinate
//     * @param blur diffuse blur size
//     * @return glass panel
//     */
//    private Polygon glassPanel(double xMin, double xMax, double blur) {
//        return (Polygon) new Polygon(
//                new Point(xMin, -72, -142),
//                new Point(xMax, -72, -142),
//                new Point(xMax, 100, -142),
//                new Point(xMin, 100, -142))
//                .setEmission(new Color(8, 24, 28))
//                .setMaterial(new Material()
//                        .setKD(0.0)
//                        .setKS(0.08)
//                        .setKT(0.90)
//                        .setShininess(120)
//                        .setDiffuseBlur(blur));
//    }
//
//    /**
//     * Adds recognizable large objects behind one glass panel.
//     *
//     * @param scene   scene to update
//     * @param xOffset group x offset
//     */
//    private void addGlassObjects(Scene scene, double xOffset) {
//        scene.geometries.add(
//                new Sphere(new Point(xOffset - 32, -35, -235), 17)
//                        .setEmission(new Color(130, 28, 28))
//                        .setMaterial(new Material()),
//                new Sphere(new Point(xOffset + 32, -34, -235), 17)
//                        .setEmission(new Color(28, 65, 150))
//                        .setMaterial(new Material()),
//                new Sphere(new Point(xOffset, 32, -245), 20)
//                        .setEmission(new Color(155, 130, 30))
//                        .setMaterial(new Material()),
//                new Triangle(
//                        new Point(xOffset - 52, 72, -250),
//                        new Point(xOffset + 52, 72, -250),
//                        new Point(xOffset, 8, -250))
//                        .setEmission(new Color(25, 105, 52))
//                        .setMaterial(new Material()),
//                new Triangle(
//                        new Point(xOffset - 50, -70, -252),
//                        new Point(xOffset + 50, -70, -252),
//                        new Point(xOffset, -20, -252))
//                        .setEmission(new Color(105, 36, 112))
//                        .setMaterial(new Material())
//        );
//    }
//
//    /**
//     * Adds lights for the glossy scene.
//     *
//     * @param scene scene to light
//     */
//    private void addGlossyLights(Scene scene) {
//        scene.lights.add(new SpotLight(new Color(620, 420, 270), new Point(-120, 140, 110),
//                new Vector(1.0, -1.2, -2.2)).setKl(0.00035).setKq(0.000001));
//        scene.lights.add(new PointLight(new Color(120, 145, 210), new Point(130, 20, 120))
//                .setKl(0.0008).setKq(0.000002));
//    }
//
//    /**
//     * Adds lights for the glass scene.
//     *
//     * @param scene scene to light
//     */
//    private void addGlassLights(Scene scene) {
//        scene.lights.add(new SpotLight(new Color(120, 110, 90), new Point(-120, 135, 140),
//                new Vector(1.0, -1.1, -2.2)).setKl(0.00045).setKq(0.0000015));
//        scene.lights.add(new PointLight(new Color(35, 50, 80), new Point(135, 20, 105))
//                .setKl(0.0008).setKq(0.000002));
//    }
//}
