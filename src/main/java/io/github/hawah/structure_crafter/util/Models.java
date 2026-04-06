package io.github.hawah.structure_crafter.util;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

public enum Models {
    BLACKBOARD("addition/blackboard_raw"),
    ;
    private final ModelResourceLocation modelResource;
    Models(String path) {
        this.modelResource = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, path));
    }

    public static void register(ModelEvent.RegisterAdditional event) {
        for (Models model : values()) {
            event.register(model.modelResource);
        }
    }

    public BakedModel getBakedModel() {
        return Minecraft.getInstance().getModelManager().getModel(this.modelResource);
    }

}
