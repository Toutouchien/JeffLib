/*
 *     Copyright (c) 2022. JEFF Media GbR / mfnalex et al.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.jeff_media.jefflib.internal.nms;

import com.google.common.collect.Multimap;
import com.jeff_media.jefflib.JeffLib;
import com.jeff_media.jefflib.internal.annotations.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;

/**
 * Represents Bukkit's {@link UnsafeValues} class with all version-dependant methods from org.bukkit.craftbukkit.&lt;version&gt;.util.CraftMagicNumbers included
 */
@SuppressWarnings("deprecation")
public interface BukkitUnsafe {

    static BukkitUnsafe getInstance() {
        return JeffLib.getNMSHandler().getUnsafe();
    }

    default Material toLegacy(final Material material) {
        return Bukkit.getUnsafe().toLegacy(material);
    }

    default Material fromLegacy(final Material material) {
        return Bukkit.getUnsafe().fromLegacy(material);
    }

    default Material fromLegacy(final MaterialData material) {
        return Bukkit.getUnsafe().fromLegacy(material);
    }

    default Material fromLegacy(final MaterialData material, final boolean itemPriority) {
        return Bukkit.getUnsafe().fromLegacy(material, itemPriority);
    }

    default BlockData fromLegacy(final Material material, final byte data) {
        return Bukkit.getUnsafe().fromLegacy(material, data);
    }

    default Material getMaterial(final String material, final int version) {
        return Bukkit.getUnsafe().getMaterial(material, version);
    }

    default int getDataVersion() {
        return Bukkit.getUnsafe().getDataVersion();
    }

    default ItemStack modifyItemStack(final ItemStack stack, final String arguments) {
        return Bukkit.getUnsafe().modifyItemStack(stack, arguments);
    }

    default void checkSupported(final PluginDescriptionFile pdf) throws InvalidPluginException {
        Bukkit.getUnsafe().checkSupported(pdf);
    }

    default byte[] processClass(final PluginDescriptionFile pdf, final String path, final byte[] clazz) {
        return Bukkit.getUnsafe().processClass(pdf, path, clazz);
    }

    default Advancement loadAdvancement(final NamespacedKey key, final String advancement) {
        return Bukkit.getUnsafe().loadAdvancement(key, advancement);
    }

    default boolean removeAdvancement(final NamespacedKey key) {
        return Bukkit.getUnsafe().removeAdvancement(key);
    }

    default Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(final Material material, final EquipmentSlot slot) {
        return Bukkit.getUnsafe().getDefaultAttributeModifiers(material, slot);
    }

    default CreativeCategory getCreativeCategory(final Material material) {
        return Bukkit.getUnsafe().getCreativeCategory(material);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    default Object getNMSBlockStateBlock(final MaterialData material) {
        return getNMSBlockState(material.getItemType(), material.getData());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object getNMSBlockState(Material material, byte data);

    MaterialData getMaterialFromNMSBlockState(Object nmsBlockState);

    Object getNMSItem(Material material, short data);

    MaterialData getMaterialDataFromNMSItem(Object nmsItem);

    Material getMaterialFromNMSBlock(Object nmsBlock);

    Material getMaterialFromNMSItem(Object nmsItem);

    /**
     * @nms 1.16.2+
     */
    @NMS("1.16.2")
    Object getFluidFromNMSFluid(Object nmsFluid);

    Object getNMSItemFromMaterial(Material material);

    Object getNMSBlockFromMaterial(Material material);

    /**
     * @nms 1.16.2+
     */
    @NMS("1.16.2")
    Object getNMSFluid(Object fluid);

    Object getNMSResourceLocation(Material material);

    byte NMSBlockStateToLegacyData(Object nmsBlockState);

    String getMappingsVersion();

    File getBukkitDataPackFolder();

    boolean isLegacy(PluginDescriptionFile file);

    final class NBT {
        public static final int TAG_END = 0;
        public static final int TAG_BYTE = 1;
        public static final int TAG_SHORT = 2;
        public static final int TAG_INT = 3;
        public static final int TAG_LONG = 4;
        public static final int TAG_FLOAT = 5;
        public static final int TAG_DOUBLE = 6;
        public static final int TAG_BYTE_ARRAY = 7;
        public static final int TAG_STRING = 8;
        public static final int TAG_LIST = 9;
        public static final int TAG_COMPOUND = 10;
        public static final int TAG_INT_ARRAY = 11;
        public static final int TAG_ANY_NUMBER = 99;

    }
}


