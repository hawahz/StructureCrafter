package io.github.hawah.structure_crafter.util;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.CompositeUnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelLoader;

public enum Models {
    BLACKBOARD("addition/blackboard_raw"),
    PHONE("addition/phone"),
    ;
    private final Identifier modelResource;
    Models(String path) {
        this.modelResource = Identifier.fromNamespaceAndPath(StructureCrafter.MODID, path);
    }

    public static void register(ModelEvent.RegisterLoaders event) {
        for (Models model : values()) {
            event.register(model.modelResource, CompositeUnbakedModel.Loader.INSTANCE);
        }
    }

    public StandaloneModelLoader.BakedModels getBakedModel() {
        return Minecraft.getInstance().getModelManager().getModel(this.modelResource);
    }

}
