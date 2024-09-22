package tfar.cornerportallinking.mixin;

import net.minecraft.commands.CommandSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.portal.PortalInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tfar.cornerportallinking.CornerPortalLinking;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable,
        EntityAccess,
        CommandSource {

    @Inject(method = "findDimensionEntryPoint", at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void inject(ServerLevel destination, CallbackInfoReturnable<PortalInfo> cir)
    {
        Entity entity = ((Entity)(Object)this);
        if (CornerPortalLinking.shouldHandle(entity,destination)) {
            cir.setReturnValue(CornerPortalLinking.getTeleportTarget(entity, destination));
        }
    }

}
