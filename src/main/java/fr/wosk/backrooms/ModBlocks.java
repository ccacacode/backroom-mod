package fr.wosk.backrooms;

import fr.wosk.backrooms.block.GlitchBlock;
import fr.wosk.backrooms.block.GlitchBlockEntity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
	public static final Block GLITCH_BLOCK = Registry.register(
		BuiltInRegistries.BLOCK,
		id("glitch_block"),
		new GlitchBlock(BlockBehaviour.Properties.of()
			.strength(1.5f)
			.sound(SoundType.AMETHYST)
			.lightLevel(state -> 8))
	);

	public static final BlockEntityType<GlitchBlockEntity> GLITCH_BLOCK_ENTITY = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		id("glitch_block"),
		BlockEntityType.Builder.of(GlitchBlockEntity::new, GLITCH_BLOCK).build(null)
	);

	public static final Item GLITCH_BLOCK_ITEM = Registry.register(
		BuiltInRegistries.ITEM,
		id("glitch_block"),
		new BlockItem(GLITCH_BLOCK, new Item.Properties())
	);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(BackroomsMod.MOD_ID, path);
	}

	public static void register() {
		// Le bloc apparaît dans l'onglet créatif "Blocs fonctionnels"
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
			.register(entries -> entries.accept(GLITCH_BLOCK_ITEM));
	}
}
