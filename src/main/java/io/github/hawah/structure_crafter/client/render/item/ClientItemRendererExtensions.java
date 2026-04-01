package io.github.hawah.structure_crafter.client.render.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@MethodsReturnNonnullByDefault
public class ClientItemRendererExtensions implements IClientItemExtensions {

    protected final BlockEntityWithoutLevelRenderer renderer;

    public ClientItemRendererExtensions(BlockEntityWithoutLevelRenderer renderer) {
        this.renderer = renderer;
    }

    public static ClientItemRendererExtensions of(BlockEntityWithoutLevelRenderer renderer) {
        return new ClientItemRendererExtensions(renderer);
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return renderer;
    }
}
