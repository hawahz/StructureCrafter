package io.github.hawah.structure_crafter.client.render.item;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@MethodsReturnNonnullByDefault
public class ClientItemRendererExtensions implements IClientItemExtensions {

    protected final SpecialModelRenderer<?> renderer;

    public ClientItemRendererExtensions(SpecialModelRenderer<?> renderer) {
        this.renderer = renderer;
    }

    public static ClientItemRendererExtensions of(SpecialModelRenderer<?> renderer) {
        return new ClientItemRendererExtensions(renderer);
    }

//    @Override
//    public SpecialModelRenderer<?> getCustomRenderer() {
//        return renderer;
//    }



}
