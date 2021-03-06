
package appeng.core;


import java.io.File;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IDefinition;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.module.Module;
import appeng.api.module.Module.ModuleEventHandler;
import appeng.core.api.ICore;
import appeng.core.definitions.CoreBlockDefinitions;
import appeng.core.definitions.CoreItemDefinitions;
import appeng.core.definitions.CoreTileDefinitions;
import appeng.core.hooks.TickHandler;
import appeng.core.lib.AELog;
import appeng.core.lib.bootstrap.FeatureFactory;
import appeng.core.lib.sync.GuiBridge;
import appeng.core.lib.sync.network.NetworkHandler;
import appeng.core.lib.worlddata.WorldData;
import appeng.core.recipes.CustomRecipeConfig;
import appeng.core.recipes.CustomRecipeForgeConfiguration;
import appeng.core.server.AECommand;
import appeng.core.services.VersionChecker;
import appeng.core.services.export.ExportConfig;
import appeng.core.services.export.ExportProcess;
import appeng.core.services.export.ForgeExportConfig;
import appeng.core.services.version.VersionCheckerConfig;


/*
 * TODO 1.10.2-MODUSEP - Dat giant mess though. Move all stuff that belongs to specific modules into these specific modules. Yes, you can boom the API.
 */
@Module( value = ICore.NAME, dependencies = "hard-before:module-*" )
public class AppEngCore implements ICore
{

	@SidedProxy( clientSide = "appeng.core.client.AppEngCoreClientProxy", serverSide = "appeng.core.server.AppEngCoreServerProxy" )
	private static AppEngCoreProxy proxy;

	private final Registration registration;

	private CustomRecipeConfig customRecipeConfig;

	/**
	 * Folder for recipes
	 *
	 * used for CSV item names and the recipes
	 */
	private File recipeDirectory;

	/**
	 * determined in pre-init but used in init
	 */
	private ExportConfig exportConfig;

	private CoreItemDefinitions itemDefinitions;
	private CoreBlockDefinitions blockDefinitions;
	private CoreTileDefinitions tileDefinitions;

	public AppEngCore()
	{
		this.registration = new Registration();
	}

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

	@Nonnull
	public final Registration getRegistration()
	{
		return this.registration;
	}

	@ModuleEventHandler
	public void preInit( FMLPreInitializationEvent event )
	{
		FeatureFactory registry = new FeatureFactory();
		this.blockDefinitions = new CoreBlockDefinitions( registry );
		this.itemDefinitions = new CoreItemDefinitions( registry );
		this.tileDefinitions = new CoreTileDefinitions( registry );

		this.recipeDirectory = new File( AppEng.instance().getConfigDirectory(), "recipes" );

		final File versionFile = new File( AppEng.instance().getConfigDirectory(), "VersionChecker.cfg" );
		final File recipeFile = new File( AppEng.instance().getConfigDirectory(), "CustomRecipes.cfg" );
		final Configuration recipeConfiguration = new Configuration( recipeFile );

		final VersionCheckerConfig versionCheckerConfig = new VersionCheckerConfig( versionFile );
		this.customRecipeConfig = new CustomRecipeForgeConfiguration( recipeConfiguration );
		this.exportConfig = new ForgeExportConfig( recipeConfiguration );

		CreativeTab.init();

		this.registration.preInitialize( event );

		proxy.preInit( event );

		if( versionCheckerConfig.isVersionCheckingEnabled() )
		{
			final VersionChecker versionChecker = new VersionChecker( versionCheckerConfig );
			final Thread versionCheckerThread = new Thread( versionChecker );

			this.startService( "AE2 VersionChecker", versionCheckerThread );
		}
		
		/*
		 * ###################################
		 * TEST CODE
		 * WITH ANY TYPE ARGS CHANGES TO DEFINITIONS, SHOULD COMPILE WITHOUT PROBLEMS
		 */
		IItemDefinition<Item> quartz = itemDefinitions.get( "quartz" );
		Class<? extends TileEntity> tile = definitions( TileEntity.class.getClass() ).get( "grinder" ).maybe().get();
		IDefinitions<Block, IBlockDefinition<Block>> bdefs = definitions( Block.class );
		IBlockDefinition<Block> chargerDef = bdefs.get( "charger" );
		chargerDef = (IBlockDefinition<Block>) definitions( Block.class ).get( "charger" );
		/*
		 * ###################################
		 */
	}

	private void startService( final String serviceName, final Thread thread )
	{
		thread.setName( serviceName );
		thread.setPriority( Thread.MIN_PRIORITY );

		AELog.info( "Starting " + serviceName );
		thread.start();
	}

	@ModuleEventHandler
	public void init( FMLInitializationEvent event )
	{
		if( this.exportConfig.isExportingItemNamesEnabled() )
		{
			final ExportProcess process = new ExportProcess( this.recipeDirectory, this.exportConfig );
			final Thread exportProcessThread = new Thread( process );

			this.startService( "AE2 CSV Export", exportProcessThread );
		}

		this.registration.initialize( event, this.recipeDirectory, this.customRecipeConfig );

		proxy.init( event );
	}

	@ModuleEventHandler
	public void postInit( FMLPostInitializationEvent event )
	{
		this.registration.postInit( event );

		proxy.postInit( event );

		NetworkRegistry.INSTANCE.registerGuiHandler( this, GuiBridge.GUI_Handler );
		NetworkHandler.instance = new NetworkHandler( "AE2" );
	}

	@ModuleEventHandler
	public void handleIMCEvent( IMCEvent event )
	{
		final IMCHandler imcHandler = new IMCHandler();

		imcHandler.handleIMCEvent( event );
	}

	@ModuleEventHandler
	public void serverAboutToStart( FMLServerAboutToStartEvent event )
	{
		WorldData.onServerAboutToStart( event.getServer() );
	}

	@ModuleEventHandler
	public void serverStarting( FMLServerStartingEvent event )
	{
		event.registerServerCommand( new AECommand( event.getServer() ) );
	}

	@ModuleEventHandler
	public void serverStopping( FMLServerStoppingEvent event )
	{
		WorldData.instance().onServerStopping();
	}

	@ModuleEventHandler
	public void serverStopped( FMLServerStoppedEvent event )
	{
		WorldData.instance().onServerStoppped();
		TickHandler.INSTANCE.shutdown();
	}

}
