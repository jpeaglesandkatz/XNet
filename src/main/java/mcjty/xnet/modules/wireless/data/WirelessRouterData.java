package mcjty.xnet.modules.wireless.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WirelessRouterData(boolean publicAccess) {

    public static final WirelessRouterData EMPTY = new WirelessRouterData(false);

    public static final Codec<WirelessRouterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("publicAccess").forGetter(WirelessRouterData::publicAccess)
    ).apply(instance, WirelessRouterData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessRouterData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, WirelessRouterData::publicAccess,
            WirelessRouterData::new
    );

    public WirelessRouterData withPublicAccess(boolean publicAccess) {
        return new WirelessRouterData(publicAccess);
    }
}
