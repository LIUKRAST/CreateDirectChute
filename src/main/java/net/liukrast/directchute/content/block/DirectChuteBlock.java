package net.liukrast.directchute.content.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.lwjgl.system.NonnullDefault;

@NonnullDefault
public class DirectChuteBlock extends Block implements IWrenchable {

    public DirectChuteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if(level.isClientSide) return;
        level.scheduleTick(pos, state.getBlock(), 5);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        var above = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.above(), Direction.DOWN);
        var below = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.below(), Direction.UP);
        level.scheduleTick(pos, state.getBlock(), 5);
        if(above == null || below == null) return;

        int itemsMoved = 0;

        for (int srcSlot = 0; srcSlot < above.getSlots(); srcSlot++) {
            ItemStack stackInSlot = above.getStackInSlot(srcSlot);
            if (stackInSlot.isEmpty()) continue;
            int remainingToTransfer = getExtractionRate() - itemsMoved;
            if (remainingToTransfer <= 0) break;
            ItemStack simulatedExtract = above.extractItem(srcSlot, remainingToTransfer, true);
            if (simulatedExtract.isEmpty()) continue;
            ItemStack remainingAfterInsert = simulatedExtract.copy();
            for (int dstSlot = 0; dstSlot < below.getSlots(); dstSlot++) {
                remainingAfterInsert = below.insertItem(dstSlot, remainingAfterInsert, true);
                if (remainingAfterInsert.isEmpty()) break;
            }

            int insertedCount = simulatedExtract.getCount() - remainingAfterInsert.getCount();
            if (insertedCount > 0) {
                ItemStack extracted = above.extractItem(srcSlot, insertedCount, false);
                ItemStack remaining = extracted.copy();
                for (int dstSlot = 0; dstSlot < below.getSlots(); dstSlot++) {
                    remaining = below.insertItem(dstSlot, remaining, false);
                    if (remaining.isEmpty()) break;
                }

                itemsMoved += insertedCount;
                if (itemsMoved >= getExtractionRate()) break;
            }
        }
    }

    /* For some addon developers */
    public int getExtractionRate() {
        return 16;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(box(2, 0, 2, 14, 8, 14), box(1, 8, 1, 15, 16, 15));
    }
}
