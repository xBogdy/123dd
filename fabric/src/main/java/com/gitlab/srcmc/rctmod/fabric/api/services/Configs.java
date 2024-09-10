/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2024, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctmod.fabric.api.services;

import com.gitlab.srcmc.rctmod.api.config.IClientConfig;
import com.gitlab.srcmc.rctmod.api.config.ICommonConfig;
import com.gitlab.srcmc.rctmod.api.config.IServerConfig;
import com.gitlab.srcmc.rctmod.api.service.IConfigs;

public class Configs implements IConfigs {
    private final IServerConfig serverConfig;
    private final IClientConfig clientConfig;
    private final ICommonConfig commonConfig;

    public Configs(IClientConfig clientConfig, IServerConfig serverConfig, ICommonConfig commonConfig) {
        this.clientConfig = clientConfig;
        this.serverConfig = serverConfig;
        this.commonConfig = commonConfig;
    }

    @Override
    public IClientConfig clientConfig() {
        return this.clientConfig;
    }

    @Override
    public ICommonConfig commonConfig() {
        return this.commonConfig;
    }

    @Override
    public IServerConfig serverConfig() {
        return this.serverConfig;
    }
}
