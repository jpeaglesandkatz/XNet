package mcjty.xnet.modules.cables.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CableItemData(ConsumerId id) {

    public static final CableItemData EMPTY = new CableItemData(new ConsumerId(-1));

    public static final Codec<CableItemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("id").forGetter(cableItemData -> cableItemData.id.id())
    ).apply(instance, id -> new CableItemData(new ConsumerId(id))));

    public static final StreamCodec<RegistryFriendlyByteBuf, CableItemData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, cableItemData -> cableItemData.id.id(),
            id -> new CableItemData(new ConsumerId(id))
    );
}
