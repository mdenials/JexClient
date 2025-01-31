package me.dustin.jex.feature.mod.impl.movement.fly;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventGetPose;
import me.dustin.jex.event.player.EventIsPlayerTouchingWater;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.fly.impl.CreativeFly;
import me.dustin.jex.feature.mod.impl.movement.fly.impl.NormalFly;
import me.dustin.jex.feature.mod.impl.movement.fly.impl.ThreeDFly;
import me.dustin.jex.feature.mod.impl.movement.fly.impl.TightFly;
import me.dustin.jex.feature.mod.impl.movement.fly.impl.JetpackFly;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

public class Fly extends Feature {

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.NORMAL)
            .build();
    public final Property<Integer> hspeedProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Horizontal Speed (km/h)")
            .value(72)
            .min(15)
            .max(100)
            .inc(1)
            .build();
    public final Property<Integer> vspeedProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Vertical Speed (km/h)")
            .value(15)
            .min(15)
            .max(100)
            .inc(1)
            .build();
    public final Property<Integer> multipleProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Multiplier")
            .value(1)
            .min(1)
            .max(100)
            .inc(1)
            .build();
    public final Property<Boolean> walkAnimationProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Walk Animation")
            .value(true)
            .build();
    public final Property<Boolean> flyCheckBypassProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Fly Check Bypass")
            .value(true)
            .build();
    public final Property<Integer> tProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Timeout (Tick)")
            .value(1)
            .min(0)
            .max(80)
            .inc(1)
            .parent(flyCheckBypassProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> distanceProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Fall Distance")
            .value(0.5f)
            .min(0.1f)
            .max(10f)
            .inc(0.1f)
            .parent(flyCheckBypassProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> glideProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Glide")
            .value(false)
            .build();
    public final Property<Float> glideSpeedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Glide Speed")
            .value(0.01f)
            .max(2)
            .inc(0.01f)
            .parent(glideProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    private float strideDistance;
    private Mode lastMode;

    public Fly() {
        super(Category.MOVEMENT,  "", GLFW.GLFW_KEY_F);
        new NormalFly();
        new TightFly();
        new ThreeDFly();
        new CreativeFly();
        new JetpackFly();
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (walkAnimationProperty.value()) {
            float g;
            if (!Wrapper.INSTANCE.getLocalPlayer().isDead() && !Wrapper.INSTANCE.getLocalPlayer().isSwimming()) {
                g = Math.min(0.1F, (float) Wrapper.INSTANCE.getLocalPlayer().getVelocity().horizontalLength());
            } else {
                g = 0.0F;
            }

            float lastStrideDist = strideDistance;
            strideDistance += (g - strideDistance) * 0.4F;
            Wrapper.INSTANCE.getLocalPlayer().strideDistance = strideDistance;
            Wrapper.INSTANCE.getLocalPlayer().prevStrideDistance = lastStrideDist;
        }
        sendEvent(event);
        this.setSuffix(modeProperty.value());
    }, Priority.LAST, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        sendEvent(event);
    });

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        float tick = tProperty.value() * 0.05f;
        if (!flyCheckBypassProperty.value() || Feature.getState(Freecam.class))
            return;
        PlayerMoveC2SPacket playerMoveC2SPacket = (PlayerMoveC2SPacket) event.getPacket();
        if (Wrapper.INSTANCE.getLocalPlayer().age >= tick) {
            if (EntityHelper.INSTANCE.distanceFromGround(Wrapper.INSTANCE.getLocalPlayer()) > 2) {
                PlayerMoveC2SPacket modified = new PlayerMoveC2SPacket.Full(playerMoveC2SPacket.getX(Wrapper.INSTANCE.getLocalPlayer().getX()), playerMoveC2SPacket.getY(Wrapper.INSTANCE.getLocalPlayer().getY()) - distanceProperty.value(), playerMoveC2SPacket.getZ(Wrapper.INSTANCE.getLocalPlayer().getZ()), playerMoveC2SPacket.getYaw(PlayerHelper.INSTANCE.getYaw()), playerMoveC2SPacket.getPitch(PlayerHelper.INSTANCE.getPitch()), true);
                event.setPacket(modified);
            }
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, PlayerMoveC2SPacket.class));

    @EventPointer
    private final EventListener<EventIsPlayerTouchingWater> eventIsPlayerTouchingWaterEventListener = new EventListener<>(event -> {
        event.setTouchingWater(false);
        event.cancel();
    });

    @EventPointer
    private final EventListener<EventGetPose> eventGetPoseEventListener = new EventListener<>(event -> {
        if (event.getPose() == EntityPose.SWIMMING) {
            event.setPose(EntityPose.STANDING);
            event.cancel();
        }
    });

    private void sendEvent(Event event) {
        if (modeProperty.value() != lastMode && lastMode != null) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(modeProperty.value(), this).enable();
        }
        FeatureExtension.get(modeProperty.value(), this).pass(event);
        lastMode = modeProperty.value();
    }

    @Override
    public void onEnable() {
        FeatureExtension.get(modeProperty.value(), this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FeatureExtension.get(modeProperty.value(), this).disable();
        super.onDisable();
    }

    public enum Mode {
        NORMAL, JETPACK, CREATIVE, TIGHT, THREE_D
    }
}
