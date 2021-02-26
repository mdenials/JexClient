package me.dustin.jex.event.render;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.client.util.math.MatrixStack;

public class EventRender3D extends Event {

    private float partialTicks;
    private MatrixStack matrixStack;
    private MatrixStack origStack;

    public EventRender3D(MatrixStack matrixStack, MatrixStack origStack, float partialTicks2) {
        this.partialTicks = partialTicks2;
        this.matrixStack = matrixStack;
        this.origStack = origStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public MatrixStack getOrigStack() {
        return origStack;
    }

    public static class EventRender3DNoBob extends Event {

        private float partialTicks;
        private MatrixStack matrixStack;

        public EventRender3DNoBob(MatrixStack matrixStack, float partialTicks2) {
            this.partialTicks = partialTicks2;
            this.matrixStack = matrixStack;
        }

        public float getPartialTicks() {
            return partialTicks;
        }

        public MatrixStack getMatrixStack() {
            return matrixStack;
        }

    }
}
