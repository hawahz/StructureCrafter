package io.github.hawah.structure_crafter.util;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public enum Models {
    BLACKBOARD("addition/blackboard_raw"),
    PHONE("addition/phone"),
    ;
    private final StandaloneModelKey<QuadCollection> modelResource;
    private final Identifier modelPath;
    Models(String path) {
        this.modelResource = new StandaloneModelKey<>(
                () -> StructureCrafter.MODID + ":" + path
        );
        this.modelPath = Identifier.fromNamespaceAndPath(StructureCrafter.MODID, path);
    }

    public static void register(ModelEvent.RegisterStandalone event) {
        for (Models model : values()) {
            event.register(
                    model.modelResource,
                    SimpleUnbakedStandaloneModel.quadCollection(
                           model.modelPath
                    )
            );
        }
    }

    public QuadCollection getBakedModel() {
        return Minecraft.getInstance().getModelManager().getStandaloneModel(this.modelResource);
    }

}
