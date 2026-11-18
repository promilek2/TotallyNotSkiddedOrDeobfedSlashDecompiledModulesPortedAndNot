package net.hachimi.client.impl.module.misc;

import net.hachimi.client.api.config.Config;
import net.hachimi.client.api.config.NumberDisplay;
import net.hachimi.client.api.config.setting.NumberConfig;
import net.hachimi.client.api.module.ModuleCategory;
import net.hachimi.client.api.module.ToggleModule;
import net.hachimi.client.impl.event.network.PlayerTickEvent;
import net.hachimi.client.init.Managers;
import net.hachimi.client.util.player.RotationUtil;
import net.hachimi.eventbus.annotation.EventListener;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Ported from Homovore's FastPortal (piolunson/mohovore). While your head is inside a nether portal
 * it throws an ender pearl from the hotbar at the portal block below you, then swaps back.
 */
public class FastPortalModule extends ToggleModule {
   Config<Float> cooldownConfig = this.register(new NumberConfig("Cooldown", "Seconds to wait between throws", 0.5F, 2.0F, 20.0F, NumberDisplay.DEFAULT));
   private int cooldownTicks;

   public FastPortalModule() {
      super("FastPortal", "Throws an ender pearl into the portal you are standing in", ModuleCategory.MISCELLANEOUS);
   }

   protected void onDisable() {
      this.cooldownTicks = 0;
   }

   @EventListener
   public void onPlayerTick(PlayerTickEvent event) {
      if (mc.player != null && mc.world != null) {
         if (this.cooldownTicks > 0) {
            --this.cooldownTicks;
         } else {
            Vec3d eye = mc.player.getEyePos();
            BlockPos eyePos = BlockPos.ofFloored(eye);
            if (mc.world.getBlockState(eyePos).isOf(Blocks.NETHER_PORTAL)) {
               int pearlSlot = this.findPearl();
               if (pearlSlot != -1 && !mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL)) {
                  Vec3d target = Vec3d.ofCenter(eyePos.down());
                  float[] rotations = RotationUtil.getRotationsTo(eye, target);
                  int prevSlot = mc.player.getInventory().selectedSlot;
                  Managers.INVENTORY.setSlotForced(pearlSlot);
                  Managers.NETWORK.sendSequencedPacket((id) -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, rotations[0], rotations[1]));
                  if (prevSlot != pearlSlot) {
                     Managers.INVENTORY.setSlotForced(prevSlot);
                  }

                  this.cooldownTicks = (int)((Float)this.cooldownConfig.getValue() * 20.0F);
               }
            }

         }
      }
   }

   private int findPearl() {
      for(int i = 0; i < 9; ++i) {
         if (mc.player.getInventory().getStack(i).isOf(Items.ENDER_PEARL)) {
            return i;
         }
      }

      return -1;
   }
}
