package mcjty.xnet.apiimpl.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

public class ItemChannelType implements IChannelType {

    public static final Codec<ItemChannelSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemChannelSettings.ChannelMode.CODEC.fieldOf("mode").forGetter(ItemChannelSettings::getChannelMode),
            Codec.INT.fieldOf("delay").forGetter(settings -> settings.delay),
            Codec.INT.fieldOf("offset").forGetter(settings -> settings.roundRobinOffset),
            Codec.unboundedMap(Codec.INT, Codec.INT).fieldOf("extidx").forGetter(ItemChannelSettings::getIndicesAsIntegerMap)
    ).apply(instance, (mode, delay, offset, indices) -> {
        ItemChannelSettings settings = new ItemChannelSettings();
        settings.channelMode = mode;
        settings.delay = delay;
        settings.roundRobinOffset = offset;
        settings.setIndicesAsIntegerMap(indices);
        return settings;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemChannelSettings> STREAM_CODEC = StreamCodec.composite(
            ItemChannelSettings.ChannelMode.STREAM_CODEC, ItemChannelSettings::getChannelMode,
            ByteBufCodecs.INT, s -> s.delay,
            ByteBufCodecs.INT, s -> s.roundRobinOffset,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ByteBufCodecs.INT), ItemChannelSettings::getIndicesAsIntegerMap,
            (mode, delay, offset, indices) -> {
                ItemChannelSettings settings = new ItemChannelSettings();
                settings.channelMode = mode;
                settings.delay = delay;
                settings.roundRobinOffset = offset;
                settings.setIndicesAsIntegerMap(indices);
                return settings;
            }
    );

    @Override
    public String getID() {
        return "xnet.item";
    }

    @Override
    public String getName() {
        return "Item";
    }

    @Override
    public Codec<? extends IChannelSettings> getCodec() {
        return null;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IChannelSettings> getStreamCodec() {
        return null;
    }

    @Override
    public boolean supportsBlock(@Nonnull Level world, @Nonnull BlockPos pos, @Nullable Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te == null) {
            return false;
        }
        if (world.getCapability(Capabilities.ItemHandler.BLOCK, pos, side) != null) {
            return true;
        }
        if (te instanceof Container) {
            return true;
        }
        return false;
    }

    @Override
    @Nonnull
    public IConnectorSettings createConnector(@Nonnull Direction side) {
        return new ItemConnectorSettings(side);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new ItemChannelSettings();
    }
}
