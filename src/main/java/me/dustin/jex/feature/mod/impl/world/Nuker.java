
package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Nuker extends Feature {
    public Nuker() {
        super(Category.WORLD, "Nuke the blocks around you. (Creative mode only)");
    }

    private final StopWatch stopWatch = new StopWatch();

    public final Property<Integer> distanceProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Distance")
            .description("The distance at which to break blocks")
            .value(4)
            .min(3)
            .max(6)
            .build();
    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Break Delay")
            .description("Delay between breaking blocks in milliseconds")
            .value(0L)
            .max(500)
            .inc(10)
            .build();
    public final Property<Boolean> keepFloorProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Keep Floor")
            .description("Don't break blocks below you, only above.")
            .value(true)
            .build();
   public final Property<Boolean> swingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Swing")
            .value(true)
            .build();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!stopWatch.hasPassed(delayProperty.value()))
            return;
        ArrayList<BlockPos> positions0 = getNegativePositions();
        positions0.forEach(block0Pos -> {
            new EventClickBlock(block0Pos, Direction.UP, EventClickBlock.Mode.PRE).run();
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, block0Pos, Direction.UP));
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, block0Pos, Direction.UP));
	    if (swingProperty.value()) {
            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
	    }
            stopWatch.reset();
        });
		ArrayList<BlockPos> positions1 = getPositivePositions();
        positions1.forEach(block1Pos -> {
            new EventClickBlock(block1Pos, Direction.UP, EventClickBlock.Mode.PRE).run();
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, block1Pos, Direction.UP));
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, block1Pos, Direction.UP));
	    if (swingProperty.value()) {
            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
	    }
            stopWatch.reset();
        });
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    public ArrayList<BlockPos> getNegativePositions() {
        ArrayList<BlockPos> blockPosList0 = new ArrayList<>();
        int dist = distanceProperty.value() + 2;
        int minX = -dist;
        int minY = keepFloorProperty.value() ? 0 : -dist;
        int minZ = -dist;
        for (int x = 0; x > minX; x--)
            for (int y = 0; y > minY; y--)
                for (int z = 0; z > minZ; z--) {
                    BlockPos pos = Wrapper.INSTANCE.getPlayer().getBlockPos().add(x, y, z);
                    Block block = WorldHelper.INSTANCE.getBlock(pos);
                    if (!(block instanceof AirBlock || block instanceof FluidBlock)) {
                        double distance = ClientMathHelper.INSTANCE.getDistance(Vec3d.ofCenter(pos), Wrapper.INSTANCE.getPlayer().getPos().add(0, 1, 0));
                        if (distance > distanceProperty.value())
                            continue;
                        blockPosList0.add(pos);
                        if (delayProperty.value() > 0)
                            break;
                    }
                }
        blockPosList0.sort(Comparator.comparing(o -> o.getSquaredDistance(Wrapper.INSTANCE.getPlayer().getPos())));
        return blockPosList0;
    }
	public ArrayList<BlockPos> getPositivePositions() {
        ArrayList<BlockPos> blockPosList1 = new ArrayList<>();
        int dist = distanceProperty.value() + 2;
        int maxX = dist;
        int maxY = keepFloorProperty.value() ? 0 : dist;
        int maxZ = dist;
        for (int x = 0; x < maxX; x++)
            for (int y = 0; y < maxY; y++)
                for (int z = 0; z < maxZ; z++) {
                    BlockPos pos = Wrapper.INSTANCE.getPlayer().getBlockPos().add(x, y, z);
                    Block block = WorldHelper.INSTANCE.getBlock(pos);
                    if (!(block instanceof AirBlock || block instanceof FluidBlock)) {
                        double distance = ClientMathHelper.INSTANCE.getDistance(Vec3d.ofCenter(pos), Wrapper.INSTANCE.getPlayer().getPos().add(0, 1, 0));
                        if (distance > distanceProperty.value())
                            continue;
                        blockPosList1.add(pos);
                        if (delayProperty.value() > 0)
                            break;
                    }
                }
        blockPosList1.sort(Comparator.comparing(o -> o.getSquaredDistance(Wrapper.INSTANCE.getPlayer().getPos())));
        return blockPosList1;
    }
}
