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
 * Small visual comparison scene for sharp/glossy reflection and sharp/blurry transparency.
 */
@SuppressWarnings("java:S109")
class SmallGlassEffectComparisonTests {
    /**
     * Creates the small glass-effect comparison test fixture.
     */
    SmallGlassEffectComparisonTests() {
    }

    /**
     * Renders a compact Stage 3 comparison image.
     */
    @Test
    void testSmallGlossyAndBlurryTransparencyComparison() {
        Scene scene = new Scene("small glossy and blurry glass comparison")
                .setBackground(new Color(20, 28, 36))
                .setAmbientLight(new AmbientLight(new Color(18, 18, 18)));

        scene.geometries.add(
                new Plane(new Point(0, -55, -120), Vector.AXIS_Y)
                        .setEmission(new Color(55, 58, 62))
                        .setMaterial(new Material().setKD(0.35).setKS(0.12).setKR(0.08).setShininess(60)),
                mirrorPanel(-95, -50, 0d),
                mirrorPanel(-45, 0, 18d),
                glassPanel(15, 45, 0d),
                glassPanel(55, 85, 14d),
                new Sphere(new Point(-72, -25, -150), 12)
                        .setEmission(new Color(210, 45, 35))
                        .setMaterial(new Material().setKD(0.25).setKS(0.45).setShininess(100)),
                new Sphere(new Point(-22, -25, -150), 12)
                        .setEmission(new Color(210, 45, 35))
                        .setMaterial(new Material().setKD(0.25).setKS(0.45).setShininess(100)),
                new Sphere(new Point(30, -28, -185), 10)
                        .setEmission(new Color(40, 120, 220))
                        .setMaterial(new Material().setKD(0.25).setKS(0.3).setShininess(80)),
                new Sphere(new Point(70, -28, -185), 10)
                        .setEmission(new Color(40, 120, 220))
                        .setMaterial(new Material().setKD(0.25).setKS(0.3).setShininess(80))
        );

        scene.lights.add(new SpotLight(new Color(420, 320, 230), new Point(-80, 80, 70),
                new Vector(0.7, -1.0, -1.8)).setKl(0.0007).setKq(0.000002));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 8, 210))
                .setDirection(new Point(0, -22, -145), Vector.AXIS_Y)
                .setVpDistance(210)
                .setVpSize(160, 100)
                .setResolution(160, 100)
                .setBlurSamples(4)
                .setBlurSamplingPattern(SamplingPattern.GRID)
                .setBlurSamplingShape(SamplingShape.SQUARE)
                .setBlurTargetDistance(70)
                .setMultithreading(0)
                .build()
                .renderImage()
                .writeToImage("stage3_small_glossy_blurry_comparison");
    }

    /**
     * Creates a small floor mirror panel.
     *
     * @param xMin left x-coordinate
     * @param xMax right x-coordinate
     * @param blur glossy blur radius
     * @return mirror panel geometry
     */
    private Polygon mirrorPanel(double xMin, double xMax, double blur) {
        return (Polygon) new Polygon(
                new Point(xMin, -52, -195),
                new Point(xMax, -52, -195),
                new Point(xMax, -52, -85),
                new Point(xMin, -52, -85))
                .setEmission(new Color(18, 38, 48))
                .setMaterial(new Material()
                        .setKD(0.05)
                        .setKS(0.3)
                        .setKR(0.75)
                        .setShininess(140)
                        .setGlossyBlur(blur));
    }

    /**
     * Creates a small vertical transparent panel.
     *
     * @param xMin left x-coordinate
     * @param xMax right x-coordinate
     * @param blur transparency blur radius
     * @return transparent panel geometry
     */
    private Polygon glassPanel(double xMin, double xMax, double blur) {
        return (Polygon) new Polygon(
                new Point(xMin, -45, -145),
                new Point(xMax, -45, -145),
                new Point(xMax, 20, -145),
                new Point(xMin, 20, -145))
                .setEmission(new Color(4, 16, 20))
                .setMaterial(new Material()
                        .setKD(0.02)
                        .setKS(0.15)
                        .setKT(0.85)
                        .setShininess(80)
                        .setRefractiveIndex(1d)
                        .setTransparencyBlur(blur));
    }
}
