package io.github.hawah.structure_crafter.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public record StructureData(StructureTemplate structureTemplate, BlockPos center) {
}
