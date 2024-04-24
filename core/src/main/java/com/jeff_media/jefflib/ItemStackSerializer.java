/*
 * Copyright (c) 2023. JEFF Media GbR / mfnalex et al.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.jeff_media.jefflib;

import com.jeff_media.jefflib.exceptions.NMSNotSupportedException;
import com.jeff_media.jefflib.internal.annotations.NMS;
import com.jeff_media.jefflib.internal.annotations.Tested;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.UncheckedIOException;
import java.util.Base64;

import com.jeff_media.jsonconfigurationserialization.JsonConfigurationSerialization;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 * Provides methods to serialize and deserialize ItemStacks, ItemStack arrays and Inventories to/from byte arrays and/or base64
 */
@UtilityClass
public class ItemStackSerializer {

    /**
     * Turns an ItemStack into a Base64 String
     *
     * @param itemStack ItemStack
     * @return ItemStack as Base64 String
     * @throws IOException exception
     */
    public static String toBase64(final ItemStack itemStack) throws IOException {
        return Base64.getEncoder().encodeToString(toBytes(itemStack));
    }

    /**
     * Turns an ItemStack into a byte array
     *
     * @param itemStack ItemStack
     * @return ItemStack as byte array
     * @throws IOException exception
     */
    public static byte[] toBytes(final ItemStack itemStack) throws IOException {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); final ObjectOutput objectOutputStream = new BukkitObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(itemStack);
            return outputStream.toByteArray();
        }
    }

    /**
     * Turns a Base64 String into an ItemStack
     *
     * @param input Base64 String
     * @return ItemStack
     */
    public static ItemStack fromBase64(final String input) {
        return fromBytes(Base64.getDecoder().decode(input));
    }

    /**
     * Turns a byte array into an ItemStack
     *
     * @param input byte array
     * @return ItemStack
     */
    @SneakyThrows
    public static ItemStack fromBytes(final byte[] input) {
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(input); final BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) objectInputStream.readObject();
        }
    }

    /**
     * Converts the player inventory to a String array of Base64 strings. First string is the content and second string is the armor.
     *
     * @param playerInventory to turn into an array of strings.
     * @return Array of strings: [ main content, armor content ]
     * @throws IllegalStateException exception
     */
    public static String[] playerInventoryToBase64(final PlayerInventory playerInventory) {
        //get the main content part, this doesn't return the armor
        final String content = toBase64(playerInventory);
        final String armor = itemStackArrayToBase64(playerInventory.getArmorContents());

        return new String[] {content, armor};
    }

    /**
     * A method to serialize an inventory to Base64 string.
     * <p>
     * Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.
     *
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     *
     * @param inventory to serialize
     * @return Base64 string of the provided inventory
     * @throws IllegalStateException exception
     */
    public static String toBase64(final Inventory inventory) {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); final ObjectOutput dataOutput = new BukkitObjectOutputStream(outputStream)) {

            // Write the size of the inventory
            dataOutput.writeInt(inventory.getSize());

            // Save every element in the list
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            // Serialize that array
            dataOutput.flush();
            outputStream.flush();
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     * A method to serialize an {@link ItemStack} array to Base64 String.
     * <p>
     * Based off of {@link #toBase64(Inventory)}.
     *
     * @param items to turn into a Base64 String.
     * @return Base64 string of the items.
     * @throws IllegalStateException exception
     */
    public static String itemStackArrayToBase64(final ItemStack[] items) {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); final ObjectOutput dataOutput = new BukkitObjectOutputStream(outputStream)) {

            // Write the size of the inventory
            dataOutput.writeInt(items.length);

            // Save every element in the list
            for (final ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            // Serialize that array
            dataOutput.flush();
            outputStream.flush();
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     * A method to get an {@link Inventory} from an encoded, Base64, string.
     * <p>
     * Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.
     *
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     *
     * @param data Base64 string of data containing an inventory.
     * @return Inventory created from the Base64 string.
     * @throws IOException exception
     */
    public static Inventory inventoryFromBase64(final String data) throws IOException {
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data)); final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            final Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

            // Read the serialized inventory
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }
            dataInput.close();
            return inventory;
        } catch (final ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    /**
     * Gets an array of ItemStacks from Base64 string.
     * <p>
     * Base off of {@link #fromBase64(String)}.
     *
     * @param data Base64 string to convert to ItemStack array.
     * @return ItemStack array created from the Base64 string.
     * @throws IOException exception
     */
    public static ItemStack[] itemStackArrayFromBase64(final String data) throws IOException {
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data)); final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            final ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (final ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    public static String toSnbtWithType(ItemStack itemStack) {
        String material = itemStack.getType().name().toLowerCase();
        ItemMeta meta = itemStack.getItemMeta();
        String metaString = (meta != null) ? meta.getAsString() : "";
        return "minecraft:" + material + metaString;
    }

    public static String toSnbtWithoutType(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        return (meta != null) ? meta.getAsString() : "";
    }

    /**
     * Turns an ItemStack into a json-formatted String
     *
     * @nms
     * @see #fromJson(String)
     */
    @NMS
    @Tested("1.19.4")
    public static String toJson(final ItemStack itemStack) {
        //try {
           return JeffLib.getNMSHandler().itemStackToJson(itemStack);
        /*}
        try {
            return JsonConfigurationSerialization.serialize(itemStack);
        } catch (NMSNotSupportedException e) {
            return toSnbtWithType(itemStack);
        }*/
    }

    /**
     * Turns a json-formatted String into an ItemStack
     *
     * @param json json-formatted String
     * @return ItemStack
     * @throws UncheckedIOException if the server couldn't parse it
     * @nms
     * @see #toJson(ItemStack)
     */
    @NMS
    @Tested("1.19.4")
    public static ItemStack fromJson(final String json) {
        try {
            return JeffLib.getNMSHandler().itemStackFromJson(json);
        //} catch(NMSNotSupportedException e) {
        //    return JsonConfigurationSerialization.deserialize(json, ItemStack.class);
        } catch (Exception ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }
}
