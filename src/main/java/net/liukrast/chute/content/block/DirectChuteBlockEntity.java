package net.liukrast.chute.content.block;

import net.liukrast.chute.DirectChute;
import net.liukrast.chute.DirectChuteConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class DirectChuteBlockEntity extends BlockEntity {

    private BlockCapabilityCache<IItemHandler, @Nullable Direction> aboveCache;
    private BlockCapabilityCache<IItemHandler, @Nullable Direction> belowCache;

    public DirectChuteBlockEntity(BlockPos pos, BlockState blockState) {
        super(DirectChute.DIRECT_CHUTE_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(level == null || level.isClientSide) return;
        this.aboveCache = BlockCapabilityCache.create(
                Capabilities.ItemHandler.BLOCK,
                (ServerLevel) level,
                getBlockPos(),
                Direction.DOWN
        );
        this.belowCache = BlockCapabilityCache.create(
                Capabilities.ItemHandler.BLOCK,
                (ServerLevel) level,
                getBlockPos().below(),
                Direction.UP
        );
    }

    public void tick(BlockState blockState) {
        if(level == null || level.isClientSide) return;
        if(level.getGameTime()% DirectChuteConfig.TICK_UPDATE.getAsInt() != 0) return;
        if(!(blockState.getBlock() instanceof DirectChuteBlock dcb)) return;
        if(!dcb.shouldTick((ServerLevel) level, getBlockState(), getBlockPos())) return;
        var a = aboveCache.getCapability();
        var b = belowCache.getCapability();
        if(a == null || b == null) return;
        dcb.extract(a, b);
    }
}
