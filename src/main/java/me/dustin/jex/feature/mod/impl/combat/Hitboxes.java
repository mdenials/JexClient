package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventEntityHitbox;
import me.dustin.jex.helper.entity.EntityHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.PiglinEntity;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.util.math.Box;

public class Hitboxes extends Feature {

    public final Property<Float> expandXProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("ExpandX")
            .value(0.1f)
            .min(0f)
            .max(2f)
            .inc(0.02f)
            .build();
     public final Property<Float> expandYProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("ExpandY")
            .value(0.1f)
            .min(0f)
            .max(2f)
            .inc(0.02f)
            .build();
     public final Property<Float> expandZProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("ExpandZ")
            .value(0.1f)
            .min(0f)
            .max(2f)
            .inc(0.02f)
            .build();
    public final Property<Boolean> playerProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Player")
            .value(true)
            .build();
    public final Property<Boolean> friendProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Friends")
            .value(true)
            .parent(playerProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> neutralProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Neutral")
            .value(false)
            .build();
    public final Property<Boolean> bossProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Boss")
            .value(true)
            .build();
    public final Property<Boolean> hostileProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hostile")
            .value(true)
            .build();
    public final Property<Boolean> passiveProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Passive")
            .value(true)
            .build();
    public final Property<Boolean> petProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Pet")
            .value(false)
	    .parent(passiveProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> specificFilterProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Specific Filter")
            .value(true)
            .build();
    public final Property<Boolean> ironGolemProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Iron Golem")
            .value(true)
            .parent(specificFilterProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> piglinProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Piglin")
            .value(true)
            .parent(specificFilterProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> zombiePiglinProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Zombie Piglin")
            .value(false)
            .parent(specificFilterProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> botCheckProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Bot")
            .value(true)
            .build();
    public final Property<Boolean> teamCheckProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Team Check")
            .value(true)
	    .parent(botCheckProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> checkArmorProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Check Armor")
            .value(true)
            .parent(teamCheckProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> nolivingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("NoLiving")
            .value(false)
            .parent(specificFilterProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
	
    public Hitboxes() {
        super(Category.COMBAT);
    }

    @EventPointer
    private final EventListener<EventEntityHitbox> eventEntityHitboxEventListener = new EventListener<>(event -> {
	    Entity entity = event.getEntity();
	    if (isEnabled(entity)) {
        if (event.getEntity() == null || Wrapper.INSTANCE.getLocalPlayer() == null || event.getEntity().getId() == Wrapper.INSTANCE.getLocalPlayer().getId())
            return;
            event.setBox(event.getBox().expand(expandXProperty.value(), expandYProperty.value(), expandZProperty.value()));
	    }
});	    

public boolean isBot(PlayerEntity playerEntity) {
        if (EntityHelper.INSTANCE.isNPC(playerEntity)) {
            return true;
        } else {
            return false;
        }
    }  
      private boolean isEnabled(Entity entity) {	  
	if (specificFilterProperty.value()) {
            if (entity instanceof IronGolemEntity)
                return ironGolemProperty.value();
            if (entity instanceof ZombifiedPiglinEntity)
                return zombiePiglinProperty.value();
            if (entity instanceof PiglinEntity)
                return piglinProperty.value();
        }
	if (!(entity instanceof LivingEntity))
            return nolivingProperty.value();	  
         if (EntityHelper.INSTANCE.isPassiveMob(entity))
	    return passiveProperty.value();
	  if (EntityHelper.INSTANCE.doesPlayerOwn(entity))
            return petProperty.value();
        if (EntityHelper.INSTANCE.isBossMob(entity))
            return bossProperty.value();
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostileProperty.value();
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutralProperty.value();
        if (entity instanceof PlayerEntity && entity != Wrapper.INSTANCE.getLocalPlayer())
	    return playerProperty.value();	
            if (FriendHelper.INSTANCE.isFriend(entity.getName().getString()))
                return friendProperty.value();
            if (EntityHelper.INSTANCE.isOnSameTeam((PlayerEntity) entity, Wrapper.INSTANCE.getLocalPlayer(), teamCheckProperty.value()))
                return false;
            if (isBot((PlayerEntity) entity))
                return botCheckProperty.value();   
        return false;
   }       
}
