package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.movement.speed.Speed;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import java.util.ArrayList;
import java.util.Comparator;

public class Tunneller extends Feature {

    public final Property<Boolean> handleLiquidsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Handle Liquids")
            .value(true)
            .build();
    public final Property<Integer> widthProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Width")
            .value(3)
            .min(1)
            .max(6)
            .build();
    public final Property<Integer> heightProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Height")
            .value(3)
            .min(1)
            .max(6)
            .build();
    public final Property<Boolean> swingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Swing")
            .value(true)
            .build();

    private Direction direction;

    public Tunneller() {
        super(Category.WORLD);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating || KillAura.INSTANCE.hasTarget())
            return;
        if (direction == null)
            direction = Wrapper.INSTANCE.getPlayer().getHorizontalFacing();
        setSuffix(getDirectionString());
        //do liquid replacing
        if (handleLiquidsProperty.value())
            for (BlockPos liquidCheckSpot : getLiquidCheckSpots()) {
                if (WorldHelper.INSTANCE.getBlock(liquidCheckSpot) instanceof FluidBlock) {
                    if (moveToBlocks()) {
                        PlayerHelper.INSTANCE.placeBlockInPos(liquidCheckSpot, Hand.MAIN_HAND, true);
                        return;
                    } else {
                        ChatHelper.INSTANCE.addClientMessage("Tunneller ran out of blocks. Disabling.");
                        this.setState(false);
                        return;
                    }
                } else if (WorldHelper.INSTANCE.isWaterlogged(liquidCheckSpot)){
                    Wrapper.INSTANCE.getClientPlayerInteractionManager().updateBlockBreakingProgress(liquidCheckSpot, Direction.UP);
                    if (swingProperty.value()) {
                    Wrapper.INSTANCE.getPlayer().swingHand(Hand.MAIN_HAND);
                    }
                    return;
                }
            }
        //break-a da blocks
        for (BlockPos blockPos : getBlocksInTunnel()) {
            if (WorldHelper.INSTANCE.getBlockState(blockPos).getOutlineShape(Wrapper.INSTANCE.getWorld(), blockPos) != VoxelShapes.empty()) {
                Wrapper.INSTANCE.getClientPlayerInteractionManager().updateBlockBreakingProgress(blockPos, Direction.UP);
                if (swingProperty.value()) {
                Wrapper.INSTANCE.getPlayer().swingHand(Hand.MAIN_HAND);
                }
                return;
            }
        }
        //make sure floor is there
        Box tunnelBox = getTunnelBox();
        Box floorBox = new Box(tunnelBox.minX, tunnelBox.minY - 1, tunnelBox.minZ, tunnelBox.maxX, tunnelBox.minY - 1, tunnelBox.maxZ);
        ArrayList<BlockPos> floorBlocks = WorldHelper.INSTANCE.getBlocksInBox(floorBox);
        for (BlockPos floorBlock : floorBlocks) {
            if (WorldHelper.INSTANCE.getBlockState(floorBlock).getOutlineShape(Wrapper.INSTANCE.getWorld(), floorBlock) == VoxelShapes.empty()) {
                if (moveToBlocks()) {
                    PlayerHelper.INSTANCE.placeBlockInPos(floorBlock, Hand.MAIN_HAND, true);
                } else {
                    ChatHelper.INSTANCE.addClientMessage("Tunneller ran out of blocks. Disabling.");
                    this.setState(false);
                }
                return;
            }
        }
        //move forward until one of the above catches
        switch (direction) {
            case NORTH -> PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), -moveSpeed());
            case SOUTH -> PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), moveSpeed());
            case WEST -> PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), -moveSpeed());
            case EAST -> PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), moveSpeed());
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getPlayer() != null) {
            direction = Wrapper.INSTANCE.getPlayer().getHorizontalFacing();
            PlayerHelper.INSTANCE.centerPerfectlyOnBlock();
        }
        super.onEnable();
    }

    private boolean moveToBlocks() {
        if (getBlockFromHotbar() == -1) {
            if (getBlockFromInv() != -1) {
                InventoryHelper.INSTANCE.swapToHotbar(getBlockFromInv() < 9 ? getBlockFromInv() + 36 : getBlockFromInv(), 8);
            }
        }
        int hotBarslot = getBlockFromHotbar();
        if (hotBarslot != -1) {
            InventoryHelper.INSTANCE.setSlot(hotBarslot, true, true);
            return true;
        }
        return false;
    }

    public int getBlockFromInv() {
        for (int i = 0; i < 36; i++) {
            if (InventoryHelper.INSTANCE.getInventory().getStack(i) != null && InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() instanceof BlockItem blockItem)
                if (shouldUse(blockItem))
                    return i;
        }
        return -1;
    }

    public int getBlockFromHotbar() {
        for (int i = 0; i < 9; i++) {
            if (InventoryHelper.INSTANCE.getInventory().getStack(i) != null && InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() instanceof BlockItem blockItem)
                if (shouldUse(blockItem))
                    return i;
        }
        return -1;
    }

    public boolean shouldUse(BlockItem blockItem) {
        return blockItem.getBlock().getDefaultState().hasSolidTopSurface(Wrapper.INSTANCE.getWorld(), BlockPos.ORIGIN, Wrapper.INSTANCE.getPlayer()) && blockItem.getBlock().getDefaultState().onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS;
    }

    private ArrayList<BlockPos> getLiquidCheckSpots() {
        Box box = getTunnelBox().expand(1);
        ArrayList<BlockPos> blocks = WorldHelper.INSTANCE.getBlocksInBox(box);
        blocks.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().getPos(), Vec3d.ofCenter(value))));
        blocks.sort(Comparator.comparingInt(value -> -value.getY()));
        return blocks;
    }

    private ArrayList<BlockPos> getBlocksInTunnel() {
        Box box = getTunnelBox();
        ArrayList<BlockPos> blocks = WorldHelper.INSTANCE.getBlocksInBox(box);
        blocks.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().getPos(), Vec3d.ofCenter(value))));
        blocks.sort(Comparator.comparingInt(value -> -value.getY()));
        return blocks;
    }

    private Box getTunnelBox() {
        Box box = new Box(Wrapper.INSTANCE.getPlayer().getBlockX() - widthProperty.value() / 2.f, Wrapper.INSTANCE.getPlayer().getBlockY(), Wrapper.INSTANCE.getPlayer().getBlockZ() - widthProperty.value() / 2.f, Wrapper.INSTANCE.getPlayer().getBlockX() + widthProperty.value() / 2.f, Wrapper.INSTANCE.getPlayer().getBlockY() + heightProperty.value() - 1, Wrapper.INSTANCE.getPlayer().getBlockZ() + widthProperty.value() / 2.f);
        switch (direction) {
            case NORTH -> box = box.offset(0, 0, -widthProperty.value());
            case SOUTH -> box = box.offset(0, 0, widthProperty.value());
            case EAST -> box = box.offset(widthProperty.value(), 0, 0);
            case WEST -> box = box.offset(-widthProperty.value(), 0, 0);
        }
        return box;
    }

    public double moveSpeed() {
        if (Speed.INSTANCE.getState()) {
            return getSpeedModSpeed();
        }
        return PlayerHelper.INSTANCE.getBaseMoveSpeed();
    }

    public double getSpeedModSpeed() {
        return switch (Speed.INSTANCE.modeProperty.value()) {
            case VANILLA -> Speed.INSTANCE.vanillaSpeedProperty.value();
            case STRAFE -> Speed.INSTANCE.strafeSpeedProperty.value();
            default -> PlayerHelper.INSTANCE.getBaseMoveSpeed();
        };
    }

    private String getDirectionString() {
        return switch (direction) {
            case NORTH -> "-Z";
            case SOUTH -> "+Z";
            case WEST -> "-X";
            case EAST -> "+X";
            default -> "";
        };
    }
}
