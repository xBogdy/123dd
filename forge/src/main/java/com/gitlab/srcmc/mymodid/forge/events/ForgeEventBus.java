package com.gitlab.srcmc.mymodid.forge.events;

import java.io.InputStream;
import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.api.RCTMod;
import com.gitlab.srcmc.mymodid.commands.MobCommands;
import com.gitlab.srcmc.mymodid.commands.PlayerCommands;
import com.gitlab.srcmc.mymodid.forge.world.VolatileTrainer;
import com.selfdot.cobblemontrainers.CobblemonTrainers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ModCommon.MOD_ID, bus = Bus.FORGE)
public class ForgeEventBus {
    @SubscribeEvent
    static void onServerStarted(ServerStartedEvent event) {
        RCTMod.get().getTrainerManager().registerTrainers(ForgeEventBus::addTrainer);
    }

    @SubscribeEvent
    static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(RCTMod.get().getTrainerManager());
    }

	@SubscribeEvent
	static void onCommandRegistry(final RegisterCommandsEvent event) {
		PlayerCommands.register(event.getDispatcher());
        MobCommands.register(event.getDispatcher());
    }

    private static void addTrainer(ResourceLocation rl, IoSupplier<InputStream> io) {
        var trainerReg = CobblemonTrainers.INSTANCE.getTRAINER_REGISTRY();
        var trainer = new VolatileTrainer(rl, io);
        trainerReg.addOrUpdateTrainer(trainer);
    }
}
