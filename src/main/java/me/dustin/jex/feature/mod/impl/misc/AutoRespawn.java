package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

public class AutoRespawn extends Feature {

    public AutoRespawn() {
        super(Category.MISC);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!Wrapper.INSTANCE.getLocalPlayer().isAlive())
            Wrapper.INSTANCE.getLocalPlayer().requestRespawn();
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
