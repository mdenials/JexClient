package me.dustin.jex.feature.impl.combat;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.event.player.EventPlayerVelocity;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.option.annotate.Op;

@Feature.Manifest(name = "Velocity", category = Feature.Category.COMBAT, description = "Remove all knockback from the player.")
public class Velocity extends Feature {

    @Op(name = "Percent", max = 300)
    public int percent = 0;

    @EventListener(events = {EventPacketReceive.class, EventExplosionVelocity.class, EventPlayerVelocity.class})
    public void run(Event event) {
        float perc = percent / 100.0f;
        if (event instanceof EventPlayerVelocity eventPlayerVelocity) {
            if (percent == 0)
                event.cancel();
            else {
                eventPlayerVelocity.setVelocityX((int)(eventPlayerVelocity.getVelocityX() * perc));
                eventPlayerVelocity.setVelocityY((int)(eventPlayerVelocity.getVelocityY() * perc));
                eventPlayerVelocity.setVelocityZ((int)(eventPlayerVelocity.getVelocityZ() * perc));
            }
        }
        if (event instanceof EventExplosionVelocity) {
            if (percent == 0)
                event.cancel();
            else {
                EventExplosionVelocity eventExplosionVelocity = (EventExplosionVelocity)event;
                eventExplosionVelocity.setMultX(perc);
                eventExplosionVelocity.setMultY(perc);
                eventExplosionVelocity.setMultZ(perc);
            }
        }
    }


}
