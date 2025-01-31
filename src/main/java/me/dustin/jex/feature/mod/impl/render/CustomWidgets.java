package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventSetScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRenderWidget;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.load.impl.ISliderWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import java.util.HashMap;
import java.util.Map;

public class CustomWidgets extends Feature {

    public Property<Float> shrinkSpeedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Shrink Speed")
            .value(4f)
            .min(1)
            .max(10)
            .inc(0.1f)
            .build();
    public Property<Float> growSpeedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Grow Speed")
            .value(1.2f)
            .min(1)
            .max(10)
            .inc(0.1f)
            .build();

    private final Map<ClickableWidget, Integer> hoverChecks = new HashMap<>();
    private final Map<ClickableWidget, Float> offsets = new HashMap<>();

    public CustomWidgets() {
        super("CustomWidgets", Category.VISUAL, "", true, false, 0);
    }

    @EventPointer
    private final EventListener<EventRenderWidget> eventRenderWidgetEventListener = new EventListener<>(event -> {
        int hoverColor = ColorHelper.INSTANCE.setAlpha(ColorHelper.INSTANCE.getClientColor(), 150);
        int nonHoverColor = 0x80000000;
        int borderColor = 0xff999999;
        int activeTextColor = 0xffbbbbbb;
        int inactiveTextColor = 0xff333333;

        ClickableWidget widget = event.getAbstractWidget();
        MatrixStack matrixStack = event.getPoseStack();
        if (!hoverChecks.containsKey(widget)) {
            hoverChecks.put(widget, 0);
            offsets.put(widget, 0.f);
        }
        int hoverTime = hoverChecks.get(widget);
        float offset = offsets.get(widget);
        float newOffset;
        if (hoverTime != 0){
            newOffset = 5 / (10.f / hoverTime);
            offset = offset + ((newOffset - offset) * Wrapper.INSTANCE.getMinecraft().getTickDelta());
        } else {
            newOffset = 0;
        }
        offsets.replace(widget, newOffset);
        if (widget instanceof ButtonWidget || widget instanceof CyclingButtonWidget) {
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, widget.x + offset, widget.y + (offset / 3.f), widget.x + widget.getWidth() - offset, widget.y + widget.getHeight() - (offset / 3.f), borderColor, widget.active && widget.isHovered() ? hoverColor : nonHoverColor, 1);
        } else if (widget instanceof SliderWidget sliderWidget) {
            ISliderWidget islider = (ISliderWidget) sliderWidget;
            double value = islider.getValue();
            float x = widget.x + 4;
            float width = widget.getWidth() - 8;
            float pos = (float)value * width;
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x - 1, widget.y, x + width + 1, widget.y + widget.getHeight(), borderColor, widget.active ? nonHoverColor : 0x99545454, 1);

            Render2DHelper.INSTANCE.fill(matrixStack, x, widget.y + 1, x + pos, widget.y + widget.getHeight() - 1, hoverColor);
        }
        event.cancel();
        FontHelper.INSTANCE.drawCenteredString(matrixStack, widget.getMessage().getString(), widget.x + (widget.getWidth() / 2.f), widget.y + (widget.getHeight() / 2.f) - 4.5f, widget.active ? activeTextColor : inactiveTextColor);
    });

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        for (ClickableWidget widget : hoverChecks.keySet()) {
            int hovered = hoverChecks.get(widget);
            if (widget.isHovered() && widget.active) {
                if (hovered < 10)
                    hovered+=shrinkSpeedProperty.value();
            } else if (hovered > 0) {
                hovered-=growSpeedProperty.value();
            }
            hovered = MathHelper.clamp(hovered, 0, 10);
            hoverChecks.replace(widget, hovered);
        }
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetScreen> eventSetScreenEventListener = new EventListener<>(event -> {
        hoverChecks.clear();
    });
}
