package renderer;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused tests for camera super sampling integration.
 */
class CameraSuperSamplingTests {
    /** Numeric comparison tolerance. */
    private static final double DELTA = 1e-10;
    /** Camera location used by these tests. */
    private static final Point CAMERA_LOCATION = Point.ZERO;
    /** View-plane distance used by these tests. */
    private static final double VP_DISTANCE = 10d;
    /** Sample counts that include perfect and non-perfect squares. */
    private static final int[] SAMPLE_COUNTS = {1, 4, 5, 9, 10};

    /**
     * Verifies the default path traces one center ray only.
     */
    @Test
    void testDefaultRenderingUsesOneCentralRay() throws Exception {
        Camera camera = baseBuilder(1, 1).build();
        RecordingRayTracer tracer = renderWithTracer(camera, new RecordingRayTracer(ray -> Color.BLACK));

        assertEquals(1, tracer.rays().size(), "Default rendering should trace one ray per pixel");
        assertEquals(new Ray(CAMERA_LOCATION, new Vector(0, 0, -VP_DISTANCE)), tracer.rays().getFirst(),
                "Default rendering should use the original center ray");
    }

    /**
     * Verifies exact ray counts for required sample counts.
     */
    @Test
    void testSupersamplingTracesExactSampleCounts() throws Exception {
        for (int count : SAMPLE_COUNTS) {
            Camera camera = baseBuilder(1, 1)
                    .setSuperSampling(count)
                    .setSamplingPattern(SamplingPattern.GRID)
                    .setSamplingShape(SamplingShape.SQUARE)
                    .build();

            RecordingRayTracer tracer = renderWithTracer(camera, new RecordingRayTracer(ray -> Color.BLACK));

            assertEquals(count, tracer.rays().size(), "Wrong ray count for sample count " + count);
            if (count == 1) {
                assertEquals(new Ray(CAMERA_LOCATION, new Vector(0, 0, -VP_DISTANCE)), tracer.rays().getFirst(),
                        "A single sample should use the original center ray");
            }
        }
    }

    /**
     * Verifies that the central ray is not added in addition to configured samples.
     */
    @Test
    void testNoAdditionalCentralRayIsAdded() throws Exception {
        Camera camera = baseBuilder(1, 1)
                .setSuperSampling(4)
                .setSamplingPattern(SamplingPattern.GRID)
                .setSamplingShape(SamplingShape.SQUARE)
                .build();

        RecordingRayTracer tracer = renderWithTracer(camera, new RecordingRayTracer(ray -> Color.BLACK));

        assertEquals(4, tracer.rays().size(), "Supersampling should trace exactly N rays");
        assertFalse(tracer.rays().contains(new Ray(CAMERA_LOCATION, new Vector(0, 0, -VP_DISTANCE))),
                "Supersampling added the original center ray");
    }

    /**
     * Verifies sample colors are summed first and averaged once.
     */
    @Test
    void testSampleColorsAreAveraged() throws Exception {
        Camera camera = baseBuilder(1, 1)
                .setSuperSampling(4)
                .setSamplingPattern(SamplingPattern.GRID)
                .setSamplingShape(SamplingShape.SQUARE)
                .build();
        RecordingRayTracer tracer = new RecordingRayTracer(List.of(
                new Color(10, 20, 30),
                new Color(30, 40, 50),
                new Color(50, 60, 70),
                new Color(70, 80, 90)
        ));

        renderWithTracer(camera, tracer);

        assertEquals(new java.awt.Color(40, 50, 60), pixelColor(camera, 0, 0),
                "Pixel color should be the arithmetic average of sample colors");
    }

    /**
     * Verifies grid sampling is deterministic and independent of jitter seed.
     */
    @Test
    void testGridSamplingIsDeterministic() throws Exception {
        RecordingRayTracer first = renderWithTracer(baseBuilder(1, 1)
                        .setSuperSampling(10)
                        .setSamplingPattern(SamplingPattern.GRID)
                        .setSamplingShape(SamplingShape.SQUARE)
                        .setSamplingSeed(17L)
                        .build(),
                new RecordingRayTracer(ray -> Color.BLACK));
        RecordingRayTracer second = renderWithTracer(baseBuilder(1, 1)
                        .setSuperSampling(10)
                        .setSamplingPattern(SamplingPattern.GRID)
                        .setSamplingShape(SamplingShape.SQUARE)
                        .setSamplingSeed(999L)
                        .build(),
                new RecordingRayTracer(ray -> Color.BLACK));

        assertEquals(first.rays(), second.rays(), "Grid sampling should be deterministic");
    }

    /**
     * Verifies jittered sampling is reproducible with the same base seed.
     */
    @Test
    void testJitteredSamplingSameSeedIsDeterministic() throws Exception {
        RecordingRayTracer first = renderWithTracer(jitteredCamera(1234L), new RecordingRayTracer(ray -> Color.BLACK));
        RecordingRayTracer second = renderWithTracer(jitteredCamera(1234L), new RecordingRayTracer(ray -> Color.BLACK));

        assertEquals(first.rays(), second.rays(), "Same jitter seed should produce the same rays");
    }

    /**
     * Verifies different base seeds affect jittered sample positions.
     */
    @Test
    void testJitteredSamplingDifferentSeedsDiffer() throws Exception {
        RecordingRayTracer first = renderWithTracer(jitteredCamera(1234L), new RecordingRayTracer(ray -> Color.BLACK));
        RecordingRayTracer second = renderWithTracer(jitteredCamera(5678L), new RecordingRayTracer(ray -> Color.BLACK));

        assertNotEquals(first.rays(), second.rays(), "Different jitter seeds should normally produce different rays");
    }

    /**
     * Verifies square and circle camera sampling shapes both stay inside the pixel.
     */
    @Test
    void testSamplingShapesStayInsidePixel() throws Exception {
        for (SamplingShape shape : List.of(SamplingShape.SQUARE, SamplingShape.CIRCLE)) {
            Camera camera = baseBuilder(1, 1)
                    .setSuperSampling(10)
                    .setSamplingPattern(SamplingPattern.GRID)
                    .setSamplingShape(shape)
                    .build();

            RecordingRayTracer tracer = renderWithTracer(camera, new RecordingRayTracer(ray -> Color.BLACK));

            for (Ray ray : tracer.rays()) {
                Point target = targetOnViewPlane(ray);
                if (shape == SamplingShape.SQUARE) {
                    assertTrue(coordinate(target, 0) >= -1d - DELTA && coordinate(target, 0) <= 1d + DELTA,
                            "Square sample x-coordinate is outside the pixel");
                    assertTrue(coordinate(target, 1) >= -1d - DELTA && coordinate(target, 1) <= 1d + DELTA,
                            "Square sample y-coordinate is outside the pixel");
                } else {
                    assertTrue(target.distance(new Point(0, 0, -VP_DISTANCE)) <= 1d + DELTA,
                            "Circle sample is outside the pixel-centered circle");
                }
            }
        }
    }

    /**
     * Verifies row/column orientation on a non-square image grid.
     */
    @Test
    void testNonSquareGridPreservesRowColumnOrientation() throws Exception {
        Camera camera = baseBuilder(3, 2)
                .setVpSize(6, 4)
                .setResolution(3, 2)
                .build();

        RecordingRayTracer tracer = renderWithTracer(camera, new RecordingRayTracer(ray -> Color.BLACK));
        List<Point> targets = tracer.rays().stream()
                .map(CameraSuperSamplingTests::targetOnViewPlane)
                .toList();

        assertEquals(List.of(
                new Point(-2, 1, -VP_DISTANCE),
                new Point(0, 1, -VP_DISTANCE),
                new Point(2, 1, -VP_DISTANCE),
                new Point(-2, -1, -VP_DISTANCE),
                new Point(0, -1, -VP_DISTANCE),
                new Point(2, -1, -VP_DISTANCE)
        ), targets, "Pixel traversal or row/column mapping is transposed");
    }

    /**
     * Verifies super sampling increases rays per pixel but not image dimensions.
     */
    @Test
    void testSupersamplingDoesNotChangeImageResolution() throws Exception {
        Camera camera = baseBuilder(3, 2)
                .setSuperSampling(10)
                .setSamplingPattern(SamplingPattern.GRID)
                .setSamplingShape(SamplingShape.SQUARE)
                .build();

        renderWithTracer(camera, new RecordingRayTracer(ray -> Color.BLACK));
        BufferedImage image = image(camera);

        assertEquals(3, image.getWidth(), "Image width should remain the configured output resolution");
        assertEquals(2, image.getHeight(), "Image height should remain the configured output resolution");
    }

    /**
     * Verifies invalid super sampling configuration is rejected.
     */
    @Test
    void testInvalidSupersamplingConfigurationRejected() {
        assertThrows(IllegalArgumentException.class, () -> baseBuilder(1, 1).setSuperSampling(0),
                "Sample count must be positive");
        assertThrows(IllegalArgumentException.class, () -> baseBuilder(1, 1).setSamples(0),
                "Sample count must be positive");
        assertThrows(NullPointerException.class, () -> baseBuilder(1, 1).setSamplingPattern(null),
                "Null sampling pattern should be rejected");
        assertThrows(NullPointerException.class, () -> baseBuilder(1, 1).setSamplingShape(null),
                "Null sampling shape should be rejected");
        assertThrows(IllegalArgumentException.class,
                () -> baseBuilder(1, 1).setSamplingShape(SamplingShape.RECTANGLE),
                "Camera should reject rectangular sampling shape");
        assertThrows(IllegalArgumentException.class, () -> baseBuilder(1, 1).setSamplingArea(0d, 1d),
                "Sampling dimensions must be positive");
        assertThrows(IllegalArgumentException.class, () -> baseBuilder(1, 1).setSamplingArea(Double.NaN, 1d),
                "Sampling dimensions must be finite");
    }

    /**
     * Creates a camera builder with square two-unit pixels unless overridden by the test.
     */
    private Camera.Builder baseBuilder(int columns, int rows) {
        return Camera.getBuilder()
                .setLocation(CAMERA_LOCATION)
                .setDirection(new Vector(0, 0, -1), Vector.AXIS_Y)
                .setVpDistance(VP_DISTANCE)
                .setVpSize(columns * 2d, rows * 2d)
                .setResolution(columns, rows)
                .setMultithreading(0);
    }

    /**
     * Creates a one-pixel jittered camera with a configurable base seed.
     */
    private Camera jitteredCamera(long seed) {
        return baseBuilder(1, 1)
                .setSuperSampling(10)
                .setSamplingPattern(SamplingPattern.JITTERED)
                .setSamplingShape(SamplingShape.SQUARE)
                .setSamplingSeed(seed)
                .build();
    }

    /**
     * Installs a recording tracer and renders the camera.
     */
    private static RecordingRayTracer renderWithTracer(Camera camera, RecordingRayTracer tracer) throws Exception {
        Field rayTracerField = Camera.class.getDeclaredField("_rayTracer");
        rayTracerField.setAccessible(true);
        rayTracerField.set(camera, tracer);
        camera.renderImage();
        return tracer;
    }

    /**
     * Computes where a recorded ray intersects the test view plane at z = -VP_DISTANCE.
     */
    private static Point targetOnViewPlane(Ray ray) {
        double directionZ = ray.direction().dotProduct(Vector.AXIS_Z);
        return ray.getPoint(-VP_DISTANCE / directionZ);
    }

    /**
     * Reads one coordinate from a point.
     */
    private static double coordinate(Point point, int index) throws Exception {
        Field xyzField = Point.class.getDeclaredField("_xyz");
        xyzField.setAccessible(true);
        Double3 xyz = (Double3) xyzField.get(point);
        return switch (index) {
            case 0 -> xyz._d1();
            case 1 -> xyz._d2();
            case 2 -> xyz._d3();
            default -> throw new IllegalArgumentException("Invalid coordinate index: " + index);
        };
    }

    /**
     * Reads the rendered image from a camera.
     */
    private static BufferedImage image(Camera camera) throws Exception {
        Field imageWriterField = Camera.class.getDeclaredField("_imageWriter");
        imageWriterField.setAccessible(true);
        ImageWriter imageWriter = (ImageWriter) imageWriterField.get(camera);

        Field imageField = ImageWriter.class.getDeclaredField("_image");
        imageField.setAccessible(true);
        return (BufferedImage) imageField.get(imageWriter);
    }

    /**
     * Reads one rendered pixel from a camera.
     */
    private static java.awt.Color pixelColor(Camera camera, int x, int y) throws Exception {
        return new java.awt.Color(image(camera).getRGB(x, y));
    }

    /**
     * Recording ray tracer used by these unit tests.
     */
    private static final class RecordingRayTracer extends RayTracerBase {
        /** Recorded rays in trace order. */
        private final List<Ray> rays = new ArrayList<>();
        /** Optional deterministic color sequence. */
        private final List<Color> colors;
        /** Color function used when no sequence is supplied. */
        private final Function<Ray, Color> colorFunction;

        /**
         * Creates a tracer with a color function.
         */
        private RecordingRayTracer(Function<Ray, Color> colorFunction) {
            super(new Scene("recording tracer"));
            this.colors = null;
            this.colorFunction = colorFunction;
        }

        /**
         * Creates a tracer with a deterministic color sequence.
         */
        private RecordingRayTracer(List<Color> colors) {
            super(new Scene("recording tracer"));
            this.colors = List.copyOf(colors);
            this.colorFunction = null;
        }

        @Override
        Color traceRay(Ray ray) {
            int index = rays.size();
            rays.add(ray);
            return colors == null ? colorFunction.apply(ray) : colors.get(index % colors.size());
        }

        /**
         * @return immutable recorded ray list
         */
        private List<Ray> rays() {
            return List.copyOf(rays);
        }
    }
}
