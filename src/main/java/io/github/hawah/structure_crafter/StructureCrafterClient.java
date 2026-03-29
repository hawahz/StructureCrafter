package io.github.hawah.structure_crafter;

import io.github.hawah.structure_crafter.item.blackboard.BlackboardHandler;
import io.github.hawah.structure_crafter.item.structure_wand.StructureWandHandler;

//@Mod(value = StampWeaver.MODID, dist = Dist.CLIENT)
public class StructureCrafterClient {
    public static final BlackboardHandler BLACKBOARD_HANDLER = new BlackboardHandler();
    public static final StructureWandHandler STRUCTURE_WAND_HANDLER = new StructureWandHandler();
    public static final double ANI_DELTA = 0.5;
    public static final float ANI_DELTAF = 0.5F;
}
