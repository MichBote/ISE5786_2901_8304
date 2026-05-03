package renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import geometries.api.Intersectable;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * Integration tests between {@link Camera#constructRay(int, int)} and
 * {@link Intersectable#findIntersections(Ray)}.
 * <p>
 * Each test constructs rays through a 3x3 view plane and counts the total number
 * of intersection points with a given geometry.
 * </p>
 */
class CameraIntersectionIntegration {

        private static final Point  LOCATION     = Point.ZERO;
        private static final Vector V_TO         = new Vector(0, 0, -1);
        private static final Vector V_UP         = new Vector(0, 1, 0);

    private static final int    NX           = 3;
    private static final int    NY           = 3;
    private static final double VP_DISTANCE  = 1d;
    private static final double VP_SIZE      = 3d;

    // Cameras used by multiple test cases (don’t rebuild the same camera repeatedly)
    private static final Camera CAMERA = Camera.getBuilder()
            .setLocation(LOCATION)
            .setDirection(V_TO, V_UP)
            .setVpDistance(VP_DISTANCE)
            .setVpSize(VP_SIZE, VP_SIZE)
            .setResolution(NX, NY)
            .build();

    @Test
    void testCameraRaySphereIntegration() {
        // TC01: Small sphere in front of view plane (2 intersections per hit ray)
        assertIntersectionsCount(CAMERA,
                new Sphere(new Point(0, 0, -3), 1),
                2,
                "Sphere TC01");

        // TC02: Sphere intersects all 9 rays (18 points)
        assertIntersectionsCount(CAMERA,
                new Sphere(new Point(0, 0, -2.5), 2.5),
                18,
                "Sphere TC02");

        // TC03: Sphere intersects some rays (10 points)
        assertIntersectionsCount(CAMERA,
                new Sphere(new Point(0, 0, -2), 2),
                10,
                "Sphere TC03");

        // TC04: Camera inside sphere (1 intersection per ray => 9)
        assertIntersectionsCount(CAMERA,
                new Sphere(new Point(0, 0, -2), 4),
                9,
                "Sphere TC04");

        // TC05: Sphere behind camera / no intersections
        assertIntersectionsCount(CAMERA,
                new Sphere(new Point(0, 0, 1), 0.5),
                0,
                "Sphere TC05");
    }

    @Test
    void testCameraRayPlaneIntegration() {
        // TC01: Plane orthogonal to camera direction (one intersection per ray)
        assertIntersectionsCount(CAMERA,
                new Plane(new Point(0, 0, -5), new Vector(0, 0, 1)),
                9,
                "Plane TC01");

        // TC02: Plane tilted so all rays still intersect
        assertIntersectionsCount(CAMERA,
                new Plane(new Point(0, 0, -5), new Vector(0, -1, 1)),
                9,
                "Plane TC02");

        // TC03: Plane steep tilt – only some rays intersect
        assertIntersectionsCount(CAMERA,
                new Plane(new Point(0, 0, -5), new Vector(0, -2, 1)),
                6,
                "Plane TC03");
    }

    @Test
    void testCameraRayTriangleIntegration() {
        // TC01: Small triangle in front of camera (1 intersection)
        assertIntersectionsCount(CAMERA,
                new Triangle(new Point(0, 1, -2), new Point(1, -1, -2), new Point(-1, -1, -2)),
                1,
                "Triangle TC01");

        // TC02: Larger triangle covering more rays (2 intersections)
        assertIntersectionsCount(CAMERA,
                new Triangle(new Point(0, 20, -2), new Point(1, -1, -2), new Point(-1, -1, -2)),
                2,
                "Triangle TC02");
    }

    /**
     * Helper that counts total intersections between rays through all pixels and a geometry.
     */
    private static void assertIntersectionsCount(Camera camera, Intersectable geometry, int expected, String testName) {
        int count = 0;
        for (int y = 0; y < NY; y++) {
            for (int x = 0; x < NX; x++) {
                Ray ray = camera.constructRay(x, y);
                List<Point> intersections = geometry.findIntersections(ray);
                if (intersections != null) {
                    count += intersections.size();
                }
            }
        }
        assertEquals(expected, count, testName + ": unexpected number of intersections");
    }
}
