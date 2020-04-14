package mcjty.xnet.client;

import mcjty.lib.network.NetworkTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.xnet.XNet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class ControllerChannelClientInfo {
    @Nonnull private final String channelName;
    @Nonnull private final String publishedName;
    @Nonnull private final BlockPos pos;
    @Nonnull private final IChannelType channelType;
    private final boolean remote;      // If this channel was made available through a wireless router
    private final int index;        // Index of the channel within that controller (0 through 7)

    public ControllerChannelClientInfo(@Nonnull String channelName, @Nonnull String publishedName, @Nonnull BlockPos pos, @Nonnull IChannelType channelType, boolean remote, int index) {
        this.channelName = channelName;
        this.publishedName = publishedName;
        this.pos = pos;
        this.channelType = channelType;
        this.remote = remote;
        this.index = index;
    }

    public ControllerChannelClientInfo(@Nonnull PacketBuffer buf) {
        channelName = NetworkTools.readStringUTF8(buf);
        publishedName = NetworkTools.readStringUTF8(buf);
        String id = buf.readString(32767);
        IChannelType t = XNet.xNetApi.findType(id);
        if (t == null) {
            throw new RuntimeException("Bad type: " + id);
        }
        channelType = t;
        pos = buf.readBlockPos();
        remote = buf.readBoolean();
        index = buf.readInt();
    }

    public void writeToNBT(@Nonnull PacketBuffer buf) {
        NetworkTools.writeStringUTF8(buf, channelName);
        NetworkTools.writeStringUTF8(buf, publishedName);
        buf.writeString(channelType.getID());
        buf.writeBlockPos(pos);
        buf.writeBoolean(remote);
        buf.writeInt(index);
    }

    @Nonnull
    public String getChannelName() {
        return channelName;
    }

    @Nonnull
    public String getPublishedName() {
        return publishedName;
    }

    @Nonnull
    public BlockPos getPos() {
        return pos;
    }

    @Nonnull
    public IChannelType getChannelType() {
        return channelType;
    }

    public int getIndex() {
        return index;
    }

    public boolean isRemote() {
        return remote;
    }
}
