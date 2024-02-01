/*
 * This file is part of Example Mod.
 * Copyright (c) 2024, HDainester, All rights reserved.
 *
 * Example Mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Example Mod is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Example Mod. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.my_mod_id.forge;

import com.gitlab.srcmc.my_mod_id.ModCommon;

import net.minecraftforge.fml.common.Mod;

@Mod(ModCommon.MOD_ID)
public class ModForge {
    static { ModRegistries.init(); }
}
