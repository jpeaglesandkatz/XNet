package mcjty.xnet.modules.controller.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.xnet.modules.controller.ChannelInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record ControllerData(int colors, List<ChannelInfo> channels) {

    public static final Codec<ControllerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("colors").forGetter(ControllerData::colors),
            ChannelInfo.CODEC.listOf().fieldOf("channels").forGetter(ControllerData::channels)
    ).apply(instance, ControllerData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ControllerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ControllerData::colors,
            ChannelInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), ControllerData::channels,
            ControllerData::new
    );

    public ControllerData withColors(int colors) {
        return new ControllerData(colors, channels);
    }
}
