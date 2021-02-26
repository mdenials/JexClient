package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.events.core.enums.EventPriority;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.module.impl.player.Jesus;
import net.minecraft.util.math.Vec3d;

@ModClass(name = "CompatSwim", category = ModCategory.MOVEMENT, description = "Change swim speed to work on pre 1.13 servers with anticheats")
public class CompatSwim extends Module {

    @EventListener(events = {EventMove.class, EventPlayerPackets.class}, priority = EventPriority.HIGH)
    private void runMethod(Event event) {
        if (event instanceof EventMove) {
            EventMove eventMove = (EventMove) event;

            if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer())) {
                PlayerHelper.INSTANCE.setMoveSpeed(eventMove, PlayerHelper.INSTANCE.getWaterSpeed());
            }
        }
        if (event instanceof EventPlayerPackets) {
            if (((EventPlayerPackets) event).getMode() == EventPlayerPackets.Mode.PRE) {
                if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer()) && !Module.get(Jesus.class).getState()) {
                    Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
                    if (Wrapper.INSTANCE.getOptions().keyJump.isPressed()) {
                        double y = ClientMathHelper.INSTANCE.cap((float) orig.getY(), 0, Wrapper.INSTANCE.getLocalPlayer().horizontalCollision ? 0.07f : 0.011f);
                        Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), y, orig.getZ());
                    } else if (!Wrapper.INSTANCE.getLocalPlayer().isSneaking() && Wrapper.INSTANCE.getLocalPlayer().isSwimming()) {
                        Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), -0.025, orig.getZ());
                    }
                }
            }
        }
    }
}