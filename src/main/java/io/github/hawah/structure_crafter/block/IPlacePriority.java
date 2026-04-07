package io.github.hawah.structure_crafter.block;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public interface IPlacePriority {
    boolean isPriority(PlayerInteractEvent.RightClickBlock event);
}
