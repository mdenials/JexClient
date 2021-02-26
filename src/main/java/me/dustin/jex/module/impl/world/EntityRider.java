package me.dustin.jex.module.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventControlLlama;
import me.dustin.jex.event.misc.EventHorseIsSaddled;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IHorseBaseEntity;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.*;

@ModClass(name = "EntityRider", category = ModCategory.WORLD, description = "Change how ridable entities work.")
public class EntityRider extends Module {

    @Op(name = "Horse")
    public boolean horse = true;
    @OpChild(name = "Always Saddle", parent = "Horse")
    public boolean alwaysSaddleHorse = true;
    @OpChild(name = "Horse Instant Jump", parent = "Horse")
    public boolean horseInstantJump = true;
    @OpChild(name = "Horse Speed", min = 0.1f, max = 2, inc = 0.05f, parent = "Horse")
    public float horseSpeed = 1;
    @OpChild(name = "Horse Jump", min = 0.1f, max = 2, inc = 0.05f, parent = "Horse")
    public float horseJump = 1;

    @Op(name = "Llama")
    public boolean llama = true;
    @OpChild(name = "Always Saddle", parent = "Llama")
    public boolean alwaysSaddleLlama = true;
    @OpChild(name = "Llama Control", parent = "Llama")
    public boolean llamaControl = true;
    @OpChild(name = "Llama Instant Jump", parent = "Llama")
    public boolean llamaInstantJump = true;
    @OpChild(name = "Llama Speed", min = 0.1f, max = 2, inc = 0.05f, parent = "Llama")
    public float llamaSpeed = 1;
    @OpChild(name = "Llama Jump", min = 0.1f, max = 2, inc = 0.05f, parent = "Llama")
    public float llamaJump = 1;

    @EventListener(events = {EventPlayerPackets.class, EventHorseIsSaddled.class, EventControlLlama.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets) {
            if (Wrapper.INSTANCE.getLocalPlayer().getVehicle() == null)
                return;
            Entity vehicle = Wrapper.INSTANCE.getLocalPlayer().getVehicle();
            if (horse && isHorse(vehicle)) {
                HorseBaseEntity horseBaseEntity = (HorseBaseEntity) Wrapper.INSTANCE.getLocalPlayer().getVehicle();
                IHorseBaseEntity iHorseBaseEntity = (IHorseBaseEntity) horseBaseEntity;
                iHorseBaseEntity.setJumpStrength(horseJump);
                iHorseBaseEntity.setSpeed(horseSpeed);
                if (horseInstantJump)
                    iHorseBaseEntity.setJumpPower(Wrapper.INSTANCE.getOptions().keyJump.isPressed() ? 1 : 0);
            }
            if (llama && isLlama(vehicle)) {
                HorseBaseEntity horseBaseEntity = (HorseBaseEntity) Wrapper.INSTANCE.getLocalPlayer().getVehicle();
                IHorseBaseEntity iHorseBaseEntity = (IHorseBaseEntity) horseBaseEntity;
                iHorseBaseEntity.setJumpStrength(llamaJump);
                iHorseBaseEntity.setSpeed(llamaSpeed);
                if (llamaInstantJump)
                    iHorseBaseEntity.setJumpPower(Wrapper.INSTANCE.getOptions().keyJump.isPressed() ? 1 : 0);
            }
        }
        if (event instanceof EventHorseIsSaddled) {
            if (horse && alwaysSaddleHorse && isHorse(((EventHorseIsSaddled) event).getEntity())) {
                ((EventHorseIsSaddled) event).setSaddled(true);
                event.cancel();
            }
            if (llama && alwaysSaddleLlama && isLlama(((EventHorseIsSaddled) event).getEntity())) {
                ((EventHorseIsSaddled) event).setSaddled(true);
                event.cancel();
            }
        }
        if (event instanceof EventControlLlama) {
            ((EventControlLlama) event).setControl(llamaControl);
            event.cancel();
        }
    }

    private boolean isHorse(Entity entity) {
        return entity instanceof HorseEntity || entity instanceof DonkeyEntity || entity instanceof MuleEntity || entity instanceof SkeletonHorseEntity;
    }

    private boolean isLlama(Entity entity) {
        return entity instanceof LlamaEntity;
    }
}
