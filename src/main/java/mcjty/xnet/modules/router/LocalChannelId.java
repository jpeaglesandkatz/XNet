package mcjty.xnet.modules.router;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;

public record LocalChannelId(@Nonnull BlockPos controllerPos, int index) {

    public static final Codec<LocalChannelId> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(LocalChannelId::controllerPos),
            Codec.INT.fieldOf("index").forGetter(LocalChannelId::index)
    ).apply(instance, LocalChannelId::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LocalChannelId> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, LocalChannelId::controllerPos,
            ByteBufCodecs.INT, LocalChannelId::index,
            LocalChannelId::new
    );
}
