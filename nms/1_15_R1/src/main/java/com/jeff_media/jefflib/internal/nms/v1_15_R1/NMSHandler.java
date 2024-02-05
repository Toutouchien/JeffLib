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

package com.jeff_media.jefflib.internal.nms.v1_15_R1;

import static com.jeff_media.jefflib.internal.nms.v1_15_R1.NMS.*;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jeff_media.jefflib.ItemStackUtils;
import com.jeff_media.jefflib.PacketUtils;
import com.jeff_media.jefflib.ReflUtils;
import com.jeff_media.jefflib.ai.goal.CustomGoal;
import com.jeff_media.jefflib.ai.goal.PathfinderGoal;
import com.jeff_media.jefflib.ai.navigation.JumpController;
import com.jeff_media.jefflib.ai.navigation.LookController;
import com.jeff_media.jefflib.ai.navigation.MoveController;
import com.jeff_media.jefflib.ai.navigation.PathNavigation;
import com.jeff_media.jefflib.data.ByteCounter;
import com.jeff_media.jefflib.data.Hologram;
import com.jeff_media.jefflib.data.OfflinePlayerPersistentDataContainer;
import com.jeff_media.jefflib.data.SerializedEntity;
import com.jeff_media.jefflib.data.tuples.Pair;
import com.jeff_media.jefflib.exceptions.NMSNotSupportedException;
import com.jeff_media.jefflib.internal.nms.AbstractNMSBlockHandler;
import com.jeff_media.jefflib.internal.nms.AbstractNMSHandler;
import com.jeff_media.jefflib.internal.nms.AbstractNMSMaterialHandler;
import com.jeff_media.jefflib.internal.nms.AbstractNMSTranslationKeyProvider;
import com.jeff_media.jefflib.internal.nms.BukkitUnsafe;
import com.jeff_media.jefflib.internal.nms.v1_15_R1.ai.CustomGoalExecutor;
import com.jeff_media.jefflib.internal.nms.v1_15_R1.ai.HatchedAvoidEntityGoal;
import com.jeff_media.jefflib.internal.nms.v1_15_R1.ai.HatchedJumpController;
import com.jeff_media.jefflib.internal.nms.v1_15_R1.ai.HatchedLookController;
import com.jeff_media.jefflib.internal.nms.v1_15_R1.ai.HatchedMoveController;
import com.jeff_media.jefflib.internal.nms.v1_15_R1.ai.HatchedMoveToBlockGoal;
import com.jeff_media.jefflib.internal.nms.v1_15_R1.ai.HatchedPathNavigation;
import com.jeff_media.jefflib.internal.nms.v1_15_R1.ai.HatchedTemptGoal;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.server.v1_15_R1.AdvancementDataWorld;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.ChatDeserializer;
import net.minecraft.server.v1_15_R1.Criterion;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityAreaEffectCloud;
import net.minecraft.server.v1_15_R1.EntityArmorStand;
import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityInsentient;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.GameRules;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketListenerPlayOut;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_15_R1.PacketPlayOutUpdateTime;
import net.minecraft.server.v1_15_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import net.minecraft.server.v1_15_R1.RandomPositionGenerator;
import net.minecraft.server.v1_15_R1.TileEntitySkull;
import net.minecraft.server.v1_15_R1.Vec3D;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_15_R1.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftNamespacedKey;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NMSHandler implements AbstractNMSHandler, AbstractNMSTranslationKeyProvider {

    private final MaterialHandler materialHandler = new MaterialHandler();
    private final BlockHandler blockHandler = new BlockHandler();
    private final com.jeff_media.jefflib.internal.nms.v1_15_R1.BukkitUnsafe unsafe = com.jeff_media.jefflib.internal.nms.v1_15_R1.BukkitUnsafe.INSTANCE;

    @Override
    public AbstractNMSMaterialHandler getMaterialHandler() {
        return materialHandler;
    }

    @Override
    public AbstractNMSBlockHandler getBlockHandler() {
        return blockHandler;
    }

    @Override
    public void changeNMSEntityName(@NotNull final Object entity, @NotNull final String name) {
        ((Entity) entity).setCustomName(CraftChatMessage.fromString(name)[0]);
        for (final Player player : Bukkit.getOnlinePlayers()) {
            sendPacket(player, new PacketPlayOutEntityMetadata(((Entity) entity).getId(), ((Entity) entity).getDataWatcher(), true));
        }
    }

    @Override
    public Object createHologram(@NotNull final Location location, @NotNull final String line, @NotNull final Hologram.Type type) {
        final CraftWorld craftWorld = (CraftWorld) location.getWorld();
        final World world = craftWorld.getHandle();
        final IChatBaseComponent baseComponent = CraftChatMessage.fromString(line)[0];
        final Entity entity;
        switch (type) {
            case EFFECTCLOUD:
                entity = new EntityAreaEffectCloud(world, location.getX(), location.getY(), location.getZ());
                final EntityAreaEffectCloud effectCloud = (EntityAreaEffectCloud) entity;
                effectCloud.setRadius(0);
                effectCloud.setWaitTime(0);
                effectCloud.setDuration(Integer.MAX_VALUE);
                break;
            case ARMORSTAND:
            default:
                entity = new EntityArmorStand(world, location.getX(), location.getY(), location.getZ());
                final EntityArmorStand armorStand = (EntityArmorStand) entity;
                armorStand.setNoGravity(true);
                armorStand.setInvisible(true);
                armorStand.setMarker(true);
                armorStand.setSmall(true);
        }

        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.setCustomName(baseComponent);
        entity.setCustomNameVisible(true);
        return entity;
    }

    @Override
    public void showEntityToPlayer(@NotNull final Object entity, @NotNull final Player player) {
        final PacketPlayOutSpawnEntity packetSpawn = new PacketPlayOutSpawnEntity((Entity) entity);
        PacketUtils.sendPacket(player, packetSpawn);

        final PacketPlayOutEntityMetadata packetMeta = new PacketPlayOutEntityMetadata(((Entity) entity).getId(), ((Entity) entity).getDataWatcher(), true);
        PacketUtils.sendPacket(player, packetMeta);
    }

    @Override
    public void hideEntityFromPlayer(@NotNull final Object entity, @NotNull final Player player) {
        final PacketPlayOutEntityDestroy packetDestroy = new PacketPlayOutEntityDestroy(((Entity) entity).getId());
        PacketUtils.sendPacket(player, packetDestroy);
    }

    @Override
    public void sendPacket(@NotNull final Player player, @NotNull final Object packet) {
        NMSPacketUtils.sendPacket(player, packet);
    }

    @Override
    public Pair<String, String> getBiomeName(@NotNull final Location location) {
        throw new NMSNotSupportedException("1_15_R1 and older does not support this method");
    }

    @Override
    public void playTotemAnimation(@NotNull final Player player) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final Packet<PacketListenerPlayOut> packet = new PacketPlayOutEntityStatus(entityPlayer, (byte) 35);
        final PlayerConnection playerConnection = entityPlayer.playerConnection;
        playerConnection.sendPacket(packet);
    }

    @Override
    public void setHeadTexture(final Block block, @NotNull final GameProfile gameProfile) {
        final World world = ((CraftWorld) block.getWorld()).getHandle();
        final BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        final TileEntitySkull skull = (TileEntitySkull) world.getTileEntity(blockPosition);
        skull.setGameProfile(gameProfile);
    }

    @Override
    public String itemStackToJson(@NotNull final org.bukkit.inventory.ItemStack itemStack) {
        final ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        final NBTTagCompound compoundTag = new NBTTagCompound();
        nmsItemStack.save(compoundTag);
        return compoundTag.asString();
    }

    @Override
    public org.bukkit.inventory.ItemStack itemStackFromJson(@NotNull String json) throws Exception {
        final NBTTagCompound compoundTag = MojangsonParser.parse(json);
        final ItemStack nmsItemStack = ItemStack.a(compoundTag);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public void setFullTimeWithoutTimeSkipEvent(@NotNull final org.bukkit.World world, final long time, final boolean notifyPlayers) {
        final WorldServer level = ((CraftWorld) world).getHandle();
        level.setDayTime(time);
        if (notifyPlayers) {
            for (final Player player : world.getPlayers()) {
                final EntityPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                if (serverPlayer.playerConnection != null) {
                    serverPlayer.playerConnection.sendPacket(new PacketPlayOutUpdateTime(serverPlayer.world.getTime(), serverPlayer.getPlayerTime(), serverPlayer.world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
                }
            }
        }
    }

    @Override
    public double[] getTps() {
        return ((CraftServer) Bukkit.getServer()).getHandle().getServer().recentTps;
    }

    @Override
    public int getItemStackSizeInBytes(final org.bukkit.inventory.ItemStack itemStack) throws IOException {
        final ByteCounter counter = new ByteCounter();
        final NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getTag();
        if (tag == null) return ItemStackUtils.NO_DATA;
        tag.write(counter);
        return counter.getBytes();
    }

    @Override
    public String getDefaultWorldName() {
        return ((CraftServer) Bukkit.getServer()).getServer().propertyManager.getProperties().levelName;
    }

    @Override
    public PathfinderGoal createTemptGoal(final Creature entity, final Stream<org.bukkit.Material> materials, final double speed, final boolean canScare) {
        final EntityCreature pathfinderMob = asPathfinder(entity);
        return new HatchedTemptGoal(entity, pathfinderMob, ingredient(materials), speed, canScare);
    }

    @Override
    public PathfinderGoal createAvoidEntityGoal(final Creature entity, final Predicate<LivingEntity> predicate, final float maxDistance, final double walkSpeedModifier, final double sprintSpeedModifier) {
        return new HatchedAvoidEntityGoal(entity, asPathfinder(entity), predicate, maxDistance, walkSpeedModifier, sprintSpeedModifier);
    }

    @Override
    public PathfinderGoal createMoveToBlockGoal(final Creature entity, final Set<org.bukkit.Material> blocks, final double speed, final int searchRange, final int verticalSearchRange) {
        return new HatchedMoveToBlockGoal.ByMaterialSet(entity, asPathfinder(entity), speed, searchRange, verticalSearchRange, blocks);
    }

    @Override
    public PathfinderGoal createMoveToBlockGoal(final Creature entity, final Predicate<Block> blockPredicate, final double speed, final int searchRange, final int verticalSearchRange) {
        return new HatchedMoveToBlockGoal.ByBlockPredicate(entity, asPathfinder(entity), speed, searchRange, verticalSearchRange, blockPredicate);
    }

    @Override
    public void addGoal(final Mob entity, final PathfinderGoal goal, final int priority) {
        asMob(entity).goalSelector.a(priority, NMS.toNms(goal));
    }


    @Override
    public void removeGoal(final Mob entity, final PathfinderGoal goal) {
        asMob(entity).goalSelector.a(NMS.toNms(goal));
    }

    @Override
    public void removeAllGoals(final Mob entity) {
        ((Set<?>) ReflUtils.getFieldValue(PathfinderGoalSelector.class, "d", asMob(entity).goalSelector)).clear();
    }

    @Override
    public void addTargetGoal(final Mob entity, final PathfinderGoal goal, final int priority) {
        asMob(entity).targetSelector.a(priority, NMS.toNms(goal));
    }

    @Override
    public void removeTargetGoal(final Mob entity, final PathfinderGoal goal) {
        asMob(entity).targetSelector.a(NMS.toNms(goal));
    }

    @Override
    public void removeAllTargetGoals(final Mob entity) {
        ((Set<?>) ReflUtils.getFieldValue(PathfinderGoalSelector.class, "d", asMob(entity).targetSelector)).clear();
    }

    @Override
    public boolean moveTo(final Mob entity, final double x, final double y, final double z, final double speed) {
        final EntityInsentient pathfinderMob = asMob(entity);
        return pathfinderMob.getNavigation().a(x, y, z, speed);
    }

    @Override
    public boolean isServerRunnning() {
        return getDedicatedServer().isRunning();
    }

    @Override
    public com.jeff_media.jefflib.ai.goal.CustomGoalExecutor getCustomGoalExecutor(final CustomGoal customGoal, final Mob entity) {
        return new CustomGoalExecutor(customGoal, asMob(entity));
    }

    @Nullable
    @Override
    public Vector getRandomPos(final Creature entity, final int var1, final int var2) {
        final EntityCreature pathfinderMob = asPathfinder(entity);
        final Vec3D vec = RandomPositionGenerator.a(pathfinderMob, var1, var2); // could be .a or .b
        return vec == null ? null : new Vector(vec.x, vec.y, vec.z);
    }

    @Nullable
    @Override
    public Vector getRandomPosAway(final Creature entity, final int var1, final int var2, final Vector var3) {
        final EntityCreature pathfinderMob = asPathfinder(entity);
        final Vec3D vec = RandomPositionGenerator.c(pathfinderMob, var1, var2, toNms(var3)); // definitely c
        return vec == null ? null : toBukkit(vec);
    }

    @Nullable
    @Override
    public Vector getRandomPosTowards(final Creature entity, final int var1, final int var2, final Vector var3, final double var4) {
        final EntityCreature pathfinderMob = asPathfinder(entity);
        final Vec3D vec = RandomPositionGenerator.a(pathfinderMob, var1, var2, toNms(var3), var4); // a is the only that matches
        return vec == null ? null : toBukkit(vec);
    }

    @NotNull
    @Override
    public MoveController getMoveControl(final Mob entity) {
        return new HatchedMoveController(asMob(entity).getControllerMove());
    }

    @NotNull
    @Override
    public JumpController getJumpControl(final Mob entity) {
        return new HatchedJumpController(asMob(entity).getControllerJump());
    }

    @NotNull
    @Override
    public LookController getLookControl(final Mob entity) {
        return new HatchedLookController(asMob(entity).getControllerLook());
    }

    @NotNull
    @Override
    public PathNavigation getPathNavigation(final Mob entity) {
        final EntityInsentient pathfinderMob = asMob(entity);
        return new HatchedPathNavigation(pathfinderMob.getNavigation());
    }

    @Nullable
    @Override
    public Advancement loadVolatileAdvancement(final NamespacedKey key, final String advancement) {
//        final MinecraftKey resourceLocation = CraftNamespacedKey.toMinecraft(key);
//        final JsonElement jsonelement = AdvancementDataWorld.DESERIALIZER.fromJson(advancement, JsonElement.class);
//        final JsonObject jsonobject = ChatDeserializer.m(jsonelement, "advancement");
//        net.minecraft.server.v1_15_R1.Advancement.SerializedAdvancement nms = net.minecraft.server.v1_15_R1.Advancement.SerializedAdvancement.a(jsonobject, Criterion(resourceLocation, NMS.getServer().aI()));
//        getServer().getAdvancementData().REGISTRY.a(Maps.newHashMap(Collections.singletonMap(resourceLocation, nms)));
//        return Bukkit.getAdvancement(key);
        throw new UnsupportedOperationException("1.16.1+ only");
    }

    @NotNull
    @Override
    public BukkitUnsafe getUnsafe() {
        return com.jeff_media.jefflib.internal.nms.v1_15_R1.BukkitUnsafe.INSTANCE;
    }

    @Override
    public String serializePdc(PersistentDataContainer pdc) {
        return ((CraftPersistentDataContainer) pdc).toTagCompound().asString();
    }

    @Override
    public void deserializePdc(String serializedPdc, PersistentDataContainer target) throws Exception {
        NBTTagCompound tag = MojangsonParser.parse(serializedPdc);
        ((CraftPersistentDataContainer) target).putAll(tag);
    }

    @Override
    public void respawnPlayer(Player player) {
        throw new UnsupportedOperationException("1.16.1+ only");
//        NMS.getServer().getPlayerList().moveToWorld(NMS.toNms(player), true);
    }

    @Override
    public SerializedEntity serialize(org.bukkit.entity.Entity entity) {
        NBTTagCompound tag = new NBTTagCompound();
        toNms(entity).save(tag);
        return new SerializedEntity(entity.getType(), tag.toString());
    }

    @Override
    public void applyNbt(org.bukkit.entity.Entity entity, String nbtData) {
//        NBTTagCompound tag = null;
//        try {
//            tag = MojangsonParser.parse(nbtData);
//        } catch (CommandSyntaxException e) {
//            throw new IllegalArgumentException(e);
//        }
//        toNms(entity).load(tag);
        throw new UnsupportedOperationException("1.16.1+ only");
    }

    @Override
    public String getItemTranslationKey(Material mat) {
        return unsafe.getNMSItemFromMaterial(mat).getName();
    }

    @Override
    public String getBlockTranslationKey(Material mat) {
//        return unsafe.getNMSBlockFromMaterial(mat).i();
        throw new UnsupportedOperationException("1.16.1+ only");
    }

    @Override
    public String getTranslationKey(Block block) {
//        return toNms(block).i();
        throw new UnsupportedOperationException("1.16.1+ only");
    }

    @Override
    public String getTranslationKey(EntityType entityType) {
        return EntityTypes.a(entityType.getName())
                .map(EntityTypes::f)
                .orElse(null);
    }

    @Override
    public String getTranslationKey(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.getItem().f(nmsItemStack);
    }

    @Override
    public OfflinePlayerPersistentDataContainer getPDCFromDatFile(File file) throws IOException {
        CraftPersistentDataTypeRegistry registry = new CraftPersistentDataTypeRegistry();
        CraftPersistentDataContainer container = new CraftPersistentDataContainer(registry);
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            NBTTagCompound fileTag = NBTCompressedStreamTools.a(inputStream);
            NBTTagCompound pdcTag = fileTag.getCompound("BukkitValues");
            container.putAll(pdcTag);
            return new OfflinePlayerPersistentDataContainer(container, file, fileTag);
        }
    }

    @Override
    public void updatePdcInDatFile(OfflinePlayerPersistentDataContainer pdc) throws IOException {
        NBTTagCompound pdcTag = ((CraftPersistentDataContainer) pdc.getCraftPersistentDataContainer()).toTagCompound();
        NBTTagCompound fileTag = (NBTTagCompound) pdc.getCompoundTag();
        fileTag.set("BukkitValues", pdcTag);
        try (OutputStream outputStream = Files.newOutputStream(pdc.getFile().toPath())) {
            NBTCompressedStreamTools.a(fileTag, outputStream);
        }
    }


}
