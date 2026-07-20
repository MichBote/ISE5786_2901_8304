package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.IntStream;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Camera represents a pinhole camera in 3D space.
 * <p>
 * The camera is built using the nested {@link Builder} class.
 * It stores an orthonormal basis: {@code vTo}, {@code vUp}, {@code vRight}.
 * The view-plane configuration includes size, distance and pixel resolution.
 * </p>
 */
public class Camera implements Cloneable {

    /** Amount of threads to use for rendering image by the camera. */
    private int threadsCount = 0;

    /** Spare threads when trying to use all available logical processors. */
    private static final int SPARE_THREADS = 2;

    /** Debug print interval in seconds. Zero disables progress output. */
    private double printInterval = 0;

    /** Pixel manager for synchronized pixel distribution and progress print. */
    private PixelManager pixelManager;

    /** Number of anti-aliasing samples per pixel (1 disables AA). */
    private int aaSamples = 1;

    /** Radius of anti-aliasing target area inside each pixel. */
    private double aaRadiusFactor = 0.5;

    /** Sampling pattern for anti-aliasing. */
    private Blackboard.Pattern aaPattern = Blackboard.Pattern.GRID;

    /** Sampling shape for anti-aliasing. */
    private Blackboard.Shape aaShape = Blackboard.Shape.SQUARE;

    /** Aperture radius for depth of field (0 disables DOF). */
    private double apertureRadius = 0;

    /** Focus distance from camera along view direction for DOF. */
    private double focusDistance = 1000;

    /** Number of depth-of-field rays per primary ray. */
    private int dofSamples = 1;

    /** Sampling pattern for depth of field aperture points. */
    private Blackboard.Pattern dofPattern = Blackboard.Pattern.JITTER;

    /** Sampling shape for depth of field aperture points. */
    private Blackboard.Shape dofShape = Blackboard.Shape.CIRCLE;

    /**
     * Camera position (lens center).
     */
    private Point _p0;

    /**
     * Camera forward direction (normalized).
     */
    private Vector _vTo;

    /**
     * Camera up direction (normalized, orthogonal to {@code _vTo}).
     */
    private Vector _vUp;

    /**
     * Camera right direction (normalized, orthogonal to {@code _vTo} and {@code _vUp}).
     */
    private Vector _vRight;

    /**
     * View-plane width.
     */
    private double _vpWidth;

    /**
     * View-plane height.
     */
    private double _vpHeight;

    /**
     * View-plane distance from camera.
     */
    private double _vpDistance;

    /**
     * Horizontal resolution (number of columns).
     */
    private int _nX = 1;

    /**
     * Vertical resolution (number of rows).
     */
    private int _nY = 1;

    /**
     * Image writer used to paint pixels.
     */
    private ImageWriter _imageWriter;

    /**
     * Ray tracer used to compute pixel colors.
     */
    private RayTracerBase _rayTracer;

    /**
     * Precomputed view-plane center point.
     */
    private Point _vpCenter;

    /**
     * Precomputed pixel width.
     */
    private double _pixelWidth;

    /**
     * Precomputed pixel height.
     */
    private double _pixelHeight;

    /**
     * Private default constructor – cameras are constructed via {@link Builder}.
     */
    private Camera() {
    }

    /**
     * Factory method for creating a new {@link Builder}.
     *
     * @return a new builder instance
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Constructs a ray from the camera location through the center of the pixel
     * identified by {@code (xIndex, yIndex)} on the view plane.
     *
     * @param xIndex pixel column index (X)
     * @param yIndex pixel row index (Y)
     * @return constructed ray
     */
    public Ray constructRay(int xIndex, int yIndex) {
        // Pixel center offsets relative to view-plane center
        double xJ = (xIndex - (_nX - 1) / 2d) * _pixelWidth;
        double yI = -(yIndex - (_nY - 1) / 2d) * _pixelHeight;

        Point pIJ = _vpCenter;
        if (!isZero(xJ)) {
            pIJ = pIJ.add(_vRight.scale(xJ));
        }
        if (!isZero(yI)) {
            pIJ = pIJ.add(_vUp.scale(yI));
        }

        return new Ray(_p0, pIJ.subtract(_p0));
    }

    /**
     * Renders the image by casting rays through all pixels.
     *
     * @return this camera (for chaining)
     */
    public Camera renderImage() {
        pixelManager = new PixelManager(_nY, _nX, printInterval);
        return switch (threadsCount) {
            case 0 -> renderImageNoThreads();
            case -1 -> renderImageStream();
            default -> renderImageRawThreads();
        };
    }

    /**
     * Render image without multi-threading.
     *
     * @return this camera
     */
    private Camera renderImageNoThreads() {
        for (int i = 0; i < _nY; i++) {
            for (int j = 0; j < _nX; j++) {
                castRay(j, i);
                pixelManager.pixelDone();
            }
        }
        return this;
    }

    /**
     * Render image with Java stream parallelization.
     *
     * @return this camera
     */
    private Camera renderImageStream() {
        IntStream.range(0, _nY).parallel().forEach(i -> IntStream.range(0, _nX).parallel().forEach(j -> {
            castRay(j, i);
            pixelManager.pixelDone();
        }));
        return this;
    }

    /**
     * Render image with raw Java threads and PixelManager work distribution.
     *
     * @return this camera
     */
    private Camera renderImageRawThreads() {
        var threads = new LinkedList<Thread>();
        int workers = threadsCount;

        while (workers-- > 0) {
            threads.add(new Thread(() -> {
                PixelManager.Pixel pixel;
                while ((pixel = pixelManager.nextPixel()) != null) {
                    castRay(pixel.col(), pixel.row());
                    pixelManager.pixelDone();
                }
            }));
        }

        for (var thread : threads) {
            thread.start();
        }

        try {
            for (var thread : threads) {
                thread.join();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        return this;
    }

    /**
     * Casts a single ray through a pixel (xIndex,yIndex) and writes its color.
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     */
    private void castRay(int xIndex, int yIndex) {
        List<Ray> rays = constructRays(xIndex, yIndex);
        Color color = rays.size() == 1 ? _rayTracer.traceRay(rays.get(0)) : _rayTracer.traceRays(rays);
        _imageWriter.writePixel(xIndex, yIndex, color);
    }

    /**
     * Constructs all rays to evaluate for a pixel according to AA/DOF configuration.
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     * @return rays for the pixel
     */
    private List<Ray> constructRays(int xIndex, int yIndex) {
        List<Ray> primaryRays = constructAntiAliasingRays(xIndex, yIndex);
        if (apertureRadius == 0 || dofSamples <= 1) {
            return primaryRays;
        }

        var allRays = new LinkedList<Ray>();
        Blackboard dofBoard = new Blackboard(dofShape, dofPattern, dofSamples, hashSeed(xIndex, yIndex, 17));

        for (Ray primary : primaryRays) {
            Point focalPoint = calcFocalPoint(primary);
            List<Point> aperturePoints = dofBoard.samplePoints(_p0, _vRight, _vUp, apertureRadius);
            for (Point aperturePoint : aperturePoints) {
                allRays.add(new Ray(aperturePoint, focalPoint.subtract(aperturePoint)));
            }
        }
        return allRays;
    }

    /**
     * Constructs anti-aliasing primary rays for a pixel.
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     * @return AA ray list
     */
    private List<Ray> constructAntiAliasingRays(int xIndex, int yIndex) {
        Ray centerRay = constructRay(xIndex, yIndex);
        if (aaSamples <= 1 || aaRadiusFactor == 0) {
            return List.of(centerRay);
        }

        Point center = centerRay.getPoint(_vpDistance);
        double areaRadius = Math.min(_pixelWidth, _pixelHeight) * aaRadiusFactor;
        Blackboard board = new Blackboard(aaShape, aaPattern, aaSamples, hashSeed(xIndex, yIndex, 3));

        List<Point> points = board.samplePoints(center, _vRight, _vUp, areaRadius);
        var rays = new LinkedList<Ray>();
        for (Point point : points) {
            rays.add(new Ray(_p0, point.subtract(_p0)));
        }
        return rays;
    }

    /**
     * Computes the focal point where a primary ray intersects the focal plane.
     *
     * @param ray primary ray from camera
     * @return focal point on focal plane
     */
    private Point calcFocalPoint(Ray ray) {
        Point focusCenter = _p0.add(_vTo.scale(focusDistance));
        double denom = ray.direction().dotProduct(_vTo);
        if (isZero(denom)) {
            return focusCenter;
        }
        double t = focusDistance / denom;
        if (t <= 0) {
            return focusCenter;
        }
        return ray.getPoint(t);
    }

    /**
     * Produces deterministic per-pixel seed for sampling patterns that use randomness.
     */
    private long hashSeed(int xIndex, int yIndex, int salt) {
        long h = 1469598103934665603L;
        h ^= xIndex + salt;
        h *= 1099511628211L;
        h ^= yIndex + (long) salt * 31;
        h *= 1099511628211L;
        return h;
    }

    /**
     * Draws a grid on top of the rendered image.
     *
     * @param interval grid cell size in pixels
     * @param color    grid line color
     * @return this camera (for chaining)
     */
    public Camera printGrid(int interval, Color color) {
        for (int yIndex = 0; yIndex < _nY; yIndex++) {
            for (int xIndex = 0; xIndex < _nX; xIndex++) {
                if (xIndex % interval == 0 || yIndex % interval == 0) {
                    _imageWriter.writePixel(xIndex, yIndex, color);
                }
            }
        }
        return this;
    }

    /**
     * Writes the rendered image to a PNG file under the project's images folder.
     *
     * @param fileName output file name (without extension)
     */
    public void writeToImage(String fileName) {
        _imageWriter.writeToImage(fileName);
    }

    /**
     * Builder for {@link Camera}.
     * <p>
     * Setters do not validate or compute; all validation/computation happens in
     * {@link #build()}.
     * </p>
     */
    public static class Builder {
        /**
         * Camera instance being configured.
         */
        private final Camera _camera = new Camera();

        /**
         * Creates a new builder instance.
         * <p>
         * Prefer using {@link Camera#getBuilder()}.
         * </p>
         */
        public Builder() {
        }

        /**
         * Explicit forward direction input, resolved during build.
         */
        private Vector _toVector;
        /**
         * Target point input, resolved during build.
         */
        private Point _targetPoint;
        /**
         * General up direction input, resolved during build.
         */
        private Vector _generalUp;

        /**
         * Optional roll around viewing direction, applied during build.
         */
        private double _rollRadians = 0d;

        /**
         * Sets the camera location.
         *
         * @param location camera position (lens center)
         * @return this builder
         */
        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        /**
         * Sets the camera direction explicitly by vectors (to, up).
         *
         * @param to forward direction vector
         * @param up general up direction vector
         * @return this builder
         */
        public Builder setDirection(Vector to, Vector up) {
            _toVector = to;
            _targetPoint = null;
            _generalUp = up == null ? Vector.AXIS_Y : up;
            return this;
        }

        /**
         * Sets the camera direction by target point and explicit up vector.
         *
         * @param target a point the camera should look at
         * @param up     general up direction vector
         * @return this builder
         */
        public Builder setDirection(Point target, Vector up) {
            _targetPoint = target;
            _toVector = null;
            _generalUp = up == null ? Vector.AXIS_Y : up;
            return this;
        }

        /**
         * Sets the camera direction by target point only (up defaults to +Y axis).
         *
         * @param target a point the camera should look at
         * @return this builder
         */
        public Builder setDirection(Point target) {
            _targetPoint = target;
            _toVector = null;
            _generalUp = Vector.AXIS_Y;
            return this;
        }

        /**
         * Sets view-plane size.
         *
         * @param width  view-plane width
         * @param height view-plane height
         * @return this builder
         */
        public Builder setVpSize(double width, double height) {
            _camera._vpWidth = width;
            _camera._vpHeight = height;
            return this;
        }

        /**
         * Sets view-plane distance from the camera.
         *
         * @param distance view-plane distance
         * @return this builder
         */
        public Builder setVpDistance(double distance) {
            _camera._vpDistance = distance;
            return this;
        }

        /**
         * Sets view-plane resolution (number of pixels).
         *
         * @param nX number of pixels horizontally (columns)
         * @param nY number of pixels vertically (rows)
         * @return this builder
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Set multi-threading mode.
         * <p>
         * Parameter value meaning:
         * -2: number of threads is number of logical processors less 2
         * -1: stream parallelization is used
         * 0: multi-threading is disabled
         * 1 and more: explicit number of threads
         * </p>
         *
         * @param threads number of threads configuration
         * @return this builder
         */
        public Builder setMultithreading(int threads) {
            if (threads < -2) {
                throw new IllegalArgumentException("Multithreading parameter must be >= -2");
            }
            if (threads >= -1) {
                _camera.threadsCount = threads;
                return this;
            }

            int cores = Runtime.getRuntime().availableProcessors() - SPARE_THREADS;
            _camera.threadsCount = Math.max(1, cores);
            return this;
        }

        /**
         * Set progress print interval in seconds.
         *
         * @param interval print interval in seconds (0 disables)
         * @return this builder
         */
        public Builder setDebugPrint(double interval) {
            if (interval < 0) {
                throw new IllegalArgumentException("Debug print interval must be non-negative");
            }
            _camera.printInterval = interval;
            return this;
        }

        /**
         * Configures anti-aliasing sampling.
         *
         * @param samples number of rays per pixel (1 disables)
         * @param shape sampling area shape
         * @param pattern sampling pattern
         * @param radiusFactor radius as factor of min(pixelWidth, pixelHeight)
         * @return this builder
         */
        public Builder setAntiAliasing(int samples, Blackboard.Shape shape, Blackboard.Pattern pattern, double radiusFactor) {
            if (samples < 1) {
                throw new IllegalArgumentException("samples must be at least 1");
            }
            if (radiusFactor < 0) {
                throw new IllegalArgumentException("radiusFactor must be non-negative");
            }

            _camera.aaSamples = samples;
            _camera.aaShape = shape == null ? Blackboard.Shape.SQUARE : shape;
            _camera.aaPattern = pattern == null ? Blackboard.Pattern.GRID : pattern;
            _camera.aaRadiusFactor = radiusFactor;
            return this;
        }

        /**
         * Configures depth-of-field sampling.
         *
         * @param apertureRadius aperture radius (0 disables)
         * @param focusDistance focus distance from camera along vTo
         * @param samples number of aperture samples per primary ray
         * @param shape aperture sampling shape
         * @param pattern aperture sampling pattern
         * @return this builder
         */
        public Builder setDepthOfField(double apertureRadius, double focusDistance, int samples,
                                       Blackboard.Shape shape, Blackboard.Pattern pattern) {
            if (apertureRadius < 0) {
                throw new IllegalArgumentException("apertureRadius must be non-negative");
            }
            if (focusDistance <= 0) {
                throw new IllegalArgumentException("focusDistance must be positive");
            }
            if (samples < 1) {
                throw new IllegalArgumentException("samples must be at least 1");
            }

            _camera.apertureRadius = apertureRadius;
            _camera.focusDistance = focusDistance;
            _camera.dofSamples = samples;
            _camera.dofShape = shape == null ? Blackboard.Shape.CIRCLE : shape;
            _camera.dofPattern = pattern == null ? Blackboard.Pattern.JITTER : pattern;
            return this;
        }

        /**
         * Sets the ray tracer implementation for the camera.
         *
         * @param scene scene to render
         * @param type  ray tracer type
         * @return this builder
         */
        public Builder setRayTracer(Scene scene, RayTracerType type) {
            if (type == RayTracerType.SIMPLE) {
                _camera._rayTracer = new SimpleRayTracer(scene);
            } else {
                throw new IllegalArgumentException("Unsupported ray tracer type: " + type);
            }
            return this;
        }

        /**
         * Rotates the camera around its viewing direction ({@code vTo}).
         * <p>
         * The angle is given in <b>degrees</b> and is defined as <b>clockwise</b>
         * when looking in the {@code vTo} direction.
         * </p>
         *
         * @param angleDegreesClockwise rotation angle in degrees (clockwise)
         * @return this builder for chaining
         */
        public Builder rotate(double angleDegreesClockwise) {
            if (!isZero(angleDegreesClockwise)) {
                _rollRadians += Math.toRadians(angleDegreesClockwise);
            }
            return this;
        }

        /**
         * Builds a valid camera instance.
         *
         * @return a cloned camera ready for use
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();

            if (_camera._rayTracer == null) {
                setRayTracer(new Scene("test"), RayTracerType.SIMPLE);
            }

            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException ex) {
                return null;
            }
        }

        /**
         * Validates resolution settings and creates the image writer.
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0) {
                throw new IllegalArgumentException("View-plane resolution must be positive");
            }
            _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
        }

        /**
         * Validates location and direction settings, then computes the camera basis vectors.
         */
        private void checkLocationAndDirection() {
            if (_camera._p0 == null)
                throw new MissingResourceException("Camera location is missing", "Camera", "location");
            if (_generalUp == null)
                throw new MissingResourceException("Camera up direction is missing", "Camera", "up");

            Vector vTo;
            if (_toVector != null) {
                vTo = _toVector;
            } else if (_targetPoint != null) {
                vTo = _targetPoint.subtract(_camera._p0);
            } else {
                throw new MissingResourceException("Camera direction is missing", "Camera", "direction");
            }

            // Normalize forward vector
            _camera._vTo = vTo.normalize();

            // Build orthonormal basis using provided general-up
            try {
                _camera._vRight = vTo.crossProduct(_generalUp).normalize();
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Camera direction vectors are parallel", ex);
            }

            // Optional roll around vTo (clockwise in degrees, applied as a right-hand rotation around vTo)
            if (!isZero(_rollRadians)) {
                _camera._vRight = _camera._vRight.rotateAround(_camera._vTo, _rollRadians).normalize();
            }

            _camera._vUp = _camera._vRight.crossProduct(_camera._vTo).normalize();
        }

        /**
         * Validates view-plane settings and precomputes view-plane geometry.
         */
        private void checkViewPlane() {
            if (alignZero(_camera._vpWidth) <= 0 || alignZero(_camera._vpHeight) <= 0) {
                throw new IllegalArgumentException("View-plane size must be positive");
            }
            if (alignZero(_camera._vpDistance) <= 0) {
                throw new IllegalArgumentException("View-plane distance must be positive");
            }

            _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._vpDistance));
            _camera._pixelWidth = _camera._vpWidth / _camera._nX;
            _camera._pixelHeight = _camera._vpHeight / _camera._nY;
        }
    }
}
