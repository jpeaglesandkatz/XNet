package mcjty.xnet.apiimpl.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LogicChannelType implements IChannelType {

    public static final MapCodec<LogicChannelSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("delay").forGetter(settings -> settings.delay),
            Codec.INT.fieldOf("colors").forGetter(settings -> settings.colors)
    ).apply(instance, (delay, colors) -> {
        LogicChannelSettings settings = new LogicChannelSettings();
        settings.delay = delay;
        settings.colors = colors;
        return settings;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, LogicChannelSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, s -> s.delay,
            ByteBufCodecs.INT, s -> s.colors,
            (delay, colors) -> {
                LogicChannelSettings settings = new LogicChannelSettings();
                settings.delay = delay;
                settings.colors = colors;
                return settings;
            }
    );

    @Override
    public String getID() {
        return "xnet.logic";
    }

    @Override
    public String getName() {
        return "Logic";
    }

    @Override
    public MapCodec<? extends IChannelSettings> getCodec() {
        return null;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IChannelSettings> getStreamCodec() {
        return null;
    }

    @Override
    public boolean supportsBlock(@Nonnull Level world, @Nonnull BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    @Nonnull
    public IConnectorSettings createConnector(@Nonnull Direction side) {
        return new LogicConnectorSettings(side);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new LogicChannelSettings();
    }
}
