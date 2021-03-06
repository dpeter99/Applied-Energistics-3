
package appeng.core.spatial.definitions;


import appeng.core.AppEng;
import net.minecraft.tileentity.TileEntity;

import appeng.api.definitions.ITileDefinition;
import appeng.core.lib.bootstrap.FeatureFactory;
import appeng.core.lib.definitions.Definitions;
import appeng.core.lib.features.AEFeature;
import appeng.core.spatial.api.definitions.ISpatialTileDefinitions;
import appeng.core.spatial.block.BlockSpatialIOPort;
import appeng.core.spatial.block.BlockSpatialPylon;
import appeng.core.spatial.tile.TileSpatialIOPort;
import appeng.core.spatial.tile.TileSpatialPylon;
import net.minecraft.util.ResourceLocation;


public class SpatialTileDefinitions extends Definitions<Class<TileEntity>, ITileDefinition<TileEntity>> implements ISpatialTileDefinitions
{

	private final ITileDefinition spatialPylon;
	private final ITileDefinition spatialIOPort;

	public SpatialTileDefinitions( FeatureFactory registry )
	{
		this.spatialPylon = registry.tile( new ResourceLocation( AppEng.MODID, "spatial_pylon" ), TileSpatialPylon.class ).features( AEFeature.SpatialIO ).build();
		this.spatialIOPort = registry.tile( new ResourceLocation( AppEng.MODID, "spatial_ioport" ), TileSpatialIOPort.class ).features( AEFeature.SpatialIO ).build();
		
		init();
	}

}
