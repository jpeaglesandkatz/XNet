package mcjty.xnet.multiblock;

import mcjty.rftoolsbase.api.xnet.channels.IChannelType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public record WirelessChannelKey(@Nonnull String name,
                                 @Nonnull IChannelType channelType,
                                 @Nullable UUID owner) {
    @Override
    public String toString() {
        return "WirelessChannelKey{" +
                "name='" + name + '\'' +
                ", channelType=" + channelType +
                ", owner=" + owner +
                '}';
    }
}
