package renderer;

/**
 * Sampling target shape used when generating local 2D sample offsets.
 */
public enum SamplingShape {
    /**
     * Square sampling area centered around the target point.
     */
    SQUARE,

    /**
     * Circular sampling area centered around the target point.
     */
    CIRCLE,

    /**
     * Rectangular sampling area retained for compatibility with existing callers.
     */
    RECTANGLE
}
