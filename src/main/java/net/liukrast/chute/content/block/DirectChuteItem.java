package net.liukrast.chute.content.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DirectChuteItem extends BlockItem {
    public DirectChuteItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @Nullable BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        var dir = context.getClickedFace();
        var pos = context.getClickedPos();
        if(context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) return super.updatePlacementContext(context);
        if(dir.getAxis().isHorizontal() && context.getLevel().getBlockState(pos.relative(dir.getOpposite())).is(this.getBlock())) {
            var above = pos.above();
            if(!context.getLevel().isEmptyBlock(above)) return null;
            return BlockPlaceContext.at(context, above, dir);
        }
        return super.updatePlacementContext(context);
    }

    @Override
    protected @Nullable BlockState getPlacementState(@NotNull BlockPlaceContext context) {
        var state = super.getPlacementState(context);
        if(state == null) return null;
        var dir = context.getClickedFace();
        var level = context.getLevel();
        if(context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) return super.getPlacementState(context);
        var pos = context.getClickedPos();
        if(dir.getAxis().isHorizontal() && level.getBlockState(pos.relative(dir.getOpposite()).below()).is(this.getBlock())) {
            var posToUpdate = context.getClickedPos().relative(dir.getOpposite()).below();
            var stateToUpdate = level.getBlockState(posToUpdate);
            if(stateToUpdate.is(this.getBlock())) {
                closure:
                {
                    if (
                            stateToUpdate.getValue(DirectChuteBlock.FACING).equals(dir)
                                    && level.getBlockState(context.getClickedPos().relative(dir.getOpposite(), 2).below(2)).is(this.getBlock())
                    ) {
                        Direction dir1 = dir;
                        for (int i = 0; i < 3; i++) {
                            dir1 = dir1.getClockWise();
                            var tState = level.getBlockState(posToUpdate.relative(dir1).above());
                            if (tState.is(this.getBlock()) && tState.getValue(DirectChuteBlock.FACING).equals(dir1))
                                break closure;
                        }
                        level.setBlockAndUpdate(posToUpdate, stateToUpdate.setValue(DirectChuteBlock.SHAPE, DirectChuteBlock.Shape.NORMAL));
                        return state.setValue(DirectChuteBlock.FACING, dir).setValue(DirectChuteBlock.SHAPE, DirectChuteBlock.Shape.INTERSECTION);
                    }
                }
                if (stateToUpdate.getValue(DirectChuteBlock.FACING).getAxis().isVertical())
                    level.setBlockAndUpdate(posToUpdate, stateToUpdate.setValue(DirectChuteBlock.SHAPE, DirectChuteBlock.Shape.ENCASED));
                else {
                    int conns = 1;
                    Direction dir1 = dir;
                    for(int i = 0; i < 4; i++) {
                        dir1 = dir1.getClockWise();
                        var tState = level.getBlockState(posToUpdate.relative(dir1).above());
                        if (tState.is(this.getBlock()) && tState.getValue(DirectChuteBlock.FACING).equals(dir1)) conns++;
                    }
                    if(conns > 1) level.setBlockAndUpdate(posToUpdate, stateToUpdate.setValue(DirectChuteBlock.SHAPE, DirectChuteBlock.Shape.ENCASED));
                }
            }
            return state.setValue(DirectChuteBlock.FACING, dir).setValue(DirectChuteBlock.SHAPE, DirectChuteBlock.Shape.INTERSECTION);
        }
        var tState = level.getBlockState(pos.below());
        if(tState.is(this.getBlock()) && tState.getValue(DirectChuteBlock.FACING).getAxis().isHorizontal()) {
            level.setBlockAndUpdate(pos.below(), tState.setValue(DirectChuteBlock.SHAPE, DirectChuteBlock.Shape.ENCASED));
        }
        return super.getPlacementState(context);
    }
}
