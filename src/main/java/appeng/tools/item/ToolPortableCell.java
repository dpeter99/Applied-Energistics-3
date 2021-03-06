/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.tools.item;


import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.core.api.config.FuzzyMode;
import appeng.core.api.implementations.guiobjects.IGuiItem;
import appeng.core.api.implementations.guiobjects.IGuiItemObject;
import appeng.core.api.implementations.items.IItemGroup;
import appeng.core.api.implementations.items.IStorageCell;
import appeng.core.api.util.AEPartLocation;
import appeng.core.lib.AEConfig;
import appeng.core.lib.AppEngApi;
import appeng.core.lib.item.powered.AEBasePoweredItem;
import appeng.core.lib.localization.GuiText;
import appeng.core.lib.sync.GuiBridge;
import appeng.core.lib.util.Platform;
import appeng.core.me.api.storage.ICellInventory;
import appeng.core.me.api.storage.ICellInventoryHandler;
import appeng.core.me.api.storage.IMEInventory;
import appeng.core.me.api.storage.StorageChannel;
import appeng.core.me.api.storage.data.IAEItemStack;
import appeng.core.me.grid.storage.CellInventoryHandler;
import appeng.core.me.item.CellConfig;
import appeng.core.me.item.CellUpgrades;
import appeng.core.me.item.PortableCellViewer;


public class ToolPortableCell extends AEBasePoweredItem implements IStorageCell, IGuiItem, IItemGroup
{
	public ToolPortableCell()
	{
		super( AEConfig.instance.portableCellBattery );
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( final World w, final EntityPlayer player, final EnumHand hand )
	{
		Platform.openGUI( player, null, AEPartLocation.INTERNAL, GuiBridge.GUI_PORTABLE_CELL );
		return new ActionResult<ItemStack>( EnumActionResult.SUCCESS, player.getHeldItem( hand ) );
	}

	@SideOnly( Side.CLIENT )
	@Override
	public boolean isFull3D()
	{
		return false;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, player, lines, displayMoreInfo );

		final IMEInventory<IAEItemStack> cdi = AppEngApi.internalApi().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( cdi instanceof CellInventoryHandler )
		{
			final ICellInventory cd = ( (ICellInventoryHandler) cdi ).getCellInv();
			if( cd != null )
			{
				lines.add( cd.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );
				lines.add( cd.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public int getBytes( final ItemStack cellItem )
	{
		return 512;
	}

	@Override
	public int getBytesPerType( final ItemStack cellItem )
	{
		return 8;
	}

	@Override
	public int getTotalTypes( final ItemStack cellItem )
	{
		return 27;
	}

	@Override
	public boolean isBlackListed( final ItemStack cellItem, final IAEItemStack requestedAddition )
	{
		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell( final ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return 0.5;
	}

	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( final ItemStack is )
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
		final String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( final Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public IGuiItemObject getGuiObject( final ItemStack is, final World w, final BlockPos pos )
	{
		return new PortableCellViewer( is, pos.getX() );
	}
}
