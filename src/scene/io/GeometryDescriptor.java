package scene.io;

import primitives.Point;

/**
 * Marker interface for parsed geometry definitions.
 */
sealed interface GeometryDescriptor permits SphereDescriptor, TriangleDescriptor { }

/** Parsed sphere definition. */
record SphereDescriptor(Point center, double radius) implements GeometryDescriptor { }

/** Parsed triangle definition. */
record TriangleDescriptor(Point p0, Point p1, Point p2) implements GeometryDescriptor { }
