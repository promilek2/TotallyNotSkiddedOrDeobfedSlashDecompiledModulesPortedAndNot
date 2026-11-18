package net.hachimi.client.impl.module.movement;

import net.hachimi.client.api.config.Config;
import net.hachimi.client.api.config.setting.BooleanConfig;
import net.hachimi.client.api.module.ModuleCategory;
import net.hachimi.client.api.module.ToggleModule;
import net.hachimi.client.impl.event.entity.PlayerClimbEvent;
import net.hachimi.eventbus.annotation.EventListener;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

/**
 * Ported from Homovore 2's NoClimb (piolunson/mohovore). Hachimi already fires a cancelable
 * {@link PlayerClimbEvent} from {@code LivingEntity#isClimbing}, so no extra mixin is needed.
 */
public class NoClimbModule extends ToggleModule {
   Config<Boolean> scaffoldingConfig = this.register(new BooleanConfig("Scaffolding", "Stops scaffolding from pulling you up", true));
   Config<Boolean> laddersConfig = this.register(new BooleanConfig("Ladders", "Also ignores ladders", false));
   Config<Boolean> vinesConfig = this.register(new BooleanConfig("Vines", "Also ignores vines, weeping/twisting vines and cave vines", false));

   public NoClimbModule() {
      super("NoClimb", "Stops scaffolding (and optionally ladders and vines) from making you climb", ModuleCategory.MOVEMENT);
   }

   @EventListener
   public void onPlayerClimb(PlayerClimbEvent event) {
      if (mc.player != null) {
         BlockState state = mc.player.getBlockStateAtPos();
         if ((Boolean)this.scaffoldingConfig.getValue() && state.isOf(Blocks.SCAFFOLDING) || (Boolean)this.laddersConfig.getValue() && state.isOf(Blocks.LADDER) || (Boolean)this.vinesConfig.getValue() && (state.isOf(Blocks.VINE) || state.isOf(Blocks.WEEPING_VINES) || state.isOf(Blocks.WEEPING_VINES_PLANT) || state.isOf(Blocks.TWISTING_VINES) || state.isOf(Blocks.TWISTING_VINES_PLANT) || state.isOf(Blocks.CAVE_VINES) || state.isOf(Blocks.CAVE_VINES_PLANT))) {
            event.cancel();
         }

      }
   }
}
