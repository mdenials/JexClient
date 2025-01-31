package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventGetPose;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.Jesus;

public class CompatSwim extends Feature {

    public CompatSwim() {
        super(Category.MOVEMENT);
    }

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer())) {
            PlayerHelper.INSTANCE.setMoveSpeed(event, PlayerHelper.INSTANCE.getWaterSpeed());
        }
    }, Priority.SECOND);

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer()) && !Feature.get(Jesus.class).getState()) {
            Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
            if (Wrapper.INSTANCE.getOptions().jumpKey.isPressed()) {
                double y = ClientMathHelper.INSTANCE.cap((float) orig.getY(), 0, Wrapper.INSTANCE.getLocalPlayer().horizontalCollision ? 0.07f : 0.011f);
                Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), y, orig.getZ());
            } else if (!Wrapper.INSTANCE.getLocalPlayer().isSneaking() && Wrapper.INSTANCE.getLocalPlayer().isSwimming()) {
                Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), -0.025, orig.getZ());
            }
        }
    }, Priority.SECOND, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventGetPose> eventGetPoseEventListener = new EventListener<>(event -> {
        if (event.getPose() == EntityPose.SWIMMING) {
            event.setPose(EntityPose.STANDING);
            event.cancel();
        }
    });
}
