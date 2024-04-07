package com.gitlab.srcmc.mymodid.forge.events;

import java.util.List;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.api.RCTMod;
import com.gitlab.srcmc.mymodid.forge.ModRegistries;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;
import kotlin.Unit;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = ModCommon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBus {
    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        RCTMod.init(ModRegistries.LootItemConditions.LEVEL_RANGE);
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, ModEventBus::handleBattleVictory);
    }

    @SubscribeEvent
    static void onEntityAttributeCreation(EntityAttributeCreationEvent event){
        event.put(ModRegistries.Entities.TRAINER.get(), TrainerMob.createAttributes().build());
    }

    private static Unit handleBattleVictory(BattleVictoryEvent event) {
        if(!checkForTrainerBattle(event.getWinners(), true)) {
            checkForTrainerBattle(event.getLosers(), false);
        }

        return Unit.INSTANCE;
    }

    private static boolean checkForTrainerBattle(List<BattleActor> actors, boolean winners) {
        for(var actor : actors) {
            var trainerBattle = RCTMod.get().getTrainerManager().getBattle(actor.getUuid());

            if(trainerBattle.isPresent()) {
                RCTMod.get().getTrainerManager().removeBattle(actor.getUuid());
                trainerBattle.get().distributeRewards(winners);
                return true;
            }
        }

        return false;
    }
}
