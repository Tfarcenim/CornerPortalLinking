package tfar.cornerportallinking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;

public class PortalHelper {


    /**
     * Returns a Rectangle describing the destination nether portal.
     * @param destWorld
     * @param destPos
     * @param destIsNether
     * @param worldBorder
     * @param corners
     * @return
     */
    public static Optional<BlockUtil.FoundRectangle> modifiedGetPortalRect(ServerLevel destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder, PortalCorners corners)
    {
        Optional<PoiRecord> pointOfInterest = findDestPortal(destWorld, destPos, destIsNether, worldBorder, corners);
        return pointOfInterest.map(poi -> {
            BlockPos blockPos = poi.getPos();
            destWorld.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockPos), 3, blockPos);
            BlockState blockState = destWorld.getBlockState(blockPos);
            return BlockUtil.getLargestRectangleAround(blockPos, blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, pos -> destWorld.getBlockState((BlockPos)pos) == blockState);
        });
    }


    /**
     * Find the best point of interest that correspond to a destination portal.
     * @param world
     * @param destPos
     * @param destIsNether
     * @param worldBorder
     * @param corners
     * @return
     */
    private static Optional<PoiRecord> findDestPortal(ServerLevel world, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder, PortalCorners corners)
    {
        PoiManager pointOfInterestStorage = world.getPoiManager();
        int i = destIsNether ? 16 : 128;
        pointOfInterestStorage.ensureLoadedAndValid(world, destPos, i);

        var collection = pointOfInterestStorage.getInSquare(poiType -> poiType.is(PoiTypes.NETHER_PORTAL), destPos, i, PoiManager.Occupancy.ANY)
                .filter(poi -> worldBorder.isWithinBounds(poi.getPos()))
                .filter(poi -> world.getBlockState(poi.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
                .toList();

        PoiRecord poi = null;

        if(!collection.isEmpty())
        {
            poi = getSortedPortalPOIs(collection, world, corners, destPos).findFirst().get();
        }

        return Optional.ofNullable(poi);
    }


    private static Stream<PoiRecord> getSortedPortalPOIs(List<PoiRecord> pointOfInterests, ServerLevel world, PortalCorners originVector, BlockPos destPos)
    {
        HashMap<PoiRecord, Float> map = new HashMap<>();

        for(var poi : pointOfInterests)
        {
            // This is not very efficient because each portal tile is a poi
            var corners = getCornersVectorAt(world, poi.getPos());
            var score = originVector.score(corners);
            var distance = 1f - score;

            map.put(poi, distance);
        }

        var comparator = Comparator.comparingDouble((PoiRecord poi) -> map.get(poi))
                // Followed by vanilla comparison
                .thenComparingDouble((PoiRecord poi) -> poi.getPos().distSqr(destPos))
                .thenComparingInt((PoiRecord poi) -> poi.getPos().getY());

        return pointOfInterests.stream().sorted(comparator);
    }

    public static PortalCorners getLastNetherPortalCornersVector(Entity entity)
    {
        return getCornersVectorAt(entity.level(), entity.portalEntrancePos);
    }

    public static PortalCorners getCornersVectorAt(Level world, BlockPos position)
    {
        var blockState = world.getBlockState(position);
        var axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS); // X or Z only
        var rectangle = BlockUtil.getLargestRectangleAround(position, axis, 21, Direction.Axis.Y, 21, pos -> world.getBlockState((BlockPos)pos) == blockState);

        return getCornersVector(rectangle, axis, world);
    }

    private static PortalCorners getCornersVector(BlockUtil.FoundRectangle rectangle, Direction.Axis axis, Level world)
    {
        BlockPos a, b, c, d;
        if( axis == Direction.Axis.X)
        {
            a = rectangle.minCorner.offset(-1, -1, 0);
            b = rectangle.minCorner.offset(rectangle.axis1Size, -1, 0);
            c = rectangle.minCorner.offset(-1, rectangle.axis2Size, 0);
            d = rectangle.minCorner.offset(rectangle.axis1Size, rectangle.axis2Size, 0);
        }
        else
        {
            a = rectangle.minCorner.offset(0, -1, -1);
            b = rectangle.minCorner.offset(0, -1, rectangle.axis1Size);
            c = rectangle.minCorner.offset(0, rectangle.axis2Size, -1);
            d = rectangle.minCorner.offset(0, rectangle.axis2Size, rectangle.axis1Size);
        }

        var corners = new PortalCorners();
        corners.lower1 = blockPosToLinkingState(world, a);
        corners.lower2 = blockPosToLinkingState(world, b);
        corners.upper1 = blockPosToLinkingState(world, c);
        corners.upper2 = blockPosToLinkingState(world, d);

        return corners;
    }

    private static BlockState blockPosToLinkingState(Level world, BlockPos position)
    {
        return filterLinkingBlock(world.getBlockState(position));
    }

    private static BlockState filterLinkingBlock(BlockState state)
    {
        return state.is(CornerPortalLinking.LINKING_BLOCKS) ? state : null;
    }
}
