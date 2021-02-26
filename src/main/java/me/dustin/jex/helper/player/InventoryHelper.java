package me.dustin.jex.helper.player;

import com.google.common.collect.Maps;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.Map;

public enum InventoryHelper {
    INSTANCE;

    public PlayerInventory getInventory() {
        return Wrapper.INSTANCE.getLocalPlayer().inventory;
    }

    public PlayerInventory getInventory(PlayerEntity playerEntity) {
        return playerEntity.inventory;
    }

    public boolean isHotbarFull() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = getInventory().getStack(i);
            if (itemStack == null || itemStack.getItem() instanceof AirBlockItem)
                return false;
        }
        return true;
    }

    public boolean isInventoryFull() {
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = getInventory().getStack(i);
            if (itemStack == null || itemStack.getItem() instanceof AirBlockItem)
                return false;
        }
        return true;
    }

    public int getFromHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (getInventory().getStack(i) != null && getInventory().getStack(i).getItem() == item)
                return i;
        }
        return -1;
    }

    public int getEmptyHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (getInventory().getStack(i) == null || getInventory().getStack(i).getItem() instanceof AirBlockItem)
                return i;
        }
        return -1;
    }


    public int getFromInv(Item item) {
        for (int i = 0; i < 36; i++) {
            if (getInventory().getStack(i) != null && getInventory().getStack(i).getItem() == item)
                return i;
        }
        return -1;
    }

    public boolean isInventoryFull(ItemStack stack) {
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = getInventory().getStack(i);
            if (itemStack == null || itemStack.getItem() instanceof AirBlockItem)
                return false;
        }
        int slot = getInventory().getSlotWithStack(stack);
        if (slot != -1) {
            if (getInventory().getStack(slot).getCount() < getInventory().getStack(slot).getMaxCount()) {
                return false;
            }
        }
        return true;
    }

    public void windowClick(ScreenHandler container, int slot, SlotActionType action) {
        windowClick(container, slot, action, 0);
    }

    public void windowClick(ScreenHandler container, int slot, SlotActionType action, int clickData) {
        short short_1 = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.getNextActionId(InventoryHelper.INSTANCE.getInventory());
        ItemStack itemStack = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.onSlotClick(slot, clickData, action, Wrapper.INSTANCE.getLocalPlayer());
        Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(new ClickSlotC2SPacket(container.syncId, slot, clickData, action, itemStack, short_1));
    }

    public HashMap<Integer, ItemStack> getStacksFromShulker(ItemStack shulkerBox) {
        HashMap<Integer, ItemStack> stacks = Maps.newHashMap();
        CompoundTag nbttagcompound = shulkerBox.getTag();
        if (nbttagcompound == null) return stacks;


        CompoundTag nbttagcompound1 = nbttagcompound.getCompound("BlockEntityTag");
        for (int i = 0; i < nbttagcompound1.getList("Items", 10).size(); i++) {
            CompoundTag compound = nbttagcompound1.getList("Items", 10).getCompound(i);
            int slot = compound.getInt("Slot");
            ItemStack itemStack = ItemStack.fromTag(compound);
            stacks.put(slot, itemStack);
        }
        return stacks;
    }

    public int getDepthStriderLevel() {
        ItemStack boots = getInventory().getArmorStack(0);
        if (boots.hasEnchantments()) {
            Map<Enchantment, Integer> equippedEnchants = EnchantmentHelper.get(boots);
            if (equippedEnchants.containsKey(Enchantments.DEPTH_STRIDER)) {
                return equippedEnchants.get(Enchantments.DEPTH_STRIDER);
            }
        }
        return 0;
    }

    public boolean isContainerEmpty(ScreenHandler container) {
        int most = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.size() - 36;
        for (int i = 0; i < most; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack != null && stack.getItem() != Items.AIR) {
                return false;
            }
        }
        return true;
    }

    public boolean compareEnchants(ItemStack equippedStack, ItemStack newPiece, Enchantment enchantment) {
        int equippedLevel = 0;
        int newLevel = 0;
        if (equippedStack.hasEnchantments()) {
            Map<Enchantment, Integer> equippedEnchants = EnchantmentHelper.get(equippedStack);
            if (!equippedEnchants.isEmpty()) {
                if (equippedEnchants.containsKey(enchantment))
                    equippedLevel = equippedEnchants.get(enchantment);
            }
        }
        if (newPiece.hasEnchantments()) {
            Map<Enchantment, Integer> newPieceEnchants = EnchantmentHelper.get(newPiece);
            if (!newPieceEnchants.isEmpty()) {
                if (newPieceEnchants.containsKey(enchantment))
                    newLevel = newPieceEnchants.get(enchantment);
            }
        }
        return newLevel > equippedLevel;
    }

    public boolean isShulker(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() == Items.SHULKER_BOX || stack.getItem() == Items.BLACK_SHULKER_BOX || stack.getItem() == Items.BLUE_SHULKER_BOX || stack.getItem() == Items.BROWN_SHULKER_BOX || stack.getItem() == Items.CYAN_SHULKER_BOX || stack.getItem() == Items.GRAY_SHULKER_BOX || stack.getItem() == Items.GREEN_SHULKER_BOX || stack.getItem() == Items.LIGHT_BLUE_SHULKER_BOX || stack.getItem() == Items.LIGHT_GRAY_SHULKER_BOX || stack.getItem() == Items.LIME_SHULKER_BOX || stack.getItem() == Items.MAGENTA_SHULKER_BOX || stack.getItem() == Items.ORANGE_SHULKER_BOX || stack.getItem() == Items.PINK_SHULKER_BOX || stack.getItem() == Items.PURPLE_SHULKER_BOX || stack.getItem() == Items.RED_SHULKER_BOX || stack.getItem() == Items.WHITE_SHULKER_BOX || stack.getItem() == Items.YELLOW_SHULKER_BOX);
    }
}
