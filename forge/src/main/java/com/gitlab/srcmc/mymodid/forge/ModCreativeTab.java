package com.gitlab.srcmc.mymodid.forge;

import com.gitlab.srcmc.mymodid.ModCommon;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModCreativeTab extends CreativeModeTab {
    private static ModCreativeTab instance;

    public ModCreativeTab() {
        super(CreativeModeTab.TABS.length, ModCommon.MOD_ID);
    }

    @Override
    public ItemStack makeIcon() {
        return Items.END_CRYSTAL.getDefaultInstance();
    }

    public static ModCreativeTab get() {
        return instance == null ? (instance = new ModCreativeTab()) : instance;
    }
}
