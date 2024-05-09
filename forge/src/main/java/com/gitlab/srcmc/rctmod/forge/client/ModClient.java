package com.gitlab.srcmc.rctmod.forge.client;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public class ModClient extends com.gitlab.srcmc.rctmod.client.ModClient {
    @Override
    public Optional<Player> getLocalPlayer() {
        var mc = Minecraft.getInstance();
        return Optional.of(mc.player);
    }
}
