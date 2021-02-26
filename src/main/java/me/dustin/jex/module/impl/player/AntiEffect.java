package me.dustin.jex.module.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.entity.effect.StatusEffects;

@ModClass(name = "AntiEffect", category = ModCategory.PLAYER, description = "Remove certain negative effects from yourself.")
public class AntiEffect extends Module {

    @Op(name = "Blindness")
    public boolean blindness = true;
    @Op(name = "Nausea")
    public boolean nausea = true;
    @Op(name = "Mining Fatigue")
    public boolean miningFatigue = false;
    @Op(name = "Levitation")
    public boolean levitation = false;
    @Op(name = "Slow Falling")
    public boolean slowFalling = true;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (blindness)
                Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.BLINDNESS);
            if (nausea)
                Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.NAUSEA);
            if (miningFatigue)
                Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.MINING_FATIGUE);
            if (levitation)
                Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.LEVITATION);
            if (slowFalling)
                Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.SLOW_FALLING);
        }
    }
}
