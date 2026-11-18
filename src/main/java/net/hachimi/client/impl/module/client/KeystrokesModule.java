package net.hachimi.client.impl.module.client;

import net.hachimi.client.api.config.Config;
import net.hachimi.client.api.config.NumberDisplay;
import net.hachimi.client.api.config.setting.BooleanConfig;
import net.hachimi.client.api.config.setting.NumberConfig;
import net.hachimi.client.api.module.ModuleCategory;
import net.hachimi.client.api.module.ToggleModule;
import net.hachimi.client.api.render.RenderManager;
import net.hachimi.client.impl.event.MouseClickEvent;
import net.hachimi.client.impl.event.gui.hud.RenderOverlayEvent;
import net.hachimi.client.util.math.PerSecondCounter;
import net.hachimi.client.util.render.ColorUtil;
import net.hachimi.client.util.render.animation.Animation;
import net.hachimi.client.util.render.animation.Easing;
import net.hachimi.eventbus.annotation.EventListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

/**
 * WASD / space / mouse buttons on the HUD with a press animation and clicks-per-second,
 * drawn in the client colour like the rest of the HUD.
 */
public class KeystrokesModule extends ToggleModule {
   Config<Integer> xConfig = this.register(new NumberConfig("X", "The x position of the keystrokes", 0, 4, 1500, NumberDisplay.DEFAULT));
   Config<Integer> yConfig = this.register(new NumberConfig("Y", "The y position of the keystrokes", 0, 60, 1000, NumberDisplay.DEFAULT));
   Config<Integer> sizeConfig = this.register(new NumberConfig("Size", "The size of each key", 14, 20, 32, NumberDisplay.DEFAULT));
   Config<Boolean> mouseConfig = this.register(new BooleanConfig("Mouse", "Shows the left and right mouse buttons", true));
   Config<Boolean> cpsConfig = this.register(new BooleanConfig("CPS", "Shows clicks per second on the mouse buttons", true, () -> this.mouseConfig.getValue()));
   Config<Boolean> spaceConfig = this.register(new BooleanConfig("Space", "Shows the jump key", true));
   private final PerSecondCounter leftClicks = new PerSecondCounter();
   private final PerSecondCounter rightClicks = new PerSecondCounter();
   private final Animation[] animations = new Animation[7];

   public KeystrokesModule() {
      super("Keystrokes", "Shows movement keys and mouse buttons on the HUD", ModuleCategory.CLIENT);

      for(int i = 0; i < this.animations.length; ++i) {
         this.animations[i] = new Animation(false, 120.0F, Easing.CUBIC_IN_OUT);
      }

   }

   @EventListener
   public void onMouseClick(MouseClickEvent event) {
      if (event.getAction() == 1 && mc.currentScreen == null) {
         if (event.getButton() == 0) {
            this.leftClicks.updateCounter();
         } else if (event.getButton() == 1) {
            this.rightClicks.updateCounter();
         }

      }
   }

   @EventListener
   public void onRenderOverlayPost(RenderOverlayEvent.Post event) {
      if (mc.player != null && mc.options != null) {
         DrawContext context = event.getContext();
         float size = (float)(Integer)this.sizeConfig.getValue();
         float gap = 2.0F;
         float x = (float)(Integer)this.xConfig.getValue();
         float y = (float)(Integer)this.yConfig.getValue();
         this.drawKey(context, 0, mc.options.forwardKey, "W", x + size + gap, y, size, size);
         y += size + gap;
         this.drawKey(context, 1, mc.options.leftKey, "A", x, y, size, size);
         this.drawKey(context, 2, mc.options.backKey, "S", x + size + gap, y, size, size);
         this.drawKey(context, 3, mc.options.rightKey, "D", x + (size + gap) * 2.0F, y, size, size);
         y += size + gap;
         float fullWidth = size * 3.0F + gap * 2.0F;
         if ((Boolean)this.mouseConfig.getValue()) {
            float half = (fullWidth - gap) / 2.0F;
            boolean cps = (Boolean)this.cpsConfig.getValue();
            this.drawKey(context, 4, mc.options.attackKey, cps ? this.leftClicks.getPerSecond() + " CPS" : "LMB", x, y, half, size);
            this.drawKey(context, 5, mc.options.useKey, cps ? this.rightClicks.getPerSecond() + " CPS" : "RMB", x + half + gap, y, half, size);
            y += size + gap;
         }

         if ((Boolean)this.spaceConfig.getValue()) {
            this.drawKey(context, 6, mc.options.jumpKey, "-----", x, y, fullWidth, size * 0.6F);
         }

      }
   }

   private void drawKey(DrawContext context, int index, KeyBinding key, String label, float x, float y, float width, float height) {
      Animation animation = this.animations[index];
      boolean pressed = key.isPressed();
      if (animation.getState() != pressed) {
         animation.setState(pressed);
      }

      float factor = (float)animation.getFactor();
      int background = ColorUtil.fixTransparency(-1341320169, 1.0F - factor * 0.5F);
      RenderManager.rect(context.getMatrices(), (double)x, (double)y, (double)width, (double)height, background);
      if (factor > 0.01F) {
         RenderManager.rect(context.getMatrices(), (double)x, (double)y, (double)width, (double)height, ColorsModule.getInstance().getRGB((int)(170.0F * factor)));
      }

      int textColor = pressed ? -1 : -3355444;
      float textX = x + (width - (float)RenderManager.textWidth(label)) / 2.0F;
      float textY = y + (height - (float)RenderManager.textHeight()) / 2.0F + 1.0F;
      RenderManager.renderText(context, label, textX, textY, textColor);
   }
}
