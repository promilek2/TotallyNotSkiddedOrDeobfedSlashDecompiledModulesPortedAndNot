package net.hachimi.client.impl.module.movement;

import net.hachimi.client.api.config.Config;
import net.hachimi.client.api.config.NumberDisplay;
import net.hachimi.client.api.config.setting.BooleanConfig;
import net.hachimi.client.api.config.setting.NumberConfig;
import net.hachimi.client.api.module.ModuleCategory;
import net.hachimi.client.api.module.ToggleModule;
import net.hachimi.client.impl.event.network.PlayerTickEvent;
import net.hachimi.client.init.Managers;
import net.hachimi.client.util.math.timer.TickTimer;
import net.hachimi.eventbus.annotation.EventListener;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

/**
 * Ported from Homovore 2's AutoElytra (piolunson/mohovore): equips an elytra, takes off and keeps
 * firing rockets from the hotbar; on disable it swaps the chestplate back if you are not flying.
 */
public class AutoElytraModule extends ToggleModule {
   private static final int CHEST_SLOT = 6;
   Config<Boolean> equipConfig = this.register(new BooleanConfig("Equip", "Swaps an elytra from your inventory into the chest slot", true));
   Config<Boolean> takeOffConfig = this.register(new BooleanConfig("TakeOff", "Jumps and opens the elytra automatically", true));
   Config<Boolean> rocketsConfig = this.register(new BooleanConfig("Rockets", "Fires rockets from the hotbar while flying", true));
   Config<Integer> rocketDelayConfig = this.register(new NumberConfig("RocketDelay", "Ticks between rockets", 10, 40, 200, NumberDisplay.DEFAULT, () -> this.rocketsConfig.getValue()));
   Config<Boolean> swapBackConfig = this.register(new BooleanConfig("SwapBack", "Puts the chestplate back on when disabled (only on the ground)", true));
   private final TickTimer rocketTimer = new TickTimer();
   private final TickTimer takeOffTimer = new TickTimer();
   private int swappedSlot = -1;

   public AutoElytraModule() {
      super("AutoElytra", "Equips an elytra, takes off and keeps firing rockets", ModuleCategory.MOVEMENT);
   }

   protected void onEnable() {
      this.swappedSlot = -1;
      if (mc.player != null && (Boolean)this.equipConfig.getValue() && !this.isWearingElytra()) {
         int slot = this.findElytra();
         if (slot == -1) {
            this.sendModuleError("No usable elytra in your inventory");
         } else {
            this.swapWithChest(slot);
            this.swappedSlot = slot;
         }
      }

   }

   protected void onDisable() {
      if (mc.player != null && this.swappedSlot != -1 && (Boolean)this.swapBackConfig.getValue()) {
         if (mc.player.isFallFlying()) {
            this.sendModuleMessage("Still flying — swap your chestplate back after landing");
         } else if (this.isWearingElytra()) {
            this.swapWithChest(this.swappedSlot);
         }
      }

      this.swappedSlot = -1;
   }

   @EventListener
   public void onPlayerTick(PlayerTickEvent event) {
      if (mc.player != null && mc.world != null && this.isWearingElytra()) {
         if (!mc.player.isFallFlying()) {
            if ((Boolean)this.takeOffConfig.getValue()) {
               if (mc.player.isOnGround()) {
                  mc.player.jump();
               } else if (mc.player.getVelocity().y < 0.0D && this.takeOffTimer.passed(5)) {
                  Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                  this.takeOffTimer.reset();
               }
            }

         } else {
            if ((Boolean)this.rocketsConfig.getValue() && this.rocketTimer.passed((Number)this.rocketDelayConfig.getValue())) {
               int rocketSlot = this.findRocket();
               if (rocketSlot != -1) {
                  int prevSlot = mc.player.getInventory().selectedSlot;
                  Managers.INVENTORY.setSlotForced(rocketSlot);
                  Managers.NETWORK.sendSequencedPacket((id) -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
                  if (prevSlot != rocketSlot) {
                     Managers.INVENTORY.setSlotForced(prevSlot);
                  }

                  this.rocketTimer.reset();
               }
            }

         }
      }
   }

   /**
    * Swaps the item in an inventory slot (0-35) with the chest armor slot using three pickups,
    * the same way FakeFly does it.
    */
   private void swapWithChest(int inventorySlot) {
      int screenSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
      int syncId = mc.player.playerScreenHandler.syncId;
      mc.interactionManager.clickSlot(syncId, screenSlot, 0, SlotActionType.PICKUP, mc.player);
      mc.interactionManager.clickSlot(syncId, CHEST_SLOT, 0, SlotActionType.PICKUP, mc.player);
      mc.interactionManager.clickSlot(syncId, screenSlot, 0, SlotActionType.PICKUP, mc.player);
   }

   private boolean isWearingElytra() {
      ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
      return chest.isOf(Items.ELYTRA) && ElytraItem.isUsable(chest);
   }

   private int findElytra() {
      for(int i = 0; i < 36; ++i) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (stack.isOf(Items.ELYTRA) && ElytraItem.isUsable(stack)) {
            return i;
         }
      }

      return -1;
   }

   private int findRocket() {
      for(int i = 0; i < 9; ++i) {
         if (mc.player.getInventory().getStack(i).isOf(Items.FIREWORK_ROCKET)) {
            return i;
         }
      }

      return -1;
   }
}
