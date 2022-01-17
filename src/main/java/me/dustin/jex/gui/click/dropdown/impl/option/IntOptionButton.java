package me.dustin.jex.gui.click.dropdown.impl.option;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.option.types.IntOption;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.file.impl.GuiFile;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.MouseHelper;
import net.minecraft.client.util.math.MatrixStack;

public class IntOptionButton extends DropdownOptionButton {
    private boolean isSliding;
    public IntOptionButton(DropdownWindow window, IntOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        super.render(matrixStack);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        super.click(double_1, double_2, int_1);
        if (isHovered() && int_1 == 0) {
            isSliding = true;
        }
    }

    @Override
    public void tick() {
        if (!MouseHelper.INSTANCE.isMouseButtonDown(0)) {
            isSliding = false;
            if (JexClient.INSTANCE.isAutoSaveEnabled())
                ConfigManager.INSTANCE.get(FeatureFile.class).write();
        }
        if (isSliding)
            handleSliders((IntOption)getOption());
        super.tick();
    }

    protected void handleSliders(IntOption v) {

    }
}
