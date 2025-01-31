package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.Category;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.Freecam;

public class FallSpeed extends Feature {

    public final Property<Float> fallDistanceProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Fall Distance")
            .value(3f)
            .min(0)
            .max(10)
            .inc(0.5f)
            .build();
    public final Property<Float> speedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Speed")
            .value(0.5f)
            .min(0.1f)
            .max(15)
            .inc(0.1f)
            .build();
    public final Property<Boolean> hoveringProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hovering")
            .value(false)
            .build();
    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Hovering Delay")
            .value(250L)
	    .min(0L)
	    .max(500L)
	    .inc(10L)
            .parent(hoveringProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
	
private final StopWatch stopWatch = new StopWatch();

    public FallSpeed() {
        super(Category.MOVEMENT);
    }

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        if (Feature.getState(Fly.class) || Feature.getState(Freecam.class))
            return;
	  if (Wrapper.INSTANCE.getLocalPlayer().fallDistance > fallDistanceProperty.value() && !Wrapper.INSTANCE.getLocalPlayer().isOnGround()) {
if (hoveringProperty.value()) {
if (stopWatch.hasPassed(delayProperty.value())) {
    event.setY(-speedProperty.value());
	stopWatch.reset();
        }
	else 
	{
	event.setY(0.0F);
	}
	}
	else if (hoveringProperty.value() == false) {
	event.setY(-speedProperty.value());
	}
    }  
});        
}
