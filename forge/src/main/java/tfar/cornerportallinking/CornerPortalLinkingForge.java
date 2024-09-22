package tfar.cornerportallinking;

import net.minecraftforge.fml.common.Mod;

@Mod(CornerPortalLinking.MOD_ID)
public class CornerPortalLinkingForge {

    public CornerPortalLinkingForge() {
    
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
    
        // Use Forge to bootstrap the Common mod.
        CornerPortalLinking.LOG.info("Hello Forge world!");
        CornerPortalLinking.init();
        
    }
}