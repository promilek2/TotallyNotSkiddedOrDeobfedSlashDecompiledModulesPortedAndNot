package net.hachimi.client.impl.gui.click.impl.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.hachimi.client.api.config.Serializable;
import net.hachimi.client.api.module.Module;
import net.hachimi.client.api.module.ModuleCategory;
import net.hachimi.client.api.module.ToggleModule;
import net.hachimi.client.api.render.RenderManager;
import net.hachimi.client.api.render.satin.ManagedShaderEffect;
import net.hachimi.client.impl.gui.click.ClickGuiScreen;
import net.hachimi.client.impl.gui.click.component.Frame;
import net.hachimi.client.impl.gui.click.impl.SearchButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.ColorButton;
import net.hachimi.client.impl.gui.click.impl.config.setting.ConfigButton;
import net.hachimi.client.impl.manager.world.sound.SoundManager;
import net.hachimi.client.impl.module.client.ClickGuiModule;
import net.hachimi.client.init.Managers;
import net.hachimi.client.util.render.animation.Animation;
import net.hachimi.client.util.render.animation.Easing;
import net.hachimi.client.util.string.EnumFormatter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class CategoryFrame extends Frame implements Serializable<CategoryFrame> {
   private final String name;
   private final ModuleCategory category;
   private final List<ModuleButton> moduleButtons;
   private float off;
   private float inner;
   private boolean open;
   private boolean drag;
   private final Animation categoryAnimation;
   private SearchButton searchButton;

   public CategoryFrame(ModuleCategory category, float x, float y, float width, float height) {
      super(x, y, width, height);
      this.moduleButtons = new CopyOnWriteArrayList();
      this.categoryAnimation = new Animation(false, 200.0F, Easing.CUBIC_IN_OUT);
      this.category = category;
      this.name = EnumFormatter.formatEnum(category);

      for(Module module : Managers.MODULE.getModules()) {
         if (module.getCategory() == category) {
            this.moduleButtons.add(new ModuleButton(module, this, x, y));
         }
      }

      if (category == ModuleCategory.CLIENT) {
         this.searchButton = new SearchButton(this, x, y);
      }

      this.setOpen(true);
   }

   public CategoryFrame(ModuleCategory category, float x, float y) {
      this(category, x, y, 105.0F, 15.0F);
   }

   public void render(DrawContext context, float mouseX, float mouseY, float delta) {
      this.setHeight((float)RenderManager.textHeight() + 6.0F);
      if (this.drag) {
         this.x += (float)ClickGuiScreen.MOUSE_X - this.px;
         this.y += (float)ClickGuiScreen.MOUSE_Y - this.py;
         int screenWidth = mc.getWindow().getScaledWidth();
         int screenHeight = mc.getWindow().getScaledHeight();
         this.x = MathHelper.clamp(this.x, 0.0F, Math.max(0.0F, (float)screenWidth - this.width * ClickGuiModule.CLICK_GUI_SCALE));
         this.y = Math.min(this.y, Math.max(0.0F, (float)screenHeight - this.height * ClickGuiModule.CLICK_GUI_SCALE));
      }

      this.fheight = 2.0F * ClickGuiModule.CLICK_GUI_SCALE;
      if (this.searchButton != null) {
         this.fheight += this.searchButton.getHeight() + 6.0F * ClickGuiModule.CLICK_GUI_SCALE;
      }

      for(ModuleButton moduleButton : this.moduleButtons) {
         if (moduleButton.isFilteredOut()) {
            continue;
         }

         this.fheight += moduleButton.getHeight() + ClickGuiModule.CLICK_GUI_SCALE;
         if (!(moduleButton.getScaledTime() < 0.01F)) {
            this.fheight += 3.0F * ClickGuiModule.CLICK_GUI_SCALE * moduleButton.getScaledTime();

            for(ConfigButton<?> configButton : moduleButton.getConfigButtons()) {
               if (configButton.getConfig().isVisible()) {
                  this.fheight += configButton.getHeight() * moduleButton.getScaledTime();
                  if (configButton instanceof ColorButton) {
                     ColorButton colorPicker = (ColorButton)configButton;
                     if (colorPicker.getScaledTime() > 0.01F) {
                        this.fheight += colorPicker.getPickerHeight() * colorPicker.getScaledTime() * moduleButton.getScaledTime();
                     }
                  }
               }
            }
         }
      }

      this.rect(context, ClickGuiModule.getInstance().fixTransparency(-301133801));
      this.fill(context, (double)this.x, (double)this.y, (double)(2.0F * ClickGuiModule.CLICK_GUI_SCALE), (double)(this.height * ClickGuiModule.CLICK_GUI_SCALE), ClickGuiModule.getInstance().getColor(255));
      this.fill(context, (double)this.x, (double)(this.y + (this.height - 1.0F) * ClickGuiModule.CLICK_GUI_SCALE), (double)(this.width * ClickGuiModule.CLICK_GUI_SCALE), (double)ClickGuiModule.CLICK_GUI_SCALE, ClickGuiModule.getInstance().getColor(80));
      int whiteText = -1;
      this.drawStringScaled(context, this.name, this.x + 6.0F * ClickGuiModule.CLICK_GUI_SCALE, this.y + 4.0F * ClickGuiModule.CLICK_GUI_SCALE, whiteText);
      if (ClickGuiModule.getInstance().getCounters()) {
         String counter = this.getCounterText();
         float counterX = this.x + this.width * ClickGuiModule.CLICK_GUI_SCALE - 4.0F * ClickGuiModule.CLICK_GUI_SCALE - (float)RenderManager.textWidth(counter) * ClickGuiModule.CLICK_GUI_SCALE;
         this.drawStringScaled(context, counter, counterX, this.y + 4.0F * ClickGuiModule.CLICK_GUI_SCALE, -7829368);
      }
      if (this.categoryAnimation.getFactor() > (double)0.01F) {
         boolean canScissor = ClickGuiModule.getInstance().getScaleFactor() == 1.0F;
         float y1 = this.y;
         if (this.searchButton != null) {
            y1 += this.searchButton.getHeight() + 6.0F * ClickGuiModule.CLICK_GUI_SCALE;
         }

         if (canScissor) {
            this.enableScissor((double)this.x, (double)(this.y + this.height * ClickGuiModule.CLICK_GUI_SCALE), (double)(this.x + this.width * ClickGuiModule.CLICK_GUI_SCALE), (double)(this.y + ClickGuiModule.CLICK_GUI_SCALE * this.height) + (double)this.fheight * this.categoryAnimation.getFactor());
         }

         int fillColor = ClickGuiModule.getInstance().fixTransparency(-586148840);
         this.fill(context, (double)this.x, (double)(this.y + this.height * ClickGuiModule.CLICK_GUI_SCALE), (double)(this.width * ClickGuiModule.CLICK_GUI_SCALE), (double)this.fheight, fillColor);
         if (ClickGuiModule.getInstance().getBlur() && ClickGuiModule.getInstance().getScaleFactor() == 1.0F) {
            Managers.SHADER.reloadShaders();
            ManagedShaderEffect blurEffect = Managers.SHADER.getBlurShaderEffect();
            if (blurEffect != null) {
               float panelX = this.x;
               float panelY = this.y;
               float panelWidth = this.width * ClickGuiModule.CLICK_GUI_SCALE;
               float panelHeight = (float)((double)(this.height * ClickGuiModule.CLICK_GUI_SCALE) + (double)this.fheight * this.categoryAnimation.getFactor());
               blurEffect.setUniformValue("uLocation", panelX, panelY);
               blurEffect.setUniformValue("uSize", panelWidth, panelHeight);
               blurEffect.setUniformValue("uStrength", 5.0F);
               blurEffect.setUniformValue("uRadius", 0.0F);
               blurEffect.render(delta);
            }
         }

         if (this.searchButton != null) {
            this.searchButton.render(context, this.x + ClickGuiModule.CLICK_GUI_SCALE, this.y + this.height * ClickGuiModule.CLICK_GUI_SCALE + 4.0F * ClickGuiModule.CLICK_GUI_SCALE, mouseX, mouseY, delta);
         }

         this.off = y1 + this.height * ClickGuiModule.CLICK_GUI_SCALE + ClickGuiModule.CLICK_GUI_SCALE;
         this.inner = this.off;

         for(ModuleButton moduleButton : this.moduleButtons) {
            if (moduleButton.isFilteredOut()) {
               continue;
            }

            moduleButton.render(context, this.x + ClickGuiModule.CLICK_GUI_SCALE, this.inner + ClickGuiModule.CLICK_GUI_SCALE, mouseX, mouseY, delta);
            this.off += (float)((double)(moduleButton.getHeight() + ClickGuiModule.CLICK_GUI_SCALE) * this.categoryAnimation.getFactor());
            this.inner += moduleButton.getHeight() + ClickGuiModule.CLICK_GUI_SCALE;
         }

         if (canScissor) {
            this.disableScissor();
         }
      }

      this.px = (float)ClickGuiScreen.MOUSE_X;
      this.py = (float)ClickGuiScreen.MOUSE_Y;
   }

   public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (mouseButton == 1 && this.isWithin(mouseX, mouseY)) {
         this.open = !this.open;
         this.categoryAnimation.setState(this.open);
         if (ClickGuiModule.getInstance().getSounds()) {
            Managers.SOUND.playSound(SoundManager.GUI_CLICK);
         }
      }

      if (this.isOpen()) {
         for(ModuleButton button : this.moduleButtons) {
            if (button.isFilteredOut()) {
               continue;
            }

            button.mouseClicked(mouseX, mouseY, mouseButton);
         }
      }

      if (this.searchButton != null) {
         this.searchButton.mouseClicked(mouseX, mouseY, mouseButton);
      }

   }

   public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
      super.mouseReleased(mouseX, mouseY, mouseButton);
      this.drag = false;
      if (this.isOpen()) {
         for(ModuleButton button : this.moduleButtons) {
            if (button.isFilteredOut()) {
               continue;
            }

            button.mouseReleased(mouseX, mouseY, mouseButton);
         }
      }

      if (this.searchButton != null) {
         this.searchButton.mouseReleased(mouseX, mouseY, mouseButton);
      }

   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      super.keyPressed(keyCode, scanCode, modifiers);
      if (this.isOpen()) {
         for(ModuleButton button : this.moduleButtons) {
            if (button.isFilteredOut()) {
               continue;
            }

            button.keyPressed(keyCode, scanCode, modifiers);
         }
      }

      if (this.searchButton != null) {
         this.searchButton.keyPressed(keyCode, scanCode, modifiers);
      }

   }

   public void charTyped(char character, int modifiers) {
      super.charTyped(character, modifiers);
      if (this.isOpen()) {
         for(ModuleButton button : this.moduleButtons) {
            if (button.isFilteredOut()) {
               continue;
            }

            button.charTyped(character, modifiers);
         }
      }

      if (this.searchButton != null) {
         this.searchButton.charTyped(character, modifiers);
      }

   }

   public JsonObject toJson() {
      JsonObject obj = new JsonObject();
      obj.addProperty("category", this.category.name());
      obj.addProperty("x", this.getX());
      obj.addProperty("y", this.getY());
      obj.addProperty("open", this.isOpen());
      return obj;
   }

   public CategoryFrame fromJson(JsonObject jsonObj) {
      if (jsonObj.has("x") && jsonObj.has("y") && jsonObj.has("open")) {
         JsonElement xElement = jsonObj.get("x");
         JsonElement yElement = jsonObj.get("y");
         JsonElement openElement = jsonObj.get("open");
         this.setPos(xElement.getAsFloat(), yElement.getAsFloat());
         this.setOpen(openElement.getAsBoolean());
      }

      return null;
   }

   /**
    * "enabled/total" for the header, or "matches/total" while a search is active.
    */
   private String getCounterText() {
      int total = this.moduleButtons.size();
      int count = 0;

      for(ModuleButton moduleButton : this.moduleButtons) {
         if (SearchButton.isSearching()) {
            if (SearchButton.matches(moduleButton.getModule())) {
               ++count;
            }
         } else {
            Module module = moduleButton.getModule();
            if (module instanceof ToggleModule) {
               ToggleModule toggleModule = (ToggleModule)module;
               if (toggleModule.isEnabled()) {
                  ++count;
               }
            }
         }
      }

      return count + "/" + total;
   }

   public boolean isWithinTotal(float mx, float my) {
      return this.isMouseOver((double)mx, (double)my, (double)this.x, (double)this.y, (double)(this.width * ClickGuiModule.CLICK_GUI_SCALE), (double)this.getTotalHeight());
   }

   public void offset(float in) {
      this.off += in;
      this.inner += in;
   }

   public ModuleCategory getCategory() {
      return this.category;
   }

   public boolean isOpen() {
      return this.open && this.categoryAnimation.getFactor() > (double)0.1F;
   }

   public void setOpen(boolean open) {
      this.open = open;
      this.categoryAnimation.setState(open);
   }

   public float getTotalHeight() {
      return this.height * ClickGuiModule.CLICK_GUI_SCALE + this.fheight;
   }

   public List<ModuleButton> getModuleButtons() {
      return this.moduleButtons;
   }

   public void setDragging(boolean drag) {
      this.drag = drag;
   }

   public boolean isDragging() {
      return this.drag;
   }

   public SearchButton getSearchButton() {
      return this.searchButton;
   }
}
