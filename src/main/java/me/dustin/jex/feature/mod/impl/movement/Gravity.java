package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.util.math.Vec3d;

public class Gravity extends Feature {

    public Gravity() {
        super(Category.MOVEMENT);
    }

        @EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		boolean gravity = Wrapper.INSTANCE.getOptions().sneakKey.isPressed();
		Vec3d orig = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
		if (gravity) {
		Wrapper.INSTANCE.getLocalPlayer().setVelocity(orig.getX(), 0.0568000030517578, orig.getZ());	
		}
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
