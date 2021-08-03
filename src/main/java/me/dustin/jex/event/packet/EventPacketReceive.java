package me.dustin.jex.event.packet;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.network.Packet;

public class EventPacketReceive extends Event {

    private Packet<?> packet;

    public EventPacketReceive(Packet<?> packet) {
        super();
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

}
