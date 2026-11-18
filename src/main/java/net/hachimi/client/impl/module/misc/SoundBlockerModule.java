package net.hachimi.client.impl.module.misc;

import java.util.HashSet;
import java.util.Set;
import net.hachimi.client.api.config.Config;
import net.hachimi.client.api.config.setting.BooleanConfig;
import net.hachimi.client.api.config.setting.StringConfig;
import net.hachimi.client.api.module.ModuleCategory;
import net.hachimi.client.api.module.ToggleModule;
import net.hachimi.client.impl.event.world.PlaySoundEvent;
import net.hachimi.eventbus.annotation.EventListener;
import net.minecraft.sound.SoundEvent;

/**
 * Ported from Homovore 2's SoundBlocker (piolunson/mohovore). Hachimi has no sound-list setting, so
 * common sounds are presets and anything else goes into {@code Custom} as comma separated ids,
 * e.g. {@code entity.generic.explode, block.anvil.land} (the {@code minecraft:} prefix is optional).
 */
public class SoundBlockerModule extends ToggleModule {
   Config<Boolean> explosionsConfig = this.register(new BooleanConfig("Explosions", "Silences explosions (crystals, anchors, TNT)", false));
   Config<Boolean> totemsConfig = this.register(new BooleanConfig("Totems", "Silences totem pops", false));
   Config<Boolean> portalsConfig = this.register(new BooleanConfig("Portals", "Silences nether portal ambience", true));
   Config<Boolean> thunderConfig = this.register(new BooleanConfig("Thunder", "Silences lightning and thunder", false));
   Config<Boolean> bossesConfig = this.register(new BooleanConfig("Bosses", "Silences wither spawn and ender dragon death", true));
   Config<String> customConfig = this.register(new StringConfig("Custom", "Comma separated sound ids to silence", ""));
   private String parsedFrom;
   private Set<String> custom = new HashSet<>();

   public SoundBlockerModule() {
      super("SoundBlocker", "Silences the sounds you pick", ModuleCategory.MISCELLANEOUS);
   }

   @EventListener
   public void onPlaySound(PlaySoundEvent event) {
      SoundEvent sound = event.getSoundEvent();
      if (sound != null && this.isBlocked(sound.getId().getPath())) {
         event.cancel();
      }

   }

   private boolean isBlocked(String id) {
      if ((Boolean)this.explosionsConfig.getValue() && id.equals("entity.generic.explode")) {
         return true;
      } else if ((Boolean)this.totemsConfig.getValue() && id.equals("item.totem.use")) {
         return true;
      } else if ((Boolean)this.portalsConfig.getValue() && id.startsWith("block.portal.")) {
         return true;
      } else if ((Boolean)this.thunderConfig.getValue() && id.startsWith("entity.lightning_bolt.")) {
         return true;
      } else if ((Boolean)this.bossesConfig.getValue() && (id.equals("entity.wither.spawn") || id.equals("entity.wither.death") || id.equals("entity.ender_dragon.death"))) {
         return true;
      } else {
         return this.getCustom().contains(id);
      }
   }

   private Set<String> getCustom() {
      String value = (String)this.customConfig.getValue();
      if (value == null) {
         value = "";
      }

      if (!value.equals(this.parsedFrom)) {
         Set<String> parsed = new HashSet<>();

         for(String part : value.split(",")) {
            String id = part.trim().toLowerCase();
            if (id.startsWith("minecraft:")) {
               id = id.substring("minecraft:".length());
            }

            if (!id.isEmpty()) {
               parsed.add(id);
            }
         }

         this.custom = parsed;
         this.parsedFrom = value;
      }

      return this.custom;
   }
}
