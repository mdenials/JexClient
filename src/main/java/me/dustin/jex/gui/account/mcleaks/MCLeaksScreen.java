package me.dustin.jex.gui.account.mcleaks;

import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCLeaksHelper;
import me.dustin.jex.helper.network.WebHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class MCLeaksScreen extends Screen {
    private Screen parent;

    private TextFieldWidget tokenField;
    private ButtonWidget restoreButton;
    private ButtonWidget useTokenButton;
    private String message;

    private boolean sessionRestored;

    public MCLeaksScreen(Screen parent, boolean sessionRestored) {
        super(new LiteralText("MCLeaks"));
        this.parent = parent;
        this.sessionRestored = sessionRestored;
    }

    public MCLeaksScreen(Screen parent, boolean sessionRestored, String message) {
        super(new LiteralText("MCLeaks"));
        this.parent = parent;
        this.sessionRestored = sessionRestored;
        this.message = message;
    }

    @Override
    protected void init() {
        restoreButton = new ButtonWidget(this.width / 2 - 150, this.height / 4 + 96 + 18, 128, 20, new LiteralText(this.sessionRestored ? "Session restored!" : "Restore Session"), button -> {
            MCLeaksHelper.INSTANCE.activeAccount = null;
            Wrapper.INSTANCE.getMinecraft().openScreen(new MCLeaksScreen(this.parent, true));
        });
        useTokenButton = new ButtonWidget(this.width / 2 - 18, this.height / 4 + 96 + 18, 168, 20, new LiteralText("Redeem Token"), button -> {
            if (this.tokenField.getText().length() != 16) {
                Wrapper.INSTANCE.getMinecraft().openScreen(new MCLeaksScreen(this.parent, false, "\247cThe token has to be 16 characters long!"));
                return;
            }
            button.active = false;
            button.setMessage(new LiteralText("Please wait ..."));
            MCLeaksHelper.MCLeaksAccount account = MCLeaksHelper.INSTANCE.getAccount(tokenField.getText());
            if (account != null) {
                MCLeaksHelper.INSTANCE.setActiveAccount(account);
                Wrapper.INSTANCE.getMinecraft().openScreen(new MCLeaksScreen(this.parent, false, "\247aYour token was redeemed successfully!"));
            } else {
                Wrapper.INSTANCE.getMinecraft().openScreen(new MCLeaksScreen(this.parent, false, "\247cInvalid token!"));
            }
        });
        ButtonWidget getTokenButton = new ButtonWidget(this.width / 2 - 150, this.height / 4 + 120 + 18, 158, 20, new LiteralText("Get Token"), button -> {
            WebHelper.INSTANCE.openLink("https://mcleaks.net/");
        });
        ButtonWidget cancelButton = new ButtonWidget(this.width / 2 + 12, this.height / 4 + 120 + 18, 138, 20, new LiteralText("Cancel"), button -> {
            Wrapper.INSTANCE.getMinecraft().openScreen(parent);
        });
        tokenField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), this.width / 2 - 100, 128, 200, 20, new LiteralText(""));
        this.addDrawableChild(restoreButton);
        this.addDrawableChild(useTokenButton);
        this.addDrawableChild(getTokenButton);
        this.addDrawableChild(cancelButton);
        this.addSelectableChild(tokenField);
        super.init();
    }

    @Override
    public void tick() {
        tokenField.tick();
        super.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        tokenField.render(matrices, mouseX, mouseY, delta);

        FontHelper.INSTANCE.drawCenteredString(matrices, Formatting.WHITE + "- " + Formatting.AQUA + "MCLeaks" + Formatting.WHITE + "." + Formatting.AQUA + "net " + Formatting.WHITE + "-", this.width / 2.f, 17, 16777215);
        FontHelper.INSTANCE.drawCenteredString(matrices, "Free minecraft accounts", this.width / 2.f, 32, 16777215);

        FontHelper.INSTANCE.drawCenteredString(matrices, "Status:", this.width / 2.f, 68, 16777215);
        FontHelper.INSTANCE.drawCenteredString(matrices, MCLeaksHelper.INSTANCE.activeAccount != null ? "Active" : "Inactive", this.width / 2.f, 78, 16777215);

        FontHelper.INSTANCE.drawWithShadow(matrices, "Token", this.width / 2.f - 100, 115, 10526880);

        if (message != null) {
            FontHelper.INSTANCE.drawCenteredString(matrices, this.message, this.width / 2.f, 158, 16777215);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Wrapper.INSTANCE.getMinecraft().openScreen(parent);
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
