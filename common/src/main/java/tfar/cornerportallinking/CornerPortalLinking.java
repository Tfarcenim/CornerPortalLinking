package tfar.cornerportallinking;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class CornerPortalLinking {

    public static final String MOD_ID = "cornerportallinking";
    public static final String MOD_NAME = "CornerPortalLinking";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static final TagKey<Block> LINKING_BLOCKS = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "portal_linking"));

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {

    }

    public static boolean shouldHandle(Entity entity,ServerLevel destination) {
        // Quick access variables
        boolean toTheNether = destination.dimension() == Level.NETHER;

        boolean fromTheNether = entity.level().dimension() == Level.NETHER;

        // Only supported situations left are nether related
        if (!fromTheNether && !toTheNether) {
            return false;
        }
        return true;
    }

    public static PortalInfo getTeleportTarget(Entity entity, ServerLevel destination)
    {
        // Very similar to vanilla except for the call to entity.getPortalRect
        // It is only called if the portal has no valid linking corners.

        // Quick access variables
        boolean toTheNether = destination.dimension() == Level.NETHER;

        // Nether related
        WorldBorder worldBorder = destination.getWorldBorder();
        double d = DimensionType.getTeleportationScale(entity.level().dimensionType(), destination.dimensionType());
        BlockPos blockPos2 = worldBorder.clampToBounds(entity.getX() * d, entity.getY(), entity.getZ() * d);

        Optional<BlockUtil.FoundRectangle> portalRect;

        // Linking
        var corners = PortalHelper.getLastNetherPortalCornersVector(entity);
        if( corners.hasLinkingBlocks())
        {
            // Modded
            portalRect = PortalHelper.modifiedGetPortalRect(destination, blockPos2, toTheNether, worldBorder, corners);
            if (!portalRect.isPresent()) {
                portalRect = entity.getExitPortal(destination, blockPos2, toTheNether, worldBorder);
            }
        }
        else
        {
            // Vanilla
            portalRect = entity.getExitPortal(destination, blockPos2, toTheNether, worldBorder);
        }

        return portalRect.map(rect -> {
            Vec3 vec3d;
            Direction.Axis axis;
            BlockState blockState = entity.level().getBlockState(entity.portalEntrancePos);
            if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
            {
                axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS); // X or Z only
                var rectangle = BlockUtil.getLargestRectangleAround(entity.portalEntrancePos, axis, 21, Direction.Axis.Y, 21, pos -> entity.level().getBlockState((BlockPos)pos) == blockState);
                vec3d = entity.getRelativePortalPosition(axis, rectangle);
            }
            else
            {
                axis = Direction.Axis.X; // Arbitrary choice between X and Z
                vec3d = new Vec3(0.5, 0.0, 0.0); // Bottom-Center of the portal
            }
            return PortalShape.createPortalInfo(destination, rect, axis, vec3d, entity, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
        }).orElse(null);
    }
}