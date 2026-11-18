package net.hachimi.client.impl.gui.click.impl.config;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.hachimi.client.api.Hideable;
import net.hachimi.client.api.config.Config;
import net.hachimi.client.api.macro.Macro;
import net.hachimi.client.api.module.Module;
import net.hachimi.client.api.module.ToggleModule;
import net.hachimi.client.api.render.RenderManager;
import net.hachimi.client.impl.gui.click.component.Button;
import net.hachimi.client.impl.gui.click.impl.SearchButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.BindButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.CheckboxButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.ColorButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.ConfigButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.DropdownButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.SliderButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.TextButton;
import net.hachimi.client.impl.manager.world.sound.SoundManager;
import net.hachimi.client.impl.module.client.ClickGuiModule;
import net.hachimi.client.init.Managers;
import net.hachimi.client.util.render.animation.Animation;
import net.hachimi.client.util.render.animation.Easing;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class ModuleButton extends Button {
   private final Module module;
   private final List<ConfigButton<?>> configComponents = new CopyOnWriteArrayList();
   private float off;
   private boolean open;
   private final Animation settingsAnimation;

   public ModuleButton(Module module, CategoryFrame frame, float x, float y) {
      super(frame, x, y, 103.0F, 13.0F);
      this.settingsAnimation = new Animation(false, 200.0F, Easing.CUBIC_IN_OUT);
      this.module = module;

      for(Config<?> config : module.getConfigs()) {
         if (!config.getName().equalsIgnoreCase("Enabled")) {
            if (config.getValue() instanceof Boolean) {
               this.configComponents.add(new CheckboxButton(frame, this, (Config)config, x, y));
            } else if (config.getValue() instanceof Double) {
               this.configComponents.add(new SliderButton(frame, this, config, x, y));
            } else if (config.getValue() instanceof Float) {
               this.configComponents.add(new SliderButton(frame, this, config, x, y));
            } else if (config.getValue() instanceof Integer) {
               this.configComponents.add(new SliderButton(frame, this, config, x, y));
            } else if (config.getValue() instanceof Enum) {
               this.configComponents.add(new DropdownButton(frame, this, (Config)config, x, y));
            } else if (config.getValue() instanceof String) {
               this.configComponents.add(new TextButton(frame, this, (Config)config, x, y));
            } else if (config.getValue() instanceof Macro) {
               this.configComponents.add(new BindButton(frame, this, (Config)config, x, y));
            } else if (config.getValue() instanceof Color) {
               this.configComponents.add(new ColorButton(frame, this, (Config)config, x, y));
            }
         }
      }

      this.open = false;
   }

   public void render(DrawContext context, float mouseX, float mouseY, float delta) {
      this.render(context, this.x, this.y, mouseX, mouseY, delta);
   }

   public void render(DrawContext context, float ix, float iy, float mouseX, float mouseY, float delta) {
      float scaledTime;
      boolean var10000;
      label151: {
         this.setHeight((float)RenderManager.textHeight() + 4.0F);
         this.x = ix;
         this.y = iy;
         scaledTime = 1.0F;
         Module var10 = this.module;
         if (var10 instanceof ToggleModule t) {
            if (!((scaledTime = (float)t.getAnimation().getFactor()) > 0.01F)) {
               var10000 = false;
               break label151;
            }
         }

         var10000 = true;
      }

      boolean fill = var10000;
      scaledTime *= 1.7F;
      if (this.module.getName().equalsIgnoreCase("ClickGui")) {
         scaledTime = 1.7F;
      }

      boolean state = this.isWithin(mouseX, mouseY);
      if (state != this.hoverAnimation.getState()) {
         this.hoverAnimation.setState(state);
      }

      boolean b2 = SearchButton.isSearching();
      boolean b1 = b2 && !SearchButton.matches(this.module);
      int hoverAlpha = (int)((double)(b1 ? 20 : 45) * MathHelper.clamp(this.hoverAnimation.getFactor(), (double)0.0F, (double)1.0F));
      int hoverAlpha2 = (int)((double)(b1 ? 15 : 35) * MathHelper.clamp(this.hoverAnimation.getFactor(), (double)0.0F, (double)1.0F));
      int unfilledColor = ClickGuiModule.getInstance().fixTransparency((new Color(hoverAlpha, hoverAlpha, hoverAlpha + 10, 30 + hoverAlpha)).getRGB());
      this.rect(context, fill ? ClickGuiModule.getInstance().getColor((b1 ? 35 : 80) + hoverAlpha2, scaledTime) : unfilledColor);
      if (fill && scaledTime > 0.99F) {
         this.fill(context, (double)ix, (double)iy, (double)ClickGuiModule.CLICK_GUI_SCALE, (double)(this.height * ClickGuiModule.CLICK_GUI_SCALE), ClickGuiModule.getInstance().getColor(220));
      }

      int whiteText = -1;
      int grayText = -5592406;
      int colorText = scaledTime > 0.99F ? whiteText : grayText;
      if (b2) {
         colorText = b1 ? grayText : whiteText;
      }

      this.drawStringScaled(context, this.module.getName(), ix + 2.0F * ClickGuiModule.CLICK_GUI_SCALE, iy + 3.5F * ClickGuiModule.CLICK_GUI_SCALE, colorText);
      String bind = this.getBindLabel();
      if (bind != null) {
         float bindX = ix + this.width * ClickGuiModule.CLICK_GUI_SCALE - 3.0F * ClickGuiModule.CLICK_GUI_SCALE - (float)RenderManager.textWidth(bind) * ClickGuiModule.CLICK_GUI_SCALE;
         this.drawStringScaled(context, bind, bindX, iy + 3.5F * ClickGuiModule.CLICK_GUI_SCALE, b1 ? -11184811 : -7829368);
      }

      if (this.settingsAnimation.getFactor() > (double)0.01F) {
         this.off = this.y + this.height * ClickGuiModule.CLICK_GUI_SCALE + ClickGuiModule.CLICK_GUI_SCALE;
         float fheight = 0.0F;

         for(ConfigButton<?> configButton : this.configComponents) {
            if (configButton.getConfig().isVisible()) {
               fheight += configButton.getHeight();
               if (configButton instanceof ColorButton) {
                  ColorButton colorPicker = (ColorButton)configButton;
                  if (colorPicker.getScaledTime() > 0.01F) {
                     fheight += colorPicker.getPickerHeight() * colorPicker.getScaledTime() * this.getScaledTime();
                  }
               }
            }
         }

         boolean canScissor = ClickGuiModule.getInstance().getScaleFactor() == 1.0F;
         if (canScissor) {
            this.enableScissor((double)this.x, (double)(this.off - ClickGuiModule.CLICK_GUI_SCALE), (double)(this.x + this.width * ClickGuiModule.CLICK_GUI_SCALE), (double)this.off + (double)(3.0F * ClickGuiModule.CLICK_GUI_SCALE) + (double)fheight * this.settingsAnimation.getFactor());
         }

         for(ConfigButton<?> configButton : this.configComponents) {
            if (configButton.getConfig().isVisible()) {
               configButton.render(context, ix + 2.0F * ClickGuiModule.CLICK_GUI_SCALE, this.off, mouseX, mouseY, delta);
               ((CategoryFrame)this.frame).offset((float)((double)configButton.getHeight() * this.settingsAnimation.getFactor()));
               this.off += configButton.getHeight();
            }
         }

         if (fill) {
            this.fill(context, (double)ix, (double)(this.y + this.height * ClickGuiModule.CLICK_GUI_SCALE), (double)ClickGuiModule.CLICK_GUI_SCALE, (double)(this.off - (this.y + this.height * ClickGuiModule.CLICK_GUI_SCALE) + ClickGuiModule.CLICK_GUI_SCALE), ClickGuiModule.getInstance().getColor(100 + hoverAlpha2, scaledTime));
            this.fill(context, (double)(ix + this.width * ClickGuiModule.CLICK_GUI_SCALE - ClickGuiModule.CLICK_GUI_SCALE), (double)(this.y + this.height * ClickGuiModule.CLICK_GUI_SCALE), (double)ClickGuiModule.CLICK_GUI_SCALE, (double)(this.off - (this.y + this.height * ClickGuiModule.CLICK_GUI_SCALE) + ClickGuiModule.CLICK_GUI_SCALE), ClickGuiModule.getInstance().getColor(100 + hoverAlpha2, scaledTime));
            this.fill(context, (double)ix, (double)(this.off + ClickGuiModule.CLICK_GUI_SCALE), (double)(this.width * ClickGuiModule.CLICK_GUI_SCALE), (double)ClickGuiModule.CLICK_GUI_SCALE, ClickGuiModule.getInstance().getColor(100 + hoverAlpha2, scaledTime));
         }

         if (canScissor) {
            this.disableScissor();
         }

         ((CategoryFrame)this.frame).offset((float)((double)(3.0F * ClickGuiModule.CLICK_GUI_SCALE) * this.settingsAnimation.getFactor()));
      } else {
         for(ConfigButton<?> configButton : this.configComponents) {
            if (configButton instanceof BindButton) {
               BindButton bindButton = (BindButton)configButton;
               bindButton.setListening(false);
            }

            if (configButton instanceof TextButton) {
               TextButton textButton = (TextButton)configButton;
               textButton.endTyping();
            }

            if (configButton instanceof ColorButton) {
               ColorButton colorButton = (ColorButton)configButton;
               colorButton.endTyping();
            }

            if (configButton instanceof SliderButton) {
               SliderButton sliderButton = (SliderButton)configButton;
               sliderButton.endTyping();
            }
         }
      }

   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isWithin(mouseX, mouseY)) {
         label37: {
            if (button == 0) {
               Module var7 = this.module;
               if (var7 instanceof ToggleModule) {
                  ToggleModule t = (ToggleModule)var7;
                  t.toggle();
                  break label37;
               }
            }

            if (button == 1) {
               this.open = !this.open;
               this.settingsAnimation.setState(this.open);
            } else if (button == 2) {
               Module var8 = this.module;
               if (var8 instanceof Hideable) {
                  Hideable hideable = (Hideable)var8;
                  hideable.setHidden(!hideable.isHidden());
               }
            }
         }

         if (ClickGuiModule.getInstance().getSounds()) {
            Managers.SOUND.playSound(SoundManager.GUI_CLICK);
         }
      }

      if (this.open) {
         for(ConfigButton<?> component : this.configComponents) {
            if (component.getConfig().isVisible()) {
               component.mouseClicked(mouseX, mouseY, button);
            }
         }
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
      if (this.open) {
         for(ConfigButton<?> component : this.configComponents) {
            if (component.getConfig().isVisible()) {
               component.mouseReleased(mouseX, mouseY, button);
            }
         }
      }

   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.open) {
         for(ConfigButton<?> component : this.configComponents) {
            if (component.getConfig().isVisible()) {
               component.keyPressed(keyCode, scanCode, modifiers);
            }
         }
      }

   }

   public void charTyped(char character, int modifiers) {
      if (this.open) {
         for(ConfigButton<?> component : this.configComponents) {
            if (component.getConfig().isVisible()) {
               component.charTyped(character, modifiers);
            }
         }
      }

   }

   /**
    * True when the search is in Hide mode and this module doesn't match it — the frame then skips
    * the row entirely (no height, no rendering, no input).
    */
   public boolean isFilteredOut() {
      return SearchButton.isSearching() && ClickGuiModule.getInstance().getSearchMode() == ClickGuiModule.SearchMode.HIDE && !SearchButton.matches(this.module);
   }

   /**
    * The keybind shown on the right side of the row, or null when there is nothing to show.
    */
   private String getBindLabel() {
      if (ClickGuiModule.getInstance().getBinds()) {
         Module var2 = this.module;
         if (var2 instanceof ToggleModule) {
            ToggleModule toggleModule = (ToggleModule)var2;
            Macro keybinding = toggleModule.getKeybinding();
            if (keybinding != null && keybinding.getKeycode() != -1) {
               String name = keybinding.getKeyName();
               if (!name.equals("NONE")) {
                  return name.length() > 6 ? name.substring(0, 6) : name;
               }
            }
         }
      }

      return null;
   }

   public void offset(float in) {
      this.off += in;
   }

   public boolean isOpen() {
      return this.open;
   }

   public float getScaledTime() {
      return (float)this.settingsAnimation.getFactor();
   }

   public Module getModule() {
      return this.module;
   }

   public List<ConfigButton<?>> getConfigButtons() {
      return this.configComponents;
   }
}
