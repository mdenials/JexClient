package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.player.EventStopUsingItem;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import me.dustin.events.core.annotate.EventPointer;

public class BowBomb extends Feature {
    
    public final Property<Integer> amountProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Amount")
            .value(10)
            .min(2)
            .max(100)
            .inc(1)
            .build();
    public final Property<Integer> exponentialProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Exponential")
            .value(2)
            .min(1)
            .max(14)
            .inc(1)
            .build();
    

    public BowBomb() {
        super(Category.COMBAT);
    }
    @EventPointer
    private final EventListener<EventStopUsingItem> eventStopUsingItem = new EventListener<>(event -> {
        ClientPlayerEntity player = Wrapper.INSTANCE.getLocalPlayer();
        if (player.getMainHandStack().getItem().equals(Items.BOW)) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            long cycles = (long) Math.pow(amountProperty.value(), exponentialProperty.value());
            for (int i = 0; i < cycles; ++i) {
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX(), player.getY() - 1.0E-9, player.getZ(), true));
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX(), player.getY() + 1.0E-9, player.getZ(), false));
            }
        }
    });
}
