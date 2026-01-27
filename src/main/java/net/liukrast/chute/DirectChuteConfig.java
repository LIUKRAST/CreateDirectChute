package net.liukrast.chute;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DirectChuteConfig {
    private DirectChuteConfig() {}

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue TICK_UPDATE = BUILDER
            .comment("Defines how many ticks it takes for the direct chute to update again")
            .defineInRange("tickUpdate", 5, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue EXTRACTION_RATE = BUILDER
            .comment("Defines how many items it extracts per update")
            .defineInRange("extractionRate", 16, 1, 64);

    public static final ModConfigSpec.BooleanValue BLOCK_ENTITY = BUILDER
            .comment("If enabled, the block uses a BlockEntity to cache the inventories instead of re-querying them every update." +
                    "This should reduce CPU usage, but increases RAM usage due to block entity instances and NBT data (even though it's empty)")
            .define("blockEntity", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
