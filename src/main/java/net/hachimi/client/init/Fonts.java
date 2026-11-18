package net.hachimi.client.init;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import net.hachimi.client.Hachimi;
import net.hachimi.client.impl.font.AWTFontRenderer;
import net.hachimi.client.impl.font.VanillaTextRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;

public class Fonts {
   public static final VanillaTextRenderer VANILLA = new VanillaTextRenderer();
   public static final String DEFAULT_FONT_FILE_PATH = "assets/hachimiclient/font/verdana.ttf";
   public static String FONT_FILE_PATH = "assets/hachimiclient/font/verdana.ttf";
   public static AWTFontRenderer CLIENT;
   public static AWTFontRenderer CLIENT_UNSCALED;
   public static float FONT_SIZE = 9.0F;
   private static boolean initialized;

   public static void init() {
      if (!initialized) {
         Hachimi.CONFIG.loadFonts();
         loadFonts();
         Hachimi.info("Loaded fonts!");
         initialized = true;
      }
   }

   public static void loadFonts() {
      try (InputStream scaled = openFontStream(); InputStream unscaled = openFontStream()) {
         CLIENT = new AWTFontRenderer(scaled, FONT_SIZE);
         CLIENT_UNSCALED = new AWTFontRenderer(unscaled, 9.0F);
      } catch (Exception e) {
         Hachimi.error("Failed to load font {} — falling back to vanilla text", FONT_FILE_PATH);
         e.printStackTrace();
      }

   }

   /**
    * Opens the configured font. Paths starting with "assets/" are read from the client's own resources,
    * everything else is treated as a file on disk (set with the customfont command).
    */
   private static InputStream openFontStream() throws IOException {
      if (FONT_FILE_PATH.startsWith("assets")) {
         String prefix = "assets/hachimiclient/";
         if (FONT_FILE_PATH.startsWith(prefix)) {
            Identifier identifier = Identifier.of("hachimiclient", FONT_FILE_PATH.substring(prefix.length()));
            Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(identifier);
            if (resource.isPresent()) {
               return resource.get().getInputStream();
            }
         }

         InputStream fallback = Fonts.class.getClassLoader().getResourceAsStream(FONT_FILE_PATH);
         if (fallback == null) {
            throw new IOException("Missing font resource " + FONT_FILE_PATH);
         }

         return fallback;
      } else {
         return new FileInputStream(FONT_FILE_PATH);
      }
   }

   public static void closeFonts() {
      if (CLIENT != null) {
         CLIENT.close();
      }

      if (CLIENT_UNSCALED != null) {
         CLIENT_UNSCALED.close();
      }

   }

   public static void setSize(float size) {
      FONT_SIZE = size;

      try (InputStream stream = openFontStream()) {
         CLIENT = new AWTFontRenderer(stream, FONT_SIZE);
      } catch (Exception e) {
         Hachimi.error("Failed to resize font {}", FONT_FILE_PATH);
         e.printStackTrace();
      }

   }

   public static boolean isInitialized() {
      return initialized;
   }
}
