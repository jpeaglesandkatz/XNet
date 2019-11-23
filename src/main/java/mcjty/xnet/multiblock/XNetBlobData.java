package mcjty.xnet.multiblock;

import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class XNetBlobData extends AbstractWorldData<XNetBlobData> {

    // @todo 1.14 CHECK

    private static final String NAME = "XNetBlobData";

    private final Map<Integer, WorldBlob> worldBlobMap = new HashMap<>();

    public XNetBlobData(String name) {
        super(name);
    }

    @Nonnull
    public static XNetBlobData getBlobData(World world) {
        return getData(world, () -> new XNetBlobData(NAME), NAME);
    }

    public WorldBlob getWorldBlob(World world) {
        return getWorldBlob(world.getDimension().getType().getId());
    }   // @todo 1.14 don't use numeric ID!

    public WorldBlob getWorldBlob(int dimId) {
        if (!worldBlobMap.containsKey(dimId)) {
            worldBlobMap.put(dimId, new WorldBlob(dimId));
        }
        return worldBlobMap.get(dimId);
    }


    @Override
    public void read(CompoundNBT compound) {
        worldBlobMap.clear();
        if (compound.contains("worlds")) {
            ListNBT worlds = (ListNBT) compound.get("worlds");
            for (int i = 0 ; i < worlds.size() ; i++) {
                CompoundNBT tc = (CompoundNBT) worlds.get(i);
                int id = tc.getInt("dimid");
                WorldBlob blob = new WorldBlob(id);
                blob.readFromNBT(tc);
                worldBlobMap.put(id, blob);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (Map.Entry<Integer, WorldBlob> entry : worldBlobMap.entrySet()) {
            WorldBlob blob = entry.getValue();
            CompoundNBT tc = new CompoundNBT();
            tc.putInt("dimid", blob.getDimId());
            blob.writeToNBT(tc);
            list.add(tc);
        }
        compound.put("worlds", list);

        return compound;
    }
}
