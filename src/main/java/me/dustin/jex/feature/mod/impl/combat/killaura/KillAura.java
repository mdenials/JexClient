package me.dustin.jex.feature.mod.impl.combat.killaura;
import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.killaura.impl.MultiAura;
import me.dustin.jex.feature.mod.impl.combat.killaura.impl.SingleAura;
import me.dustin.jex.feature.mod.impl.settings.Targets;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import org.lwjgl.glfw.GLFW;
import java.awt.*;
import java.util.ArrayList;
public class KillAura extends Feature {
    public static KillAura INSTANCE;
    public final Property<TargetMode> targetModeProperty = new Property.PropertyBuilder<TargetMode>(this.getClass())
            .name("Mode")
            .value(TargetMode.SINGLE)
            .build();
    public final Property<AttackTiming> attackTimingProperty = new Property.PropertyBuilder<AttackTiming>(this.getClass())
            .name("Attack")
            .value(AttackTiming.PRE)
            .build();
    public final Property<Float> reachProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Reach")
            .value(3.8f)
            .min(2f)
            .max(6f)
            .inc(0.1f)
            .build();
    public final Property<Integer> ticksExistedProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Ticks Exsited")
            .value(50)
            .min(0)
            .max(300)
            .inc(1)
            .build();
    public final Property<Boolean> ignoreNewCombatProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Old Combat")
            .value(false)
            .build();
    public final Property<Float> apsProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("APS")
            .value(10f)
            .min(1)
            .max(128)
            .inc(1f)
            .parent(ignoreNewCombatProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> baritoneOverrideProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Baritone Override")
            .value(true)
            .build();
    public final Property<Boolean> followUntilDeadProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Follow until dead")
            .value(true)
            .parent(baritoneOverrideProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> bMinDistProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Min Distance")
            .value(3f)
            .max(6f)
            .inc(0.1f)
            .parent(baritoneOverrideProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> autoBlockProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("AutoBlock")
            .value(true)
            .build();
    public final Property<Float> autoBlockDistanceProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Distance")
            .value(7.5f)
            .min(3)
            .max(15)
            .inc(0.1f)
            .parent(autoBlockProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> rayTraceProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("RayTrace")
            .value(false)
            .build();
    public final Property<Boolean> rotateProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Rotate")
            .value(true)
            .build();
    public final Property<Boolean> lockviewProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Lockview")
            .value(false)
            .parent(rotateProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> randomizeProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Randomize")
            .value(false)
            .parent(rotateProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> randomWidthProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Random Width")
            .value(0.1f)
            .min(-1f)
            .max(1f)
            .inc(0.1f)
            .parent(randomizeProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> randomHeightProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Random Height")
            .value(0.1f)
            .min(-1f)
            .max(1f)
            .inc(0.1f)
            .parent(randomizeProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> ignoreWallsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Ignore Walls")
            .value(true)
            .build();
    public final Property<Boolean> showTargetProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Show Target")
            .value(true)
            .build();
    public final Property<Color> targetColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Target Color")
            .value(Color.BLACK)
            .parent(showTargetProperty)
            .depends(parent-> (boolean) parent.value())
            .build();
    public final Property<Boolean> reachCircleProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Reach Sphere")
            .value(false)
            .build();
    public final Property<Color> reachCircleColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Sphere Color")
            .value(Color.GREEN)
            .parent(reachCircleProperty)
            .depends(parent-> (boolean) parent.value())
            .build();
	public final Property<Boolean> projectilesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
	    .name("Projectiles")
            .value(true)
	    .build();
    public final Property<Boolean> fireballProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
	    .name("Fireball")
	    .value(true)
            .parent(projectilesProperty)
            .depends(parent -> (boolean) parent.value())
	    .build();
    public final Property<Boolean> dfireballProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
	    .name("DragonFireball")
	    .value(true)
            .parent(projectilesProperty)
            .depends(parent -> (boolean) parent.value())
	    .build();
    public final Property<Boolean> bulletProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
	    .name("ShulkerBullet")
	    .value(true)
            .parent(projectilesProperty)
            .depends(parent -> (boolean) parent.value())
	    .build();
    public final Property<Boolean> skullProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
	    .name("WitherSkull")
	    .value(true)
            .parent(projectilesProperty)
            .depends(parent -> (boolean) parent.value())
	    .build();
    private final StopWatch stopWatch = new StopWatch();
    private TargetMode lastMode;
    private boolean hasTarget = false;
    public ArrayList<PlayerEntity> touchedGround = new ArrayList<>();
    public ArrayList<PlayerEntity> swung = new ArrayList<>();
    public KillAura() {
        super(Category.COMBAT, "", GLFW.GLFW_KEY_R);
        INSTANCE = this;
        new SingleAura();
        new MultiAura();
    }
    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        for(Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if(entity instanceof PlayerEntity playerEntity) {
                if(playerEntity.isOnGround() && !touchedGround.contains(playerEntity))
                    touchedGround.add(playerEntity);
                if(playerEntity.handSwingProgress > 0 && !swung.contains(playerEntity))
                    swung.add(playerEntity);
            }
        }
        for(int i = 0; i < swung.size() - 1; i++) {
            PlayerEntity playerEntity = swung.get(i);
            if(playerEntity == null) {
                swung.remove(i);
            }
        }
        for(int i = 0; i < touchedGround.size() - 1; i++) {
            PlayerEntity playerEntity = touchedGround.get(i);
            if(playerEntity == null) {
                touchedGround.remove(i);
            }
        }
        setSuffix(targetModeProperty.value(), attackTimingProperty.value());
        sendEvent(event);
    }, Priority.LAST);

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> sendEvent(event));

    public void sendEvent(Event event) {
        if (targetModeProperty.value() != lastMode && lastMode != null) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(targetModeProperty.value(), this).enable();
        }
        FeatureExtension.get(targetModeProperty.value(), this).pass(event);
        lastMode = targetModeProperty.value();
    }

    public boolean canSwing() {
        if (ignoreNewCombatProperty.value()) {
            if (stopWatch.hasPassed((long) (1000 / apsProperty.value()))) {
                stopWatch.reset();
                return true;
            }
        } else {
            if (Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) == 1) {
                return true;
            }
        }
        return false;
    }
    public boolean isValid(Entity entity, boolean rangecheck) {
	 boolean canSee = Wrapper.INSTANCE.getLocalPlayer().canSee(entity);
        if (!canSee)
            return ignoreWallsProperty.value();
        if (rangecheck) {
            float distance = reachProperty.value();
            if (!canSee)
                distance = 3;
            if (entity.distanceTo(Wrapper.INSTANCE.getPlayer()) > distance)
                return false;
        }
	 if (projectilesProperty.value()) {
            if (entity instanceof ShulkerBulletEntity)
               return bulletProperty.value();
            if (entity instanceof FireballEntity)
               return fireballProperty.value();
            if (entity instanceof DragonFireballEntity)
               return dfireballProperty.value();
            if (entity instanceof WitherSkullEntity)
               return skullProperty.value();
        }       
	if (!(entity instanceof LivingEntity livingEntity))
		return Targets.INSTANCE.nolivingProperty.value();
	if (livingEntity.isSleeping())
               return Targets.INSTANCE.sleepingProperty.value();
	if (entity.hasCustomName())
            return Targets.INSTANCE.nametaggedProperty.value();
        if (entity.isInvisible())
            return Targets.INSTANCE.invisiblesProperty.value();
        if (!entity.isAlive() || (((LivingEntity) entity).getHealth() <= 0 && !Double.isNaN(((LivingEntity) entity).getHealth())))
            return Targets.INSTANCE.deadProperty.value();
	if (entity.age < ticksExistedProperty.value())
            return false;
	if (entity == Wrapper.INSTANCE.getLocalPlayer() || entity == Freecam.playerEntity)
                return false;
        if (Wrapper.INSTANCE.getLocalPlayer().getVehicle() != null) {
            if (entity == Wrapper.INSTANCE.getLocalPlayer().getVehicle())
                return false;
        }
	if (entity instanceof PlayerEntity && entity != Wrapper.INSTANCE.getLocalPlayer()) {
            if (FriendHelper.INSTANCE.isFriend(entity.getName().getString()))
                return Targets.INSTANCE.friendProperty.value();
            if (EntityHelper.INSTANCE.isOnSameTeam((PlayerEntity) entity, Wrapper.INSTANCE.getLocalPlayer(), Targets.INSTANCE.teamCheckProperty.value()))
                return false;
            if (isBot((PlayerEntity) entity))
                return Targets.INSTANCE.botCheckProperty.value();
            return Targets.INSTANCE.playerProperty.value();
        }
	if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return Targets.INSTANCE.neutralProperty.value();    
	if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return Targets.INSTANCE.passiveProperty.value();
	if (EntityHelper.INSTANCE.doesPlayerOwn(entity))
	    return Targets.INSTANCE.petProperty.value();
        if (EntityHelper.INSTANCE.isBossMob(entity))
            return Targets.INSTANCE.bossProperty.value();
        if (EntityHelper.INSTANCE.isHostileMob(entity))
	 return Targets.INSTANCE.hostileProperty.value();
	if (Targets.INSTANCE.specificFilterProperty.value()) {
            if (entity instanceof IronGolemEntity)
                return Targets.INSTANCE.ironGolemProperty.value();
            if (entity instanceof ZombifiedPiglinEntity)
                return Targets.INSTANCE.zombiePiglinProperty.value();
            if (entity instanceof PiglinEntity)
                return Targets.INSTANCE.piglinProperty.value();
        }
	 return false;
    }
    public boolean isBot(PlayerEntity playerEntity) {
        if (EntityHelper.INSTANCE.isNPC(playerEntity)) {
            return true;
        } else {
            return (!swung.contains(playerEntity) && !touchedGround.contains(playerEntity)) || playerEntity.getGameProfile().getProperties().isEmpty();
        }
    }
    public void setHasTarget(boolean hasTarget) {
        this.hasTarget = hasTarget;
    }
    public boolean hasTarget() {
        return hasTarget;
    }
    @Override
    public void onDisable() {
        super.onDisable();
        setHasTarget(false);
        if (BaritoneHelper.INSTANCE.baritoneExists())
            BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
        FeatureExtension.get(targetModeProperty.value(), this).disable();
    }
    public enum TargetMode {
        SINGLE, MULTI
    }
    public enum AttackTiming {
        PRE, POST
    }
}
