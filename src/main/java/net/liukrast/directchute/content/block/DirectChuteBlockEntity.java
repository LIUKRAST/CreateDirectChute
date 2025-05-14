package net.liukrast.directchute.content.block;

import net.liukrast.directchute.DirectChute;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DirectChuteBlockEntity extends BlockEntity {
    public DirectChuteBlockEntity(BlockPos pos, BlockState blockState) {
        super(DirectChute.DC.get(), pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(side == Direction.DOWN && cap == ForgeCapabilities.ITEM_HANDLER) {
            var be = level.getBlockEntity(getBlockPos().above());
            if(be != null) {
                return be.getCapability(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }
}
