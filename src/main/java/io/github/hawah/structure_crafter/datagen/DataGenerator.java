package io.github.hawah.structure_crafter.datagen;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
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
        addClient(event, ModBlockStateGenerator::new);
        addClient(event, ModItemModelProvider::new);
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        generator.addProvider(
                event.includeServer(),
                (DataProvider.Factory<LootTableProvider>) output -> new LootTableProvider(
                        output,
                        Set.of(),
                        List.of(new LootTableProvider.SubProviderEntry(
                                ModLootTablesProvider::new,
                                LootContextParamSets.BLOCK
                        )),
                        event.getLookupProvider()
                )
        );
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
