package renderer;

import primitives.Color;
import primitives.Ray;
import scene.Scene;

/**
 * Base class for ray tracers.
 * <p>
 * A ray tracer receives a {@link Ray} and returns the {@link Color} that should
 * be written to the corresponding pixel.
 * </p>
 */
abstract class RayTracerBase {
   /** Scene to trace rays in. */
   protected final Scene _scene;

   /**
    * Constructs a ray tracer for a given scene.
    *
    * @param scene the scene
    */
   RayTracerBase(Scene scene) {
      _scene = scene;
   }

   /**
    * Traces a ray and returns the computed color.
    *
    * @param ray ray to trace
    * @return resulting color
    */
   abstract Color traceRay(Ray ray);
}
