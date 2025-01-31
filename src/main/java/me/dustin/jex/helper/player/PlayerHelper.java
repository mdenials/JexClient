package me.dustin.jex.helper.player;

import com.google.common.collect.Maps;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.Sprint;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public enum PlayerHelper {
    INSTANCE;

    private final ArrayList<UUID> requestedUUIDs = new ArrayList<>();
    private final ArrayList<String> requestedNames = new ArrayList<>();
    private final HashMap<UUID, String> nameMap = Maps.newHashMap();
    private final HashMap<String, UUID> uuidMap = Maps.newHashMap();
    private float yaw;
    private float pitch;

    public String getName(UUID uuid) {
        if (!requestedUUIDs.contains(uuid)) {
            new Thread(() -> {
                String name = MCAPIHelper.INSTANCE.getNameFromUUID(uuid);
                nameMap.put(uuid, name);
            }).start();
            requestedUUIDs.add(uuid);
        }
        return nameMap.get(uuid);
    }

    public UUID getUUID(String name) {
        if (!requestedNames.contains(name.toLowerCase())) {
            new Thread(() -> {
                UUID uuid = MCAPIHelper.INSTANCE.getUUIDFromName(name);
                uuidMap.put(name.toLowerCase(), uuid);
            }).start();
            requestedNames.add(name.toLowerCase());
        }
        return uuidMap.get(name.toLowerCase());
    }

    public void block(boolean ignoreNewCombat) {
        if (ignoreNewCombat) {
            if (Wrapper.INSTANCE.getLocalPlayer().getMainHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() instanceof SwordItem) {
                Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND);
                Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Hand.OFF_HAND);
            }
        } else {
            if (Wrapper.INSTANCE.getLocalPlayer().getOffHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() instanceof ShieldItem) {
                Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Hand.OFF_HAND);
            }
        }
    }

    public void unblock() {
        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
    }

    public float getYaw() {
        return Wrapper.INSTANCE.getLocalPlayer().getYaw(Wrapper.INSTANCE.getMinecraft().getTickDelta());
    }

    public float getPitch() {
        return Wrapper.INSTANCE.getLocalPlayer().getPitch(Wrapper.INSTANCE.getMinecraft().getTickDelta());
    }

    public void setYaw(float yaw) {
        Wrapper.INSTANCE.getLocalPlayer().setYaw(yaw);
    }

    public void setPitch(float pitch) {
        Wrapper.INSTANCE.getLocalPlayer().setPitch(pitch);
    }

    public void setRotation(RotationVector rotation) {
        setYaw(rotation.getYaw());
        setPitch(rotation.getPitch());
    }

    public void addYaw(float add) {
        setYaw(getYaw() + add);
    }

    public void addPitch(float add) {
        setPitch(getPitch() + add);
    }

    public void setVelocityX(double x) {
        Vec3d velo = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
        Wrapper.INSTANCE.getLocalPlayer().setVelocity(x, velo.y, velo.z);
    }

    public void setVelocityY(double y) {
        Vec3d velo = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
        Wrapper.INSTANCE.getLocalPlayer().setVelocity(velo.x, y, velo.z);
    }

    public void setVelocityZ(double z) {
        Vec3d velo = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
        Wrapper.INSTANCE.getLocalPlayer().setVelocity(velo.x, velo.y, z);
    }

    public void setVelocityX(Entity entity, double x) {
        Vec3d velo = entity.getVelocity();
        entity.setVelocity(x, velo.y, velo.z);
    }

    public void setVelocityY(Entity entity, double y) {
        Vec3d velo = entity.getVelocity();
        entity.setVelocity(velo.x, y, velo.z);
    }

    public void setVelocityZ(Entity entity, double z) {
        Vec3d velo = entity.getVelocity();
        entity.setVelocity(velo.x, velo.y, z);
    }

    public void placeBlockInPos(BlockPos blockPos, Hand hand, boolean illegallPlace) {
        BlockPos north = blockPos.north();
        BlockPos east = blockPos.east();
        BlockPos south = blockPos.south();
        BlockPos west = blockPos.west();
        BlockPos down = blockPos.down();
        BlockPos up = blockPos.up();

        BlockPos placePos = null;
        Direction placeDir = null;

        if (!WorldHelper.INSTANCE.getBlockState(north).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(north).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = north;
            placeDir = Direction.SOUTH;
        } else if (!WorldHelper.INSTANCE.getBlockState(south).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(south).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = south;
            placeDir = Direction.NORTH;
        } else if (!WorldHelper.INSTANCE.getBlockState(east).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(east).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = east;
            placeDir = Direction.WEST;
        } else if (!WorldHelper.INSTANCE.getBlockState(west).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(west).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = west;
            placeDir = Direction.EAST;
        } else if (!WorldHelper.INSTANCE.getBlockState(up).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(up).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = up;
            placeDir = Direction.DOWN;
        } else if (!WorldHelper.INSTANCE.getBlockState(down).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(down).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = down;
            placeDir = Direction.UP;
        }
        if (placePos == null) {
            if (illegallPlace) {
                Wrapper.INSTANCE.getClientPlayerInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), hand, new BlockHitResult(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Direction.UP, blockPos, false));
                swing(hand);
            }
        } else {
            Vec3d placeVec = WorldHelper.INSTANCE.sideOfBlock(placePos, placeDir);
            BlockHitResult blockHitResult = new BlockHitResult(placeVec, placeDir, placePos, false);
            Wrapper.INSTANCE.getClientPlayerInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), hand, blockHitResult);
            swing(hand);
        }
    }

    public boolean canPlaceHere(BlockPos blockPos) {
        BlockPos north = blockPos.north();
        BlockPos east = blockPos.east();
        BlockPos south = blockPos.south();
        BlockPos west = blockPos.west();
        BlockPos down = blockPos.down();
        BlockPos up = blockPos.up();

        BlockPos placePos = null;

        if (!WorldHelper.INSTANCE.getBlockState(north).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(north).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = north;
        } else if (!WorldHelper.INSTANCE.getBlockState(south).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(south).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = south;
        } else if (!WorldHelper.INSTANCE.getBlockState(east).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(east).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = east;
        } else if (!WorldHelper.INSTANCE.getBlockState(west).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(west).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = west;
        } else if (!WorldHelper.INSTANCE.getBlockState(up).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(up).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = up;
        } else if (!WorldHelper.INSTANCE.getBlockState(down).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(down).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS) {
            placePos = down;
        }
        return placePos != null;
    }

    public Vec3d getPlacingLookPos(BlockPos blockPos) {
        BlockPos north = blockPos.north();
        BlockPos east = blockPos.east();
        BlockPos south = blockPos.south();
        BlockPos west = blockPos.west();
        BlockPos down = blockPos.down();
        BlockPos up = blockPos.up();

        if (!WorldHelper.INSTANCE.getBlockState(north).getMaterial().isReplaceable()) {
            Direction direction = Direction.SOUTH;
            return ClientMathHelper.INSTANCE.getVec(north).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        } else if (!WorldHelper.INSTANCE.getBlockState(south).getMaterial().isReplaceable()) {
            Direction direction = Direction.NORTH;
            return ClientMathHelper.INSTANCE.getVec(south).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        } else if (!WorldHelper.INSTANCE.getBlockState(east).getMaterial().isReplaceable()) {
            Direction direction = Direction.WEST;
            return ClientMathHelper.INSTANCE.getVec(east).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        } else if (!WorldHelper.INSTANCE.getBlockState(west).getMaterial().isReplaceable()) {
            Direction direction = Direction.EAST;
            return ClientMathHelper.INSTANCE.getVec(west).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        } else if (!WorldHelper.INSTANCE.getBlockState(up).getMaterial().isReplaceable()) {
            Direction direction = Direction.DOWN;
            return ClientMathHelper.INSTANCE.getVec(up).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        } else if (!WorldHelper.INSTANCE.getBlockState(down).getMaterial().isReplaceable()) {
            Direction direction = Direction.UP;
            return ClientMathHelper.INSTANCE.getVec(down).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        }
        return ClientMathHelper.INSTANCE.getVec(blockPos);
    }

    public RotationVector rotateToEntity(Entity entityIn) {
        double xDif = entityIn.getX() - Wrapper.INSTANCE.getLocalPlayer().getX();
        double zDif = entityIn.getZ() - Wrapper.INSTANCE.getLocalPlayer().getZ();
        double yDif;

        if (entityIn instanceof LivingEntity livingEntity) {
            yDif = livingEntity.getY() + (double) livingEntity.getEyeHeight(livingEntity.getPose()) - (Wrapper.INSTANCE.getLocalPlayer().getY() + (double) Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()));
        } else {
            yDif = (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D - (Wrapper.INSTANCE.getLocalPlayer().getY() + (double) (Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()) * Math.random()));
        }

        double var141 = MathHelper.sqrt((float)(xDif * xDif + zDif * zDif));
        float var12 = (float) (Math.atan2(zDif, xDif) * 180.0D / Math.PI) - 90.0F;
        float var13 = (float) (-(Math.atan2(yDif, var141) * 180.0D / Math.PI));
        return new RotationVector(var12, var13);
    }

    public RotationVector rotateToCenter(Entity entityIn) {
        double xDif = entityIn.getX() - Wrapper.INSTANCE.getLocalPlayer().getX();
        double zDif = entityIn.getZ() - Wrapper.INSTANCE.getLocalPlayer().getZ();
        double yDif = (entityIn.getY() + entityIn.getHeight() / 2.f) - (Wrapper.INSTANCE.getLocalPlayer().getY() + (double) Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()));

        double var141 = MathHelper.sqrt((float)(xDif * xDif + zDif * zDif));
        float var12 = (float) (Math.atan2(zDif, xDif) * 180.0D / Math.PI) - 90.0F;
        float var13 = (float) (-(Math.atan2(yDif, var141) * 180.0D / Math.PI));
        return new RotationVector(var12, var13);
    }

    public RotationVector rotateToVec(Entity entityIn, Vec3d vec3d) {
        double xDif = vec3d.x - entityIn.getX();
        double zDif = vec3d.z - entityIn.getZ();
        double yDif = vec3d.y - (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D;

        double var141 = MathHelper.sqrt((float)(xDif * xDif + zDif * zDif));
        float yaw = (float) (Math.atan2(zDif, xDif) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(yDif, var141) * 180.0D / Math.PI));
        return new RotationVector(yaw, pitch);
    }

    public RotationVector rotateFromVec(Vec3d vec3d, Entity entityIn) {
        double xDif = entityIn.getX() - vec3d.x;
        double zDif = entityIn.getZ() - vec3d.z;
        double yDif = (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D - vec3d.y;

        double var141 = MathHelper.sqrt((float)(xDif * xDif + zDif * zDif));
        float yaw = (float) (Math.atan2(zDif, xDif) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(yDif, var141) * 180.0D / Math.PI));
        return new RotationVector(yaw, pitch);
    }

    public RotationVector randomRotateTo(Entity ent2, float sideOffset, float heightOffset) {
        Random random = new Random();
        sideOffset = ent2.getWidth() * sideOffset;
        heightOffset = ent2.getHeight() * heightOffset;
        double xDif = (ent2.getX() - sideOffset + (random.nextFloat() * (sideOffset * 2))) - Wrapper.INSTANCE.getLocalPlayer().getX();
        double zDif = (ent2.getZ() - sideOffset + (random.nextFloat() * (sideOffset * 2))) - Wrapper.INSTANCE.getLocalPlayer().getZ();
        double yDif = (ent2.getY() + (double) (ent2.getHeight() / 2) - heightOffset + (random.nextFloat() * (heightOffset * 2))) - (Wrapper.INSTANCE.getLocalPlayer().getY() + (double) Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()));

        double var141 = MathHelper.sqrt((float)(xDif * xDif + zDif * zDif));
        float yaw = (float) (Math.atan2(zDif, xDif) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(yDif, var141) * 180.0D / Math.PI));
        return new RotationVector(yaw, pitch);
    }

    public Entity getCrosshairEntity(float tickDelta, RotationVector rots, float reach) {
        Entity entity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
        if (entity != null) {
            if (Wrapper.INSTANCE.getMinecraft().world != null) {
                Vec3d vec3d = entity.getCameraPosVec(tickDelta);
                Vec3d vec3d2 = getRotationVector(rots.getPitch(), rots.getYaw());
                Vec3d vec3d3 = vec3d.add(vec3d2.x * reach, vec3d2.y * reach, vec3d2.z * reach);

                Box box = entity.getBoundingBox().stretch(vec3d2.multiply(reach)).expand(1.0D, 1.0D, 1.0D);
                EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, (entityx) -> !entityx.isSpectator() && entityx.canHit(), reach);
                if (entityHitResult != null) {
                    Entity entity2 = entityHitResult.getEntity();
                    if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrameEntity) {
                        return entity2;
                    }
                }
            }
        }
        return null;
    }

    public Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((double) (i * j), (double) (-k), (double) (h * j));
    }

    public int getDistanceFromMouse(Entity entity) {
        RotationVector neededRotations = rotateToCenter(entity);
        RotationVector currentRotations = new RotationVector(getYaw(), getPitch());
        neededRotations.normalize();
        currentRotations.normalize();
        float neededYaw = currentRotations.getYaw() - neededRotations.getYaw();
        float neededPitch = currentRotations.getPitch() - neededRotations.getPitch();
        float distanceFromMouse = MathHelper.sqrt(neededYaw * neededYaw + neededPitch * neededPitch);
        return (int) distanceFromMouse;
    }

    public int getDistanceFromMouse(Vec3d vec3d) {
        RotationVector neededRotations = rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), vec3d);
        RotationVector currentRotations = new RotationVector(getYaw(), getPitch());
        neededRotations.normalize();
        currentRotations.normalize();
        float neededYaw = currentRotations.getYaw() - neededRotations.getYaw();
        float neededPitch = currentRotations.getPitch() - neededRotations.getPitch();
        float distanceFromMouse = MathHelper.sqrt(neededYaw * neededYaw + neededPitch * neededPitch);
        return (int) distanceFromMouse;
    }

    public double getWaterSpeed() {
        double speed = 1.96 / 20;
        int dsLevel = InventoryHelper.INSTANCE.getDepthStriderLevel();
        switch (dsLevel) {
            case 1 -> speed = 3.21 / 20;
            case 2 -> speed = 3.89 / 20;
            case 3 -> speed = 4.32 / 20;
        }
        return speed;
    }

    public double getWaterSpeed(int depthStriderLevel, boolean accountSprint) {
        double speed = switch (depthStriderLevel) {
            case 1 -> 3.21 / 20;
            case 2 -> 3.89 / 20;
            case 3 -> 4.32 / 20;
            default -> 1.96 / 20;
        };
        if ((Wrapper.INSTANCE.getLocalPlayer().isSprinting() || (Feature.getState(Sprint.class) && isMoving())) && accountSprint)
            speed += (speed * 0.3);
        return speed;
    }
    
    public double getSoulSandSpeed() {
        double speed = 2.50 / 20;
        int ssLevel = InventoryHelper.INSTANCE.getSoulSpeedLevel();
        switch (ssLevel) {
            case 1 -> speed = 6.06 / 20;
            case 2 -> speed = 6.52 / 20;
            case 3 -> speed = 6.97 / 20;
        }
        return speed;
    }

    public double getSoulSandSpeed(int soulSpeedLevel, boolean accountSprint) {
        double speed = switch (soulSpeedLevel) {
            case 1 -> 6.06 / 20;
            case 2 -> 6.52 / 20;
            case 3 -> 6.97 / 20;
            default -> 2.50 / 20;
        };
        if ((Wrapper.INSTANCE.getLocalPlayer().isSprinting() || (Feature.getState(Sprint.class) && isMoving())) && accountSprint)
            speed += (speed * 0.3);
        return speed;
    }

    public boolean isMoving() {
        if (Wrapper.INSTANCE.getLocalPlayer() == null)
            return false;
        return Wrapper.INSTANCE.getLocalPlayer().input.movementForward != 0 || Wrapper.INSTANCE.getLocalPlayer().input.movementSideways != 0;
    }

    public void setMoveSpeed(EventMove event, final double speed) {
        double forward = Wrapper.INSTANCE.getLocalPlayer().input.movementForward;
        double strafe = Wrapper.INSTANCE.getLocalPlayer().input.movementSideways;
        float yaw = this.yaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += ((forward > 0.0) ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += ((forward > 0.0) ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            event.setX(forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f)));
            event.setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f)));
        }
    }

    public double getBaseMoveSpeed() {
        double baseSpeed = 0.2873;
        if (Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.SPEED)) {
            final int amplifier = Wrapper.INSTANCE.getLocalPlayer().getStatusEffect(StatusEffects.SPEED).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

    public void centerOnBlock() {
        double fracX = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getX());
        double fracZ = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getZ());
        if (fracX < 0.3) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.3;
            Wrapper.INSTANCE.getLocalPlayer().setPos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        } else if (fracX > 0.7) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.7;
            Wrapper.INSTANCE.getLocalPlayer().setPos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        }

        if (fracZ < 0.3) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.3;
            Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        } else if (fracZ > 0.7) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.7;
            Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        }
    }

    public void centerPerfectlyOnBlock() {
        double fracX = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getX());
        double fracZ = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getZ());
        if (fracX < 0.5) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        } else if (fracX > 0.5) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        }

        if (fracZ < 0.5) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        } else if (fracZ > 0.5) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        }
    }

    public boolean isOnEdgeOfBlock() {
        double fracX = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getX());
        double fracZ = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getZ());
        return fracX < 0.3 || fracX > 0.7 || fracZ < 0.3 || fracZ > 0.7;
    }

    public Vec3d getPlayerVec() {
        return new Vec3d(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
    }

    public void rightClickBlock(BlockPos blockPos, Hand hand, boolean insideBlock) {
        BlockHitResult blockHitResult = new BlockHitResult(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Direction.DOWN, blockPos, insideBlock);
        Wrapper.INSTANCE.getClientPlayerInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), hand, blockHitResult);
    }

    public ItemStack mainHandStack() {
        return Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
    }

    public ItemStack offHandStack() {
        return Wrapper.INSTANCE.getLocalPlayer().getOffHandStack();
    }

    public void useItem(Hand hand) {
        Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), hand);
    }

    public void stopUsingItem() {
        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
    }

    public void swing(Hand hand) {
        if (Wrapper.INSTANCE.getPlayer() == Freecam.playerEntity) {
            NetworkHelper.INSTANCE.sendPacket(new HandSwingC2SPacket(hand));
            Wrapper.INSTANCE.getPlayer().swingHand(hand);
        } else
            Wrapper.INSTANCE.getLocalPlayer().swingHand(hand);
    }

    public float getYawWithBaritone() {
        return this.yaw;
    }

    public float getPitchWithBaritone() {
        return this.pitch;
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        this.yaw = event.getYaw();
        this.pitch = event.getPitch();

        if (!BaritoneHelper.INSTANCE.baritoneExists())
            return;
        if (!BaritoneHelper.INSTANCE.isBaritoneRunning())
            return;
        Wrapper.INSTANCE.getLocalPlayer().headYaw = yaw;
        Wrapper.INSTANCE.getLocalPlayer().bodyYaw = yaw;
    }, Priority.FIRST, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
