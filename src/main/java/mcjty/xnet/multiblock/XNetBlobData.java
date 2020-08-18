package mcjty.xnet.multiblock;

import mcjty.lib.varia.DimensionId;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class XNetBlobData extends AbstractWorldData<XNetBlobData> {

    private static final String NAME = "XNetBlobData";

    private final Map<DimensionId, WorldBlob> worldBlobMap = new HashMap<>();

    public XNetBlobData(String name) {
        super(name);
    }

    @Nonnull
    public static XNetBlobData get(World world) {
        return getData(world, () -> new XNetBlobData(NAME), NAME);
    }

    public WorldBlob getWorldBlob(World world) {
        return getWorldBlob(DimensionId.fromWorld(world));
    }

    public WorldBlob getWorldBlob(DimensionId type) {
        if (!worldBlobMap.containsKey(type)) {
            worldBlobMap.put(type, new WorldBlob(type));
        }
        return worldBlobMap.get(type);
    }


    @Override
    public void read(CompoundNBT compound) {
        worldBlobMap.clear();
        if (compound.contains("worlds")) {
            ListNBT worlds = (ListNBT) compound.get("worlds");
            for (int i = 0 ; i < worlds.size() ; i++) {
                CompoundNBT tc = (CompoundNBT) worlds.get(i);
                String dimtype = tc.getString("dimtype");
                DimensionId dim = DimensionId.fromResourceLocation(new ResourceLocation(dimtype));
                WorldBlob blob = new WorldBlob(dim);
                blob.readFromNBT(tc);
                worldBlobMap.put(dim, blob);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (Map.Entry<DimensionId, WorldBlob> entry : worldBlobMap.entrySet()) {
            WorldBlob blob = entry.getValue();
            CompoundNBT tc = new CompoundNBT();
            tc.putString("dimtype", blob.getDimensionType().getRegistryName().toString());
            blob.writeToNBT(tc);
            list.add(tc);
        }
        compound.put("worlds", list);

        return compound;
    }
}
