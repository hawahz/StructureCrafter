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

    SHIFT("tooltip_shift", "-[Shift]-", 0, ChatFormatting.DARK_GRAY , ChatFormatting.ITALIC),

    TOOLTIP_WAND_0("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "0", "[Right Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_1("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "1", "Place current structure", 0),
    TOOLTIP_WAND_2("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "2", "[Left Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_3("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "3", "Freeze/Unfreeze current structure", 0),
    TOOLTIP_WAND_4("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "4", "[Ctrl + Scroll]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_5("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "5", "Rotate structure around anchor", 0),
    TOOLTIP_WAND_6("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "6", "[Alt + Scroll]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_7("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "7", "Select structure in folder", 0),
    TOOLTIP_WAND_8("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "8", "[Shift + Left Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_9("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "9", "Clear Selection", 0),
    TOOLTIP_WAND_CONFIG("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "config", "-[Scroll] / [Right Click]-", 0, ChatFormatting.YELLOW, ChatFormatting.BOLD),

    TOOLTIP_BLACKBOARD_0("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "0", "[Right Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_BLACKBOARD_1("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "1", "Select the first/second point", 0),
    TOOLTIP_BLACKBOARD_2("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "2", "[Left Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_BLACKBOARD_3("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "3", "Select an Anchor", 0),
    TOOLTIP_BLACKBOARD_4("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "4", "[Ctrl + Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_BLACKBOARD_5("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "5", "Select a point in the air", 0),
    TOOLTIP_BLACKBOARD_6("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "6", "[Ctrl + Scroll]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_BLACKBOARD_7("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "7", "Change the range in the air", 0),
    TOOLTIP_BLACKBOARD_8("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "8", "[Right Click] when \nHold on Off Hand, and an Ink Sac on Main Hand", 0, ChatFormatting.AQUA, ChatFormatting.ITALIC),
    TOOLTIP_BLACKBOARD_9("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "9", "Save structure", 0),
    TOOLTIP_BLACKBOARD_10("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "10", "[Shift + Left Click] ", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_BLACKBOARD_11("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "11", "Clear selection", 0),

    HUD_BLACKBOARD_SELECTION("hud.blackboard_selection", "Size (%1$s, %2$s, %3$s) (%4$s)", 4),

    ERROR_AREA_TOO_LARGE("error.area_too_large","The area is too large!",0, ChatFormatting.RED),

    WARN_STRUCTURE_WAND_NOT_ENOUGH_ITEM("error.structure_wand_not_enough_item","Missing %1$s %2s(s)",2),
    WARN_STRUCTURE_WAND_NOT_ENOUGH_ITEM_TOO_LONG("error.structure_wand_not_enough_item_too_long","Insufficient number of items",0),

    CONFIG_STRUCTURE_WAND_UPDATE_FLAG("config.structure_wand_update_flag","-Update Flag- :%s",1),
    CONFIG_STRUCTURE_WAND_REPLACE_AIR("config.structure_wand_replace_air","-Replace Air- :%s",1),
    CONFIG_STRUCTURE_WAND_RENDER_BOUND("config.structure_wand_render_bound","-Render Bound- :%s",1),
    CONFIG_STRUCTURE_WAND_UPDATE_ALL("config.structure_wand_update_all","-Update All-",0),
    CONFIG_STRUCTURE_WAND_NO_UPDATE("config.structure_wand_no_update","-No Update-",0),
    CONFIG_STRUCTURE_WAND_CLEAR_AREA("config.structure_wand_clear_area","-Clear Area-",0),
    CONFIG_STRUCTURE_WAND_KEEP_AREA("config.structure_wand_keep_area","-Keep Area-",0),
    CONFIG_STRUCTURE_WAND_RENDER_BOUNDS("config.structure_wand_render_bounds","-Solid Bounds-",0),
    CONFIG_STRUCTURE_WAND_RENDER_NONE("config.structure_wand_render_none","-Boundless-",0),

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
