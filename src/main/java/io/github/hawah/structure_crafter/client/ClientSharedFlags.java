package io.github.hawah.structure_crafter.client;

import io.github.hawah.structure_crafter.networking.ServerboundSharedFlagUpdatePacket;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.intellij.lang.annotations.MagicConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientSharedFlags {
    public static final int CTRL = 1;
    public static final int ALT  = 1 << 1;
    private static boolean dirty = false;

    @MagicConstant(flags = {CTRL, ALT})
    public @interface Flags {}

    private static int sharedFlags = 0;

    public static void setSharedFlags(@ClientSharedFlags.Flags int type, boolean flag) {
        setSharedFlags(sharedFlags & ~type | (flag ? type : 0));
    }

    public static void setDirty() {
        dirty = true;
    }
    public static void setSharedFlags(int flag) {
        if (flag == sharedFlags) {
            return;
        }
        setDirty();
        sharedFlags = flag;
    }
    
    public static void synchronize() {
        if (!dirty) {
            return;
        }
        Networking.sendToServer(new ServerboundSharedFlagUpdatePacket(
                Minecraft.getInstance().player.getUUID(),
                sharedFlags
        ));
        dirty = false;
    }

    
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        assert mc.player != null;
        setSharedFlags(CTRL, Screen.hasControlDown());
        setSharedFlags(ALT, Screen.hasAltDown());
        if (enableSync()) {
            synchronize();
        }
    }

    
    private static final List<Supplier<Boolean>> syncValidators = new ArrayList<>();

    
    public static void registerSyncValidator(Supplier<Boolean> validator) {
        syncValidators.add(validator);
    }

    
    public static boolean enableSync() {
        return dirty && syncValidators.stream().anyMatch(Supplier::get);
    }
}
