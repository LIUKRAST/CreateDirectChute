package net.liukrast.chute;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = DirectChuteConstants.MOD_ID, dist = Dist.CLIENT)
public class DirectChuteClient {
    public DirectChuteClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> new BaseConfigScreen(parent, modContainer.getModId()));
    }
}
