/*
 * This file is part of <MOD_NAME>.
 * Copyright (c) 2024, HDainester, All rights reserved.
 *
 * <MOD_NAME> is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * <MOD_NAME> is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with <MOD_NAME>. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.modtemplate.forge;

import com.gitlab.srcmc.modtemplate.ModTemplate;

import net.minecraftforge.fml.common.Mod;

@Mod(ModTemplate.MODID)
public class ModForge {
    static { ModRegistries.init(); }
}
