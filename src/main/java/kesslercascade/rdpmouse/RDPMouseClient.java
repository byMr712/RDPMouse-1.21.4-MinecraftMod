package kesslercascade.rdpmouse;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class RDPMouseClient implements ClientModInitializer {

    public static final RDPMouseClient INSTANCE = new RDPMouseClient();

    public static final String RDPMOUSE_CATEGORY = "key.category.rdpmouse";

    public static final KeyBinding TOGGLE_KEY = new KeyBinding(
            "key.rdpmouse.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            RDPMOUSE_CATEGORY
    );

    public static final KeyBinding FREE_MOUSE_KEY = new KeyBinding(
            "key.rdpmouse.freemouse",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            RDPMOUSE_CATEGORY
    );

    public static final KeyBinding PAN_LEFT = new KeyBinding(
            "key.rdpmouse.pan_left",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT,
            RDPMOUSE_CATEGORY
    );

    public static final KeyBinding PAN_RIGHT = new KeyBinding(
            "key.rdpmouse.pan_right",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT,
            RDPMOUSE_CATEGORY
    );

    public static final KeyBinding PAN_UP = new KeyBinding(
            "key.rdpmouse.pan_up",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            RDPMOUSE_CATEGORY
    );

    public static final KeyBinding PAN_DOWN = new KeyBinding(
            "key.rdpmouse.pan_down",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            RDPMOUSE_CATEGORY
    );

    private boolean wasFreeMouse = false;
    private boolean releasedForFree = false;

    private int panLeftTicks, panRightTicks, panUpTicks, panDownTicks;

    public RDPMouseClient() {}

    @Override
    public void onInitializeClient() {
        RDPMouse.init();
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);
        KeyBindingHelper.registerKeyBinding(FREE_MOUSE_KEY);
        KeyBindingHelper.registerKeyBinding(PAN_LEFT);
        KeyBindingHelper.registerKeyBinding(PAN_RIGHT);
        KeyBindingHelper.registerKeyBinding(PAN_UP);
        KeyBindingHelper.registerKeyBinding(PAN_DOWN);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private static double panSpeed(int ticks) {
        return Math.min(2.0 + ticks * 0.5, 25.0);
    }

    public void onClientTick(MinecraftClient mc) {
        // Toggle
        while (TOGGLE_KEY.wasPressed()) {
            RDPMouseState.enabled = !RDPMouseState.enabled;
            RDPMouseState.reset();

            long window = mc.getWindow().getHandle();
            if (mc.mouse.isCursorLocked() && window != 0L) {
                if (RDPMouseState.enabled) {
                    if (InputUtil.isRawMouseMotionSupported()) {
                        GLFW.glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_FALSE);
                    }
                    GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
                    RDPMouseCursor.clipCursor(window);
                } else {
                    if (InputUtil.isRawMouseMotionSupported() && mc.options.getRawMouseInput().getValue()) {
                        GLFW.glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);
                    }
                    GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
                    RDPMouseCursor.releaseClip();
                }
            }

            if (mc.player != null) {
                String key = RDPMouseState.enabled ? "rdpmouse.status.on" : "rdpmouse.status.off";
                mc.player.sendMessage(Text.translatable(key), true);
            }
        }

        // Free mouse (hold to release grab)
        boolean holdingFree = FREE_MOUSE_KEY.isPressed();
        if (holdingFree && !wasFreeMouse) {
            if (mc.mouse.isCursorLocked()) {
                mc.mouse.unlockCursor();
                releasedForFree = true;
            }
        } else if (!holdingFree && wasFreeMouse && releasedForFree) {
            mc.mouse.lockCursor();
            releasedForFree = false;
        }
        wasFreeMouse = holdingFree;

        // Keyboard pan — divide by the vanilla sensitivity factor so pan speed is
        // independent of the mouse sensitivity setting, then apply 2.5x multiplier.
        double sens = mc.options.getMouseSensitivity().getValue();
        double d = sens * 0.6 + 0.2;
        double sensitivityFactor = d * d * d * 8.0;

        if (PAN_LEFT.isPressed()) {
            RDPMouseState.panDX -= panSpeed(panLeftTicks++) * 2.5 / sensitivityFactor;
        } else {
            panLeftTicks = 0;
        }
        if (PAN_RIGHT.isPressed()) {
            RDPMouseState.panDX += panSpeed(panRightTicks++) * 2.5 / sensitivityFactor;
        } else {
            panRightTicks = 0;
        }
        if (PAN_UP.isPressed()) {
            RDPMouseState.panDY -= panSpeed(panUpTicks++) * 2.5 / sensitivityFactor;
        } else {
            panUpTicks = 0;
        }
        if (PAN_DOWN.isPressed()) {
            RDPMouseState.panDY += panSpeed(panDownTicks++) * 2.5 / sensitivityFactor;
        } else {
            panDownTicks = 0;
        }
    }
}
