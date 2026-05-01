package io.github.hawah.structure_crafter.client.wand_modifier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ChainedAnchorModifier extends AnchorModifier{
    @Override
    public void onPlace(BlockPos placeAt, Direction direction) {
        anchor = placeAt;
    }
}
