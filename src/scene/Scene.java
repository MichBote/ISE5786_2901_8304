package scene;

import geometries.impl.Geometries;
import lighting.AmbientLight;
import primitives.Color;

/**
 * Passive data structure representing a 3D scene.
 * <p>
 * Holds scene data only (no rendering logic): name, background color, ambient
 * light and geometries.
 * </p>
 */
public class Scene {
   /** Scene name. */
   public final String name;

   /** Background color (default: black). */
   public Color background = Color.BLACK;

   /** Ambient light (default: none). */
   public AmbientLight ambientLight = AmbientLight.NONE;

   /** Scene geometries (default: empty collection). */
   public Geometries geometries = new Geometries();

   /**
    * Constructs a scene with the given name.
    *
    * @param name scene name
    */
   public Scene(String name) {
      this.name = name;
   }

   /** Sets the background color (chaining). */
   public Scene setBackground(Color background) {
      this.background = background;
      return this;
   }

   /** Sets the ambient light (chaining). */
   public Scene setAmbientLight(AmbientLight ambientLight) {
      this.ambientLight = ambientLight;
      return this;
   }

   /** Sets the geometries collection (chaining). */
   public Scene setGeometries(Geometries geometries) {
      this.geometries = geometries;
      return this;
   }
}
