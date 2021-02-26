package me.dustin.jex.module.impl.render.esp;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.render.EventOutlineColor;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.ModuleExtension;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.module.impl.render.esp.impl.BoxESP;
import me.dustin.jex.module.impl.render.esp.impl.OutlineBox;
import me.dustin.jex.module.impl.render.esp.impl.ShaderESP;
import me.dustin.jex.module.impl.render.esp.impl.TwoDeeESP;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;


@ModClass(name = "ESP", category = ModCategory.VISUAL, description = "Mark entities/players through walls")
public class ESP extends Module {

    @Op(name = "Mode", all = {"Shader", "2D", "Box Outline", "Box"})
    public String mode = "Shader";

    @OpChild(name = "Line Width", min = 1, max = 10, inc = 0.1f, parent = "Mode", dependency = "Box Outline")
    public float lineWidth = 2;

    @Op(name = "Player")
    public boolean player = true;
    @Op(name = "Hostile")
    public boolean hostile = true;
    @Op(name = "Passive")
    public boolean passive = true;

    @Op(name = "Item")
    public boolean item = false;

    @OpChild(name = "Player Color", isColor = true, parent = "Player")
    public int playerColor = 0xffff0000;
    @OpChild(name = "Friend Color", isColor = true, parent = "Player Color")
    public int friendColor = 0xff0080ff;
    @OpChild(name = "Hostile Color", isColor = true, parent = "Hostile")
    public int hostileColor = 0xffff8000;
    @OpChild(name = "Passive Color", isColor = true, parent = "Passive")
    public int passiveColor = 0xff00ff00;
    @OpChild(name = "Pets Color", isColor = true, parent = "Passive Color")
    public int petColor = 0xff0000ff;
    @OpChild(name = "Item Color", isColor = true, parent = "Item")
    public int itemColor = 0xffffffff;
    String lastMode;

    public static boolean spoofOutline;

    public ESP() {
        ShaderESP shaderESP = new ShaderESP();
        BoxESP boxESP = new BoxESP();
        OutlineBox outlineBox = new OutlineBox();
        TwoDeeESP twoDeeESP = new TwoDeeESP();
    }

    @EventListener(events = {EventRender3D.class, EventRender2D.class, EventOutlineColor.class, EventJoinWorld.class}, priority = 1)
    public void run(Event event) {
        if (lastMode != null && !mode.equalsIgnoreCase(lastMode)) {
            ModuleExtension.get(lastMode, this).disable();
            ModuleExtension.get(mode, this).enable();
        }
        ModuleExtension.get(mode, this).pass(event);
        this.setSuffix(mode);
        lastMode = mode;
    }

    @Override
    public void onEnable() {
        ModuleExtension.get(mode, this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ModuleExtension.get(mode, this).disable();
        super.onDisable();
    }

    public boolean isValid(Entity entity) {
        if (entity == null)
            return false;
        if (entity instanceof ItemEntity)
            return item;
        if (!(entity instanceof LivingEntity))
            return false;
        LivingEntity livingEntity = (LivingEntity) entity;
        if (livingEntity == Wrapper.INSTANCE.getLocalPlayer())
            return false;
        if (livingEntity instanceof PlayerEntity && EntityHelper.INSTANCE.isNPC((PlayerEntity) livingEntity))
            return false;
        if (livingEntity instanceof PlayerEntity)
            return player;
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostile;
        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return passive;
        return false;
    }



    public int getColor(Entity entity) {
        if (entity instanceof ItemEntity)
            return itemColor;
        if (Friend.isFriend(entity.getName().getString()))
            return friendColor;
        if (entity instanceof PlayerEntity)
            return playerColor;

        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            if (EntityHelper.INSTANCE.doesPlayerOwn((LivingEntity) entity))
                return petColor;
            else
                return passiveColor;
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostileColor;
        return -1;
    }



}
