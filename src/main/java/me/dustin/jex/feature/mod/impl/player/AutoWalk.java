package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.option.KeyBinding;
import me.dustin.jex.feature.mod.core.Feature;

public class AutoWalk extends Feature {

    public AutoWalk() {
        super(Category.PLAYER);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        KeyBinding.setKeyPressed(Wrapper.INSTANCE.getOptions().forwardKey.getDefaultKey(), true);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onDisable() {
        try {
            KeyBinding.setKeyPressed(Wrapper.INSTANCE.getOptions().forwardKey.getDefaultKey(), false);
        } catch (NullPointerException ignored) {
        }
        super.onDisable();
    }
}
