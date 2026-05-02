package io.github.hawah.structure_crafter.util;

import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public enum UploadValidation {
    ALL,
    MODERATORS,
    GAMEMASTERS,
    ADMINS,
    OWNERS,
    NO_ONE;

    private final Function<Player, Boolean> isValid;

    UploadValidation() {
        this.isValid = (player -> player.hasPermissions(ordinal()));
    }

    public boolean hasPermission(Player player) {
        return this.isValid.apply(player);
    }
}
