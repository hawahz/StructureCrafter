package io.github.hawah.structure_crafter.util;

import net.minecraft.world.entity.player.Player;
import org.intellij.lang.annotations.MagicConstant;

import java.util.*;

public class SharedFlags {
    public static final int CTRL = 1;
    public static final int ALT  = 1 << 1;


    @MagicConstant(flags = {CTRL, ALT})
    public @interface Flags {}

    private static final Map<UUID, Integer> sharedFlags = new HashMap<>();

    public static void setSharedFlags(@Flags int type, boolean flag, Player player) {
        sharedFlags.put(player.getUUID(), getSharedFlags(player) & ~type | (flag ? type : 0));
    }

    public static void setSharedFlags(UUID uid, int flag) {
        sharedFlags.put(uid, flag);
    }

    public static int getSharedFlags(Player player) {
        return getSharedFlags(player.getUUID());
    }

    public static int getSharedFlags(UUID playerUUID) {
        return sharedFlags.computeIfAbsent(playerUUID, uuid -> 0);
    }

    public static boolean hasShiftDown(Player player) {
        return (getSharedFlags(player) & CTRL) != 0;
    }
}
