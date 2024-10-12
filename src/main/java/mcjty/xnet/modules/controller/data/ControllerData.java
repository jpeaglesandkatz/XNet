package mcjty.xnet.modules.controller.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.xnet.modules.controller.ChannelInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Optional;

public record ControllerData(int colors, List<ChannelInfo> channels) {

    public static final Codec<ControllerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("colors").forGetter(ControllerData::colors),
            ChannelInfo.CODEC.xmap(Optional::ofNullable, s -> s.orElse(null)).listOf().fieldOf("channels").forGetter(ControllerData::getOptionalChannels)
    ).apply(instance, ControllerData::createWithOptional));

    private List<Optional<ChannelInfo>> getOptionalChannels() {
        return channels.stream().map(Optional::ofNullable).toList();
    }

    static ControllerData createWithOptional(int colors, List<Optional<ChannelInfo>> channels) {
        return new ControllerData(colors, channels.stream().map(channelInfo -> channelInfo.orElse(null)).toList());
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ControllerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ControllerData::colors,
            ChannelInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), ControllerData::channels,
            ControllerData::new
    );

    public ControllerData withColors(int colors) {
        return new ControllerData(colors, channels);
    }
}
