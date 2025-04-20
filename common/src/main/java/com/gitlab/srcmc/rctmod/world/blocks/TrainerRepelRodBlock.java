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

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class TrainerRepelRodBlock extends RodBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<RodBlock> CODEC = TrainerRepelRodBlock.simpleCodec(TrainerRepelRodBlock::new);
    public static final BooleanProperty WATERLOGGED = BooleanProperty.create("waterlogged");

    public TrainerRepelRodBlock() {
        this(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHTNING_ROD));
        this.registerDefaultState((this.stateDefinition.any()).setValue(FACING, Direction.UP).setValue(WATERLOGGED, false));
    }

    public TrainerRepelRodBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        var fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        return this.defaultBlockState().setValue(FACING, blockPlaceContext.getClickedFace()).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState.getValue(WATERLOGGED)) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }

        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING, WATERLOGGED});
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        var facing = blockState.getValue(FACING);
        double x, y, z;

        switch (facing) {
            case UP:
                x = 0.5;
                y = 1.2;
                z = 0.5;
                break;
            case DOWN:
                x = 0.5;
                y = -0.2;
                z = 0.5;
                break;
            case NORTH:
                x = 0.5;
                y = 0.5;
                z = -0.2;
                break;
            case EAST:
                x = 1.2;
                y = 0.5;
                z = 0.5;
                break;
            case SOUTH:
                x = 0.5;
                y = 0.5;
                z = 1.2;
                break;
            case WEST:
                x = -0.2;
                y = 0.5;
                z = 0.5;
                break;
            default:
                x = y = z = 0.5;
        }

        double d = (double)blockPos.getX() + x + (randomSource.nextDouble() - 0.5) * 0.2;
        double e = (double)blockPos.getY() + y + (randomSource.nextDouble() - 0.5) * 0.2;
        double f = (double)blockPos.getZ() + z + (randomSource.nextDouble() - 0.5) * 0.2;
        level.addParticle(DustParticleOptions.REDSTONE, d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    public MapCodec<? extends RodBlock> codec() {
        return TrainerRepelRodBlock.CODEC;
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onPlace(blockState, level, blockPos, blockState2, bl);
        RCTMod.getInstance().getTrainerSpawner().markChunks(level, blockPos, true);
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onRemove(blockState, level, blockPos, blockState2, bl);
        RCTMod.getInstance().getTrainerSpawner().markChunks(level, blockPos, false);
    }
}
