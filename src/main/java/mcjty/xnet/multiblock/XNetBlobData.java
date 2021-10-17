package mcjty.xnet.multiblock;

import mcjty.lib.varia.LevelTools;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class XNetBlobData extends AbstractWorldData<XNetBlobData> {

    private static final String NAME = "XNetBlobData";

    private final Map<RegistryKey<World>, WorldBlob> worldBlobMap = new HashMap<>();

    public XNetBlobData(String name) {
        super(name);
    }

    @Nonnull
    public static XNetBlobData get(World world) {
        return getData(world, () -> new XNetBlobData(NAME), NAME);
    }

    public WorldBlob getWorldBlob(World world) {
        return getWorldBlob(world.dimension());
    }

    public WorldBlob getWorldBlob(RegistryKey<World> type) {
        if (!worldBlobMap.containsKey(type)) {
            worldBlobMap.put(type, new WorldBlob(type));
        }
        return worldBlobMap.get(type);
    }


    @Override
    public void load(CompoundNBT compound) {
        worldBlobMap.clear();
        if (compound.contains("worlds")) {
            ListNBT worlds = (ListNBT) compound.get("worlds");
            for (net.minecraft.nbt.INBT world : worlds) {
                CompoundNBT tc = (CompoundNBT) world;
                RegistryKey<World> dim = LevelTools.getId(tc.getString("dimtype"));
                WorldBlob blob = new WorldBlob(dim);
                blob.readFromNBT(tc);
                worldBlobMap.put(dim, blob);
            }
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (Map.Entry<RegistryKey<World>, WorldBlob> entry : worldBlobMap.entrySet()) {
            WorldBlob blob = entry.getValue();
            CompoundNBT tc = new CompoundNBT();
            tc.putString("dimtype", blob.getDimensionType().location().toString());
            blob.writeToNBT(tc);
            list.add(tc);
        }
        compound.put("worlds", list);

        return compound;
    }
}
