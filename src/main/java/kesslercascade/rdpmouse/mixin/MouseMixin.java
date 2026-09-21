package kesslercascade.rdpmouse.mixin;

import kesslercascade.rdpmouse.RDPMouseCursor;
import kesslercascade.rdpmouse.RDPMouseState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;
    @Shadow private boolean cursorLocked;

    @Unique private GLFWCursorPosCallback rdpmouse$vanillaCallback;
    @Unique private long rdpmouse$window;

    @Inject(method = "setup", at = @At("RETURN"))
    private void rdpmouse$onSetup(long window, CallbackInfo ci) {
        rdpmouse$window = window;
        rdpmouse$vanillaCallback = GLFW.glfwSetCursorPosCallback(window, (win, x, y) -> {
            if (RDPMouseState.enabled && cursorLocked) {
                if (RDPMouseState.lastX == RDPMouseState.UNSET) {
                    RDPMouseState.lastX = x;
                    RDPMouseState.lastY = y;
                    return;
                }

                double dx = x - RDPMouseState.lastX;
                double dy = y - RDPMouseState.lastY;
                RDPMouseState.lastX = x;
                RDPMouseState.lastY = y;

                int[] w = new int[1], h = new int[1];
                GLFW.glfwGetWindowSize(win, w, h);
                int winW = w[0];
                int winH = h[0];

                if (winW > 0 && winH > 0) {
                    double maxDelta = Math.max(100.0, Math.min(winW, winH) / 6.0);

                    // Accumulate movement if it's within normal physical delta threshold
                    if (Math.abs(dx) < maxDelta && Math.abs(dy) < maxDelta) {
                        cursorDeltaX += dx;
                        cursorDeltaY += dy;
                    }

                    // Seamlessly recenter cursor when it approaches window boundaries
                    int marginX = (int) (winW * 0.25);
                    int marginY = (int) (winH * 0.25);

                    if (x < marginX || x > winW - marginX || y < marginY || y > winH - marginY) {
                        GLFW.glfwSetCursorPos(win, winW / 2.0, winH / 2.0);
                    }
                }
            } else if (rdpmouse$vanillaCallback != null) {
                rdpmouse$vanillaCallback.invoke(win, x, y);
            }
        });
    }

    @Inject(method = "lockCursor", at = @At("TAIL"))
    private void rdpmouse$onLockCursor(CallbackInfo ci) {
        if (RDPMouseState.enabled) {
            long window = this.client.getWindow().getHandle();
            if (window != 0L) {
                RDPMouseState.reset();
                if (InputUtil.isRawMouseMotionSupported()) {
                    GLFW.glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_FALSE);
                }
                GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
                RDPMouseCursor.clipCursor(window);

                int[] w = new int[1], h = new int[1];
                GLFW.glfwGetWindowSize(window, w, h);
                if (w[0] > 0 && h[0] > 0) {
                    GLFW.glfwSetCursorPos(window, w[0] / 2.0, h[0] / 2.0);
                }
            }
        }
    }

    @Inject(method = "unlockCursor", at = @At("HEAD"))
    private void rdpmouse$onUnlockCursor(CallbackInfo ci) {
        if (!this.cursorLocked) return;
        RDPMouseState.reset();
        long window = this.client.getWindow().getHandle();
        if (window != 0L && RDPMouseState.enabled) {
            RDPMouseCursor.releaseClip();
        }
    }

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void rdpmouse$onUpdateMouse(double timeDelta, CallbackInfo ci) {
        if (RDPMouseState.panDX != 0 || RDPMouseState.panDY != 0) {
            cursorDeltaX += RDPMouseState.panDX;
            cursorDeltaY += RDPMouseState.panDY;
            RDPMouseState.panDX = 0;
            RDPMouseState.panDY = 0;
        }
    }
}
