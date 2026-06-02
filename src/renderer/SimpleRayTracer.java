package renderer;

import java.util.List;

import geometries.api.Intersectable.Intersection;
import primitives.Color;
import primitives.Ray;
import scene.Scene;

/**
 * A basic ray tracer implementation.
 * <p>
 * At this stage, the color is determined by geometry emission and attenuated
 * ambient light.
 * </p>
 */
class SimpleRayTracer extends RayTracerBase {

   /**
    * Constructs a simple ray tracer.
    *
    * @param scene the scene
    */
   SimpleRayTracer(Scene scene) {
      super(scene);
   }

   @Override
   Color traceRay(Ray ray) {
      List<Intersection> intersections = _scene.geometries.calcIntersections(ray);
      if (intersections == null) {
         return _scene.background;
      }

      Intersection closest = ray.findClosestIntersection(intersections);
      return calcColor(closest);
   }

   /**
    * Computes the color at an intersection point.
    *
    * @param intersection geometry-aware intersection point
    * @return computed color
    */
   private Color calcColor(Intersection intersection) {
      return intersection.geometry.getEmission()
         .add(_scene.ambientLight.getIntensity().scale(intersection.material.kA));
   }

}
