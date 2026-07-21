package renderer;

import primitives.Point;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Target area sampling utility for super-sampling features.
 */
public final class Blackboard {
    /**
     * Shape of the 2D sampling area.
     */
    public enum Shape {
        /** Square sampling area centered on the target point. */
        SQUARE,
        /** Circular sampling area centered on the target point. */
        CIRCLE
    }

    /**
     * Pattern used to distribute samples across the sampling area.
     */
    public enum Pattern {
        /** Deterministic cell-center grid sampling. */
        GRID,
        /** Deterministic grid sampling with per-cell jitter. */
        JITTER,
        /** Pseudo-random sampling across the target area. */
        RANDOM
    }

    /** Sampling area shape. */
    private final Shape shape;
    /** Sampling distribution pattern. */
    private final Pattern pattern;
    /** Requested number of samples. */
    private final int samples;
    /** Random source used for jittered/random sampling. */
    private final Random random;

    /**
     * Creates a blackboard sampler.
     *
     * @param shape sampling area shape
     * @param pattern sampling distribution pattern
     * @param samples number of points to sample
     * @param seed deterministic seed for jittered/random sampling
     */
    public Blackboard(Shape shape, Pattern pattern, int samples, long seed) {
        if (samples < 1) {
            throw new IllegalArgumentException("samples must be at least 1");
        }
        this.shape = shape;
        this.pattern = pattern;
        this.samples = samples;
        this.random = new Random(seed);
    }

    /**
     * Generates sampled points on a plane centered at {@code center}.
     *
     * @param center center of sampling area
     * @param axisX first normalized axis in the plane
     * @param axisY second normalized axis in the plane
     * @param radius half-size (square) or radius (circle)
     * @return sampled points including center when samples=1
     */
    public List<Point> samplePoints(Point center, Vector axisX, Vector axisY, double radius) {
        if (samples == 1 || radius == 0) {
            return List.of(center);
        }

        return switch (pattern) {
            case GRID -> gridPoints(center, axisX, axisY, radius, false);
            case JITTER -> gridPoints(center, axisX, axisY, radius, true);
            case RANDOM -> randomPoints(center, axisX, axisY, radius);
        };
    }

    /**
     * Generates grid-based sample points.
     *
     * @param center sampling area center
     * @param axisX first sampling plane axis
     * @param axisY second sampling plane axis
     * @param radius half-size or radius of the sampling area
     * @param jitter true to jitter each grid cell
     * @return generated sample points
     */
    private List<Point> gridPoints(Point center, Vector axisX, Vector axisY, double radius, boolean jitter) {
        int side = (int) Math.ceil(Math.sqrt(samples));
        double cell = (2d * radius) / side;

        List<Point> points = new ArrayList<>(samples);
        for (int row = 0; row < side && points.size() < samples; row++) {
            for (int col = 0; col < side && points.size() < samples; col++) {
                double jitterX = jitter ? (random.nextDouble() - 0.5) * cell : 0;
                double jitterY = jitter ? (random.nextDouble() - 0.5) * cell : 0;

                double x = -radius + (col + 0.5) * cell + jitterX;
                double y = radius - (row + 0.5) * cell + jitterY;

                if (shape == Shape.CIRCLE && x * x + y * y > radius * radius) {
                    continue;
                }
                points.add(offsetPoint(center, axisX, axisY, x, y));
            }
        }

        if (points.isEmpty()) {
            points.add(center);
        }
        return points;
    }

    /**
     * Generates pseudo-random sample points.
     *
     * @param center sampling area center
     * @param axisX first sampling plane axis
     * @param axisY second sampling plane axis
     * @param radius half-size or radius of the sampling area
     * @return generated sample points
     */
    private List<Point> randomPoints(Point center, Vector axisX, Vector axisY, double radius) {
        List<Point> points = new ArrayList<>(samples);
        int tries = 0;
        while (points.size() < samples && tries < samples * 10) {
            tries++;
            double x = -radius + random.nextDouble() * 2d * radius;
            double y = -radius + random.nextDouble() * 2d * radius;
            if (shape == Shape.CIRCLE && x * x + y * y > radius * radius) {
                continue;
            }
            points.add(offsetPoint(center, axisX, axisY, x, y));
        }

        if (points.isEmpty()) {
            points.add(center);
        }
        return points;
    }

    /**
     * Maps a 2D local offset to a 3D point.
     *
     * @param center sampling center
     * @param axisX first sampling plane axis
     * @param axisY second sampling plane axis
     * @param x local offset along {@code axisX}
     * @param y local offset along {@code axisY}
     * @return offset point
     */
    private Point offsetPoint(Point center, Vector axisX, Vector axisY, double x, double y) {
        Point p = center;
        if (x != 0) {
            p = p.add(axisX.scale(x));
        }
        if (y != 0) {
            p = p.add(axisY.scale(y));
        }
        return p;
    }
}
