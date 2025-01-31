package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.gui.account.AccountManagerScreen;
import me.dustin.jex.gui.thealtening.TheAlteningScreen;
import me.dustin.jex.gui.mcleaks.MCLeaksScreen;
import me.dustin.jex.gui.proxy.ProxyScreen;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MixinMultiplayerScreen extends Screen {

    protected MixinMultiplayerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        this.addDrawableChild(new ButtonWidget(2, 2, 75, 20, Text.of("Alt Manager"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new AccountManagerScreen());
        }));
        this.addDrawableChild(new ButtonWidget(79, 2, 75, 20, Text.of("TheAltening"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new TheAlteningScreen((MultiplayerScreen)(Object)this));
        }));
        this.addDrawableChild(new ButtonWidget(156, 2, 75, 20, Text.of("MCLeaks"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new MCLeaksScreen((MultiplayerScreen)(Object)this, false));
        }));
        this.addDrawableChild(new ButtonWidget(width - 77, 2, 75, 20, Text.of("Proxy"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new ProxyScreen());
        }));
    }

}
