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

package appeng.tools.client.gui;


import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.core.api.implementations.guiobjects.INetworkTool;
import appeng.core.lib.AELog;
import appeng.core.lib.client.gui.AEBaseGui;
import appeng.core.lib.client.gui.widgets.GuiToggleButton;
import appeng.core.lib.localization.GuiText;
import appeng.core.lib.sync.network.NetworkHandler;
import appeng.core.lib.sync.packets.PacketValueConfig;
import appeng.core.me.container.ContainerNetworkTool;


public class GuiNetworkTool extends AEBaseGui
{

	private GuiToggleButton tFacades;

	public GuiNetworkTool( final InventoryPlayer inventoryPlayer, final INetworkTool te )
	{
		super( new ContainerNetworkTool( inventoryPlayer, te ) );
		this.ySize = 166;
	}

	@Override
	protected void actionPerformed( final GuiButton btn ) throws IOException
	{
		super.actionPerformed( btn );

		try
		{
			if( btn == this.tFacades )
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "NetworkTool", "Toggle" ) );
			}
		}
		catch( final IOException e )
		{
			AELog.debug( e );
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.tFacades = new GuiToggleButton( this.guiLeft - 18, this.guiTop + 8, 23, 22, GuiText.TransparentFacades.getLocal(), GuiText.TransparentFacadesHint.getLocal() );

		this.buttonList.add( this.tFacades );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		if( this.tFacades != null )
		{
			this.tFacades.setState( ( (ContainerNetworkTool) this.inventorySlots ).isFacadeMode() );
		}

		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.NetworkTool.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );
	}

	@Override
	public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.bindTexture( "guis/toolbox.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );
	}
}
