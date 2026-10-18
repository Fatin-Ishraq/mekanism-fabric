package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.bus.api.ICancellableEvent;

public abstract class InputEvent extends Event {

    public static class MouseScrollingEvent extends InputEvent implements ICancellableEvent {

        private final double scrollDeltaX;
        private final double scrollDeltaY;
        private final double mouseX;
        private final double mouseY;
        private final boolean leftDown;
        private final boolean middleDown;
        private final boolean rightDown;

        public MouseScrollingEvent(double scrollDeltaX, double scrollDeltaY, boolean leftDown, boolean middleDown, boolean rightDown, double mouseX, double mouseY) {
            this.scrollDeltaX = scrollDeltaX;
            this.scrollDeltaY = scrollDeltaY;
            this.leftDown = leftDown;
            this.middleDown = middleDown;
            this.rightDown = rightDown;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        public double getScrollDeltaX() {
            return scrollDeltaX;
        }

        public double getScrollDeltaY() {
            return scrollDeltaY;
        }

        public boolean isLeftDown() {
            return leftDown;
        }

        public boolean isRightDown() {
            return rightDown;
        }

        public boolean isMiddleDown() {
            return middleDown;
        }

        public double getMouseX() {
            return mouseX;
        }

        public double getMouseY() {
            return mouseY;
        }
    }
}
