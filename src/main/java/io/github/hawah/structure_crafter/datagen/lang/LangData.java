package io.github.hawah.structure_crafter.datagen.lang;

import io.github.hawah.structure_crafter.StructureCrafter;
import io.github.hawah.structure_crafter.item.ItemRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("unused")
public enum LangData {
    TITLE_BLACKBOARD_NAMETAG("title.blackboard_name_tag", "You decide to name your structure as...", 0),

    INFO_NO_SELECTION("client_message.info_no_selection","No Selection",0),
    INFO_ALARM_NO_NAME("client_message.info_alarm_no_name","You must name your structure!",0),
    INFO_WAND_LOCKED("client_message.info_wand_locked","You need [Left Click] to unlock your wand",0),
    INFO_NO_ANCHOR("client_message.info_no_anchor","No anchor selected",0),
    INFO_CREATE_FILE_SUCCESS("client_message.info_create_file_success","Saved as %1$s",1),
    INFO_CONTAINER_BUILD_CAPABILITY("client_message.info_container_build_capability","Items in the container can build up to %s structures.",1),
    INFO_CONTAINER_BUILD_CAPABILITY_WITH_INVENTORY("client_message.info_container_build_capability_with_inventory","Items in the container plus your inventory can build up to %s structures.",1),

    SHIFT("tooltip_shift", "-[Shift]-", 0, ChatFormatting.DARK_GRAY , ChatFormatting.ITALIC),

    TOOLTIP_WAND_0("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "0", "[Right Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_1("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "1", "Place", 0),
    TOOLTIP_WAND_2("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "2", "[Left Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_3("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "3", "Freeze/Unfreeze", 0),
    TOOLTIP_WAND_4("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "4", "[Ctrl + Scroll]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_5("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "5", "Rotate", 0),
    TOOLTIP_WAND_6("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "6", "[Alt + Scroll]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_7("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "7", "Switch Structure", 0),
    TOOLTIP_WAND_8("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "8", "[Shift + Left Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_9("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "9", "Clear Selection", 0),
    TOOLTIP_WAND_10("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "10", "[Shift + Right Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_WAND_11("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "11", "Open Settings", 0),
    TOOLTIP_WAND_CONFIG("tooltip." + ItemRegistries.STRUCTURE_WAND.getRegisteredName() + "config", "-[Scroll] / [Right Click]-", 0, ChatFormatting.YELLOW, ChatFormatting.BOLD),

    TOOLTIP_BLACKBOARD_0("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "0", "[Right Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_BLACKBOARD_1("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "1", "Select the first/second point", 0),
    TOOLTIP_BLACKBOARD_2("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "2", "[Left Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_BLACKBOARD_3("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "3", "Set Anchor", 0),
    TOOLTIP_BLACKBOARD_4("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "4", "[Ctrl + Click]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_BLACKBOARD_5("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "5", "Select Position in Air", 0),
    TOOLTIP_BLACKBOARD_6("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "6", "[Ctrl + Scroll]", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_BLACKBOARD_7("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "7", "Adjust Air Selection Range", 0),
    TOOLTIP_BLACKBOARD_8("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "8", "[Right Click] with \nBlackboard in Offhand and Ink Sac in Main Hand", 0, ChatFormatting.AQUA, ChatFormatting.ITALIC),
    TOOLTIP_BLACKBOARD_9("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "9", "Save Structure", 0),
    TOOLTIP_BLACKBOARD_10("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "10", "[Shift + Left Click] ", 0, ChatFormatting.AQUA, ChatFormatting.BOLD),
    TOOLTIP_BLACKBOARD_11("tooltip." + ItemRegistries.BLACKBOARD.getRegisteredName() + "11", "Clear Selection", 0),

    TOOLTIP_TELEPHONE_HANDSET("tooltip." + ItemRegistries.TELEPHONE_HANDSET.getRegisteredName() + "0", "Binding (%s, %s, %s)", 3, ChatFormatting.GRAY, ChatFormatting.BOLD),

    TOOLTIP_BUTTON_OPEN_FOLDER("tooltip.button.open_folder", "Open Folder", 0),
    TOOLTIP_BUTTON_REFRESH("tooltip.button.refresh", "Refresh", 0),
    TOOLTIP_BUTTON_DELETE("tooltip.button.delete", "Discard Temporary", 0),
    TOOLTIP_BUTTON_UPDATE("tooltip.button.update", "Update All", 0),
    TOOLTIP_BUTTON_NO_UPDATE("tooltip.button.no_update", "Disable Updates", 0),
    TOOLTIP_BUTTON_REPLACE("tooltip.button.replace", "Replace", 0),
    TOOLTIP_BUTTON_PADDING("tooltip.button.padding", "Padding", 0),
    TOOLTIP_BUTTON_BOUNDS_VISIBLE("tooltip.button.bounds_visible", "Show Bounds", 0),
    TOOLTIP_BUTTON_BOUNDS_HIDDEN("tooltip.button.bounds_hidden", "Hide Bounds", 0),
    TOOLTIP_BUTTON_LOCK("tooltip.button.lock", "Lock", 0),
    TOOLTIP_BUTTON_UNLOCK("tooltip.button.unlock", "Unlock", 0),
    TOOLTIP_BUTTON_ROTATE_LOCK("tooltip.button.rotate_lock", "Lock Rotation", 0),
    TOOLTIP_BUTTON_ROTATE_UNLOCK("tooltip.button.rotate_unlock", "Unlock Rotation", 0),
    TOOLTIP_BUTTON_MATERIAL("tooltip.button.material", "Material", 0),
    TOOLTIP_BUTTON_PREVIEW("tooltip.button.preview", "Statistics", 0),
    TOOLTIP_BUTTON_CLIP("tooltip.button.clip", "Clip", 0),

    HUD_TIP_BLACKBOARD_SELECT_FIRST_POINT("hud.blackboard_select_first_point", "Select the first point", 0),
    HUD_TIP_BLACKBOARD_SELECT_SECOND_POINT("hud.blackboard_select_second_point","Select the second point", 0),
    HUD_TIP_BLACKBOARD_CLEAR_AND_SELECT_FIRST("hud.blackboard_clear_and_select_first","Clear and Select First Point", 0),
    HUD_TIP_BLACKBOARD_SELECT_ANCHOR("hud.blackboard_select_anchor","Set Anchor", 0),
    HUD_TIP_BLACKBOARD_DELETE_ALL("hud.blackboard_delete_all","Delete All", 0),
    HUD_TIP_BLACKBOARD_DELETE_ANCHOR("hud.blackboard_delete_anchor","Delete Anchor", 0),
    HUD_TIP_BLACKBOARD_SHOW_ALL_FACES("hud.blackboard_show_all_faces","Show All Faces", 0),
    HUD_TIP_BLACKBOARD_PICK_AIR_CENTER("hud.blackboard_pick_air_center","Pick Air Center", 0),
    HUD_TIP_BLACKBOARD_PICK_AIR_POINT("hud.blackboard_pick_air_point","Pick Air Point", 0),
    HUD_TIP_BLACKBOARD_CHANGE_DISTANCE("hud.blackboard_change_distance","Change Reach Distance", 0),
    HUD_TIP_BLACKBOARD_SELECT_OPPOSITE_FACE("hud.blackboard_select_opposite_face","Select Opposite Face", 0),
    HUD_TIP_BLACKBOARD_PUSH_OR_PULL_FACE("hud.blackboard_push_or_pull_face","Push/Pull Face", 0),

    HUD_TIP_STRUCTURE_WAND_PLACE("hud.structure_wand_place","Place Structure", 0),
    HUD_TIP_STRUCTURE_WAND_OPENC_ONFIG("hud.structure_wand_open_config","Open Settings", 0),
    HUD_TIP_STRUCTURE_WAND_LOCK_UNLOCK("hud.structure_lock_unlock","Lock/Unlock", 0),

    GUI_PAGE_TOTAL_BUILD("gui.page_total_build","You can build up to %s of this structure.",1),
    GUI_PAGE_BOTTLENECK_MATERIAL("gui.page_bottleneck_material","The current bottleneck material is:",0),

    HUD_BLACKBOARD_SELECTION("hud.blackboard_selection", "Size (%1$s, %2$s, %3$s) (%4$s)", 4),

    ERROR_AREA_TOO_LARGE("error.area_too_large","Selection is too large!",0, ChatFormatting.RED),

    WARN_STRUCTURE_WAND_NOT_ENOUGH_ITEM("error.structure_wand_not_enough_item","Missing %1$s %2$s(s)",2),
    WARN_STRUCTURE_WAND_NOT_ENOUGH_ITEM_TOO_LONG("error.structure_wand_not_enough_item_too_long","Not enough itemIds",0),

    CONFIG_STRUCTURE_WAND_UPDATE_FLAG("config.structure_wand_update_flag","-Update Mode- :%s",1),
    CONFIG_STRUCTURE_WAND_REPLACE_AIR("config.structure_wand_replace_air","-Replace Air- :%s",1),
    CONFIG_STRUCTURE_WAND_RENDER_BOUND("config.structure_wand_render_bound","-Render Bound- :%s",1),
    CONFIG_STRUCTURE_WAND_UPDATE_ALL("config.structure_wand_update_all","-Update All-",0),
    CONFIG_STRUCTURE_WAND_NO_UPDATE("config.structure_wand_no_update","-Disable Updates-",0),
    CONFIG_STRUCTURE_WAND_CLEAR_AREA("config.structure_wand_clear_area","-Clear Area-",0),
    CONFIG_STRUCTURE_WAND_KEEP_AREA("config.structure_wand_keep_area","-Keep Area-",0),
    CONFIG_STRUCTURE_WAND_RENDER_BOUNDS("config.structure_wand_render_bounds","-Show Bounds-",0),
    CONFIG_STRUCTURE_WAND_RENDER_NONE("config.structure_wand_render_none","-Hide Bounds-",0),

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
