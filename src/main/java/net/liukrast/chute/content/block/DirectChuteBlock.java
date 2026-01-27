package net.liukrast.chute.content.block;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.createmod.catnip.lang.Lang;
import net.liukrast.chute.DirectChute;
import net.liukrast.chute.DirectChuteConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

@NonnullDefault
public class DirectChuteBlock extends BaseEntityBlock implements IWrenchable, ProperWaterloggedBlock {

    public static final MapCodec<DirectChuteBlock> CODEC = simpleCodec(DirectChuteBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;
    public static final Property<Shape> SHAPE = EnumProperty.create("shape", Shape.class);


    public DirectChuteBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SHAPE, Shape.NORMAL)
                .setValue(FACING, Direction.DOWN)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if(level.isClientSide) return;
        reschedule((ServerLevel) level, state, pos);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return ProperWaterloggedBlock.withWater(context.getLevel(), super.getStateForPlacement(context), context.getClickedPos());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(stack.is(AllBlocks.ZINC_BLOCK.get().asItem()) && !state.getValue(SHAPE).equals(Shape.ENCASED) && (state.getValue(FACING).getAxis().isVertical() || state.getValue(SHAPE).equals(Shape.NORMAL))) {
            level.setBlockAndUpdate(pos, state.setValue(SHAPE, Shape.ENCASED));
            level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.BLOCKS, 0.5f, 1.05f);
            return ItemInteractionResult.SUCCESS;
        }
        var dir = hitResult.getDirection();
        if(dir.getAxis().isVertical()) return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        var pos1 = pos.above().relative(dir);
        var state1 = level.getBlockState(pos1);
        if(stack.is(this.asItem()) && state1.is(this) && state1.getValue(FACING) == Direction.DOWN) {
            boolean shouldBeEnd = true;
            var state2 = level.getBlockState(pos1.above().relative(dir));
            if(state2.is(this)) shouldBeEnd = false;
            level.setBlockAndUpdate(pos1, state1.setValue(FACING, dir).setValue(SHAPE, shouldBeEnd ? Shape.INTERSECTION : Shape.NORMAL));
            level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.BLOCKS, 0.5f, 1.05f);
            if(state.getValue(FACING).getAxis().isHorizontal()) {
                level.setBlockAndUpdate(pos, state.setValue(SHAPE, Shape.NORMAL));
            } else {
                level.setBlockAndUpdate(pos, state.setValue(SHAPE, Shape.ENCASED));
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        if(state.getValue(SHAPE) == Shape.ENCASED) {
            level.setBlockAndUpdate(pos, state.setValue(SHAPE, Shape.NORMAL));
            level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.BLOCKS, 0.5f, 1.05f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if(newState.is(state.getBlock())) return;
        Direction dir1 = Direction.NORTH;
        for(int i = 0; i < 4; i++) {
            dir1 = dir1.getClockWise();
            var tPos = pos.relative(dir1).above();
            var tState = level.getBlockState(tPos);
            if(tState.is(this) && tState.getValue(FACING).equals(dir1)) {
                level.setBlockAndUpdate(tPos, tState.setValue(SHAPE, Shape.NORMAL).setValue(FACING, Direction.DOWN));
            }
        }
        var dir = state.getValue(FACING);
        if(dir.getAxis().isVertical()) return;
        var stateToChange = level.getBlockState(pos.relative(dir.getOpposite()).below());
        if(stateToChange.is(this) && stateToChange.getValue(FACING).equals(dir)) {
            level.setBlockAndUpdate(pos.relative(dir.getOpposite()).below(), stateToChange.setValue(SHAPE, Shape.INTERSECTION));
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        level.invalidateCapabilities(pos);
        if(level.isClientSide) return;
        reschedule((ServerLevel) level, state, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, SHAPE, WATERLOGGED);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        var above = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.DOWN);
        var below = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.below(), Direction.UP);
        reschedule(level, state, pos);
        if(above == null || below == null) return;
        extract(above, below);
    }

    public void extract(IItemHandler above, IItemHandler below) {
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

    public void reschedule(ServerLevel level, BlockState state, BlockPos pos) {
        if(level.getBlockEntity(pos) != null) return;
        if(!this.shouldTick(level, state, pos)) return;
        level.scheduleTick(pos, state.getBlock(), DirectChuteConfig.TICK_UPDATE.getAsInt());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldTick(ServerLevel level, BlockState state, BlockPos pos) {
        var dir = state.getValue(FACING);
        if(dir.getAxis().isHorizontal()) return false;
        return !level.getBlockState(pos.below()).is(this);
    }

    public int getExtractionRate() {
        return DirectChuteConfig.EXTRACTION_RATE.getAsInt();
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_,
                               CollisionContext p_220053_4_) {
        return DirectChuteShapes.getShape(p_220053_1_);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockGetter p_220071_2_, BlockPos p_220071_3_,
                                        CollisionContext p_220071_4_) {
        return DirectChuteShapes.getCollisionShape(p_220071_1_);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, DirectChute.DIRECT_CHUTE_BLOCK_ENTITY.get(), ($,$1,state1,be) -> be.tick(state1));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(!DirectChuteConfig.BLOCK_ENTITY.getAsBoolean()) return null;
        return new DirectChuteBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public enum Shape implements StringRepresentable {
        INTERSECTION, NORMAL, ENCASED;

        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
