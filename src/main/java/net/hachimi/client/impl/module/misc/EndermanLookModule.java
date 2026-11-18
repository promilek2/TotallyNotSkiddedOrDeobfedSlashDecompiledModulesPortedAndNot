package net.hachimi.client.impl.module.misc;

import net.hachimi.client.api.config.Config;
import net.hachimi.client.api.config.NumberDisplay;
import net.hachimi.client.api.config.setting.BooleanConfig;
import net.hachimi.client.api.config.setting.EnumConfig;
import net.hachimi.client.api.config.setting.NumberConfig;
import net.hachimi.client.api.module.ModuleCategory;
import net.hachimi.client.impl.event.network.PlayerTickEvent;
import net.hachimi.client.impl.module.RotationModule;
import net.hachimi.client.util.player.RotationUtil;
import net.hachimi.eventbus.annotation.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

/**
 * Ported from Homovore's EndermanLook (piolunson/mohovore), rewritten for Hachimi's rotation manager.
 *
 * AWAY — stops you from provoking endermen: if one is in your line of sight the way vanilla checks it
 * ({@code EndermanEntity#isPlayerStaring}), the server-side pitch is pushed to the floor. Already angry
 * endermen can optionally be stared at, which freezes them in place while you look.
 * AT — stares at the closest calm enderman (to farm them on purpose).
 */
public class EndermanLookModule extends RotationModule {
   Config<Mode> modeConfig = this.register(new EnumConfig("Mode", "AWAY never provokes endermen, AT stares at the closest one", EndermanLookModule.Mode.AWAY, EndermanLookModule.Mode.values()));
   Config<Boolean> stunConfig = this.register(new BooleanConfig("StunAngry", "Stares at endermen that are already angry so they stop moving", true, () -> this.modeConfig.getValue() == EndermanLookModule.Mode.AWAY));
   Config<Float> rangeConfig = this.register(new NumberConfig("Range", "Only endermen inside this range are considered", 4.0F, 64.0F, 128.0F, NumberDisplay.DEFAULT));

   public EndermanLookModule() {
      super("EndermanLook", "Stops you from provoking endermen, or stares at them on purpose", ModuleCategory.MISCELLANEOUS, 50);
   }

   @EventListener
   public void onPlayerTick(PlayerTickEvent event) {
      if (mc.player != null && mc.world != null) {
         if (!mc.player.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.CARVED_PUMPKIN) && !mc.player.getAbilities().creativeMode) {
            double range = (double)(Float)this.rangeConfig.getValue();
            double rangeSq = range * range;
            EndermanEntity stareTarget = null;
            EndermanEntity threat = null;
            double bestStare = Double.MAX_VALUE;
            double bestThreat = Double.MAX_VALUE;

            for(Entity entity : mc.world.getEntities()) {
               if (entity instanceof EndermanEntity enderman) {
                  if (enderman.isAlive() && mc.player.canSee(enderman)) {
                     double dist = mc.player.squaredDistanceTo(enderman);
                     if (!(dist > rangeSq)) {
                        if (this.modeConfig.getValue() == EndermanLookModule.Mode.AT) {
                           if (!enderman.isAngry() && dist < bestStare) {
                              bestStare = dist;
                              stareTarget = enderman;
                           }
                        } else if (enderman.isAngry() && (Boolean)this.stunConfig.getValue()) {
                           if (dist < bestStare) {
                              bestStare = dist;
                              stareTarget = enderman;
                           }
                        } else if (this.isLookingAt(enderman) && dist < bestThreat) {
                           bestThreat = dist;
                           threat = enderman;
                        }
                     }
                  }
               }
            }

            if (stareTarget != null) {
               float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), stareTarget.getEyePos());
               this.setRotation(rotations[0], rotations[1]);
            } else if (threat != null) {
               this.setRotation(mc.player.getYaw(), 90.0F);
            }

         }
      }
   }

   /**
    * Same test vanilla uses to decide if a player is staring at an enderman.
    */
   private boolean isLookingAt(EndermanEntity enderman) {
      Vec3d view = mc.player.getRotationVec(1.0F).normalize();
      Vec3d diff = new Vec3d(enderman.getX() - mc.player.getX(), enderman.getEyeY() - mc.player.getEyeY(), enderman.getZ() - mc.player.getZ());
      double length = diff.length();
      double dot = view.dotProduct(diff.normalize());
      return dot > 1.0D - 0.025D / length;
   }

   public static enum Mode {
      AWAY,
      AT;
   }
}
