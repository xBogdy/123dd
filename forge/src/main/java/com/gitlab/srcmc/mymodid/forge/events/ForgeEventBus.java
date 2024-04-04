package com.gitlab.srcmc.mymodid.forge.events;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.commands.MobCommands;
import com.gitlab.srcmc.mymodid.commands.PlayerCommands;
import com.gitlab.srcmc.mymodid.forge.world.VolatileTrainer;
import com.selfdot.cobblemontrainers.CobblemonTrainers;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ModCommon.MOD_ID, bus = Bus.FORGE)
public class ForgeEventBus {
    @SubscribeEvent
    static void onLevelLoad(LevelEvent.Load event) {
        Minecraft.getInstance().getResourceManager()
            .listResources("trainers", rl -> rl.getPath().toLowerCase().endsWith(".json"))
            .forEach(ForgeEventBus::addTrainer);
    }

	@SubscribeEvent
	static void onCommandRegistry(final RegisterCommandsEvent event) {
		PlayerCommands.register(event.getDispatcher());
        MobCommands.register(event.getDispatcher());
    }

    private static void addTrainer(ResourceLocation rl, Resource rs) {
        var trainerReg = CobblemonTrainers.INSTANCE.getTRAINER_REGISTRY();
        var trainer = new VolatileTrainer(rl, rs);
        trainerReg.addOrUpdateTrainer(trainer);
    }
}
