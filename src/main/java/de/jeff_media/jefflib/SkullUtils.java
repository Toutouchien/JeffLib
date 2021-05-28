package de.jeff_media.jefflib;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class SkullUtils {

    /**
     * Gives an already plaved skull another skin.
     *
     * @param block Skull
     * @param uuid  UUID of the player
     */
    public static void changeSkullInWorld(final Block block, final UUID uuid) {

        block.setType(Material.PLAYER_HEAD);

        if (!(block.getState() instanceof Skull)) {
            throw new IllegalStateException("Given block is not a skull");
        }

        final Skull state = (Skull) block.getState();

        // Use the player skin's texture
        final OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);
        state.setOwningPlayer(player);
        state.update();
    }


    // Use a predefined texture

    /**
     * Gives an already plaved skull another skin.
     *
     * @param block Skull
     * @param base64 Base64 encoded skin
     */
    public static void changeSkullInWorld(final Block block, final String base64) {

        block.setType(Material.PLAYER_HEAD);

        if (!(block.getState() instanceof Skull)) {
            throw new IllegalStateException("Given block is not a skull");
        }

        final Skull state = (Skull) block.getState();
        final GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", base64));

        try {
            final Object nmsWorld = ReflUtil.getMethodCached(World.class, "getHandle").invoke(block.getWorld());
            final Class<?> blockPositionClass = ReflUtil.getNMSClass("BlockPosition");
            final Class<?> tileEntityClass = ReflUtil.getNMSClass("TileEntitySkull");
            final Constructor<?> blockPositionConstructor = ReflUtil.getConstructorCached(blockPositionClass, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            final Object blockPosition = blockPositionConstructor.newInstance(block.getX(), block.getY(), block.getZ());
            final Method getTileEntityMethod = ReflUtil.getMethodCached(nmsWorld.getClass(),"getTileEntity", blockPositionClass);
            final Object tileEntity = getTileEntityMethod.invoke(nmsWorld, blockPosition);
            final Method setGameProfileMethod = ReflUtil.getMethodCached(tileEntityClass, "setGameProfile", GameProfile.class);
            setGameProfileMethod.invoke(tileEntity, profile);

        } catch (final IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException | InstantiationException e) {
            Bukkit.getLogger().warning("JeffLib: Could not set custom base64 player head.");
        }

    }
}
