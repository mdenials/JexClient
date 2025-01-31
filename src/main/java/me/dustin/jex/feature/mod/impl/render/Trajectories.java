package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.BufferHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.load.impl.IPersistentProjectileEntity;
import me.dustin.jex.load.impl.IProjectile;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import java.awt.*;
import java.util.ArrayList;

public class Trajectories extends Feature {

    public final Property<Float> sizeProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Box Size")
            .value(0.1f)
            .min(0.01f)
            .max(0.3f)
            .inc(0.01f)
            .build();
    public final Property<Boolean> disableDepthProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Z-Clip")
            .value(true)
            .build();
    public final Property<Color> missColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Miss Color")
            .value(Color.GREEN)
            .build();
    public final Property<Color> hitColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Hit Color")
            .value(Color.RED)
            .build();

    private Entity hitEntity = null;
    private final ArrayList<Vec3d> positions = new ArrayList<>();

    public Trajectories() {
        super(Category.VISUAL);
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (!positions.isEmpty()) {
            MatrixStack matrixStack = event.getPoseStack();
            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            for (int i = 0; i < positions.size(); i++) {
                if (i != positions.size() - 1) {

                    Color color = hitEntity == null ? missColorProperty.value() : hitColorProperty.value();

                    Vec3d vec = positions.get(i);
                    Vec3d vec1 = positions.get(i + 1);
                    double x = vec.x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
                    double y = vec.y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
                    double z = vec.z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;

                    double x1 = vec1.x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
                    double y1 = vec1.y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
                    double z1 = vec1.z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;

                    Render3DHelper.INSTANCE.setup3DRender(disableDepthProperty.value());
                    BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                    bufferBuilder.vertex(matrix4f, (float) x, (float) y, (float) z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                    bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) z1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                    BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
                    Render3DHelper.INSTANCE.end3DRender();
                } else {
                    Vec3d vec = Render3DHelper.INSTANCE.getRenderPosition(positions.get(i).x, positions.get(i).y, positions.get(i).z);
                    if (hitEntity != null) {
                        Vec3d vec2 = Render3DHelper.INSTANCE.getEntityRenderPosition(hitEntity, event.getPartialTicks());
                        Render3DHelper.INSTANCE.drawEntityBox(event.getPoseStack(), hitEntity, vec2.x, vec2.y, vec2.z, hitColorProperty.value().getRGB());
                    } else {
                        Box bb1 = new Box(vec.x - sizeProperty.value(), vec.y - sizeProperty.value(), vec.z - sizeProperty.value(), vec.x + sizeProperty.value(), vec.y + sizeProperty.value(), vec.z + sizeProperty.value());
                        Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), bb1, missColorProperty.value().getRGB());
                    }
                }
            }
        }
    });

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        positions.clear();
        ItemStack mainStack = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
        hitEntity = null;
        if (isGoodItem(mainStack)) {
            if (mainStack.getItem() instanceof BowItem) {
                BowItem bowItem = (BowItem) mainStack.getItem();
                int i = bowItem.getMaxUseTime(mainStack) - Wrapper.INSTANCE.getLocalPlayer().getItemUseTimeLeft();
                float f = BowItem.getPullProgress(i);
                if (f == 0)
                    f = 1;
                ItemStack itemStack = new ItemStack(Items.ARROW);
                ArrowItem arrowItem = (ArrowItem) itemStack.getItem();
                PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(Wrapper.INSTANCE.getWorld(), itemStack, Wrapper.INSTANCE.getLocalPlayer());
                persistentProjectileEntity.setVelocity(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPitch(), PlayerHelper.INSTANCE.getYaw(), 0.0F, f * 3.0F, 0);
                for (int j = 0; j < 200; j++) {
                    persistentProjectileEntity.tick();
                    positions.add(persistentProjectileEntity.getPos());
                    hitEntity = getHitEntity(persistentProjectileEntity);
                    if (hitEntity != null) {
                        break;
                    }
                }
            } else if (mainStack.getItem() instanceof CrossbowItem) {
                if (isCharged(mainStack)) {
                    ItemStack itemStack = new ItemStack(Items.ARROW);
                    ArrowItem arrowItem = (ArrowItem) itemStack.getItem();
                    PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(Wrapper.INSTANCE.getWorld(), itemStack, Wrapper.INSTANCE.getLocalPlayer());

                    Vec3d vec3d = Wrapper.INSTANCE.getLocalPlayer().getOppositeRotationVector(1.0F);
                    Quaternion quaternion = new Quaternion(new Vec3f(vec3d), 0, true);
                    Vec3d vec3d2 = Wrapper.INSTANCE.getLocalPlayer().getRotationVec(1.0F);
                    Vec3f vector3f = new Vec3f(vec3d2);
                    vector3f.rotate(quaternion);
                    ((ProjectileEntity) persistentProjectileEntity).setVelocity(vector3f.getX(), vector3f.getY(), vector3f.getZ(), getSpeed(mainStack), 0);
                    for (int j = 0; j < 200; j++) {
                        persistentProjectileEntity.tick();
                        positions.add(persistentProjectileEntity.getPos());
                        hitEntity = getHitEntity(persistentProjectileEntity);
                        if (hitEntity != null) {
                            break;
                        }
                    }
                }
            } else if (mainStack.getItem() instanceof SnowballItem) {
                SnowballEntity snowballEntity = new SnowballEntity(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer());
                snowballEntity.setItem(mainStack);
                snowballEntity.setVelocity(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPitch(), PlayerHelper.INSTANCE.getYaw(), 0.0F, 1.5F, 0);
                IProjectile iProjectile = (IProjectile) (ProjectileEntity) snowballEntity;
                for (int j = 0; j < 200; j++) {
                    snowballEntity.tick();
                    positions.add(snowballEntity.getPos());
                    HitResult hitResult = ProjectileUtil.getCollision(snowballEntity, iProjectile::callCanHit);
                    if (hitResult != null) {
                        if (hitResult.getType() == HitResult.Type.ENTITY) {
                            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                            hitEntity = entityHitResult.getEntity();
                            if (hitEntity != null)
                                break;
                        } else if (hitResult.getType() != HitResult.Type.MISS) {
                            break;
                        }
                    }
                }
            } else if (mainStack.getItem() instanceof EnderPearlItem) {
                EnderPearlEntity enderPearlEntity = new EnderPearlEntity(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer());
                enderPearlEntity.setItem(mainStack);
                enderPearlEntity.setVelocity(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPitch(), PlayerHelper.INSTANCE.getYaw(), 0.0F, 1.5F, 0);
                IProjectile iProjectile = (IProjectile) (ProjectileEntity) enderPearlEntity;
                for (int j = 0; j < 200; j++) {
                    enderPearlEntity.tick();
                    if (WorldHelper.INSTANCE.getBlock(new BlockPos(enderPearlEntity.getPos())) == Blocks.END_GATEWAY) {
                        hitEntity = Wrapper.INSTANCE.getLocalPlayer();
                    } else {
                        positions.add(enderPearlEntity.getPos());
                        HitResult hitResult = ProjectileUtil.getCollision(enderPearlEntity, iProjectile::callCanHit);
                        if (hitResult != null) {
                            if (hitResult.getType() == HitResult.Type.ENTITY) {
                                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                                hitEntity = entityHitResult.getEntity();
                                if (hitEntity != null)
                                    break;
                            } else if (hitResult.getType() != HitResult.Type.MISS) {
                                break;
                            }
                        }
                    }
                }
            } else if (mainStack.getItem() instanceof ThrowablePotionItem) {
                PotionEntity potionEntity = new PotionEntity(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer());
                potionEntity.setItem(mainStack);
                potionEntity.setVelocity(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPitch(), PlayerHelper.INSTANCE.getYaw(), -20.0F, 0.5F, 0);
                IProjectile iProjectile = (IProjectile) (ProjectileEntity) potionEntity;
                for (int j = 0; j < 200; j++) {
                    potionEntity.tick();
                    positions.add(potionEntity.getPos());
                    HitResult hitResult = ProjectileUtil.getCollision(potionEntity, iProjectile::callCanHit);
                    if (hitResult != null) {
                        if (hitResult.getType() == HitResult.Type.ENTITY) {
                            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                            hitEntity = entityHitResult.getEntity();
                            if (hitEntity != null)
                                break;
                        } else if (hitResult.getType() != HitResult.Type.MISS) {
                            break;
                        }
                    }
                }
            } else if (mainStack.getItem() instanceof TridentItem) {
                int j1 = EnchantmentHelper.getRiptide(mainStack);
                TridentEntity tridentEntity = new TridentEntity(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), mainStack);
                tridentEntity.setVelocity(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPitch(), PlayerHelper.INSTANCE.getYaw(), 0.0F, 2.5F + (float) j1 * 0.5F, 0);
                for (int j = 0; j < 200; j++) {
                    tridentEntity.tick();
                    positions.add(tridentEntity.getPos());
                    hitEntity = getHitEntity(tridentEntity);
                    if (hitEntity != null) {
                        break;
                    }
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private Entity getHitEntity(PersistentProjectileEntity persistentProjectileEntity) {
        EntityHitResult entityHitResult = getEntityCollision(persistentProjectileEntity, persistentProjectileEntity.getPos(), persistentProjectileEntity.getPos().add(persistentProjectileEntity.getVelocity()));
        if (entityHitResult != null)
            return entityHitResult.getEntity();
        return null;
    }

    protected EntityHitResult getEntityCollision(PersistentProjectileEntity persistentProjectileEntity, Vec3d currentPosition, Vec3d nextPosition) {
        IPersistentProjectileEntity iPersistentProjectileEntity = (IPersistentProjectileEntity) persistentProjectileEntity;
        return ProjectileUtil.getEntityCollision(persistentProjectileEntity.world, persistentProjectileEntity, currentPosition, nextPosition, persistentProjectileEntity.getBoundingBox().stretch(persistentProjectileEntity.getVelocity()).expand(1.0D), iPersistentProjectileEntity::callCanHit);
    }

    private boolean isGoodItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItem() == Items.AIR)
            return false;
        else
            return itemStack.getItem() == Items.BOW || itemStack.getItem() == Items.CROSSBOW || itemStack.getItem() == Items.SNOWBALL || itemStack.getItem() == Items.ENDER_PEARL || itemStack.getItem() == Items.TRIDENT || itemStack.getItem() instanceof ThrowablePotionItem;
    }

    private float getSpeed(ItemStack stack) {
        return stack.getItem() == Items.CROSSBOW && CrossbowItem.hasProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }

    private boolean isCharged(ItemStack stack) {
        NbtCompound compoundTag = stack.getNbt();
        return compoundTag != null && compoundTag.getBoolean("Charged");
    }
}
