package net.hachimi.client.impl.gui.click.impl;

import net.hachimi.client.api.module.Module;
import net.hachimi.client.api.render.RenderManager;
import net.hachimi.client.impl.gui.click.component.Button;
import net.hachimi.client.impl.gui.click.component.Frame;
import net.hachimi.client.impl.manager.world.sound.SoundManager;
import net.hachimi.client.impl.module.client.ClickGuiModule;
import net.hachimi.client.init.Managers;
import net.hachimi.client.util.math.timer.CacheTimer;
import net.hachimi.client.util.math.timer.Timer;
import net.minecraft.client.gui.DrawContext;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Search box shown at the top of the Client panel.
 *
 * <ul>
 *    <li>Left click — start / resume typing</li>
 *    <li>Right click — clear the search</li>
 *    <li>Ctrl+F anywhere in the GUI — focus the box</li>
 *    <li>Enter — stop typing but keep the filter, Esc — clear and stop typing</li>
 *    <li>Ctrl+Backspace — clear the text</li>
 * </ul>
 *
 * The filter matches module names and descriptions. Clicking elsewhere in the GUI only stops typing,
 * so a module found by search can be toggled without losing the filter.
 */
public class SearchButton extends Button {
   public static String SEARCH_TEXT = null;
   private char[] buffer = "".toCharArray();
   private boolean typing;
   private boolean idling;
   private final Timer idleTimer = new CacheTimer();

   public SearchButton(Frame frame, float x, float y) {
      super(frame, x, y, 103.0F, 13.0F);
   }

   public static boolean isSearching() {
      return SEARCH_TEXT != null && !SEARCH_TEXT.isBlank();
   }

   public static boolean matches(Module module) {
      if (!isSearching()) {
         return true;
      } else {
         String query = SEARCH_TEXT.trim().toLowerCase();
         if (module.getName().toLowerCase().contains(query)) {
            return true;
         } else {
            String description = module.getDescription();
            return description != null && description.toLowerCase().contains(query);
         }
      }
   }

   public void render(DrawContext context, float mouseX, float mouseY, float delta) {
      this.render(context, this.x, this.y, mouseX, mouseY, delta);
   }

   public void render(DrawContext context, float ix, float iy, float mouseX, float mouseY, float delta) {
      this.setHeight((float)RenderManager.textHeight() + 4.0F);
      this.x = ix;
      this.y = iy;
      int unfilledColor = ClickGuiModule.getInstance().fixTransparency(this.typing ? 1426063360 : 855638016);
      int borderColor = ClickGuiModule.getInstance().getColor(this.typing ? 1.7F : 1.0F);
      this.fill(context, (double)(ix + 1.0F), (double)iy, (double)1.0F, (double)this.getHeight(), borderColor);
      this.fill(context, (double)(ix + this.getWidth() - 2.0F), (double)iy, (double)1.0F, (double)this.getHeight(), borderColor);
      this.fill(context, (double)(ix + 1.0F), (double)(iy - 1.0F), (double)(this.getWidth() - 2.0F), (double)1.0F, borderColor);
      this.fill(context, (double)(ix + 1.0F), (double)(iy + this.getHeight()), (double)(this.getWidth() - 2.0F), (double)1.0F, borderColor);
      this.rect(context, unfilledColor);
      int whiteText = -1;
      int grayText = -5592406;
      String text = new String(this.buffer);
      String renderText;
      int color;
      if (this.typing) {
         renderText = text + this.getInsertionPoint();
         color = whiteText;
      } else if (!text.isEmpty()) {
         renderText = text;
         color = whiteText;
      } else {
         renderText = "Search (Ctrl+F)";
         color = grayText;
      }

      this.drawStringScaled(context, renderText, ix + 5.0F * ClickGuiModule.CLICK_GUI_SCALE, iy + 3.5F * ClickGuiModule.CLICK_GUI_SCALE, color);
      if (isSearching()) {
         int found = 0;

         for(Module module : Managers.MODULE.getModules()) {
            if (matches(module)) {
               ++found;
            }
         }

         String count = String.valueOf(found);
         float countX = ix + this.getWidth() - 5.0F * ClickGuiModule.CLICK_GUI_SCALE - (float)RenderManager.textWidth(count) * ClickGuiModule.CLICK_GUI_SCALE;
         this.drawStringScaled(context, count, countX, iy + 3.5F * ClickGuiModule.CLICK_GUI_SCALE, found == 0 ? -43691 : grayText);
      }

   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isWithin(mouseX, mouseY)) {
         if (button == 0) {
            this.typing = true;
         } else if (button == 1) {
            this.clear();
         }

         if (ClickGuiModule.getInstance().getSounds()) {
            Managers.SOUND.playSound(SoundManager.GUI_CLICK);
         }
      } else {
         this.typing = false;
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.typing) {
         switch (keyCode) {
            case 256:
               this.clear();
               break;
            case 257:
            case 335:
               this.typing = false;
               break;
            case 259:
               if ((modifiers & 2) != 0) {
                  this.buffer = "".toCharArray();
                  SEARCH_TEXT = null;
               } else if (this.buffer.length != 0) {
                  this.buffer = ArrayUtils.remove(this.buffer, this.buffer.length - 1);
                  SEARCH_TEXT = new String(this.buffer);
               }
         }
      }

   }

   public void charTyped(char character, int modifiers) {
      if (this.typing && this.buffer.length < 32) {
         this.buffer = ArrayUtils.add(this.buffer, character);
         SEARCH_TEXT = new String(this.buffer);
      }

   }

   public void startTyping() {
      this.typing = true;
   }

   public void clear() {
      this.typing = false;
      SEARCH_TEXT = null;
      this.buffer = "".toCharArray();
   }

   public boolean isTyping() {
      return this.typing;
   }

   public String getInsertionPoint() {
      if (this.idleTimer.passed(250)) {
         this.idling = !this.idling;
         this.idleTimer.reset();
      }

      return this.idling && this.typing ? "_" : "";
   }
}
