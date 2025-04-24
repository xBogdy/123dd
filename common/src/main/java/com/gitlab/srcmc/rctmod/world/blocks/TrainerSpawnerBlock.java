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
package com.gitlab.srcmc.rctmod.world.blocks;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.world.blocks.entities.TrainerSpawnerBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

public class TrainerSpawnerBlock extends BaseEntityBlock {
    public static final MapCodec<TrainerSpawnerBlock> CODEC = TrainerSpawnerBlock.simpleCodec(TrainerSpawnerBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty LOOTABLE = BooleanProperty.create("lootable");

    public static boolean isPowered(BlockState blockState) {
        return blockState.getValue(POWERED);
    }

    static BlockState setPowered(BlockState blockState, boolean value) {
        return blockState.setValue(POWERED, value);
    }

    public static boolean isLootable(BlockState blockState) {
        return blockState.getValue(LOOTABLE);
    }

    static BlockState setLootable(BlockState blockState, boolean value) {
        return blockState.setValue(LOOTABLE, value);
    }

    // sub-optimal for ai pathing (TODO: adjust model?)
    // public static final VoxelShape SHAPE = Shapes.or(
    //     Block.box(0, 0, 0, 16, 3, 16),
    //     Block.box(0, 13, 0, 16, 16, 16),
    //     Block.box(1, 3, 11, 5, 13, 15),
    //     Block.box(11, 3, 1, 15, 13, 5),
    //     Block.box(11, 3, 11, 15, 13, 15),
    //     Block.box(1, 3, 1, 5, 13, 5));

    public TrainerSpawnerBlock() {
        this(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion());
    }

    public TrainerSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(POWERED, false)
            .setValue(LOOTABLE, true));
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        checkNeighbour(blockState, level, blockPos);
        super.onPlace(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if(!blockState.is(blockState2.getBlock())) {
            level.getBlockEntity(blockPos, ModRegistries.BlockEntityTypes.TRAINER_SPAWNER.get()).ifPresent(be -> be.setOwner(null));
        }

        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
        if(isLootable(blockState)) {
            var be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

            if(be instanceof TrainerSpawnerBlockEntity sbe) {
                var drops = new ArrayList<>(super.getDrops(blockState, builder));
                var tm = RCTMod.getInstance().getTrainerManager();
                var reg = BuiltInRegistries.ITEM;

                sbe.getTrainerIds().stream().map(tm::getData)
                    .filter(t -> t.getSignatureItem() != null && !t.getSignatureItem().isBlank())
                    .map(t -> reg.get(ResourceLocation.parse(t.getSignatureItem())))
                    .distinct().filter(i -> i != null && i != Items.AIR)
                    .map(i -> i.getDefaultInstance())
                    .forEach(drops::add);

                return drops;
            }
        }

        return super.getDrops(blockState, builder);
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        checkNeighbour(blockState, level, blockPos);
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
    }

    private static void checkNeighbour(BlockState blockState, Level level, BlockPos blockPos) {
        var neigh = level.hasNeighborSignal(blockPos);

        if(neigh != isPowered(blockState)) {
            level.setBlock(blockPos, setPowered(blockState, neigh), 2);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if(isPowered(blockState)) {
            double d = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 0.2;
            double e = (double)blockPos.getY() + 0.7 + (randomSource.nextDouble() - 0.5) * 0.2;
            double f = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 0.2;
            level.addParticle(DustParticleOptions.REDSTONE, d, e, f, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{POWERED, LOOTABLE});
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        var result = ItemInteractionResult.FAIL;

        if(level.getBlockEntity(blockPos) instanceof TrainerSpawnerBlockEntity be) {
            if(be.addTrainerIdsFromItem(RCTMod.getInstance().getTrainerManager(), itemStack)) {
                if(!level.isClientSide) {
                    itemStack.consume(1, player);
                }
                
                result = ItemInteractionResult.SUCCESS;
            }
        }

        return result;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TrainerSpawnerBlockEntity(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    // @Override
    // protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
    //     return TrainerSpawnerBlock.SHAPE;
    // }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return TrainerSpawnerBlock.CODEC;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return TrainerSpawnerBlock.createTickerHelper(blockEntityType, ModRegistries.BlockEntityTypes.TRAINER_SPAWNER.get(), level.isClientSide ? TrainerSpawnerBlockEntity::clientTick : TrainerSpawnerBlockEntity::serverTick);
    }
}
