package net.hachimi.client.impl.module.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.hachimi.client.api.config.Config;
import net.hachimi.client.api.config.NumberDisplay;
import net.hachimi.client.api.config.setting.BooleanConfig;
import net.hachimi.client.api.config.setting.EnumConfig;
import net.hachimi.client.api.config.setting.NumberConfig;
import net.hachimi.client.api.module.ModuleCategory;
import net.hachimi.client.api.module.ToggleModule;
import net.hachimi.client.api.render.RenderManager;
import net.hachimi.client.impl.event.gui.hud.RenderOverlayEvent;
import net.hachimi.client.util.render.ColorUtil;
import net.hachimi.client.util.render.animation.Animation;
import net.hachimi.client.util.render.animation.Easing;
import net.hachimi.eventbus.annotation.EventListener;
import net.minecraft.client.gui.DrawContext;

/**
 * Toast notifications in a corner of the screen. Module toggles are routed here from
 * {@link ToggleModule#enable()} / {@link ToggleModule#disable()}; any other feature can call
 * {@link #push(String, String, int)}.
 */
public class NotificationsModule extends ToggleModule {
   private static NotificationsModule INSTANCE;
   Config<Position> positionConfig = this.register(new EnumConfig("Position", "The corner the notifications stack from", NotificationsModule.Position.BOTTOM_RIGHT, NotificationsModule.Position.values()));
   Config<Integer> durationConfig = this.register(new NumberConfig("Duration", "How long each notification stays on screen", 500, 2000, 6000, NumberDisplay.DEFAULT));
   Config<Integer> maxConfig = this.register(new NumberConfig("Max", "The maximum number of notifications on screen", 1, 5, 10, NumberDisplay.DEFAULT));
   Config<Integer> yOffsetConfig = this.register(new NumberConfig("YOffset", "Vertical distance from the screen edge", 0, 4, 200, NumberDisplay.DEFAULT));
   Config<Boolean> togglesConfig = this.register(new BooleanConfig("Toggles", "Shows a notification when a module is toggled", true));
   Config<Boolean> replaceChatConfig = this.register(new BooleanConfig("ReplaceChat", "Stops module toggle messages from being sent to chat", true, () -> this.togglesConfig.getValue()));
   Config<Boolean> progressConfig = this.register(new BooleanConfig("Progress", "Draws a bar showing how long the notification has left", true));
   private final List<Notification> notifications = new CopyOnWriteArrayList<>();

   public NotificationsModule() {
      super("Notifications", "Shows toggles and client messages as toasts in a corner of the screen", ModuleCategory.CLIENT);
      INSTANCE = this;
   }

   public static NotificationsModule getInstance() {
      return INSTANCE;
   }

   /**
    * Called by {@link ToggleModule} when a module changes state.
    *
    * @return true if the toggle was shown as a toast and the chat message should be skipped
    */
   public static boolean onModuleToggle(ToggleModule module, boolean enabled) {
      if (INSTANCE != null && INSTANCE.isEnabled() && (Boolean)INSTANCE.togglesConfig.getValue() && module != INSTANCE) {
         if (module instanceof ClickGuiModule) {
            return false;
         } else {
            INSTANCE.push(module.getName(), enabled ? "§aenabled" : "§cdisabled", enabled ? -11141291 : -43691);
            return (Boolean)INSTANCE.replaceChatConfig.getValue();
         }
      } else {
         return false;
      }
   }

   public void push(String title, String message, int accent) {
      if (mc.world != null) {
         for(Notification notification : this.notifications) {
            if (notification.title.equals(title) && !notification.isExpired()) {
               this.notifications.remove(notification);
            }
         }

         this.notifications.add(new Notification(title, message, accent, (long)(Integer)this.durationConfig.getValue()));
         int max = (Integer)this.maxConfig.getValue();

         while(this.notifications.size() > max) {
            this.notifications.remove(0);
         }

      }
   }

   protected void onDisable() {
      this.notifications.clear();
   }

   @EventListener
   public void onRenderOverlayPost(RenderOverlayEvent.Post event) {
      if (mc.player != null && !this.notifications.isEmpty()) {
         DrawContext context = event.getContext();
         int screenWidth = context.getScaledWindowWidth();
         int screenHeight = context.getScaledWindowHeight();
         Position position = (Position)this.positionConfig.getValue();
         boolean top = position == NotificationsModule.Position.TOP_RIGHT || position == NotificationsModule.Position.TOP_LEFT;
         boolean right = position == NotificationsModule.Position.TOP_RIGHT || position == NotificationsModule.Position.BOTTOM_RIGHT;
         int textHeight = RenderManager.textHeight();
         float boxHeight = (float)textHeight + 8.0F;
         float offset = (float)(Integer)this.yOffsetConfig.getValue();

         for(int i = this.notifications.size() - 1; i >= 0; --i) {
            Notification notification = this.notifications.get(i);
            if (notification.isExpired()) {
               notification.animation.setState(false);
            }

            float factor = (float)notification.animation.getFactor();
            if (!notification.animation.getState() && factor <= 0.0F) {
               this.notifications.remove(notification);
            } else {
               String text = notification.title + " §7" + notification.message;
               float boxWidth = (float)RenderManager.textWidth(text) + 12.0F;
               float slide = (1.0F - factor) * (boxWidth + 6.0F);
               float x = right ? (float)screenWidth - 4.0F - boxWidth + slide : 4.0F - slide;
               float y = top ? offset : (float)screenHeight - offset - boxHeight;
               int background = ColorUtil.fixTransparency(-1341320169, factor);
               int accent = ColorUtil.fixTransparency(notification.accent, factor);
               RenderManager.rect(context.getMatrices(), (double)x, (double)y, (double)boxWidth, (double)boxHeight, background);
               RenderManager.rect(context.getMatrices(), right ? (double)x : (double)(x + boxWidth - 2.0F), (double)y, 2.0D, (double)boxHeight, accent);
               if ((Boolean)this.progressConfig.getValue()) {
                  float progress = notification.getProgress();
                  RenderManager.rect(context.getMatrices(), (double)x, (double)(y + boxHeight - 1.0F), (double)(boxWidth * (1.0F - progress)), 1.0D, ColorUtil.fixTransparency(ColorsModule.getInstance().getRGB(200), factor));
               }

               RenderManager.renderText(context, text, x + 6.0F, y + 4.0F, ColorUtil.fixTransparency(-1, factor));
               offset += (boxHeight + 2.0F) * factor;
            }
         }

      }
   }

   public static enum Position {
      BOTTOM_RIGHT,
      TOP_RIGHT,
      BOTTOM_LEFT,
      TOP_LEFT;
   }

   private static final class Notification {
      private final String title;
      private final String message;
      private final int accent;
      private final long created;
      private final long duration;
      private final Animation animation;

      private Notification(String title, String message, int accent, long duration) {
         this.title = title;
         this.message = message;
         this.accent = accent;
         this.duration = duration;
         this.created = System.currentTimeMillis();
         this.animation = new Animation(false, 250.0F, Easing.CUBIC_IN_OUT);
         this.animation.setState(true);
      }

      private boolean isExpired() {
         return System.currentTimeMillis() - this.created > this.duration;
      }

      private float getProgress() {
         return Math.min(1.0F, (float)(System.currentTimeMillis() - this.created) / (float)this.duration);
      }
   }
}
