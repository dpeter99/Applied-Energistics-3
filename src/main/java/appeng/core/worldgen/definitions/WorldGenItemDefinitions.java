
package appeng.core.worldgen.definitions;


import appeng.api.definitions.IDefinition;
import net.minecraft.item.Item;

import appeng.api.definitions.IItemDefinition;
import appeng.core.lib.bootstrap.FeatureFactory;
import appeng.core.lib.definitions.Definitions;
import appeng.core.worldgen.api.definitions.IWorldGenItemDefinitions;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

import java.util.Map;


public class WorldGenItemDefinitions extends Definitions<Item, IItemDefinition<Item>> implements IWorldGenItemDefinitions
{

	public WorldGenItemDefinitions( FeatureFactory registry )
	{
		init( registry.buildDefaultItemBlocks() );
	}

}
