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

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.cardboardpowered.impl.entity.HumanEntityImpl;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;

import java.util.List;

public interface IMixinScreenHandler {

    public CardboardInventoryView getBukkitView();

    public Text getTitle();

    public void setTitle(Text title);

    public void transferTo(ScreenHandler other, HumanEntityImpl player);

    public DefaultedList<ItemStack> getTrackedStacksBF();

    public void setTrackedStacksBF(DefaultedList<ItemStack> trackedStacks);

    public void setSlots(List<Slot> slots);

    public void setCheckReachable(boolean bl);

}