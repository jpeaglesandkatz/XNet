package mcjty.xnet.modules.router.data;

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
            Codec.unboundedMap(LocalChannelId.CODEC, Codec.STRING).fieldOf("publishedChannels").forGetter(RouterData::publishedChannels)
    ).apply(instance, RouterData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RouterData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, RouterData::channelCount,
            ByteBufCodecs.map(HashMap::new, LocalChannelId.STREAM_CODEC, ByteBufCodecs.STRING_UTF8), RouterData::publishedChannels,
            RouterData::new
    );

    public RouterData withChannelCount(int channelCount) {
        return new RouterData(channelCount, publishedChannels);
    }
}
