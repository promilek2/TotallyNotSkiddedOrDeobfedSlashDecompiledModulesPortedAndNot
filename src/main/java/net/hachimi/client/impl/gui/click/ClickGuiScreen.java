package net.hachimi.client.impl.gui.click;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.hachimi.client.api.Hideable;
import net.hachimi.client.api.module.Module;
import net.hachimi.client.api.module.ModuleCategory;
import net.hachimi.client.api.render.RenderManager;
import net.hachimi.client.impl.gui.click.component.ScissorStack;
import net.hachimi.client.impl.gui.click.impl.config.CategoryFrame;
import net.hachimi.client.impl.gui.click.impl.config.ModuleButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.BindButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.ColorButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.ConfigButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.SliderButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.TextButton;
import net.hachimi.client.impl.module.client.ClickGuiModule;
import net.hachimi.client.util.Globals;
import net.hachimi.client.util.render.ColorUtil;
import net.hachimi.client.util.render.animation.Animation;
import net.hachimi.client.util.render.animation.Easing;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.Screen;

public class ClickGuiScreen extends Screen implements Globals {
   public static int MOUSE_X;
   public static int MOUSE_Y;
   public static boolean MOUSE_RIGHT_CLICK;
   public static boolean MOUSE_RIGHT_HOLD;
   public static boolean MOUSE_LEFT_CLICK;
   public static boolean MOUSE_LEFT_HOLD;
   public static final ScissorStack SCISSOR_STACK = new ScissorStack();
   private final List<CategoryFrame> frames = new CopyOnWriteArrayList();
   private final ClickGuiModule module;
   private String text;
   private final Animation animation;
   private boolean shouldCloseOnEsc;

   public ClickGuiScreen(ClickGuiModule module) {
      super(Text.literal("ClickGui"));
      this.animation = new Animation(false, 200.0F, Easing.LINEAR);
      this.shouldCloseOnEsc = true;
      this.module = module;
      float x = 15.0F;

      for(ModuleCategory category : ModuleCategory.values()) {
         CategoryFrame frame = new CategoryFrame(category, x, 15.0F);
         this.frames.add(frame);
         x += frame.getWidth() + 2.0F * ClickGuiModule.CLICK_GUI_SCALE;
      }

   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      context.getMatrices().push();
      this.renderAndScaleGUI(context);
      boolean hovering = false;

      for(CategoryFrame frame : this.frames) {
         if (frame.isWithin((float)mouseX, (float)mouseY) && MOUSE_LEFT_HOLD && this.checkDragging()) {
            frame.setDragging(true);
         }

         frame.render(context, (float)mouseX, (float)mouseY, delta);

         for(ModuleButton moduleButton : frame.getModuleButtons()) {
            if (moduleButton.isFilteredOut()) {
               continue;
            }

            if (moduleButton.isWithin((float)mouseX, (float)mouseY)) {
               this.text = moduleButton.getModule().getDescription();
               Module hovered = moduleButton.getModule();
               if (hovered instanceof Hideable) {
                  Hideable hideable = (Hideable)hovered;
                  if (!hideable.isHidden()) {
                     this.text = this.text + " [shown in arraylist]";
                  }
               }

               hovering = true;
               break;
            }

            if (moduleButton.isOpen()) {
               for(ConfigButton<?> configButton : moduleButton.getConfigButtons()) {
                  if (configButton.isWithin((float)mouseX, (float)mouseY)) {
                     this.text = configButton.getConfig().getDescription();
                     hovering = true;
                     break;
                  }
               }
            }
         }
      }

      context.getMatrices().pop();
      if (ClickGuiModule.getInstance().getDescriptions()) {
         this.animation.setState(hovering);
         if (this.animation.getFactor() > (double)0.01F) {
            context.getMatrices().scale(ClickGuiModule.CLICK_GUI_SCALE, ClickGuiModule.CLICK_GUI_SCALE, 0.0F);
            float j = 1.0F / ClickGuiModule.CLICK_GUI_SCALE;
            int width = RenderManager.textWidth(this.text);
            int hoverAlpha = (int)((double)60.0F * this.animation.getFactor());
            int unfilledColor = ClickGuiModule.getInstance().fixTransparency((new Color(0, 0, 0, 50 + hoverAlpha)).getRGB());
            float boxWidth = ((float)width + 4.0F) * ClickGuiModule.CLICK_GUI_SCALE;
            float boxHeight = ((float)RenderManager.textHeight() + 2.0F) * ClickGuiModule.CLICK_GUI_SCALE;
            float boxX = (float)mouseX + 10.0F;
            float boxY = (float)mouseY - 8.0F;
            if (boxX + boxWidth > (float)this.width) {
               boxX = Math.max(0.0F, (float)mouseX - 6.0F - boxWidth);
            }

            boxY = MathHelper.clamp(boxY, 0.0F, Math.max(0.0F, (float)this.height - boxHeight));
            RenderManager.rect(context.getMatrices(), (double)(boxX * j), (double)(boxY * j), (double)((float)width + 4.0F), (double)((float)RenderManager.textHeight() + 2.0F), unfilledColor);
            RenderManager.renderText(context, this.text, (boxX + 2.0F) * j, (boxY + 2.0F) * j, ColorUtil.fixTransparency(-1, (float)this.animation.getFactor()));
            context.getMatrices().scale(j, j, 0.0F);
         }
      }

      MOUSE_LEFT_CLICK = false;
      MOUSE_RIGHT_CLICK = false;
      MOUSE_X = mouseX;
      MOUSE_Y = mouseY;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
      if (mouseButton == 0) {
         MOUSE_LEFT_CLICK = true;
         MOUSE_LEFT_HOLD = true;
      } else if (mouseButton == 1) {
         MOUSE_RIGHT_CLICK = true;
         MOUSE_RIGHT_HOLD = true;
      }

      for(CategoryFrame frame : this.frames) {
         frame.mouseClicked(mouseX, mouseY, mouseButton);
      }

      return super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         MOUSE_LEFT_HOLD = false;
      } else if (button == 1) {
         MOUSE_RIGHT_HOLD = false;
      }

      for(CategoryFrame frame : this.frames) {
         frame.mouseReleased(mouseX, mouseY, button);
      }

      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      boolean onlyHovered = ClickGuiModule.getInstance().getScrollHovered() && !Screen.hasShiftDown();

      for(CategoryFrame frame : this.frames) {
         if (onlyHovered && !frame.isWithinTotal((float)mouseX, (float)mouseY)) {
            continue;
         }

         float scrolledY = (float)((double)frame.getY() + verticalAmount * (double)ClickGuiModule.getInstance().getScrollSpeed());
         scrolledY = MathHelper.clamp(scrolledY, -(frame.getTotalHeight() - 10.0F), 15.0F);
         frame.setPos(frame.getX(), scrolledY);
      }

      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 70 && (modifiers & 2) != 0) {
         for(CategoryFrame frame : this.frames) {
            if (frame.getSearchButton() != null) {
               frame.getSearchButton().startTyping();
            }
         }

         return true;
      }

      this.shouldCloseOnEsc = true;

      for(CategoryFrame frame : this.frames) {
         if (frame.getSearchButton() != null && frame.getSearchButton().isTyping()) {
            this.shouldCloseOnEsc = false;
         } else {
            for(ModuleButton moduleButton : frame.getModuleButtons()) {
               for(ConfigButton<?> configButton : moduleButton.getConfigButtons()) {
                  label81: {
                     if (configButton instanceof BindButton) {
                        BindButton bindButton = (BindButton)configButton;
                        if (bindButton.isListening()) {
                           break label81;
                        }
                     }

                     if (configButton instanceof TextButton) {
                        TextButton textButton = (TextButton)configButton;
                        if (textButton.isTyping()) {
                           break label81;
                        }
                     }

                     if (configButton instanceof ColorButton) {
                        ColorButton colorButton = (ColorButton)configButton;
                        if (colorButton.isTyping()) {
                           break label81;
                        }
                     }

                     if (!(configButton instanceof SliderButton)) {
                        continue;
                     }

                     SliderButton sliderButton = (SliderButton)configButton;
                     if (!sliderButton.isTyping()) {
                        continue;
                     }
                  }

                  this.shouldCloseOnEsc = false;
               }
            }
         }
      }

      for(CategoryFrame frame : this.frames) {
         frame.keyPressed(keyCode, scanCode, modifiers);
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean charTyped(char chr, int modifiers) {
      for(CategoryFrame frame : this.frames) {
         frame.charTyped(chr, modifiers);
      }

      return super.charTyped(chr, modifiers);
   }

   public boolean shouldPause() {
      return false;
   }

   public void close() {
      this.module.disable();
      MOUSE_LEFT_CLICK = false;
      MOUSE_LEFT_HOLD = false;
      MOUSE_RIGHT_CLICK = false;
      MOUSE_RIGHT_HOLD = false;
      super.close();
   }

   public boolean shouldCloseOnEsc() {
      return this.shouldCloseOnEsc;
   }

   private void renderAndScaleGUI(DrawContext context) {
      int backgroundColor = ClickGuiModule.getInstance().fixTransparency(-1156771817);
      RenderManager.rect(context.getMatrices(), (double)0.0F, (double)0.0F, (double)context.getScaledWindowWidth(), (double)context.getScaledWindowHeight(), backgroundColor);
      float currentProgress = ClickGuiModule.getInstance().getScaleFactor();
      if (currentProgress != 1.0F) {
         float centerX = (float)context.getScaledWindowWidth() / 2.0F;
         float centerY = (float)context.getScaledWindowHeight() / 2.0F;
         context.getMatrices().translate(centerX, centerY, 0.0F);
         float goal = currentProgress * 0.2F;
         context.getMatrices().scale(0.8F + goal, 0.8F + goal, 1.0F);
         context.getMatrices().translate(-centerX, -centerY, 0.0F);
      }
   }

   private boolean checkDragging() {
      for(CategoryFrame frame : this.frames) {
         if (frame.isDragging()) {
            return false;
         }
      }

      return true;
   }

   public List<CategoryFrame> getCategoryFrames() {
      return this.frames;
   }
}
