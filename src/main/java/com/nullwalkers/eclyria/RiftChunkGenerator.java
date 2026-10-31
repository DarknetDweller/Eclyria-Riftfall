package com.nullwalkers.eclyria;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Locale;
import java.util.Random;

/** Lightweight, deterministic, theme-driven terrain for newly-created Rift worlds. */
public final class RiftChunkGenerator extends ChunkGenerator {
    private final String theme;

    public RiftChunkGenerator(String theme) {
        String value = theme == null ? "verdant" : theme.toLowerCase(Locale.ROOT);
        this.theme = switch (value) {
            case "ember", "void" -> value;
            default -> "verdant";
        };
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData data) {
        final int minY = Math.max(worldInfo.getMinHeight(), -64);
        final int maxY = Math.min(worldInfo.getMaxHeight() - 1, 319);
        final int seaLevel = theme.equals("ember") ? 54 : theme.equals("void") ? 42 : 68;
        final Material stone = theme.equals("ember") ? Material.BLACKSTONE
                : theme.equals("void") ? Material.DEEPSLATE : Material.STONE;
        final Material surface = theme.equals("ember") ? Material.BASALT
                : theme.equals("void") ? Material.CRYING_OBSIDIAN : Material.MOSS_BLOCK;
        final Material upper = theme.equals("ember") ? Material.MAGMA_BLOCK
                : theme.equals("void") ? Material.PURPLE_TERRACOTTA : Material.MOSS_BLOCK;
        final long seed = worldInfo.getSeed() ^ (theme.hashCode() * 0x9E3779B97F4A7C15L);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                double broad = Math.sin((worldX + (theme.equals("ember") ? 71 : 0)) * 0.018)
                        * 9.0 + Math.cos((worldZ - (theme.equals("void") ? 43 : 0)) * 0.021) * 7.0;
                double detail = Math.sin((worldX + worldZ) * 0.075) * 2.3
                        + Math.cos((worldX - worldZ) * 0.052) * 1.8;
                Random column = new Random(seed ^ (worldX * 341873128712L) ^ (worldZ * 132897987541L));
                int top = Math.max(minY + 12, Math.min(maxY - 8, seaLevel + (int)Math.round(broad + detail) + column.nextInt(3)));
                int floor = Math.max(minY, seaLevel - 35);
                for (int y = floor; y <= top; y++) {
                    // Deterministic pockets and winding cave-like voids, restricted below the surface.
                    boolean cave = y < top - 4 && y > floor + 3
                            && (Math.sin(worldX * 0.12 + y * 0.19) + Math.cos(worldZ * 0.11 - y * 0.17)
                            + Math.sin((worldX + worldZ) * 0.07 + y * 0.08)) > 2.35;
                    if (cave) continue;
                    Material block = y == floor ? Material.BEDROCK
                            : y < floor + 4 ? (theme.equals("ember") ? Material.BASALT : Material.DEEPSLATE)
                            : y >= top - 1 ? (y == top ? surface : upper) : stone;
                    data.setBlock(x, y, z, block);
                }
                if (theme.equals("ember") && column.nextInt(85) == 0 && top > floor + 10) {
                    int lavaY = floor + 5 + column.nextInt(Math.max(1, top - floor - 10));
                    data.setBlock(x, lavaY, z, Material.LAVA);
                }
                if (theme.equals("verdant") && column.nextInt(75) == 0) {
                    int crystalY = Math.min(maxY - 2, top + 1);
                    data.setBlock(x, crystalY, z, Material.AMETHYST_BLOCK);
                }
                if (theme.equals("void") && column.nextInt(60) == 0 && top > floor + 6) {
                    data.setBlock(x, top - 2, z, Material.SCULK);
                }
                // Ore families are represented by vanilla block types and converted to Eclyria drops by the core listener.
                if (column.nextInt(32) == 0) {
                    placeOre(column, data, x, z, floor + 5, Math.max(floor + 6, top - 7));
                }
            }
        }
    }

    private void placeOre(Random random, ChunkData data, int x, int z, int low, int high) {
        if (high <= low) return;
        int y = low + random.nextInt(high - low + 1);
        Material ore;
        if (theme.equals("verdant")) {
            ore = switch (random.nextInt(3)) {
                case 0 -> Material.EMERALD_ORE;
                case 1 -> Material.COAL_ORE;
                default -> Material.DIAMOND_ORE;
            };
        } else if (theme.equals("ember")) {
            ore = switch (random.nextInt(4)) {
                case 0 -> Material.NETHER_GOLD_ORE;
                case 1 -> Material.COPPER_ORE;
                case 2 -> Material.REDSTONE_ORE;
                default -> Material.GOLD_ORE;
            };
        } else {
            ore = switch (random.nextInt(4)) {
                case 0 -> Material.LAPIS_ORE;
                case 1 -> Material.IRON_ORE;
                case 2 -> Material.NETHER_QUARTZ_ORE;
                default -> Material.DIAMOND_ORE;
            };
        }
        data.setBlock(x, y, z, ore);
    }

    @Override
    public boolean shouldGenerateCaves() { return false; }
    @Override
    public boolean shouldGenerateDecorations() { return false; }
    @Override
    public boolean shouldGenerateMobs() { return true; }
    @Override
    public boolean shouldGenerateStructures() { return false; }
}
