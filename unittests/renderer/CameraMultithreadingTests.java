package renderer;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused tests for camera pixel dispatch across supported threading modes.
 */
class CameraMultithreadingTests {
    /**
     * Creates the camera multithreading test fixture.
     */
    CameraMultithreadingTests() {
    }

    /** Camera location used by these tests. */
    private static final Point CAMERA_LOCATION = Point.ZERO;
    /** View-plane distance used by these tests. */
    private static final double VP_DISTANCE = 10d;

    /**
     * Verifies supported threading modes render every rectangular-grid pixel exactly once.
     *
     * @throws Exception if tracer injection fails
     */
    @Test
    void testThreadingModesRenderEveryPixelExactlyOnce() throws Exception {
        for (int mode : new int[]{0, -1, -2, 3}) {
            Camera camera = baseBuilder()
                    .setMultithreading(mode)
                    .build();
            RecordingRayTracer tracer = new RecordingRayTracer();

            renderWithTracer(camera, tracer);

            assertEquals(12, tracer.count(), "Wrong ray count in threading mode " + mode);
            for (Point expectedTarget : expectedTargets()) {
                long hits = tracer.targets().stream().filter(expectedTarget::equals).count();
                assertEquals(1, hits, "Wrong or duplicated pixel target " + expectedTarget + " in mode " + mode);
            }
        }
    }

    /**
     * Verifies raw-thread rendering propagates worker exceptions after joining workers.
     *
     * @throws Exception if tracer injection fails
     */
    @Test
    void testRawThreadModePropagatesWorkerException() throws Exception {
        Camera camera = baseBuilder()
                .setMultithreading(2)
                .build();
        FailingRayTracer tracer = new FailingRayTracer();
        installTracer(camera, tracer);

        assertThrows(RuntimeException.class, camera::renderImage,
                "Raw-thread rendering should propagate worker failures");
    }

    /**
     * Creates a rectangular-grid camera builder.
     *
     * @return configured camera builder
     */
    private Camera.Builder baseBuilder() {
        return Camera.getBuilder()
                .setLocation(CAMERA_LOCATION)
                .setDirection(new Vector(0, 0, -1), Vector.AXIS_Y)
                .setVpDistance(VP_DISTANCE)
                .setVpSize(8, 6)
                .setResolution(4, 3);
    }

    /**
     * Installs a recording tracer and renders the camera.
     *
     * @param camera camera to render
     * @param tracer ray tracer to install
     * @throws Exception if tracer injection fails
     */
    private static void renderWithTracer(Camera camera, RayTracerBase tracer) throws Exception {
        installTracer(camera, tracer);
        camera.renderImage();
    }

    /**
     * Installs a test ray tracer by reflection.
     *
     * @param camera camera to mutate
     * @param tracer ray tracer to install
     * @throws Exception if reflection fails
     */
    private static void installTracer(Camera camera, RayTracerBase tracer) throws Exception {
        Field rayTracerField = Camera.class.getDeclaredField("_rayTracer");
        rayTracerField.setAccessible(true);
        rayTracerField.set(camera, tracer);
    }

    /**
     * Computes where a recorded ray intersects the test view plane at z = -VP_DISTANCE.
     *
     * @param ray recorded ray
     * @return target point on the view plane
     */
    private static Point targetOnViewPlane(Ray ray) {
        double directionZ = ray.direction().dotProduct(Vector.AXIS_Z);
        return ray.getPoint(-VP_DISTANCE / directionZ);
    }

    /**
     * Expected target centers for the 4x3 rectangular test grid.
     *
     * @return expected target centers
     */
    private static List<Point> expectedTargets() {
        return List.of(
                new Point(-3, 2, -VP_DISTANCE),
                new Point(-1, 2, -VP_DISTANCE),
                new Point(1, 2, -VP_DISTANCE),
                new Point(3, 2, -VP_DISTANCE),
                new Point(-3, 0, -VP_DISTANCE),
                new Point(-1, 0, -VP_DISTANCE),
                new Point(1, 0, -VP_DISTANCE),
                new Point(3, 0, -VP_DISTANCE),
                new Point(-3, -2, -VP_DISTANCE),
                new Point(-1, -2, -VP_DISTANCE),
                new Point(1, -2, -VP_DISTANCE),
                new Point(3, -2, -VP_DISTANCE)
        );
    }

    /**
     * Thread-safe ray tracer that records one target point per traced primary ray.
     */
    private static class RecordingRayTracer extends RayTracerBase {
        /** Number of traced rays. */
        private final AtomicInteger count = new AtomicInteger();
        /** Unique target points hit on the view plane. */
        private final ConcurrentLinkedQueue<Point> targets = new ConcurrentLinkedQueue<>();

        /**
         * Creates a recording ray tracer.
         */
        private RecordingRayTracer() {
            super(new Scene("threading recording tracer"));
        }

        @Override
        Color traceRay(Ray ray) {
            count.incrementAndGet();
            targets.add(targetOnViewPlane(ray));
            return Color.BLACK;
        }

        /**
         * Returns traced ray count.
         *
         * @return traced ray count
         */
        private int count() {
            return count.get();
        }

        /**
         * Returns unique view-plane targets.
         *
         * @return recorded target list
         */
        private List<Point> targets() {
            return List.copyOf(targets);
        }
    }

    /**
     * Ray tracer that fails on its first traced ray.
     */
    private static final class FailingRayTracer extends RecordingRayTracer {
        /**
         * Creates a failing tracer.
         */
        private FailingRayTracer() {
        }

        @Override
        Color traceRay(Ray ray) {
            throw new RuntimeException("synthetic render failure");
        }
    }
}
