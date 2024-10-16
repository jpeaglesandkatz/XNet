package mcjty.xnet.modules.router.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.xnet.modules.router.LocalChannelId;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public record RouterData(int channelCount, Map<LocalChannelId, String> publishedChannels) {

    public static final RouterData EMPTY = new RouterData(0, new HashMap<>());

    public static final Codec<RouterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("channelCount").forGetter(RouterData::channelCount),
            Codec.pair(LocalChannelId.CODEC, Codec.STRING).listOf().fieldOf("publishedChannels").forGetter(s -> s.publishedChannels().entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).toList())
    ).apply(instance, (channelCount, publishedChannels) -> {
        Map<LocalChannelId, String> map = new HashMap<>();
        publishedChannels.forEach(pair -> map.put(pair.getFirst(), pair.getSecond()));
        return new RouterData(channelCount, map);
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, RouterData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, RouterData::channelCount,
            ByteBufCodecs.map(HashMap::new, LocalChannelId.STREAM_CODEC, ByteBufCodecs.STRING_UTF8), RouterData::publishedChannels,
            RouterData::new
    );

    public RouterData withChannelCount(int channelCount) {
        return new RouterData(channelCount, publishedChannels);
    }

    public RouterData removeChannel(LocalChannelId channel) {
        Map<LocalChannelId, String> newChannels = new HashMap<>(publishedChannels);
        newChannels.remove(channel);
        return new RouterData(channelCount, newChannels);
    }

    public RouterData addChannel(LocalChannelId channel, String name) {
        Map<LocalChannelId, String> newChannels = new HashMap<>(publishedChannels);
        newChannels.put(channel, name);
        return new RouterData(channelCount, newChannels);
    }
}
