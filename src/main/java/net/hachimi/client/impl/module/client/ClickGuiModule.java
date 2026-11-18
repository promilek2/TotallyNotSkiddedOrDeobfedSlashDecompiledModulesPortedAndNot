package net.hachimi.client.impl.module.client;

import net.hachimi.client.Hachimi;
import net.hachimi.client.api.config.Config;
import net.hachimi.client.api.config.setting.BooleanConfig;
import net.hachimi.client.api.config.setting.EnumConfig;
import net.hachimi.client.api.config.setting.NumberConfig;
import net.hachimi.client.api.module.ModuleCategory;
import net.hachimi.client.api.module.ToggleModule;
import net.hachimi.client.impl.event.config.ConfigUpdateEvent;
import net.hachimi.client.impl.gui.click.ClickGuiScreen;
import net.hachimi.client.util.render.animation.Animation;
import net.hachimi.client.util.render.animation.Easing;
import net.hachimi.eventbus.annotation.EventListener;
import net.hachimi.eventbus.event.StageEvent;

public class ClickGuiModule extends ToggleModule {
   private static ClickGuiModule INSTANCE;
   Config<Boolean> blurConfig = this.register(new BooleanConfig("Blur", "Adds a blur background to the panels", false));
   Config<Float> scaleConfig = this.register(new NumberConfig("Scale", "The gui scale", 0.5F, 1.0F, 3.0F));
   Config<Integer> scrollSpeedConfig = this.register(new NumberConfig("ScrollSpeed", "The speed of GUI scrolling", 5, 30, 100));
   Config<Boolean> soundsConfig = this.register(new BooleanConfig("Sounds", "Click sounds", true));
   Config<Boolean> descriptionsConfig = this.register(new BooleanConfig("Descriptions", "Shows feature descriptions", true));
   Config<Boolean> bindsConfig = this.register(new BooleanConfig("Binds", "Shows each module's keybind on the right side of its row", true));
   Config<Boolean> countersConfig = this.register(new BooleanConfig("Counters", "Shows the enabled/total module count in each panel header", true));
   Config<SearchMode> searchModeConfig = this.register(new EnumConfig("SearchMode", "Dim greys out modules that don't match the search, Hide removes them from the panels", ClickGuiModule.SearchMode.HIDE, ClickGuiModule.SearchMode.values()));
   Config<Boolean> scrollHoveredConfig = this.register(new BooleanConfig("ScrollHovered", "Scrolls only the panel under the cursor (hold shift to scroll every panel)", true));
   public static ClickGuiScreen CLICK_GUI_SCREEN;
   public static float CLICK_GUI_SCALE = 1.0F;
   private final Animation openCloseAnimation;
   private final Animation transparencyAnimation;

   public ClickGuiModule() {
      super("ClickGui", "Opens the clickgui screen", ModuleCategory.CLIENT, 344);
      this.openCloseAnimation = new Animation(false, 400.0F, Easing.BACK_OUT);
      this.transparencyAnimation = new Animation(false, 300.0F, Easing.CUBIC_IN_OUT);
      INSTANCE = this;
   }

   public static ClickGuiModule getInstance() {
      return INSTANCE;
   }

   public void onEnable() {
      if (mc.player != null && mc.world != null) {
         if (CLICK_GUI_SCREEN == null) {
            CLICK_GUI_SCALE = (Float)this.scaleConfig.getValue();
            CLICK_GUI_SCREEN = new ClickGuiScreen(this);
            Hachimi.CONFIG.loadClickGui();
         }

         if (CLICK_GUI_SCALE != (Float)this.scaleConfig.getValue()) {
            CLICK_GUI_SCALE = (Float)this.scaleConfig.getValue();
            CLICK_GUI_SCREEN = new ClickGuiScreen(this);
         }

         this.openCloseAnimation.setState(true);
         this.transparencyAnimation.setState(true);
         this.openCloseAnimation.reset();
         this.transparencyAnimation.reset();
         mc.setScreen(CLICK_GUI_SCREEN);
      } else {
         this.toggle();
      }
   }

   public void onDisable() {
      if (mc.player != null && mc.world != null) {
         if (CLICK_GUI_SCREEN != null) {
            Hachimi.CONFIG.saveClickGui();
         }

         mc.player.closeScreen();
         this.openCloseAnimation.setState(false);
         this.transparencyAnimation.setState(false);
      } else {
         this.toggle();
      }
   }

   @EventListener
   public void onConfigUpdate(ConfigUpdateEvent event) {
      if (event.getStage() == StageEvent.EventStage.POST && event.getConfig() == this.scaleConfig && mc.world == null) {
         CLICK_GUI_SCALE = (Float)this.scaleConfig.getValue();
      }

   }

   public int getColor() {
      return ColorsModule.getInstance().getColor((int)((double)100.0F * this.openCloseAnimation.getFactor())).getRGB();
   }

   public int getColor(int a) {
      return ColorsModule.getInstance().getColor((int)((double)a * this.openCloseAnimation.getFactor())).getRGB();
   }

   public int getColor(float alpha) {
      return ColorsModule.getInstance().getColor((int)((double)(100.0F * alpha) * this.openCloseAnimation.getFactor())).getRGB();
   }

   public int getColor(int a, float alpha) {
      return ColorsModule.getInstance().getColor((int)((double)((float)a * alpha) * this.openCloseAnimation.getFactor())).getRGB();
   }

   public int fixTransparency(int color) {
      float alpha = this.getAlpha();
      if (alpha == 1.0F) {
         return color;
      } else {
         float colorAlpha = (float)(color >> 24 & 255);
         alpha = Math.max(0.0F, Math.min(1.0F, alpha));
         int colorAlphaInt = Math.max(10, (int)(colorAlpha * alpha));
         return colorAlphaInt << 24 | color & 16777215;
      }
   }

   public boolean getBlur() {
      return (Boolean)this.blurConfig.getValue();
   }

   public boolean getSounds() {
      return (Boolean)this.soundsConfig.getValue();
   }

   public boolean getDescriptions() {
      return (Boolean)this.descriptionsConfig.getValue();
   }

   public float getAlpha() {
      return (float)this.transparencyAnimation.getFactor();
   }

   public float getScaleFactor() {
      return (float)this.openCloseAnimation.getFactor();
   }

   public int getScrollSpeed() {
      return (Integer)this.scrollSpeedConfig.getValue();
   }

   public boolean getBinds() {
      return (Boolean)this.bindsConfig.getValue();
   }

   public boolean getCounters() {
      return (Boolean)this.countersConfig.getValue();
   }

   public SearchMode getSearchMode() {
      return (SearchMode)this.searchModeConfig.getValue();
   }

   public boolean getScrollHovered() {
      return (Boolean)this.scrollHoveredConfig.getValue();
   }

   public static enum SearchMode {
      DIM,
      HIDE;
   }
}
