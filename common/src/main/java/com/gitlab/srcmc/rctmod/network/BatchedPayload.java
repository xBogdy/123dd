/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2025, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctmod.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.utils.ArrUtils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public class BatchedPayload {
    public static final int DEFAULT_BATCH_SIZE = (int)Math.pow(2, 18);

    public final StreamCodec<RegistryFriendlyByteBuf, Payload> CODEC = StreamCodec.of(
        (b, v) -> {
            b.writeInt(v.remainingBatches());
            b.writeByteArray(v.bytes());
        },
        b -> {
            var i = b.readInt();
            var a = b.readByteArray();
            return new Payload(a, i);
        }
    );

    public class Payload implements CustomPacketPayload {
        private byte[] bytes;
        private int remainingBatches;

        Payload(byte[] bytes, int remainingBatches) {
            this.bytes = bytes;
            this.remainingBatches = remainingBatches;
        }

        public byte[] bytes() {
            return this.bytes;
        }

        public int remainingBatches() {
            return this.remainingBatches;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public final Type<Payload> TYPE;

    BatchedPayload(Type<Payload> type) {
        TYPE = type;
    }

    public Payload payload(byte[] bytes) {
        return payload(bytes);
    }

    public Payload payload(byte[] bytes, int remainingBatches) {
        return new Payload(bytes, remainingBatches);
    }

    public Payload[] payloads(byte[] bytes) {
        return this.payloads(bytes, DEFAULT_BATCH_SIZE);
    }

    public Payload[] payloads(byte[] bytes, int batchSize) {
        var batches = ArrUtils.split(bytes, batchSize);
        var pls = new Payload[batches.size()];
        var i = 0;

        for(var b : batches) {
            pls[i] = new Payload(b, batches.size() - (i + 1));
            ++i;
        }

        return pls;
    }

    public <T extends Serializable> Payload[] payloads(T obj) {
        return this.payloads(obj, DEFAULT_BATCH_SIZE);
    }

    public <T extends Serializable> Payload[] payloads(T obj, int batchSize) {
        var bytes = new ByteArrayOutputStream();

        try(var oos = new ObjectOutputStream(bytes)) {
            oos.writeObject(obj);
            return this.payloads(bytes.toByteArray(), batchSize);
        } catch (IOException e) {
            ModCommon.LOG.error(e.getMessage(), e);
        }

        return new Payload[0];
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> T from(Payload[] payloads) {
        var bytes = new ByteArrayInputStream(ArrUtils.combine(Stream.of(payloads).map(pl -> pl.bytes).toList()));

        try(var ois = new ObjectInputStream(bytes)) {
            return (T)ois.readObject();
        } catch(IOException | ClassNotFoundException e) {
            ModCommon.LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public static BatchedPayload create(String id) {
        return new BatchedPayload(new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, id)));
    }
}
