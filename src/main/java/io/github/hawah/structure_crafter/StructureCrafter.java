package io.github.hawah.structure_crafter;

import com.mojang.logging.LogUtils;
import io.github.hawah.structure_crafter.block.BlockRegistry;
import io.github.hawah.structure_crafter.block.blockentity.BlockEntityRegistry;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.networking.NetworkPackets;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(StructureCrafter.MODID)
public class StructureCrafter {

    public static final String MODID = "structure_crafter";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STAMP_WEAVER_TAB = CREATIVE_MODE_TABS.register(
            "stamp_weaver_tab",
            () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.structure_crafter"))
                            .withTabsBefore(CreativeModeTabs.COMBAT)
                            .icon(() -> ItemRegistries.TELEPHONE_HANDSET.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                output.accept(ItemRegistries.STRUCTURE_WAND.get());
                                output.accept(ItemRegistries.BLACKBOARD.get());
                                output.accept(ItemRegistries.MATERIAL_LIST.get());
                                output.accept(ItemRegistries.TELEPHONE_BLOCK_ITEM.get());
                            })
                            .build()
    );

    public StructureCrafter(IEventBus modEventBus, ModContainer modContainer) {

        ItemRegistries.register(modEventBus);

        BlockRegistry.register(modEventBus);

        BlockEntityRegistry.register(modEventBus);

        DataComponentTypeRegistries.register(modEventBus);

        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        NetworkPackets.register();

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.CommonConfig.SPEC);
//        modContainer.registerConfig(ModConfig.Type.SERVER, Config.ServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.ClientConfig.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @EventBusSubscriber(modid = MODID)
    public static class ModEvents {
        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
//                event.accept(ItemRegistries.STRUCTURE_WAND.get());
//                event.accept(ItemRegistries.BLACKBOARD.get());
//                event.accept(ItemRegistries.MATERIAL_LIST.get());
//                event.accept(ItemRegistries.TELEPHONE_BLOCK_ITEM.get());
            }
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
