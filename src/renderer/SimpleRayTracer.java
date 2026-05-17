package renderer;

import java.util.List;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import scene.Scene;

/**
 * A basic ray tracer implementation.
 * <p>
 * At this stage, the color is determined solely by ambient light.
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
      List<Point> intersections = _scene.geometries.findIntersections(ray);
      if (intersections == null) {
         return _scene.background;
      }

      Point closest = ray.findClosestPoint(intersections);
      return calcColor(closest);
   }

   /**
    * Computes the color at an intersection point.
    *
    * @param intersection intersection point
    * @return computed color
    */
   private Color calcColor(Point intersection) {
      return _scene.ambientLight.getIntensity();
   }
}
