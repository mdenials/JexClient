package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class CoordinatesElement extends HudElement{
    
    public CoordinatesElement(float x, float y, float minWidth, float minHeight) {
        super("Coordinates", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        float longest = 0;

        Vec3d pos = Wrapper.INSTANCE.getLocalPlayer().getPos();
        String coordString = String.format("XYZ\247f: \2477%.15f\247f/\2477%.15f\247f/\2477%.15f", pos.getX(), pos.getY(), pos.getZ());
        float strLength = FontHelper.INSTANCE.getStringWidth(coordString);
        float strX = isLeftSide() ? getX() + 3 : getX() + getWidth() - strLength;
        float strY = getY() + (isTopSide() ? 2.5f : 12.5f);
        if (strLength > longest)
            longest = strLength;
        FontHelper.INSTANCE.drawWithShadow(matrixStack, coordString, strX, strY, ColorHelper.INSTANCE.getClientColor());

        if (getHud().netherCoordsProperty.value()) {
            double coordScale = Wrapper.INSTANCE.getLocalPlayer().clientWorld.getDimension().coordinateScale();
            if (coordScale != 1.0D) {
                coordString = String.format("Overworld\247f: \2477%.15f\247f/\2477%.15f\247f/\2477%.15f", pos.getX() * coordScale, pos.getY() * coordScale, pos.getZ() * coordScale);
            } else {
                coordString = String.format("Nether\247f: \2477%.15f\247f/\2477%.15f\247f/\2477%.15f", pos.getX() / 8, pos.getY(), pos.getZ() / 8);
            }
            strLength = FontHelper.INSTANCE.getStringWidth(coordString);
            strX = isLeftSide() ? getX() + 3 : getX() + getWidth() - strLength;
            strY = getY() + (!isTopSide() ? 2.5f : 12.5f);
            if (strLength > longest)
                longest = strLength;
            FontHelper.INSTANCE.drawWithShadow(matrixStack, coordString, strX, strY, ColorHelper.INSTANCE.getClientColor());
        }

        this.setHeight(getHud().netherCoordsProperty.value() ? 22.5f : 12.5f);
        this.setWidth(longest + 4);
    }

    @Override
    public boolean isVisible() {
        return getHud().coordsProperty.value();
    }
}
