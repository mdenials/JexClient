
package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.events.core.annotate.EventPointer;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockPos;
import java.util.concurrent.ThreadLocalRandom;
import java.util.*;

public class ArrowJuke extends Feature {

public final Property<Float> rangeProperty = new Property.PropertyBuilder<Float>(this.getClass())
        .name("Range")
        .value(5f)
        .min(1f)
        .max(6f)
        .inc(0.1f)
        .build();
public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
        .name("Delay (MS)")
        .value(50L)
	.min(0)
        .max(1000)
        .inc(10)
        .build();
	
private final StopWatch stopWatch = new StopWatch();


    public ArrowJuke() {
        super(Category.COMBAT, "dodges arrows-(beta feature)");
    }
	
    @EventPointer
    public final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
          int randomx = ThreadLocalRandom.current().nextInt(-1, 1 + 1);
          int randomz = ThreadLocalRandom.current().nextInt(-1, 1 + 1);
	    if (entity instanceof ArrowEntity arrowEntity) {
              if (arrowEntity.age < 75) {
            if (arrowEntity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= rangeProperty.value()) {
             event.setX(randomx);	
             event.setZ(randomz);
            }
      }
    }
}));
}
