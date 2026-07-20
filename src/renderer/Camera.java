package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
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
     * Number of rendering threads. Values follow the project contract.
     */
    private int threadsCount = 0;

    /**
     * Debug progress print interval, in percent.
     */
    private double printInterval = 0;

    /**
     * Pixel manager used for raw-thread rendering and optional progress output.
     */
    private PixelManager pixelManager;

    /**
     * Enables super sampling when {@code true}.
     */
    private boolean superSamplingEnabled = false;

    /**
     * Number of samples used for super sampling.
     */
    private int samplingSamples = 9;

    /**
     * Sampling area width inside each pixel. When non-positive, the full pixel width is used.
     */
    private double samplingWidth = 0d;

    /**
     * Sampling area height inside each pixel. When non-positive, the full pixel height is used.
     */
    private double samplingHeight = 0d;

    /**
     * Sampling pattern used for camera super sampling.
     */
    private SamplingPattern samplingPattern = SamplingPattern.JITTERED;

    /**
     * Sampling shape used for camera super sampling.
     */
    private SamplingShape samplingShape = SamplingShape.SQUARE;

    /**
     * Base deterministic seed for jittered camera super sampling.
     */
    private long samplingSeed = 0L;

    /**
     * Reusable sampling board for camera super sampling.
     */
    private SamplingBoard samplingBoard;

    /**
     * Optional aggregate render profiling counters.
     */
    private RenderStats renderStats;

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
        return new Ray(_p0, pixelCenter(xIndex, yIndex).subtract(_p0));
    }

    /**
     * Constructs all primary rays for a pixel.
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     * @return primary rays to trace
     */
    private List<Ray> constructSampleRays(int xIndex, int yIndex) {
        Point pixelCenter = pixelCenter(xIndex, yIndex);
        SamplingBoard board = Objects.requireNonNull(samplingBoard, "Sampling board is not configured");

        long seed = samplingSeedForPixel(xIndex, yIndex);
        List<Point> samplePoints = board.sample(pixelCenter, _vRight, _vUp, seed);
        List<Ray> rays = new ArrayList<>(samplePoints.size());
        for (Point samplePoint : samplePoints) {
            rays.add(new Ray(_p0, samplePoint.subtract(_p0)));
        }
        return rays;
    }

    /**
     * Computes the center point of a pixel on the view plane.
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     * @return pixel center point
     */
    private Point pixelCenter(int xIndex, int yIndex) {
        double xJ = (xIndex - (_nX - 1) / 2d) * _pixelWidth;
        double yI = -(yIndex - (_nY - 1) / 2d) * _pixelHeight;

        Point pIJ = _vpCenter;
        if (!isZero(xJ)) {
            pIJ = pIJ.add(_vRight.scale(xJ));
        }
        if (!isZero(yI)) {
            pIJ = pIJ.add(_vUp.scale(yI));
        }
        return pIJ;
    }

    /**
     * Renders the image by casting rays through all pixels.
     *
     * @return this camera (for chaining)
     */
    public Camera renderImage() {
        pixelManager = new PixelManager(_nY, _nX, printInterval);

        long start = System.nanoTime();
        try {
            if (threadsCount == 0) {
                renderImageNoThreads();
            } else if (threadsCount == -1) {
                renderImageStream();
            } else {
                renderImageRawThreads();
            }
        } finally {
            if (renderStats != null) {
                renderStats.addRenderNanos(System.nanoTime() - start);
            }
        }
        return this;
    }

    /**
     * Renders the image using the original single-threaded nested loops.
     *
     * @return this camera
     */
    private Camera renderImageNoThreads() {
        for (int yIndex = 0; yIndex < _nY; yIndex++) {
            for (int xIndex = 0; xIndex < _nX; xIndex++) {
                castPixel(xIndex, yIndex);
            }
        }
        return this;
    }

    /**
     * Renders the image using parallel streams.
     * <p>
     * Pixels are addressed by a single flat index rather than nested per-row streams,
     * so the common {@link java.util.concurrent.ForkJoinPool} can split and work-steal
     * at pixel granularity. This keeps all cores busy even when render cost varies
     * sharply across the image (e.g. a glass/reflective region concentrated in a few
     * rows), which coarser row-chunked splitting would not rebalance as effectively.
     * </p>
     *
     * @return this camera
     */
    private Camera renderImageStream() {
        int totalPixels = _nY * _nX;
        IntStream.range(0, totalPixels).parallel()
                .forEach(index -> castPixel(index % _nX, index / _nX));
        return this;
    }

    /**
     * Renders the image using raw worker threads.
     *
     * @return this camera
     */
    private Camera renderImageRawThreads() {
        AtomicReference<RuntimeException> renderException = new AtomicReference<>();
        Thread[] workers = new Thread[threadsCount];
        for (int i = 0; i < threadsCount; i++) {
            workers[i] = new Thread(() -> {
                PixelManager.Pixel pixel;
                while (renderException.get() == null && (pixel = pixelManager.nextPixel()) != null) {
                    try {
                        castPixel(pixel.col, pixel.row);
                    } catch (RuntimeException ex) {
                        renderException.compareAndSet(null, ex);
                        break;
                    }
                }
            }, "camera-render-worker-" + i);
            workers[i].start();
        }

        boolean interrupted = false;
        for (Thread worker : workers) {
            while (worker.isAlive()) {
                try {
                    worker.join();
                } catch (InterruptedException ex) {
                    interrupted = true;
                    renderException.compareAndSet(null,
                            new IllegalStateException("Rendering was interrupted", ex));
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        RuntimeException ex = renderException.get();
        if (ex != null) {
            throw ex;
        }
        return this;
    }

    /**
     * Casts rays through a pixel and writes the averaged color.
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     */
    private void castPixel(int xIndex, int yIndex) {
        _imageWriter.writePixel(xIndex, yIndex, castRayForPixel(xIndex, yIndex));
        pixelManager.pixelDone();
    }

    /**
     * Calculates the final color for one output pixel.
     * <p>
     * Without super sampling, or when the configured sample count is one, this uses
     * the original single center ray. With super sampling, it traces exactly the
     * configured number of sample rays and averages their colors.
     * </p>
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     * @return final pixel color
     */
    private Color castRayForPixel(int xIndex, int yIndex) {
        if (!superSamplingEnabled || samplingSamples == 1) {
            recordPrimaryRays(1);
            return _rayTracer.traceRay(constructRay(xIndex, yIndex));
        }
        List<Ray> rays = constructSampleRays(xIndex, yIndex);
        recordPrimaryRays(rays.size());
        return castRays(rays);
    }

    /**
     * Adds primary ray count to the optional render statistics.
     */
    private void recordPrimaryRays(int count) {
        if (renderStats != null) {
            renderStats.addPrimaryRays(count);
        }
    }

    /**
     * Traces all rays in a beam and averages their colors.
     *
     * @param rays rays to trace
     * @return averaged color
     */
    private Color castRays(List<Ray> rays) {
        Color color = Color.BLACK;
        for (Ray ray : rays) {
            color = color.add(_rayTracer.traceRay(ray));
        }
        return color.scale(1d / rays.size());
    }

    /**
     * Derives a stable per-pixel jitter seed from the configured base seed and the
     * pixel coordinates. Row and column use different constants so pixels with the
     * same {@code row + column} do not collide.
     *
     * @param xIndex pixel column index
     * @param yIndex pixel row index
     * @return mixed deterministic seed for this pixel
     */
    private long samplingSeedForPixel(int xIndex, int yIndex) {
        long value = samplingSeed;
        value ^= (long) xIndex * 0x9E3779B97F4A7C15L;
        value ^= (long) yIndex * 0xBF58476D1CE4E5B9L;
        return mix64(value);
    }

    /**
     * SplitMix64 finalizer used for deterministic seed mixing.
     *
     * @param value input value
     * @return mixed value
     */
    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
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
         * Enables or disables super sampling.
         */
        private boolean _superSamplingEnabled = false;

        /**
         * Number of super sampling rays.
         */
        private int _samplingSamples = 9;

        /**
         * Sampling area width inside each pixel.
         */
        private double _samplingWidth = 0d;

        /**
         * Sampling area height inside each pixel.
         */
        private double _samplingHeight = 0d;

        /**
         * Sampling pattern.
         */
        private SamplingPattern _samplingPattern = SamplingPattern.JITTERED;

        /**
         * Sampling shape.
         */
        private SamplingShape _samplingShape = SamplingShape.SQUARE;

        /**
         * Base deterministic seed for jittered sampling.
         */
        private long _samplingSeed = 0L;

        /**
         * Number of render threads.
         */
        private int _threadsCount = 0;

        /**
         * Debug print interval.
         */
        private double _printInterval = 0d;

        /**
         * Enables or disables blurred reflection/refraction.
         */
        private boolean _blurEnabled = true;

        /**
         * Number of samples in blurred global-effect beams.
         */
        private int _blurSamples = SimpleRayTracer.DEFAULT_BLUR_SAMPLES;

        /**
         * Sampling pattern for blurred global-effect beams.
         */
        private SamplingPattern _blurSamplingPattern = SamplingPattern.JITTERED;

        /**
         * Sampling shape for blurred global-effect beams.
         */
        private SamplingShape _blurSamplingShape = SamplingShape.SQUARE;

        /**
         * Base deterministic seed for jittered blurred global-effect beams.
         */
        private long _blurSamplingSeed = 0L;

        /**
         * Target distance for blurred global-effect beams.
         */
        private double _blurTargetDistance = SimpleRayTracer.DEFAULT_BLUR_TARGET_DISTANCE;

        /**
         * Optional aggregate render profiling counters.
         */
        private RenderStats _renderStats;

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
         * Sets the ray tracer implementation for the camera.
         *
         * @param scene scene to render
         * @param type  ray tracer type
         * @return this builder
         */
        public Builder setRayTracer(Scene scene, RayTracerType type) {
            if (type == RayTracerType.SIMPLE) {
                _camera._rayTracer = new SimpleRayTracer(scene);
                configureRayTracer();
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
         * Enables or disables super sampling.
         *
         * @param enabled {@code true} to enable super sampling
         * @return this builder
         */
        public Builder setSuperSampling(boolean enabled) {
            _superSamplingEnabled = enabled;
            return this;
        }

        /**
         * Enables super sampling and sets the exact number of samples per pixel.
         *
         * @param samples exact number of samples per pixel, must be positive
         * @return this builder
         */
        public Builder setSuperSampling(int samples) {
            setSamples(samples);
            _superSamplingEnabled = true;
            return this;
        }

        /**
         * Alias for {@link #setSuperSampling(boolean)}.
         *
         * @param enabled {@code true} to enable super sampling
         * @return this builder
         */
        public Builder setAntiAliasing(boolean enabled) {
            return setSuperSampling(enabled);
        }

        /**
         * Enables anti-aliasing and sets the exact number of samples per pixel.
         *
         * @param samples exact number of samples per pixel, must be positive
         * @return this builder
         */
        public Builder setAntiAliasing(int samples) {
            return setSuperSampling(samples);
        }

        /**
         * Sets the number of super sampling rays.
         *
         * @param samples number of rays, must be positive
         * @return this builder
         */
        public Builder setSamples(int samples) {
            if (samples <= 0) {
                throw new IllegalArgumentException("Samples must be positive");
            }
            _samplingSamples = samples;
            return this;
        }

        /**
         * Alias for {@link #setSamples(int)}.
         *
         * @param samples number of rays, must be positive
         * @return this builder
         */
        public Builder setSampling(int samples) {
            return setSamples(samples);
        }

        /**
         * Sets the sampling area size inside each pixel.
         *
         * @param width  sample area width
         * @param height sample area height
         * @return this builder
         */
        public Builder setSamplingArea(double width, double height) {
            validatePositive(width, "Sampling area width");
            validatePositive(height, "Sampling area height");
            _samplingWidth = width;
            _samplingHeight = height;
            return this;
        }

        /**
         * Alias for {@link #setSamplingArea(double, double)}.
         *
         * @param width  sample area width
         * @param height sample area height
         * @return this builder
         */
        public Builder setTargetArea(double width, double height) {
            return setSamplingArea(width, height);
        }

        /**
         * Sets the sampling pattern.
         *
         * @param pattern sampling pattern
         * @return this builder
         */
        public Builder setSamplingPattern(SamplingPattern pattern) {
            _samplingPattern = Objects.requireNonNull(pattern, "Sampling pattern must not be null");
            return this;
        }

        /**
         * Alias for {@link #setSamplingPattern(SamplingPattern)}.
         *
         * @param pattern sampling pattern
         * @return this builder
         */
        public Builder setPattern(SamplingPattern pattern) {
            return setSamplingPattern(pattern);
        }

        /**
         * Sets the sampling shape used for camera super sampling.
         *
         * @param shape sampling shape; must be {@link SamplingShape#SQUARE} or {@link SamplingShape#CIRCLE}
         * @return this builder
         */
        public Builder setSamplingShape(SamplingShape shape) {
            _samplingShape = Objects.requireNonNull(shape, "Sampling shape must not be null");
            if (_samplingShape == SamplingShape.RECTANGLE) {
                throw new IllegalArgumentException("Camera sampling shape must be SQUARE or CIRCLE");
            }
            return this;
        }

        /**
         * Alias for {@link #setSamplingShape(SamplingShape)}.
         *
         * @param shape sampling shape
         * @return this builder
         */
        public Builder setShape(SamplingShape shape) {
            return setSamplingShape(shape);
        }

        /**
         * Sets the base deterministic seed for jittered camera sampling.
         *
         * @param seed base seed
         * @return this builder
         */
        public Builder setSamplingSeed(long seed) {
            _samplingSeed = seed;
            return this;
        }

        /**
         * Sets the rendering mode based on thread count.
         *
         * @param threads {@code -2} automatic raw threads, {@code -1} streams,
         *                {@code 0} single-threaded, {@code 1+} raw threads
         * @return this builder
         */
        public Builder setMultithreading(int threads) {
            if (threads < -2) {
                throw new IllegalArgumentException("Invalid thread count: " + threads);
            }
            _threadsCount = threads;
            return this;
        }

        /**
         * Sets debug progress printing interval in percent.
         *
         * @param interval print interval, must be non-negative
         * @return this builder
         */
        public Builder setDebugPrint(double interval) {
            if (interval < 0) {
                throw new IllegalArgumentException("Debug print interval must be non-negative");
            }
            _printInterval = interval;
            return this;
        }

        /**
         * Enables or disables blurred reflection and transparency effects.
         *
         * @param enabled {@code true} to use material blur values
         * @return this builder
         */
        public Builder setBlurEnabled(boolean enabled) {
            _blurEnabled = enabled;
            configureRayTracer();
            return this;
        }

        /**
         * Sets the number of samples for glossy and diffuse-glass beams.
         *
         * @param samples number of samples, must be positive
         * @return this builder
         */
        public Builder setBlurSamples(int samples) {
            if (samples <= 0) {
                throw new IllegalArgumentException("Blur samples must be positive");
            }
            _blurSamples = samples;
            configureRayTracer();
            return this;
        }

        /**
         * Sets the sampling pattern for glossy reflection and blurry transparency beams.
         *
         * @param pattern sampling pattern
         * @return this builder
         */
        public Builder setBlurSamplingPattern(SamplingPattern pattern) {
            _blurSamplingPattern = Objects.requireNonNull(pattern, "Blur sampling pattern must not be null");
            configureRayTracer();
            return this;
        }

        /**
         * Sets the sampling shape for glossy reflection and blurry transparency beams.
         *
         * @param shape sampling shape; must be {@link SamplingShape#SQUARE} or {@link SamplingShape#CIRCLE}
         * @return this builder
         */
        public Builder setBlurSamplingShape(SamplingShape shape) {
            _blurSamplingShape = Objects.requireNonNull(shape, "Blur sampling shape must not be null");
            if (_blurSamplingShape == SamplingShape.RECTANGLE) {
                throw new IllegalArgumentException("Blur sampling shape must be SQUARE or CIRCLE");
            }
            configureRayTracer();
            return this;
        }

        /**
         * Sets the base deterministic seed for jittered glossy/blurry beams.
         *
         * @param seed base seed
         * @return this builder
         */
        public Builder setBlurSamplingSeed(long seed) {
            _blurSamplingSeed = seed;
            configureRayTracer();
            return this;
        }

        /**
         * Sets the target distance for glossy and diffuse-glass beams.
         *
         * @param distance distance from hit point to target rectangle, must be positive
         * @return this builder
         */
        public Builder setBlurTargetDistance(double distance) {
            if (!Double.isFinite(distance) || alignZero(distance) <= 0) {
                throw new IllegalArgumentException("Blur target distance must be positive");
            }
            _blurTargetDistance = distance;
            configureRayTracer();
            return this;
        }

        /**
         * Attaches aggregate render profiling counters.
         *
         * @param renderStats statistics object, or {@code null} to disable profiling
         * @return this builder
         */
        public Builder setRenderStats(RenderStats renderStats) {
            _renderStats = renderStats;
            configureRayTracer();
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
            configureRendering();

            if (_camera._rayTracer == null) {
                setRayTracer(new Scene("test"), RayTracerType.SIMPLE);
            }
            configureRayTracer();

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

        /**
         * Copies the rendering configuration into the built camera and resolves defaults.
         */
        private void configureRendering() {
            _camera.superSamplingEnabled = _superSamplingEnabled;
            _camera.samplingSamples = _samplingSamples;
            _camera.samplingPattern = Objects.requireNonNull(_samplingPattern, "Sampling pattern must not be null");
            _camera.samplingShape = Objects.requireNonNull(_samplingShape, "Sampling shape must not be null");
            _camera.samplingSeed = _samplingSeed;
            _camera.samplingWidth = _samplingWidth > 0
                    ? Math.min(_samplingWidth, _camera._pixelWidth)
                    : _camera._pixelWidth;
            _camera.samplingHeight = _samplingHeight > 0
                    ? Math.min(_samplingHeight, _camera._pixelHeight)
                    : _camera._pixelHeight;
            _camera.samplingBoard = _superSamplingEnabled && _samplingSamples > 1
                    ? createCameraSamplingBoard()
                    : null;
            _camera.threadsCount = _threadsCount == -2
                    ? resolveAutomaticThreads()
                    : _threadsCount;
            _camera.printInterval = _printInterval;
            _camera.renderStats = _renderStats;
        }

        /**
         * Creates the immutable sampling board used by camera super sampling.
         *
         * @return sampling board sized to stay inside one output pixel
         */
        private SamplingBoard createCameraSamplingBoard() {
            return switch (_samplingShape) {
                case SQUARE -> SamplingBoard.rectangle(
                        _camera.samplingWidth, _camera.samplingHeight,
                        _samplingSamples, _samplingPattern, _samplingSeed);
                case CIRCLE -> SamplingBoard.circle(
                        Math.min(_camera.samplingWidth, _camera.samplingHeight) / 2d,
                        _samplingSamples, _samplingPattern, _samplingSeed);
                case RECTANGLE -> throw new IllegalArgumentException("Camera sampling shape must be SQUARE or CIRCLE");
            };
        }

        /**
         * Resolves automatic raw-thread count.
         *
         * @return effective thread count
         */
        private int resolveAutomaticThreads() {
            return Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
        }

        /**
         * Applies ray tracer settings when the current ray tracer supports them.
         */
        private void configureRayTracer() {
            if (_camera._rayTracer instanceof SimpleRayTracer simpleRayTracer) {
                simpleRayTracer
                        .setBlurEnabled(_blurEnabled)
                        .setBlurSamples(_blurSamples)
                        .setBlurSamplingPattern(_blurSamplingPattern)
                        .setBlurSamplingShape(_blurSamplingShape)
                        .setBlurSamplingSeed(_blurSamplingSeed)
                        .setBlurTargetDistance(_blurTargetDistance)
                        .setRenderStats(_renderStats);
            }
        }

        /**
         * Validates positive finite sampling dimensions.
         */
        private static void validatePositive(double value, String name) {
            if (!Double.isFinite(value) || alignZero(value) <= 0d) {
                throw new IllegalArgumentException(name + " must be positive");
            }
        }
    }
}
