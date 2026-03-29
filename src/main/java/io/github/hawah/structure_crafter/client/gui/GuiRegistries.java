package io.github.hawah.structure_crafter.client.gui;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class GuiRegistries {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(Registries.MENU, StructureCrafter.MODID);

}
