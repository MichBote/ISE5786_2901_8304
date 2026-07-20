//package renderer;
//
//import geometries.impl.Plane;
//import geometries.impl.Polygon;
//import geometries.impl.Sphere;
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
/// **
// * Render tests that demonstrate bent refraction through glass.
// */
//@SuppressWarnings("java:S109")
//class RefractionDistortionTests {
//    /** Z coordinate of the striped wall behind the sphere. */
//    private static final double WALL_Z = -340;
//
//    /** Default constructor to satisfy JavaDoc generator. */
//    RefractionDistortionTests() { /* to satisfy JavaDoc generator */ }
//
//    /**
//     * Produces a striped-background scene where the glass sphere visibly bends the stripes.
//     */
//    @Test
//    void testGlassSphereRefractsStripedBackground() {
//        Scene scene = new Scene("Glass sphere refraction distortion")
//                .setBackground(new Color(185, 205, 220))
//                .setAmbientLight(new AmbientLight(new Color(12, 12, 14)));
//
//        scene.geometries.add(
//                new Plane(new Point(0, -80, 0), Vector.AXIS_Y)
//                        .setEmission(new Color(185, 185, 180))
//                        .setMaterial(new Material()
//                                .setKD(0.55)
//                                .setKS(0.20)
//                                .setKR(0.10)
//                                .setShininess(100)),
//                new Sphere(new Point(0, -5, -185), 72)
//                        .setEmission(new Color(0, 55, 70))
//                        .setMaterial(new Material()
//                                .setKD(0.04)
//                                .setKS(0.85)
//                                .setKT(0.92)
//                                .setKR(0.10)
//                                .setShininess(320)
//                                .setRefractiveIndex(1.65))
//        );
//
//        addStripedWall(scene);
//
//        scene.lights.add(new SpotLight(new Color(520, 430, 330), new Point(-120, 170, 120),
//                new Vector(0.8, -1.35, -2.1))
//                .setKl(0.00035)
//                .setKq(0.000001));
//        scene.lights.add(new PointLight(new Color(70, 120, 170), new Point(130, 30, 60))
//                .setKl(0.0007)
//                .setKq(0.000002));
//
//        Camera.getBuilder()
//                .setRayTracer(scene, RayTracerType.SIMPLE)
//                .setLocation(new Point(0, 25, 520))
//                .setDirection(new Point(0, -12, -195), Vector.AXIS_Y)
//                .setVpDistance(520)
//                .setVpSize(230, 230)
//                .setResolution(420, 420)
//                .setBlurEnabled(false)
//                .build()
//                .renderImage()
//                .writeToImage("glassSphereRefractionDistortion");
//    }
//
//    /**
//     * Adds high-contrast vertical stripes behind the sphere.
//     *
//     * @param scene target scene
//     */
//    private void addStripedWall(Scene scene) {
//        Color[] colors = {
//                new Color(245, 245, 245),
//                new Color(20, 35, 45),
//                new Color(225, 60, 55),
//                new Color(35, 120, 220),
//                new Color(250, 220, 60)
//        };
//
//        double x = -175;
//        double stripeWidth = 25;
//        for (int i = 0; i < 14; i++) {
//            Color color = colors[i % colors.length];
//            scene.geometries.add(new Polygon(
//                    new Point(x, -80, WALL_Z),
//                    new Point(x + stripeWidth, -80, WALL_Z),
//                    new Point(x + stripeWidth, 130, WALL_Z),
//                    new Point(x, 130, WALL_Z))
//                    .setEmission(color)
//                    .setMaterial(new Material().setKD(0.0)));
//            x += stripeWidth;
//        }
//    }
//}
