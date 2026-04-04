package io.github.hawah.structure_crafter.item;

import io.github.hawah.structure_crafter.client.gui.MaterialListScreen;
import io.github.hawah.structure_crafter.client.gui.ScreenOpener;
import io.github.hawah.structure_crafter.data_component.DataComponentTypeRegistries;
import io.github.hawah.structure_crafter.data_component.MaterialListComponent;
import io.github.hawah.structure_crafter.networking.utils.Networking;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MaterialList extends Item {
    public MaterialList() {
        super(new Properties().component(DataComponentTypeRegistries.MATERIAL_LIST, MaterialListComponent.EMPTY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {

        ItemStack itemStack = player.getItemInHand(usedHand);
        MaterialListComponent component = itemStack.getOrDefault(DataComponentTypeRegistries.MATERIAL_LIST, MaterialListComponent.EMPTY);
        if (level.isClientSide) {
            ScreenOpener.open(new MaterialListScreen());
        }

        if (component.isEmpty()) {
            return super.use(level, player, usedHand);
        }


        return super.use(level, player, usedHand);
    }
}
