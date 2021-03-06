
package appeng.decorative;


import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import appeng.api.definitions.IDefinition;
import appeng.api.definitions.IDefinitions;
import appeng.api.module.Module;
import appeng.api.module.Module.ModuleEventHandler;
import appeng.api.module.ModuleIMCMessageEvent;
import appeng.core.AppEng;
import appeng.core.crafting.definitions.CraftingBlockDefinitions;
import appeng.core.crafting.definitions.CraftingItemDefinitions;
import appeng.core.crafting.definitions.CraftingTileDefinitions;
import appeng.core.lib.AEConfig;
import appeng.core.lib.bootstrap.FeatureFactory;
import appeng.decorative.api.IDecorative;


@Module( IDecorative.NAME )
@Mod( modid = AppEngDecorative.MODID, name = IDecorative.NAME, version = AEConfig.VERSION, dependencies = "required-after:" + AppEng.MODID, acceptedMinecraftVersions = ForgeVersion.mcVersion )
public class AppEngDecorative implements IDecorative
{

	public static final String MODID = AppEng.MODID + "|" + IDecorative.NAME;

	public static final String MODNAME = AppEng.NAME + " | " + IDecorative.NAME;

	private CraftingItemDefinitions itemDefinitions;
	private CraftingBlockDefinitions blockDefinitions;
	private CraftingTileDefinitions tileDefinitions;

	@Override
	public <T, D extends IDefinitions<T, ? extends IDefinition<T>>> D definitions( Class<T> clas )
	{
		if( clas == Item.class )
		{
			return (D) itemDefinitions;
		}
		if( clas == Block.class )
		{
			return (D) blockDefinitions;
		}
		if( clas == TileEntity.class )
		{
			return (D) tileDefinitions;
		}
		return null;
	}

	@ModuleEventHandler
	public void preInitAE( FMLPreInitializationEvent event )
	{
		FeatureFactory registry = new FeatureFactory();
		this.blockDefinitions = new CraftingBlockDefinitions( registry );
		this.itemDefinitions = new CraftingItemDefinitions( registry );
		this.tileDefinitions = new CraftingTileDefinitions( registry );
	}

	@EventHandler
	public void preInitForge( FMLPreInitializationEvent event )
	{

	}

	@ModuleEventHandler
	public void initAE( final FMLInitializationEvent event )
	{

	}

	@EventHandler
	public void initForge( final FMLInitializationEvent event )
	{

	}

	@ModuleEventHandler
	public void postInitAE( final FMLPostInitializationEvent event )
	{

	}

	@EventHandler
	public void postInitForge( final FMLPostInitializationEvent event )
	{

	}

	@ModuleEventHandler
	public void handleIMCEventAE( ModuleIMCMessageEvent event )
	{

	}

	@EventHandler
	public void handleIMCEventForge( IMCEvent event )
	{

	}

	@ModuleEventHandler
	public void serverAboutToStartAE( FMLServerAboutToStartEvent event )
	{

	}

	@EventHandler
	public void serverAboutToStartForge( FMLServerAboutToStartEvent event )
	{

	}

	@ModuleEventHandler
	public void serverStartingAE( FMLServerStartingEvent event )
	{

	}

	@EventHandler
	public void serverStartingForge( FMLServerStartingEvent event )
	{

	}

	@ModuleEventHandler
	public void serverStoppingAE( FMLServerStoppingEvent event )
	{

	}

	@EventHandler
	public void serverStoppingForge( FMLServerStoppingEvent event )
	{

	}

	@ModuleEventHandler
	public void serverStoppedAE( FMLServerStoppedEvent event )
	{

	}

	@EventHandler
	public void serverStoppedForge( FMLServerStoppedEvent event )
	{

	}

}
