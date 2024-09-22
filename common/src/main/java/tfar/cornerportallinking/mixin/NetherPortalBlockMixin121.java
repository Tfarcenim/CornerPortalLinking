package tfar.cornerportallinking.mixin;


import net.minecraft.world.level.block.NetherPortalBlock;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

@Debug(export = true)
@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin121 {


    /*@Inject(method = "getOrCreateExitPortalTarget", at = @At("HEAD"), cancellable = true)
    private void inject(ServerLevel world, Entity entity, BlockPos pos, BlockPos scaledPos, boolean inNether, WorldBorder worldBorder, CallbackInfoReturnable<DimensionTransition> cir)
    {
        var corners = PortalHelper.getCornersVectorAt(entity.level(), pos);

        if( corners.hasLinkingBlocks())
        {
            var portalRect = PortalHelper.modifiedGetPortalRect(scaledPos, inNether, worldBorder, corners);

            if(portalRect.isPresent())
            {
                var that = (NetherPortalBlock)(Object)this;
                DimensionTransition.PostDimensionTransition postDimensionTransition = DimensionTransition.PLAY_PORTAL_SOUND.then((entityx) -> {
                    entityx.placePortalTicket(portalRect.get().minCorner);
                });

                var teleportTarget = that.getDimensionTransitionFromExit(entity, scaledPos, portalRect.get(), world, postDimensionTransition);
                cir.setReturnValue(teleportTarget);
            }
        }
    }*/
}
