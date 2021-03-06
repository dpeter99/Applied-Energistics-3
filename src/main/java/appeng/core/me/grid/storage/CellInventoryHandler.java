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

package appeng.core.me.grid.storage;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.core.api.config.FuzzyMode;
import appeng.core.api.config.IncludeExclude;
import appeng.core.api.config.Upgrades;
import appeng.core.api.implementations.items.IUpgradeModule;
import appeng.core.lib.AppEngApi;
import appeng.core.lib.util.Platform;
import appeng.core.lib.util.item.AEItemStack;
import appeng.core.lib.util.prioitylist.FuzzyPriorityList;
import appeng.core.lib.util.prioitylist.PrecisePriorityList;
import appeng.core.me.api.storage.ICellInventory;
import appeng.core.me.api.storage.ICellInventoryHandler;
import appeng.core.me.api.storage.IMEInventory;
import appeng.core.me.api.storage.StorageChannel;
import appeng.core.me.api.storage.data.IAEItemStack;
import appeng.core.me.api.storage.data.IItemList;


public class CellInventoryHandler extends MEInventoryHandler<IAEItemStack> implements ICellInventoryHandler
{

	CellInventoryHandler( final IMEInventory c )
	{
		super( c, StorageChannel.ITEMS );

		final ICellInventory ci = this.getCellInv();
		if( ci != null )
		{
			final IItemList<IAEItemStack> priorityList = AppEngApi.internalApi().storage().createItemList();

			final IInventory upgrades = ci.getUpgradesInventory();
			final IInventory config = ci.getConfigInventory();
			final FuzzyMode fzMode = ci.getFuzzyMode();

			boolean hasInverter = false;
			boolean hasFuzzy = false;

			for( int x = 0; x < upgrades.getSizeInventory(); x++ )
			{
				final ItemStack is = upgrades.getStackInSlot( x );
				if( is != null && is.getItem() instanceof IUpgradeModule )
				{
					final Upgrades u = ( (IUpgradeModule) is.getItem() ).getType( is );
					if( u != null )
					{
						switch( u )
						{
							case FUZZY:
								hasFuzzy = true;
								break;
							case INVERTER:
								hasInverter = true;
								break;
							default:
						}
					}
				}
			}

			for( int x = 0; x < config.getSizeInventory(); x++ )
			{
				final ItemStack is = config.getStackInSlot( x );
				if( is != null )
				{
					priorityList.add( AEItemStack.create( is ) );
				}
			}

			this.setWhitelist( hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST );

			if( !priorityList.isEmpty() )
			{
				if( hasFuzzy )
				{
					this.setPartitionList( new FuzzyPriorityList<IAEItemStack>( priorityList, fzMode ) );
				}
				else
				{
					this.setPartitionList( new PrecisePriorityList<IAEItemStack>( priorityList ) );
				}
			}
		}
	}

	@Override
	public ICellInventory getCellInv()
	{
		Object o = this.getInternal();

		if( o instanceof MEPassThrough )
		{
			o = ( (MEPassThrough) o ).getInternal();
		}

		return (ICellInventory) ( o instanceof ICellInventory ? o : null );
	}

	@Override
	public boolean isPreformatted()
	{
		return !this.getPartitionList().isEmpty();
	}

	@Override
	public boolean isFuzzy()
	{
		return this.getPartitionList() instanceof FuzzyPriorityList;
	}

	@Override
	public IncludeExclude getIncludeExcludeMode()
	{
		return this.getWhitelist();
	}

	NBTTagCompound openNbtData()
	{
		return Platform.openNbtData( this.getCellInv().getItemStack() );
	}

	public int getStatusForCell()
	{
		int val = this.getCellInv().getStatusForCell();

		if( val == 1 && this.isPreformatted() )
		{
			val = 2;
		}

		return val;
	}
}
