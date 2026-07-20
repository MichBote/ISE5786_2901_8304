package renderer;

/**
 * Sampling pattern used when generating a beam of rays.
 */
public enum SamplingPattern {
    /**
     * One deterministic sample at the center of each sampling cell.
     */
    GRID,

    /**
     * One randomly jittered sample per sampling cell.
     */
    JITTERED
}
