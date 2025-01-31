package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventSetupFog;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;

public class NoFog extends Feature {

    public NoFog() {
        super(Category.VISUAL);
    }

    @EventPointer
    private final EventListener<EventSetupFog> eventApplyFogEventListener = new EventListener<>(event -> {
        RenderSystem.setShaderFogStart(0);
        RenderSystem.setShaderFogEnd(10000);
    });

}
