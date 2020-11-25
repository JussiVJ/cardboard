/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.util.math.BlockPos;
import org.cardboardpowered.impl.block.CapturedBlockState;
import org.cardboardpowered.impl.world.WorldImpl;

import java.util.Map;

public interface IMixinWorld {

    public WorldImpl getWorldImpl();

    public Map<BlockPos, CapturedBlockState> getCapturedBlockStates_BF();

    public boolean isCaptureBlockStates_BF();

    public void setCaptureBlockStates_BF(boolean b);

}