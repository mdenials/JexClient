package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.filters.KeyPressFilter;
import me.dustin.jex.event.filters.MousePressFilter;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.load.impl.IHandledScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Search for items while in a chest or your inventory")
public class ItemSearcher extends Feature {

    private boolean typing;
    private String searchField = "";

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (!typing)
            return;
        int keyCode = event.getKey();
        if (Screen.isPaste(keyCode)) {
            searchField += MinecraftClient.getInstance().keyboard.getClipboard();
            event.cancel();
            return;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_ESCAPE -> typing = false;
            case GLFW.GLFW_KEY_SPACE -> searchField += " ";
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (searchField.isEmpty())
                    break;
                searchField = searchField.substring(0, searchField.length() - 1);
            }
            default -> {
                String keyName = InputUtil.fromKeyCode(keyCode, event.getScancode()).getTranslationKey().replace("key.keyboard.", "");
                if (keyName.length() == 1) {
                    if (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                        keyName = keyName.toUpperCase();
                        if (isInt(keyName))
                            keyName = getFromNumKey(Integer.parseInt(keyName));
                    }
                    searchField += keyName;
                }
            }
        }
        event.cancel();
    }, new KeyPressFilter(EventKeyPressed.PressType.IN_MENU));

    @EventPointer
    private final EventListener<EventMouseButton> eventMouseButtonEventListener = new EventListener<>(event -> {
        typing = Render2DHelper.INSTANCE.isHovered(Render2DHelper.INSTANCE.getScaledWidth() / 2.f - 150, Render2DHelper.INSTANCE.getScaledHeight() - 22, Render2DHelper.INSTANCE.getScaledWidth() / 2.f + 150, Render2DHelper.INSTANCE.getScaledHeight() - 1);
    }, new MousePressFilter(EventMouseButton.ClickType.IN_MENU, 0));

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        if (event.getScreen() instanceof HandledScreen<?> handledScreen) {
            FontHelper.INSTANCE.drawCenteredString(event.getMatrixStack(), "Type here to search:", Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() - 30, -1);
            Render2DHelper.INSTANCE.fillAndBorder(event.getMatrixStack(), Render2DHelper.INSTANCE.getScaledWidth() / 2.f - 150, Render2DHelper.INSTANCE.getScaledHeight() - 22, Render2DHelper.INSTANCE.getScaledWidth() / 2.f + 150, Render2DHelper.INSTANCE.getScaledHeight() - 1, typing ? ColorHelper.INSTANCE.getClientColor() : 0xffffffff, 0x90000000, 1);
            FontHelper.INSTANCE.drawCenteredString(event.getMatrixStack(), searchField, Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() - 15, -1);

            IHandledScreen iHandledScreen = (IHandledScreen) handledScreen;
            if (!searchField.isEmpty())
                for (Slot slot : handledScreen.getScreenHandler().slots) {
                    float x = iHandledScreen.getX() + slot.x;
                    float y = iHandledScreen.getY() + slot.y;
                    boolean correct = slot.hasStack() && slot.getStack().getName().getString().toLowerCase().contains(searchField.toLowerCase());
                    if (slot.getStack().hasEnchantments()) {
                        Map<Enchantment, Integer> enchants = EnchantmentHelper.fromNbt(slot.getStack().getEnchantments());
                        for (Enchantment enchantment : enchants.keySet()) {
                            if (enchantment.getName(enchantment.getMaxLevel()).getString().toLowerCase().contains(searchField.toLowerCase()))
                                correct = true;
                        }
                    }
                    if (slot.getStack().getItem() instanceof EnchantedBookItem) {
                        Map<Enchantment, Integer> enchants = EnchantmentHelper.fromNbt(EnchantedBookItem.getEnchantmentNbt(slot.getStack()));
                        for (Enchantment enchantment : enchants.keySet()) {
                            if (enchantment.getName(enchantment.getMaxLevel()).getString().toLowerCase().contains(searchField.toLowerCase()))
                                correct = true;
                        }
                    }
                    Render2DHelper.INSTANCE.fillAndBorder(event.getMatrixStack(), x - 1, y - 1, x + 17, y + 17, correct ? ColorHelper.INSTANCE.getClientColor() : 0x00000000, correct ? 0x00ffffff : 0xaa000000, 1);
                }
        }
    }, Priority.FIRST, new DrawScreenFilter(EventDrawScreen.Mode.POST_CONTAINER));


    private boolean isInt(String intStr) {
        try {
            Integer.parseInt(intStr);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    private String getFromNumKey(int i) {
        return switch (i) {
            case 1 -> "!";
            case 2 -> "@";
            case 3 -> "#";
            case 4 -> "$";
            case 5 -> "%";
            case 6 -> "^";
            case 7 -> "&";
            case 8 -> "*";
            case 9 -> "(";
            case 0 -> ")";
            default -> String.valueOf(i);
        };
    }
}
