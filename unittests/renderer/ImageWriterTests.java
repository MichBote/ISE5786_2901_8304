package renderer;

import org.junit.jupiter.api.Test;

import primitives.Color;

/**
 * Usage tests for {@link ImageWriter}.
 * <p>
 * This is a visual test: it generates a PNG file under the project's images
 * folder. No assertions are used.
 * </p>
 */
class ImageWriterTests {
   /** Default constructor to satisfy documentation tools. */
   ImageWriterTests() { /* Default constructor to satisfy documentation tools */ }

   /** Image width in pixels (nX). */
   private static final int   IMAGE_WIDTH     = 800;
   /** Image height in pixels (nY). */
   private static final int   IMAGE_HEIGHT    = 500;
   /** Grid square size in pixels. */
   private static final int   SQUARE_SIZE     = 50;

   /** Background color (choose high contrast). */
   private static final Color BACKGROUND_COLOR = new Color(255, 255, 0);
   /** Grid color (choose high contrast). */
   private static final Color GRID_COLOR       = new Color(255, 0, 0);

   @Test
   void testImageWriter() {
      ImageWriter imageWriter = new ImageWriter(IMAGE_WIDTH, IMAGE_HEIGHT);

      for (int yIndex = 0; yIndex < IMAGE_HEIGHT; yIndex++) {
         final boolean isGridRow = yIndex % SQUARE_SIZE == 0;
         for (int xIndex = 0; xIndex < IMAGE_WIDTH; xIndex++) {
            final boolean isGridCol = xIndex % SQUARE_SIZE == 0;
            imageWriter.writePixel(xIndex, yIndex, (isGridRow || isGridCol) ? GRID_COLOR : BACKGROUND_COLOR);
         }
      }

      imageWriter.writeToImage("image-writer-grid");
   }
}
