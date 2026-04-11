package io.github.hawah.structure_crafter.datagen;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.function.*;

//@EventBusSubscriber(modid = StructureCrafter.MODID)
public class DataGenerator {
//    @SubscribeEvent // on the mod event bus
    public static void gatherData(GatherDataEvent event) {

        // other providers here
//        addClient(event, StampWeaverItemModelProvider::new);
//        event.createProvider(ModRecipeGenerator.Runner::new);
//        addClient(event, ModEnUsLangProvider::new);
//        addClient(event, ModItemModelProvider::new);
    }



    public static void addClient(GatherDataEvent event, Function<PackOutput, DataProvider> register) {
        net.minecraft.data.DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(
                event.includeDev(),
                register.apply(output)
        );
    }
}
