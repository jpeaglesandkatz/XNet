package mcjty.xnet.multiblock;

import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class XNetBlobData extends AbstractWorldData<XNetBlobData> {

    // @todo 1.14 CHECK

    private static final String NAME = "XNetBlobData";

    private final Map<DimensionType, WorldBlob> worldBlobMap = new HashMap<>();

    public XNetBlobData(String name) {
        super(name);
    }

    @Nonnull
    public static XNetBlobData getBlobData(World world) {
        return getData(world, () -> new XNetBlobData(NAME), NAME);
    }

    public WorldBlob getWorldBlob(World world) {
        return getWorldBlob(world.getDimension().getType());
    }   // @todo 1.14 don't use numeric ID!

    public WorldBlob getWorldBlob(DimensionType type) {
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
                DimensionType dim = DimensionType.byName(new ResourceLocation(dimtype));
                WorldBlob blob = new WorldBlob(dim);
                blob.readFromNBT(tc);
                worldBlobMap.put(dim, blob);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (Map.Entry<DimensionType, WorldBlob> entry : worldBlobMap.entrySet()) {
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
