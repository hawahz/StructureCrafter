package io.github.hawah.structure_crafter;

import io.github.hawah.structure_crafter.client.gui.KeyTipHUD;
import io.github.hawah.structure_crafter.client.handler.BlackboardHandler;
import io.github.hawah.structure_crafter.client.handler.RulerHandler;
import io.github.hawah.structure_crafter.client.handler.StructureWandHandler;
import io.github.hawah.structure_crafter.client.render.TelephoneWireRenderer;
import io.github.hawah.structure_crafter.client.utils.TimerWarper;

//@Mod(value = StampWeaver.MODID, dist = Dist.CLIENT)
public class StructureCrafterClient {
    public static final BlackboardHandler BLACKBOARD_HANDLER = new BlackboardHandler();
    public static final StructureWandHandler STRUCTURE_WAND_HANDLER = new StructureWandHandler();
    public static final RulerHandler RULER_HANDLER = new RulerHandler();
    public static final KeyTipHUD KEY_TIP_HUD = new KeyTipHUD();
    public static final TimerWarper TIMER_NORMAL = new TimerWarper();
    public static final TelephoneWireRenderer TELEPHONE_WIRE_RENDERER = new TelephoneWireRenderer();
    public static final double ANI_DELTA = 0.5;
    public static final float ANI_DELTAF = 0.5F;
}
