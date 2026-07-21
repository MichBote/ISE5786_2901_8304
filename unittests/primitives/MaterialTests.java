package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Material} glass-effect configuration.
 */
class MaterialTests {
    /**
     * Creates the material test fixture.
     */
    MaterialTests() {
    }

    /**
     * Verifies default blur values preserve ideal reflection/transparency behavior.
     */
    @Test
    void testDefaultBlurValues() {
        Material material = new Material();

        assertEquals(0d, material.glossyBlur, "Glossy blur should be zero by default");
        assertEquals(0d, material.transparencyBlur, "Transparency blur should be zero by default");
    }

    /**
     * Verifies blur setters are fluent and store the requested values.
     */
    @Test
    void testBlurSetters() {
        Material material = new Material();

        assertSame(material, material.setGlossyBlur(2.5), "Glossy blur setter should be fluent");
        assertEquals(2.5, material.glossyBlur, "Wrong glossy blur value");

        assertSame(material, material.setTransparencyBlur(1.25), "Transparency blur setter should be fluent");
        assertEquals(1.25, material.transparencyBlur, "Wrong transparency blur value");

        assertSame(material, material.setDiffuseBlur(0.75), "Diffuse blur alias should be fluent");
        assertEquals(0.75, material.transparencyBlur, "Diffuse blur alias should set transparency blur");
    }

    /**
     * Verifies invalid blur values are rejected.
     */
    @Test
    void testNegativeBlurRejected() {
        Material material = new Material();

        assertThrows(IllegalArgumentException.class, () -> material.setGlossyBlur(-1d),
                "Negative glossy blur should be rejected");
        assertThrows(IllegalArgumentException.class, () -> material.setTransparencyBlur(-1d),
                "Negative transparency blur should be rejected");
        assertThrows(IllegalArgumentException.class, () -> material.setDiffuseBlur(-1d),
                "Negative diffuse blur alias should be rejected");
        assertThrows(IllegalArgumentException.class, () -> material.setGlossyBlur(Double.NaN),
                "NaN glossy blur should be rejected");
    }
}
