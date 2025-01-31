package me.dustin.jex.feature.mod.impl.player;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;

public class ElytraSwap extends Feature {

    public ElytraSwap() {
        super(Category.PLAYER);
    }

    @Override
    public void onEnable() {
        ItemStack equippedStack = InventoryHelper.INSTANCE.getInventory().getStack(38);
        if (equippedStack.getItem() instanceof ArmorItem) {
            //wearing armor, look for elytra
            int bestElytraSlot = -1;
            ItemStack bestSelected = null;

            for (int i = 0; i < 36; i++) {
                ItemStack selected = InventoryHelper.INSTANCE.getInventory().getStack(i);
                if (selected.getItem() == Items.ELYTRA) {
                    if (ElytraItem.isUsable(selected)) {
                        if (bestElytraSlot == -1) {
                            bestElytraSlot = i;
                            bestSelected = selected;
                        } else {
                            if (InventoryHelper.INSTANCE.compareEnchants(selected, bestSelected, Enchantments.MENDING)) {
                                bestElytraSlot = i;
                                bestSelected = selected;
                            } else if (InventoryHelper.INSTANCE.compareEnchants(selected, bestSelected, Enchantments.UNBREAKING)) {
                                bestElytraSlot = i;
                                bestSelected = selected;
                            }
                        }
                    }
                }
            }
            if (bestElytraSlot != -1) {
                if (bestElytraSlot > 8) {
                    InventoryHelper.INSTANCE.swapToHotbar(bestElytraSlot, 8);
                    bestElytraSlot = 8;
                }
                //swap on slot 6 as that's the chest slot
                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 6, SlotActionType.SWAP, bestElytraSlot);
                ChatHelper.INSTANCE.addRawMessage(String.format("%s[%sElytraSwap%s]%s: %sEquipped %s", Formatting.DARK_GRAY, Formatting.GREEN, Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY, bestSelected.getName().getString()));
            } else {
                ChatHelper.INSTANCE.addRawMessage(String.format("%s[%sElytraSwap%s]%s: %sNo elytra available for swap!", Formatting.DARK_GRAY, Formatting.GREEN, Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY));
            }
        } else if (equippedStack.getItem() == Items.ELYTRA){
            //wearing elytra, look for armor
            int bestChestSlot = -1;
            ItemStack bestSelectedStack = null;
            for (int i = 0; i < 36; i++) {
                ItemStack selected = InventoryHelper.INSTANCE.getInventory().getStack(i);
                if (selected.getItem() instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.CHEST) {
                    if (bestSelectedStack == null) {
                        bestChestSlot = i;
                        bestSelectedStack = selected;
                        continue;
                    }
                    ArmorItem bestSelected = (ArmorItem) bestSelectedStack.getItem();
                    if (bestSelected.getMaterial().getProtectionAmount(bestSelected.getSlotType()) < armorItem.getMaterial().getProtectionAmount(armorItem.getSlotType())) {
                        bestChestSlot = i;
                        bestSelectedStack = selected;
                    } else if (bestSelected.getMaterial().getProtectionAmount(bestSelected.getSlotType()) == armorItem.getMaterial().getProtectionAmount(armorItem.getSlotType()) && bestSelected.getMaterial().getToughness() < armorItem.getMaterial().getToughness()) {
                        bestChestSlot = i;
                        bestSelectedStack = selected;
                    } else if (bestSelected.getMaterial().getToughness() == armorItem.getMaterial().getToughness() && InventoryHelper.INSTANCE.compareEnchants(equippedStack, selected, Enchantments.PROTECTION)) {
                        bestChestSlot = i;
                        bestSelectedStack = selected;
                    }
                }
            }
            if (bestChestSlot != -1) {
                if (bestChestSlot > 8) {
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, bestChestSlot, SlotActionType.SWAP, 8);
                    bestChestSlot = 8;
                }
                //swap on slot 6 as that's the chest slot
                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 6, SlotActionType.SWAP, bestChestSlot);
                ChatHelper.INSTANCE.addRawMessage("\2478[\247aElytraSwap\2478]\247f: \2477Equipped " + bestSelectedStack.getName().getString());
            } else {
                ChatHelper.INSTANCE.addRawMessage("\2478[\247aElytraSwap\2478]\247f: \2477No chestplate available for swap!");
            }
        }
        setState(false);
    }

    @Override
    public void onDisable() {
    }
}
