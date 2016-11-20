/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.me.tile;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import appeng.core.api.config.SecurityPermissions;
import appeng.core.api.config.Settings;
import appeng.core.api.config.SortDir;
import appeng.core.api.config.SortOrder;
import appeng.core.api.config.ViewItems;
import appeng.core.api.events.LocatableEventAnnounce;
import appeng.core.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.core.api.features.ILocatable;
import appeng.core.api.implementations.items.IBiometricCard;
import appeng.core.api.implementations.tiles.IColorableTile;
import appeng.core.api.util.AECableType;
import appeng.core.api.util.AEColor;
import appeng.core.api.util.AEPartLocation;
import appeng.core.api.util.DimensionalCoord;
import appeng.core.api.util.IConfigManager;
import appeng.core.lib.AppEngApi;
import appeng.core.lib.features.registries.PlayerRegistry;
import appeng.core.lib.helpers.PlayerSecurityWrapper;
import appeng.core.lib.tile.TileEvent;
import appeng.core.lib.tile.events.TileEventType;
import appeng.core.lib.tile.inventory.AppEngInternalInventory;
import appeng.core.lib.tile.inventory.IAEAppEngInventory;
import appeng.core.lib.tile.inventory.InvOperation;
import appeng.core.lib.util.ConfigManager;
import appeng.core.lib.util.IConfigManagerHost;
import appeng.core.lib.util.Platform;
import appeng.core.lib.util.item.AEItemStack;
import appeng.core.me.api.networking.GridFlags;
import appeng.core.me.api.networking.events.MENetworkChannelsChanged;
import appeng.core.me.api.networking.events.MENetworkEventSubscribe;
import appeng.core.me.api.networking.events.MENetworkPowerStatusChange;
import appeng.core.me.api.networking.events.MENetworkSecurityChange;
import appeng.core.me.api.networking.security.ISecurityProvider;
import appeng.core.me.api.storage.IMEInventoryHandler;
import appeng.core.me.api.storage.IMEMonitor;
import appeng.core.me.api.storage.ITerminalHost;
import appeng.core.me.api.storage.MEMonitorHandler;
import appeng.core.me.api.storage.data.IAEFluidStack;
import appeng.core.me.api.storage.data.IAEItemStack;
import appeng.core.me.grid.GridAccessException;
import appeng.core.me.grid.storage.SecurityInventory;


public class TileSecurity extends AENetworkTile implements ITerminalHost, IAEAppEngInventory, ILocatable, IConfigManagerHost, ISecurityProvider, IColorableTile
{

	private static int difference = 0;
	private final AppEngInternalInventory configSlot = new AppEngInternalInventory( this, 1 );
	private final IConfigManager cm = new ConfigManager( this );
	private final SecurityInventory inventory = new SecurityInventory( this );
	private final MEMonitorHandler<IAEItemStack> securityMonitor = new MEMonitorHandler<IAEItemStack>( this.inventory );
	private long securityKey;
	private AEColor paintedColor = AEColor.Transparent;
	private boolean isActive = false;

	public TileSecurity()
	{
		this.getProxy().setFlags( GridFlags.REQUIRE_CHANNEL );
		this.getProxy().setIdlePowerUsage( 2.0 );
		difference++;

		this.securityKey = System.currentTimeMillis() * 10 + difference;
		if( difference > 10 )
		{
			difference = 0;
		}

		this.cm.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		this.cm.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		this.cm.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{

	}

	@Override
	public void getDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{
		if( !this.getConfigSlot().isEmpty() )
		{
			drops.add( this.getConfigSlot().getStackInSlot( 0 ) );
		}

		for( final IAEItemStack ais : this.inventory.getStoredItems() )
		{
			drops.add( ais.getItemStack() );
		}
	}

	IMEInventoryHandler<IAEItemStack> getSecurityInventory()
	{
		return this.inventory;
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileSecurity( final ByteBuf data )
	{
		final boolean wasActive = this.isActive;
		this.isActive = data.readBoolean();

		final AEColor oldPaintedColor = this.paintedColor;
		this.paintedColor = AEColor.values()[data.readByte()];

		return oldPaintedColor != this.paintedColor || wasActive != this.isActive;
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileSecurity( final ByteBuf data )
	{
		data.writeBoolean( this.getProxy().isActive() );
		data.writeByte( this.paintedColor.ordinal() );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileSecurity( final NBTTagCompound data )
	{
		this.cm.writeToNBT( data );
		data.setByte( "paintedColor", (byte) this.paintedColor.ordinal() );

		data.setLong( "securityKey", this.securityKey );
		this.getConfigSlot().writeToNBT( data, "config" );

		final NBTTagCompound storedItems = new NBTTagCompound();

		int offset = 0;
		for( final IAEItemStack ais : this.inventory.getStoredItems() )
		{
			final NBTTagCompound it = new NBTTagCompound();
			ais.getItemStack().writeToNBT( it );
			storedItems.setTag( String.valueOf( offset ), it );
			offset++;
		}

		data.setTag( "storedItems", storedItems );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileSecurity( final NBTTagCompound data )
	{
		this.cm.readFromNBT( data );
		if( data.hasKey( "paintedColor" ) )
		{
			this.paintedColor = AEColor.values()[data.getByte( "paintedColor" )];
		}

		this.securityKey = data.getLong( "securityKey" );
		this.getConfigSlot().readFromNBT( data, "config" );

		final NBTTagCompound storedItems = data.getCompoundTag( "storedItems" );
		for( final Object key : storedItems.getKeySet() )
		{
			final NBTBase obj = storedItems.getTag( (String) key );
			if( obj instanceof NBTTagCompound )
			{
				this.inventory.getStoredItems().add( AEItemStack.create( new ItemStack( (NBTTagCompound) obj ) ) );
			}
		}
	}

	public void inventoryChanged()
	{
		try
		{
			this.saveChanges();
			this.getProxy().getGrid().postEvent( new MENetworkSecurityChange() );
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	@MENetworkEventSubscribe
	public void bootUpdate( final MENetworkChannelsChanged changed )
	{
		this.markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerUpdate( final MENetworkPowerStatusChange changed )
	{
		this.markForUpdate();
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.SMART;
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );
		this.isActive = false;
	}

	@Override
	public void onReady()
	{
		super.onReady();
		if( Platform.isServer() )
		{
			this.isActive = true;
			MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Register ) );
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );
		this.isActive = false;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	public boolean isActive()
	{
		return this.isActive;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return this.securityMonitor;
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return null;
	}

	@Override
	public long getLocatableSerial()
	{
		return this.securityKey;
	}

	public boolean isPowered()
	{
		return this.getProxy().isActive();
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.cm;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{

	}

	@Override
	public long getSecurityKey()
	{
		return this.securityKey;
	}

	@Override
	public void readPermissions( final HashMap<Integer, EnumSet<SecurityPermissions>> playerPerms )
	{
		final PlayerRegistry pr = AppEngApi.internalApi().registries().players();

		// read permissions
		for( final IAEItemStack ais : this.inventory.getStoredItems() )
		{
			final ItemStack is = ais.getItemStack();
			final Item i = is.getItem();
			if( i instanceof IBiometricCard )
			{
				final IBiometricCard bc = (IBiometricCard) i;
				bc.registerPermissions( new PlayerSecurityWrapper( playerPerms ), pr, is );
			}
		}

		// make sure thea admin is Boss.
		playerPerms.put( this.getProxy().getNode().getPlayerID(), EnumSet.allOf( SecurityPermissions.class ) );
	}

	@Override
	public boolean isSecurityEnabled()
	{
		return this.isActive && this.getProxy().isActive();
	}

	@Override
	public int getOwner()
	{
		return this.getProxy().getNode().getPlayerID();
	}

	@Override
	public AEColor getColor()
	{
		return this.paintedColor;
	}

	@Override
	public boolean recolourBlock( final EnumFacing side, final AEColor newPaintedColor, final EntityPlayer who )
	{
		if( this.paintedColor == newPaintedColor )
		{
			return false;
		}

		this.paintedColor = newPaintedColor;
		this.markDirty();
		this.markForUpdate();
		return true;
	}

	public AppEngInternalInventory getConfigSlot()
	{
		return this.configSlot;
	}
}
