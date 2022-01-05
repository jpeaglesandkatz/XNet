package mcjty.xnet.multiblock;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.keys.NetworkId;
import mcjty.xnet.XNet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class XNetWirelessChannels extends AbstractWorldData<XNetWirelessChannels> {

    private static final String NAME = "XNetWirelessChannels";

    private final Map<WirelessChannelKey, WirelessChannelInfo> channelToWireless = new HashMap<>();

    public XNetWirelessChannels(String name) {
        super(name);
    }

    private int cnt = 30;

    private int globalChannelVersion = 0;

    public void transmitChannel(String channel, @Nonnull IChannelType channelType, @Nullable UUID ownerUUID, ResourceKey<Level> dimension, BlockPos wirelessRouterPos, NetworkId network) {
        WirelessChannelInfo channelInfo;
        WirelessChannelKey key = new WirelessChannelKey(channel, channelType, ownerUUID);
        if (channelToWireless.containsKey(key)) {
            channelInfo = channelToWireless.get(key);
        } else {
            channelInfo = new WirelessChannelInfo();
            System.out.println("New channel: key = " + key);
            channelToWireless.put(key, channelInfo);
        }

        GlobalPos pos = GlobalPos.of(dimension, wirelessRouterPos);
        WirelessRouterInfo info = channelInfo.getRouter(pos);
        if (info == null) {
            info = new WirelessRouterInfo(pos);
            channelInfo.updateRouterInfo(pos, info);
            channelInfo.incVersion();

            // Global version increase to make sure all wireless routers can detect if there is a need
            // to make their local network dirty to ensure that all controllers connected to that network
            // get a chance to pick up the new transmitted channel
            channelUpdated();
        }
        info.setAge(0);
        info.setNetworkId(network);
        save();

//        cnt--;
//        if (cnt > 0) {
//            return;
//        }
//        cnt = 30;
//        dump();
    }

    private void channelUpdated() {
        globalChannelVersion++;
    }

    public int getGlobalChannelVersion() {
        return globalChannelVersion;
    }

    private void dump() {
        for (Map.Entry<WirelessChannelKey, WirelessChannelInfo> entry : channelToWireless.entrySet()) {
            System.out.println("Channel = " + entry.getKey());
            WirelessChannelInfo channelInfo = entry.getValue();
            for (Map.Entry<GlobalPos, WirelessRouterInfo> infoEntry : channelInfo.getRouters().entrySet()) {
                GlobalPos pos = infoEntry.getKey();
                WirelessRouterInfo info = infoEntry.getValue();
                System.out.println("    Pos = " + BlockPosTools.toString(pos.pos()) + " (age " + info.age + ", net " + info.networkId.getId() + ")");
            }
        }
    }

    public void tick(Level world, int amount) {
        if (channelToWireless.isEmpty()) {
            return;
        }

        XNetBlobData blobData = XNetBlobData.get(world);

        Set<WirelessChannelKey> toDeleteChannel = new HashSet<>();
        for (Map.Entry<WirelessChannelKey, WirelessChannelInfo> entry : channelToWireless.entrySet()) {
            WirelessChannelInfo channelInfo = entry.getValue();
            Set<GlobalPos> toDelete = new HashSet<>();
            for (Map.Entry<GlobalPos, WirelessRouterInfo> infoEntry : channelInfo.getRouters().entrySet()) {
                WirelessRouterInfo info = infoEntry.getValue();
                int age = info.getAge();
                age += amount;
                info.setAge(age);
                if (age > 40) { // @todo configurable
                    System.out.println("toDelete.add: infoEntry.getKey() = " + infoEntry.getKey());
                    toDelete.add(infoEntry.getKey());
                }
            }
            for (GlobalPos pos : toDelete) {
                WorldBlob worldBlob = blobData.getWorldBlob(pos.dimension());
                NetworkId networkId = channelInfo.getRouter(pos).getNetworkId();
//                System.out.println("Clean up wireless network = " + networkId + " (" + entry.getKey() + ")");
                worldBlob.markNetworkDirty(networkId);
                channelInfo.removeRouterInfo(pos);
                channelInfo.incVersion();
            }
            if (channelInfo.getRouters().isEmpty()) {
                toDeleteChannel.add(entry.getKey());
            }
        }

        if (!toDeleteChannel.isEmpty()) {
            for (WirelessChannelKey key : toDeleteChannel) {
                System.out.println("actual delete: key = " + key);
                channelToWireless.remove(key);
            }
        }

        save();
    }

    @Nonnull
    public static XNetWirelessChannels get(Level world) {
        return getData(world, () -> new XNetWirelessChannels(NAME), NAME);
    }

    public WirelessChannelInfo findChannel(String name, @Nonnull IChannelType channelType, @Nullable UUID owner) {
        WirelessChannelKey key = new WirelessChannelKey(name, channelType, owner);
        return findChannel(key);
    }

    public WirelessChannelInfo findChannel(WirelessChannelKey key) {
        return channelToWireless.get(key);
    }

    public Stream<WirelessChannelInfo> findChannels(@Nullable UUID owner) {
        return channelToWireless.entrySet().stream().filter(pair -> {
            WirelessChannelKey key = pair.getKey();
            return (owner == null && key.getOwner() == null) || (owner != null && (key.getOwner() == null || owner.equals(key.getOwner())));
        }).map(Map.Entry::getValue);
    }

    @Override
    public void load(CompoundTag compound) {
        channelToWireless.clear();
        ListTag tagList = compound.getList("channels", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < tagList.size() ; i++) {
            CompoundTag tc = tagList.getCompound(i);
            WirelessChannelInfo channelInfo = new WirelessChannelInfo();
            readRouters(tc.getList("routers", Constants.NBT.TAG_COMPOUND), channelInfo);
            UUID owner = null;
            if (tc.hasUUID("owner")) {
                owner = tc.getUUID("owner");
            }
            String name = tc.getString("name");
            IChannelType type = XNet.xNetApi.findType(tc.getString("type"));
            channelToWireless.put(new WirelessChannelKey(name, type, owner), channelInfo);
        }
    }

    private void readRouters(ListTag tagList, WirelessChannelInfo channelInfo) {
        for (int i = 0 ; i < tagList.size() ; i++) {
            CompoundTag tc = tagList.getCompound(i);
            ResourceKey<Level> dim = LevelTools.getId(tc.getString("dim"));
            GlobalPos pos = GlobalPos.of(dim, new BlockPos(tc.getInt("x"), tc.getInt("y"), tc.getInt("z")));
            WirelessRouterInfo info = new WirelessRouterInfo(pos);
            info.setAge(tc.getInt("age"));
            info.setNetworkId(new NetworkId(tc.getInt("network")));
            channelInfo.updateRouterInfo(pos, info);
        }
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        ListTag channelTagList = new ListTag();

        for (Map.Entry<WirelessChannelKey, WirelessChannelInfo> entry : channelToWireless.entrySet()) {
            CompoundTag channelTc = new CompoundTag();
            WirelessChannelInfo channelInfo = entry.getValue();
            WirelessChannelKey key = entry.getKey();
            channelTc.putString("name", key.getName());
            channelTc.putString("type", key.getChannelType().getID());
            if (key.getOwner() != null) {
                channelTc.putUUID("owner", key.getOwner());
            }
            channelTc.put("routers", writeRouters(channelInfo));
            channelTagList.add(channelTc);
        }

        compound.put("channels", channelTagList);

        return compound;
    }

    private ListTag writeRouters(WirelessChannelInfo channelInfo) {
        ListTag tagList = new ListTag();

        for (Map.Entry<GlobalPos, WirelessRouterInfo> infoEntry : channelInfo.getRouters().entrySet()) {
            CompoundTag tc = new CompoundTag();
            GlobalPos pos = infoEntry.getKey();
            tc.putString("dim", pos.dimension().location().toString());
            tc.putInt("x", pos.pos().getX());
            tc.putInt("y", pos.pos().getY());
            tc.putInt("z", pos.pos().getZ());
            WirelessRouterInfo info = infoEntry.getValue();
            tc.putInt("age", info.getAge());
            tc.putInt("network", info.getNetworkId().getId());
            tagList.add(tc);
        }
        return tagList;
    }

    public static class WirelessChannelInfo {
        private final Map<GlobalPos, WirelessRouterInfo> routers = new HashMap<>();
        private int version = 0;

        public void updateRouterInfo(GlobalPos pos, WirelessRouterInfo info) {
            routers.put(pos, info);
        }

        public void removeRouterInfo(GlobalPos pos) {
            routers.remove(pos);
        }

        public WirelessRouterInfo getRouter(GlobalPos pos) {
            return routers.get(pos);
        }

        public Map<GlobalPos, WirelessRouterInfo> getRouters() {
            return routers;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public void incVersion() {
            version++;
        }
    }

    public static class WirelessRouterInfo {
        private int age;
        private NetworkId networkId;
        private final GlobalPos coordinate;

        public WirelessRouterInfo(GlobalPos coordinate) {
            age = 0;
            this.coordinate = coordinate;
        }

        public NetworkId getNetworkId() {
            return networkId;
        }

        public void setNetworkId(NetworkId networkId) {
            this.networkId = networkId;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public GlobalPos getCoordinate() {
            return coordinate;
        }
    }
}
