package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;

@Cmd(name = "Head", syntax = ".head <player>", description = "Get the head of a chosen player.")
public class CommandHead extends Command {

    @Override
    public void runCommand(String command, String[] args) {
        try {
            if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
                ChatHelper.INSTANCE.addClientMessage("You must be in creative for this command!");
                return;
            }
            String playerName = args[1];
            ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
            CompoundTag tag = new CompoundTag();
            tag.putString("SkullOwner", playerName);
            itemStack.setTag(tag);
            Wrapper.INSTANCE.getInteractionManager().clickCreativeStack(itemStack, 36);
            ChatHelper.INSTANCE.addClientMessage("Done! You now have " + playerName + "'s head.");
        } catch (Exception e) {
            giveSyntaxMessage();
        }
    }
}
