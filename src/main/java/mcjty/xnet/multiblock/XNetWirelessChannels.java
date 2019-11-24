package mcjty.xnet.multiblock;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.keys.NetworkId;
import mcjty.xnet.XNet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class XNetWirelessChannels extends AbstractWorldData<XNetWirelessChannels> {

    // @todo 1.14

    private static final String NAME = "XNetWirelessChannels";

    private final Map<WirelessChannelKey, WirelessChannelInfo> channelToWireless = new HashMap<>();

    public XNetWirelessChannels(String name) {
        super(name);
    }

    private int cnt = 30;

    private int globalChannelVersion = 0;

    public void transmitChannel(String channel, @Nonnull IChannelType channelType, @Nullable UUID ownerUUID, DimensionType dimension, BlockPos wirelessRouterPos, NetworkId network) {
        WirelessChannelInfo channelInfo;
        WirelessChannelKey key = new WirelessChannelKey(channel, channelType, ownerUUID);
        if (channelToWireless.containsKey(key)) {
            channelInfo = channelToWireless.get(key);
        } else {
            channelInfo = new WirelessChannelInfo();
            channelToWireless.put(key, channelInfo);
        }

        GlobalCoordinate pos = new GlobalCoordinate(wirelessRouterPos, dimension);
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
            for (Map.Entry<GlobalCoordinate, WirelessRouterInfo> infoEntry : channelInfo.getRouters().entrySet()) {
                GlobalCoordinate pos = infoEntry.getKey();
                WirelessRouterInfo info = infoEntry.getValue();
                System.out.println("    Pos = " + BlockPosTools.toString(pos.getCoordinate()) + " (age " + info.age + ", net " + info.networkId.getId() + ")");
            }
        }
    }

    public void tick(World world, int amount) {
        if (channelToWireless.isEmpty()) {
            return;
        }

        XNetBlobData blobData = XNetBlobData.getBlobData(world);

        Set<WirelessChannelKey> toDeleteChannel = new HashSet<>();
        for (Map.Entry<WirelessChannelKey, WirelessChannelInfo> entry : channelToWireless.entrySet()) {
            WirelessChannelInfo channelInfo = entry.getValue();
            Set<GlobalCoordinate> toDelete = new HashSet<>();
            for (Map.Entry<GlobalCoordinate, WirelessRouterInfo> infoEntry : channelInfo.getRouters().entrySet()) {
                WirelessRouterInfo info = infoEntry.getValue();
                int age = info.getAge();
                age += amount;
                info.setAge(age);
                if (age > 40) { // @todo configurable
                    toDelete.add(infoEntry.getKey());
                }
            }
            for (GlobalCoordinate pos : toDelete) {
                WorldBlob worldBlob = blobData.getWorldBlob(pos.getDimension());
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
                channelToWireless.remove(key);
            }
        }

        save();
    }

    @Nonnull
    public static XNetWirelessChannels getWirelessChannels(World world) {
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
        }).map(pair -> pair.getValue());
    }

    @Override
    public void read(CompoundNBT compound) {
        channelToWireless.clear();
        ListNBT tagList = compound.getList("channels", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < tagList.size() ; i++) {
            CompoundNBT tc = tagList.getCompound(i);
            WirelessChannelInfo channelInfo = new WirelessChannelInfo();
            readRouters(tc.getList("routers", Constants.NBT.TAG_COMPOUND), channelInfo);
            UUID owner = null;
            if (tc.hasUniqueId("owner")) {
                owner = tc.getUniqueId("owner");
            }
            String name = tc.getString("name");
            IChannelType type = XNet.xNetApi.findType(tc.getString("type"));
            channelToWireless.put(new WirelessChannelKey(name, type, owner), channelInfo);
        }
    }

    private void readRouters(ListNBT tagList, WirelessChannelInfo channelInfo) {
        for (int i = 0 ; i < tagList.size() ; i++) {
            CompoundNBT tc = tagList.getCompound(i);
            GlobalCoordinate pos = new GlobalCoordinate(new BlockPos(tc.getInt("x"), tc.getInt("y"), tc.getInt("z")), DimensionType.getById(tc.getInt("dim"))); // @todo 1.14 dimension id!
            WirelessRouterInfo info = new WirelessRouterInfo(pos);
            info.setAge(tc.getInt("age"));
            info.setNetworkId(new NetworkId(tc.getInt("network")));
            channelInfo.updateRouterInfo(pos, info);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT channelTagList = new ListNBT();

        for (Map.Entry<WirelessChannelKey, WirelessChannelInfo> entry : channelToWireless.entrySet()) {
            CompoundNBT channelTc = new CompoundNBT();
            WirelessChannelInfo channelInfo = entry.getValue();
            WirelessChannelKey key = entry.getKey();
            channelTc.putString("name", key.getName());
            channelTc.putString("type", key.getChannelType().getID());
            if (key.getOwner() != null) {
                channelTc.putUniqueId("owner", key.getOwner());
            }
            channelTc.put("routers", writeRouters(channelInfo));
            channelTagList.add(channelTc);
        }

        compound.put("channels", channelTagList);

        return compound;
    }

    private ListNBT writeRouters(WirelessChannelInfo channelInfo) {
        ListNBT tagList = new ListNBT();

        for (Map.Entry<GlobalCoordinate, WirelessRouterInfo> infoEntry : channelInfo.getRouters().entrySet()) {
            CompoundNBT tc = new CompoundNBT();
            GlobalCoordinate pos = infoEntry.getKey();
            tc.putInt("dim", pos.getDimension().getId());   // @todo 1.14 (store reg!)
            tc.putInt("x", pos.getCoordinate().getX());
            tc.putInt("y", pos.getCoordinate().getY());
            tc.putInt("z", pos.getCoordinate().getZ());
            WirelessRouterInfo info = infoEntry.getValue();
            tc.putInt("age", info.getAge());
            tc.putInt("network", info.getNetworkId().getId());
            tagList.add(tc);
        }
        return tagList;
    }

    public static class WirelessChannelInfo {
        private final Map<GlobalCoordinate, WirelessRouterInfo> routers = new HashMap<>();
        private int version = 0;

        public void updateRouterInfo(GlobalCoordinate pos, WirelessRouterInfo info) {
            routers.put(pos, info);
        }

        public void removeRouterInfo(GlobalCoordinate pos) {
            routers.remove(pos);
        }

        public WirelessRouterInfo getRouter(GlobalCoordinate pos) {
            return routers.get(pos);
        }

        public Map<GlobalCoordinate, WirelessRouterInfo> getRouters() {
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
        private final GlobalCoordinate coordinate;

        public WirelessRouterInfo(GlobalCoordinate coordinate) {
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

        public GlobalCoordinate getCoordinate() {
            return coordinate;
        }
    }
}
