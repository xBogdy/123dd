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
package com.gitlab.srcmc.rctmod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.EnvironmentInterface;

/**
 * Intended to be implemented on types together with the {@link
 * EnvironmentInterface} annotation, i.e:
 *<pre>{@code
 *&#64;EnvironmentInterface(itf = ClientType.class, value = EnvType.CLIENT)
 *class SomeClass implements ClientType {}
 *}</pre>
 * This allows to distinguish between the the two sides (client and server) within
 * any class simply by checking if the current type implements {@link ClientType},
 * e.g. with {@link ClientType#is(Class)} (it is adviced to store the result).
 */
@EnvironmentInterface(itf = ClientType.class, value = EnvType.CLIENT)
public interface ClientType {
    static <T> boolean is(Class<T> clazz) {
        return ClientType.class.isAssignableFrom(clazz);
    }
}
