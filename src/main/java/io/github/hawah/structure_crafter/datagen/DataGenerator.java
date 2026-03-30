package io.github.hawah.structure_crafter.datagen;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.*;

@EventBusSubscriber(modid = StructureCrafter.MODID)
public class DataGenerator {
    @SubscribeEvent // on the mod event bus
    public static void gatherData(GatherDataEvent event) {

        // other providers here
//        addClient(event, StampWeaverItemModelProvider::new);
        addServer(event, ModRecipeGenerator::new);
        addClient(event, ModEnUsLangProvider::new);

    }



    public static void addClient(GatherDataEvent event, BiFunction<PackOutput, ExistingFileHelper, DataProvider> register) {
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(
                event.includeClient(),
                register.apply(output, existingFileHelper)
        );
    }

    public static void addClient(GatherDataEvent event, Function<PackOutput, DataProvider> register) {
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(
                event.includeClient(),
                register.apply(output)
        );
    }

    public static void addServer(GatherDataEvent event, BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, DataProvider> register) {
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(
                event.includeServer(),
                register.apply(output, event.getLookupProvider())
        );
    }
}
