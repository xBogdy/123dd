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
