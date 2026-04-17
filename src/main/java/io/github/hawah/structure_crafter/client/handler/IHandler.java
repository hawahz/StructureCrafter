package io.github.hawah.structure_crafter.client.handler;

import net.minecraft.client.Minecraft;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface IHandler {
    void tick();

    /**
     * Checks if the handler is active
     */
    boolean isActive();

    /**
     * Checks if the handler is visible. Mostly same to isActive.
     * <br></br>
     * Only Handlers that visibility and activity are separated should overwrite this.
     */
    default boolean isVisible() {
        return isActive();
    }

    default boolean isPresent() {
        return Minecraft.getInstance().level != null
                /*&& Minecraft.getInstance().screen == null*/ && Minecraft.getInstance().player != null;
    }
}
