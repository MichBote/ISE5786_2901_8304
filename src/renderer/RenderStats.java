package renderer;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Thread-safe aggregate rendering counters for profiling preview renders.
 * <p>
 * The renderer only updates totals; callers decide when to print or reset them.
 * </p>
 */
public final class RenderStats {
    /** Number of primary camera rays traced. */
    private final LongAdder primaryRays = new LongAdder();
    /** Number of recursive reflected rays traced. */
    private final LongAdder reflectionRays = new LongAdder();
    /** Number of recursive transmitted/refracted rays traced. */
    private final LongAdder transparencyRays = new LongAdder();
    /** Number of shadow rays traced toward light sources. */
    private final LongAdder shadowRays = new LongAdder();
    /** Number of scene intersection queries. */
    private final LongAdder intersectionCalculations = new LongAdder();
    /** Total render time accumulated by profiled camera renders. */
    private final AtomicLong renderNanos = new AtomicLong();

    /**
     * Clears all counters.
     */
    public void reset() {
        primaryRays.reset();
        reflectionRays.reset();
        transparencyRays.reset();
        shadowRays.reset();
        intersectionCalculations.reset();
        renderNanos.set(0L);
    }

    /**
     * Adds primary camera rays.
     *
     * @param count number of rays to add
     */
    public void addPrimaryRays(long count) {
        primaryRays.add(count);
    }

    /**
     * Adds recursive reflected rays.
     *
     * @param count number of rays to add
     */
    public void addReflectionRays(long count) {
        reflectionRays.add(count);
    }

    /**
     * Adds recursive transparency/refraction rays.
     *
     * @param count number of rays to add
     */
    public void addTransparencyRays(long count) {
        transparencyRays.add(count);
    }

    /**
     * Adds shadow rays.
     *
     * @param count number of rays to add
     */
    public void addShadowRays(long count) {
        shadowRays.add(count);
    }

    /**
     * Adds scene intersection calculations.
     *
     * @param count number of intersection queries to add
     */
    public void addIntersectionCalculations(long count) {
        intersectionCalculations.add(count);
    }

    /**
     * Adds elapsed render time.
     *
     * @param nanos elapsed nanoseconds
     */
    public void addRenderNanos(long nanos) {
        renderNanos.addAndGet(nanos);
    }

    /**
     * Returns primary camera rays.
     *
     * @return primary ray count
     */
    public long primaryRays() {
        return primaryRays.sum();
    }

    /**
     * Returns recursive reflected rays.
     *
     * @return reflection ray count
     */
    public long reflectionRays() {
        return reflectionRays.sum();
    }

    /**
     * Returns recursive transparency/refraction rays.
     *
     * @return transparency ray count
     */
    public long transparencyRays() {
        return transparencyRays.sum();
    }

    /**
     * Returns shadow rays.
     *
     * @return shadow ray count
     */
    public long shadowRays() {
        return shadowRays.sum();
    }

    /**
     * Returns scene intersection queries.
     *
     * @return intersection calculation count
     */
    public long intersectionCalculations() {
        return intersectionCalculations.sum();
    }

    /**
     * Returns accumulated render time in nanoseconds.
     *
     * @return render time in nanoseconds
     */
    public long renderNanos() {
        return renderNanos.get();
    }

    /**
     * Returns accumulated render time in seconds.
     *
     * @return render time in seconds
     */
    public double renderSeconds() {
        return renderNanos() / 1_000_000_000d;
    }

    /**
     * Formats the aggregate counters for benchmark output.
     *
     * @return one-line summary
     */
    public String summary() {
        return String.format(
                "primary=%d, reflection=%d, transparency=%d, shadow=%d, intersections=%d, render=%.3fs",
                primaryRays(), reflectionRays(), transparencyRays(), shadowRays(),
                intersectionCalculations(), renderSeconds());
    }
}
