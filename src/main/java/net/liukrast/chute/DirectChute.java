package net.liukrast.directchute;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllCreativeModeTabs;
import net.liukrast.chute.content.CombinedItemHandler;
import net.liukrast.chute.content.block.DirectChuteBlock;
import net.liukrast.chute.content.block.DirectChuteBlockEntity;
import net.liukrast.chute.content.block.DirectChuteItem;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(DirectChute.MOD_ID)
public class DirectChute {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DirectChuteConstants.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DirectChuteConstants.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, DirectChuteConstants.MOD_ID);

    public static final DeferredItem<BlockItem> DIRECT_CHUTE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("direct_chute", DIRECT_CHUTE);
    public static final DeferredBlock<Block> DIRECT_CHUTE = BLOCKS.register("direct_chute", () -> new DirectChuteBlock(BlockBehaviour.Properties.ofFullCopy(AllBlocks.ZINC_BLOCK.get())));
    private static final DeferredItem<BlockItem> DIRECT_CHUTE_BLOCK_ITEM = ITEMS.register("direct_chute", () -> new DirectChuteItem(DIRECT_CHUTE.get(), new Item.Properties()));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DirectChuteBlockEntity>> DIRECT_CHUTE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "direct_chute",
            () -> BlockEntityType.Builder.of(
                    (pos, state) -> DirectChuteConfig.BLOCK_ENTITY.getAsBoolean() ? new DirectChuteBlockEntity(pos, state) : null,
                    DIRECT_CHUTE.get()
            ).build(null)
    );

    public DirectChute(IEventBus modEventBus, ModContainer container) {

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);

        modEventBus.register(this);
    }

    @SubscribeEvent
    private void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey()) event.accept(DIRECT_CHUTE_BLOCK_ITEM);
    }

    @SubscribeEvent
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, context) -> {
            if(context != Direction.DOWN) return null;
            List<IItemHandler> handlers = new ArrayList<>();

            var facing = state.getValue(DirectChuteBlock.FACING);
            var shape = state.getValue(DirectChuteBlock.SHAPE);
            if(facing.getAxis().isVertical() || shape == DirectChuteBlock.Shape.INTERSECTION) {
                var cap1 = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.above(), context);
                if(cap1 != null) handlers.add(cap1);
                Direction dir = Direction.NORTH;
                for(int i = 0; i < 4; i++) {
                    dir = dir.getClockWise();
                    var pos1 = pos.relative(dir).above();
                    var state1 = level.getBlockState(pos1);
                    if(state1.is(state.getBlock()) && state1.getValue(DirectChuteBlock.FACING) == dir) {
                        var cap2 = level.getCapability(Capabilities.ItemHandler.BLOCK, pos1, context);
                        if(cap2 != null) handlers.add(cap2);
                    }
                }
            } else if(facing.getAxis().isHorizontal()) {
                var cap1 = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.above().relative(facing), context);
                if(cap1 != null) handlers.add(cap1);
            }
            if(handlers.isEmpty()) return null;
            return new CombinedItemHandler(handlers.toArray(IItemHandler[]::new));
        }, DIRECT_CHUTE.get());
    }
}
