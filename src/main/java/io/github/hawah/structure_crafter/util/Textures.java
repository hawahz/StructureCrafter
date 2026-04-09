package io.github.hawah.structure_crafter.util;

import io.github.hawah.structure_crafter.StructureCrafter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public enum Textures {
    NAMETAG_BACKGROUND("textures/gui/nametag.png", 73, 32, 109, 32, 0),
    DELETE_NAMETAG("textures/gui/nametag.png", 96, 96, 15, 16, 2, 96, 112, 96, 128),
    CONFIRM_NAMETAG("textures/gui/nametag.png", 80, 96, 16, 14, 2, 80, 112, 80, 128),
    DISCARD_NAMETAG("textures/gui/nametag.png", 111, 96, 17, 16, 2, 111, 112, 111, 128),
    STRUCTURE_WAND("textures/gui/structure_wand.png", 0, 0, 80, 195, 0),
    KEYMAP("textures/gui/buttons.png", 0, 0, 16, 16, 5, 0, 16, 0, 32, 0, 48, 0, 64, 0, 80),
    FULL_RED("textures/gui/full_red.png", 0, 0, 16, 16, 1),
    ;
    private final ResourceLocation resource;
    private final int startX;
    private final int startY;
    private final int width;
    private final int height;
    private final int variantCounts;
    private final int[] variant;
    private final Builder builder;

    Textures(String path, int startX, int startY, int width, int height, int variantCounts, int... variant) {
        this.resource = ResourceLocation.fromNamespaceAndPath(StructureCrafter.MODID, path);
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.variantCounts = variantCounts;
        assert variant.length == variantCounts * 2;
        this.variant = variant;
        this.builder = new Builder(this);
    }

    public ResourceLocation getResource() {
        return resource;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Builder builder() {
        return builder.variant(0);
    }

    public static class Builder {
        private final Textures textures;
        private int variant = 0;

        public Builder(Textures textures) {
            this.textures = textures;
        }
        public Builder variant(int variant) {
            this.variant = Mth.clamp(variant, 0, textures.variantCounts);
            return this;
        }
        public <T extends Enum<T>> Builder variant(Enum<T> variant) {
            return this.variant(variant.ordinal());
        }
        public ResourceLocation getResource() {
            return textures.getResource();
        }

        public int getStartX() {
            return variant == 0? textures.getStartX() : textures.variant[(variant - 1) * 2];
        }

        public int getStartY() {
            return variant == 0? textures.getStartY() : textures.variant[(variant - 1) * 2 + 1];
        }

        public int getWidth() {
            return textures.getWidth();
        }

        public int getHeight() {
            return textures.getHeight();
        }

        public Builder reset() {
            variant = 0;
            return this;
        }
    }

    public enum Variants {
        NORMAL,
        HOVER,
        DISABLED;
    }
    public enum ToggleVariants {
        NORMAL,
        NORBAL_HOVER,
        TOGGLE,
        TOGGLE_HOVER;
    }
    public enum KeyVariants {
        CTRL,
        SHIFT,
        ALT,
        RIGHT,
        LEFT,
        SCROLL
    }
}
