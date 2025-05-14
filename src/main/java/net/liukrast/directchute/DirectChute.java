package net.liukrast.directchute;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllCreativeModeTabs;
import net.liukrast.directchute.content.block.DirectChuteBlock;
import net.liukrast.directchute.content.block.DirectChuteBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Set;

@Mod(DirectChute.MOD_ID)
public class DirectChute {
    public static final String MOD_ID = "direct_chute";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<Block> DIRECT_CHUTE = BLOCKS.register("direct_chute", () -> new DirectChuteBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    public static final RegistryObject<BlockItem> DIRECT_CHUTE_BLOCK_ITEM = ITEMS.register("direct_chute", () -> new BlockItem(DIRECT_CHUTE.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<DirectChuteBlockEntity>> DC = BLOCK_ENTITY_TYPES.register("direct_chute", () -> new BlockEntityType<>(DirectChuteBlockEntity::new, Set.of(DIRECT_CHUTE.get()), null));

    public DirectChute() {

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);

        modEventBus.register(this);
    }

    @SubscribeEvent
    public void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey()) event.accept(DIRECT_CHUTE_BLOCK_ITEM);
    }
}
