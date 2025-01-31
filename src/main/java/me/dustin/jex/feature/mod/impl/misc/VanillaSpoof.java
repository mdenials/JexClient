package me.dustin.jex.feature.mod.impl.misc;

import io.netty.buffer.Unpooled;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.DirectClientPacketFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import java.nio.charset.StandardCharsets;

public class VanillaSpoof extends Feature {

    public VanillaSpoof() {
        super(Category.MISC);
    }

    @EventPointer
    private final EventListener<EventPacketSent.EventPacketSentDirect> eventPacketSentEventListener = new EventListener<>(event -> {
        CustomPayloadC2SPacket packet = (CustomPayloadC2SPacket) event.getPacket();
        if (packet.getChannel() == CustomPayloadC2SPacket.BRAND) {
            CustomPayloadC2SPacket newPacket = new CustomPayloadC2SPacket(CustomPayloadC2SPacket.BRAND, new PacketByteBuf(Unpooled.buffer()).writeString("vanilla"));
            event.setPacket(newPacket);
        } else if (packet.getData().toString(StandardCharsets.UTF_8).toLowerCase().contains("fabric")) {
            event.cancel();
        }
    }, new DirectClientPacketFilter(EventPacketSent.Mode.PRE, CustomPayloadC2SPacket.class));
}
