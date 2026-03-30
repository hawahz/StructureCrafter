package io.github.hawah.structure_crafter.datagen.lang;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.List;

public enum LangData {
    TITLE_BLACKBOARD_NAMETAG("title.blackboard_name_tag", "You decide to name your structure as...", 0),

    INFO_NO_SELECTION("client_message.info_no_selection","No Selection",0),
    INFO_ALARM_NO_NAME("client_message.info_alarm_no_name","You need to name your structure!",0),
    INFO_WAND_LOCKED("client_message.info_wand_locked","You need [Left Click] to unlock your wand",0),
    INFO_NO_ANCHOR("client_message.info_no_anchor","You're still thinking what's the center of this structure...",0),
    INFO_CREATE_FILE_SUCCESS("client_message.info_create_file_success","Saved as %1$s",1),

    SHIFT("tooltip_shift", "Press [Shift]", 0, ChatFormatting.DARK_GRAY , ChatFormatting.ITALIC),

    TOOLTIP_WAND_0("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "0", "[Right Click]", 0, ChatFormatting.AQUA, ChatFormatting.ITALIC),
    TOOLTIP_WAND_1("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "1", "Place current structure", 0),
    TOOLTIP_WAND_2("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "2", "[Left Click]", 0, ChatFormatting.AQUA, ChatFormatting.ITALIC),
    TOOLTIP_WAND_3("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "3", "Freeze/Unfreeze current structure", 0),
    TOOLTIP_WAND_4("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "4", "[Ctrl + Scroll]", 0, ChatFormatting.AQUA, ChatFormatting.ITALIC),
    TOOLTIP_WAND_5("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "5", "Rotate structure around anchor", 0),
    TOOLTIP_WAND_6("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "6", "[Alt + Scroll]", 0, ChatFormatting.AQUA, ChatFormatting.ITALIC),
    TOOLTIP_WAND_7("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "7", "Select structure in folder", 0),
    ;

    public final String key;
    public final String def;
    private final int arg;
    private final ChatFormatting[] format;

    LangData(String key, String def, int arg, @Nullable ChatFormatting... format) {
        this.key = StructureCrafter.MODID + "." + key;
        this.def = def;
        this.arg = arg;
        this.format = format;
    }

    private static List<LangData> getTitleLang(){
        return List.of(LangData.values());
    }

    public MutableComponent get(Object... args) {
        if (args.length != arg) {
            throw new IllegalArgumentException("for " + name() + ": expect " + arg + " parameters, got " + args.length);
        }
        MutableComponent ans = Component.translatable(key, args);
        if (format != null) {
            return ans.withStyle(format);
        }
        return ans;
    }

    public static MutableComponent getFromTag(String tag) {
        List<LangData> titleLang = getTitleLang();
        for (LangData data : titleLang){
            if (data.key.equals(StructureCrafter.MODID + ".tooltip."+tag)){
                MutableComponent ans = Component.translatable(data.key);
                if (data.format != null) {
                    return ans.withStyle(data.format);
                }
                return ans;
            }
        }
        return Component.literal("Error").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.ITALIC);
    }

}
