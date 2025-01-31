package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.gui.jexgui.JexGuiScreen;
import me.dustin.jex.helper.misc.Wrapper;
import org.lwjgl.glfw.GLFW;

public class Gui extends Feature {

    public final Property<Boolean> noCategoriesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("No Categories")
            .value(false)
            .build();

    public Gui() {
        super(Category.VISUAL, "", GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        Wrapper.INSTANCE.getMinecraft().setScreen(new JexGuiScreen(Wrapper.INSTANCE.getMinecraft().currentScreen, noCategoriesProperty.value()));
        this.toggleState();
    }

}
