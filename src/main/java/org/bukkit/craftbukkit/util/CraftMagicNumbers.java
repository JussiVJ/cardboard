package org.bukkit.craftbukkit.util;

import com.javazilla.bukkitfabric.BukkitLogger;
import com.google.common.base.Preconditions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.bukkit.Bukkit;
import org.bukkit.Fluid;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;

@SuppressWarnings("deprecation")
public final class CraftMagicNumbers implements UnsafeValues {

    public static final UnsafeValues INSTANCE = new CraftMagicNumbers();

    private CraftMagicNumbers() {}

    public static BlockState getBlock(MaterialData material) {
        return getBlock(material.getItemType(), material.getData());
    }

    public static BlockState getBlock(Material material, byte data) {
        return CraftLegacyMaterials.fromLegacyData(CraftLegacyMaterials.toLegacy(material), data);
    }

    public static MaterialData getMaterial(BlockState data) {
        return CraftLegacyMaterials.toLegacy(getMaterial(data.getBlock())).getNewData(toLegacyData(data));
    }

    public static Item getItem(Material material, short data) {
        if (material.isLegacy())
            return CraftLegacyMaterials.fromLegacyData(CraftLegacyMaterials.toLegacy(material), data);

        return getItem(material);
    }

    public static MaterialData getMaterialData(Item item) {
        return CraftLegacyMaterials.toLegacyData(getMaterial(item));
    }

    // ========================================================================
    private static final Map<Block, Material> BLOCK_MATERIAL = new HashMap<>();
    private static final Map<Item, Material> ITEM_MATERIAL = new HashMap<>();
    private static final Map<Material, Item> MATERIAL_ITEM = new HashMap<>();
    private static final Map<Material, Block> MATERIAL_BLOCK = new HashMap<>();
    private static final Map<net.minecraft.fluid.Fluid, org.bukkit.Fluid> FLUID_MATERIAL = new HashMap<>();
    private static final Map<Material, net.minecraft.fluid.Fluid> MATERIAL_FLUID = new HashMap<>();


    static {
        for (Block block : Registry.BLOCK){
            Material material = Material.getMaterial(Registry.BLOCK.getId(block).getPath().toUpperCase(Locale.ROOT));
            BLOCK_MATERIAL.put(block, material == null ? Material.STONE : material);
        }

        for (Item item : Registry.ITEM){
            Material material = Material.getMaterial(Registry.ITEM.getId(item).getPath().toUpperCase(Locale.ROOT));
            ITEM_MATERIAL.put(item, material == null ? Material.STONE : material);
        }

        for (net.minecraft.fluid.Fluid fluid : Registry.FLUID){
            FLUID_MATERIAL.put(fluid, org.bukkit.Registry.FLUID.get(CraftNamespacedKey.fromMinecraft(Registry.FLUID.getId(fluid))));
        }

        for (Material material : Material.values()) {
            if (material.isLegacy()) continue;

            Identifier key = key(material);
            Registry.ITEM.getOrEmpty(key).ifPresent((item) -> MATERIAL_ITEM.put(material, item));
            Registry.BLOCK.getOrEmpty(key).ifPresent((block) -> MATERIAL_BLOCK.put(material, block));
            Registry.FLUID.getOrEmpty(key).ifPresent((fluid) -> MATERIAL_FLUID.put(material, fluid));
        }
    }

    public static Material getMaterial(Block block) {
        return BLOCK_MATERIAL.getOrDefault(block, Material.STONE);
    }

    public static Material getMaterial(Item item) {
        return ITEM_MATERIAL.getOrDefault(item, Material.AIR);
    }

    public static Item getItem(Material material) {
        if (material != null && material.isLegacy()) material = CraftLegacyMaterials.fromLegacy(material);
        return MATERIAL_ITEM.get(material);
    }

    public static Block getBlock(Material material) {
        if (material != null && material.isLegacy()) material = CraftLegacyMaterials.fromLegacy(material);
        return MATERIAL_BLOCK.get(material);
    }

    public static Identifier key(Material mat) {
        return CraftNamespacedKey.toMinecraft(mat.getKey());
    }
    // ========================================================================

    public static byte toLegacyData(BlockState data) {
        return CraftLegacyMaterials.toLegacyData(data);
    }

    @Override
    public Material toLegacy(Material material) {
        return CraftLegacyMaterials.toLegacy(material);
    }

    @Override
    public Material fromLegacy(Material material) {
        return CraftLegacyMaterials.fromLegacy(material);
    }

    @Override
    public Material fromLegacy(MaterialData material) {
        return CraftLegacyMaterials.fromLegacy(material);
    }

    @Override
    public Material fromLegacy(MaterialData material, boolean itemPriority) {
        return CraftLegacyMaterials.fromLegacy(material, itemPriority);
    }

    @Override
    public BlockData fromLegacy(Material material, byte data) {
        return CraftBlockData.fromData(getBlock(material, data));
    }

    @Override
    public Material getMaterial(String material, int version) {
        Preconditions.checkArgument(material != null, "material == null");
        Preconditions.checkArgument(version <= this.getDataVersion(), "Newer version! Server downgrades are not supported!");

        // Fastpath up to date materials
        if (version == this.getDataVersion()) return Material.getMaterial(material);

        Dynamic<Tag> name = new Dynamic<>(NbtOps.INSTANCE, StringTag.of("minecraft:" + material.toLowerCase(Locale.ROOT)));
        Dynamic<Tag> converted = Schemas.getFixer().update(TypeReferences.ITEM_NAME, name, version, this.getDataVersion());

        if (name.equals(converted)) converted = Schemas.getFixer().update(TypeReferences.BLOCK_NAME, name, version, this.getDataVersion());
        return Material.matchMaterial(converted.asString(""));
    }

    @Deprecated
    public String getMappingsVersion() {
        return "MinecraftMapping-spigot2intermediary.srg";
    }

    @Override
    public int getDataVersion() {
        return SharedConstants.getGameVersion().getWorldVersion();
    }

    @Override
    public ItemStack modifyItemStack(ItemStack stack, String arguments) {
        net.minecraft.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        try {
            nmsStack.setTag((CompoundTag) StringNbtReader.parse(arguments));
        } catch (CommandSyntaxException ex) {
            BukkitLogger.getLogger(CraftMagicNumbers.class.getName()).log(Level.SEVERE, null, ex);
        }

        stack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));
        return stack;
    }

    @Override
    public Advancement loadAdvancement(NamespacedKey key, String advancement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeAdvancement(NamespacedKey key) {
        // TODO Auto-generated method stub
        return false;
    }

    private static final List<String> SUPPORTED_API = Arrays.asList("1.13", "1.14", "1.15", "1.16");

    @Override
    public void checkSupported(PluginDescriptionFile pdf) throws InvalidPluginException {
        String minimumVersion = "1.12"; // TODO
        int minimumIndex = SUPPORTED_API.indexOf(minimumVersion);

        if (pdf.getAPIVersion() != null) {
            int pluginIndex = SUPPORTED_API.indexOf(pdf.getAPIVersion());
            if (pluginIndex == -1) throw new InvalidPluginException("Unsupported API version " + pdf.getAPIVersion());

            if (pluginIndex < minimumIndex)
                throw new InvalidPluginException("Plugin API version " + pdf.getAPIVersion() + " is lower than the minimum allowed version. Please update or replace it.");
        } else {
            if (minimumIndex == -1) {
                CraftLegacyMaterials.init();
                Bukkit.getLogger().log(Level.WARNING, "Legacy plugin " + pdf.getFullName() + " does not specify an api-version.");
            } else throw new InvalidPluginException("Plugin API version " + pdf.getAPIVersion() + " is lower than the minimum allowed version. Please update or replace it.");
        }
    }

    public static boolean isLegacy(PluginDescriptionFile pdf) {
        return pdf.getAPIVersion() == null;
    }

    @Override
    public byte[] processClass(PluginDescriptionFile pdf, String path, byte[] clazz) {
        try {
            clazz = Commodore.convert(clazz, !isLegacy(pdf), pdf.getName());
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Fatal error trying to convert " + pdf.getFullName() + ":" + path, ex);
        }
        return clazz;
    }

    // Paper start
    public boolean isSupportedApiVersion(String apiVersion) {
        return apiVersion != null && SUPPORTED_API.contains(apiVersion);
    }
    // Paper end

    /**
     * This helper class represents the different NBT Tags.
     * <p>
     * These should match NBTBase#getTypeId
     */
    public static class NBT {
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

    public static Fluid getFluid(net.minecraft.fluid.Fluid fluid) {
        return FLUID_MATERIAL.get(fluid);
    }

    public static net.minecraft.fluid.Fluid getFluid(Fluid fluid) {
        return MATERIAL_FLUID.get(fluid);
    }

}