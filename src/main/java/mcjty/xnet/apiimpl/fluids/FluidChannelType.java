package mcjty.xnet.apiimpl.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidChannelType implements IChannelType {

    public static final MapCodec<FluidChannelSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidChannelSettings.ChannelMode.CODEC.fieldOf("mode").forGetter(FluidChannelSettings::getChannelMode),
            Codec.INT.fieldOf("delay").forGetter(settings -> settings.delay),
            Codec.INT.fieldOf("offset").forGetter(settings -> settings.roundRobinOffset)
    ).apply(instance, (mode, delay, offset) -> {
        FluidChannelSettings settings = new FluidChannelSettings();
        settings.channelMode = mode;
        settings.delay = delay;
        settings.roundRobinOffset = offset;
        return settings;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidChannelSettings> STREAM_CODEC = StreamCodec.composite(
            FluidChannelSettings.ChannelMode.STREAM_CODEC, FluidChannelSettings::getChannelMode,
            ByteBufCodecs.INT, s -> s.delay,
            ByteBufCodecs.INT, s -> s.roundRobinOffset,
            (mode, delay, offset) -> {
                FluidChannelSettings settings = new FluidChannelSettings();
                settings.channelMode = mode;
                settings.delay = delay;
                settings.roundRobinOffset = offset;
                return settings;
            }
    );

    @Override
    public String getID() {
        return "xnet.fluid";
    }

    @Override
    public String getName() {
        return "Fluid";
    }

    @Override
    public MapCodec<? extends IChannelSettings> getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IChannelSettings> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public boolean supportsBlock(@Nonnull Level world, @Nonnull BlockPos pos, @Nullable Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te == null) {
            return false;
        }
        if (world.getCapability(Capabilities.FluidHandler.BLOCK, pos, side) != null) {
            return true;
        }
        return false;
    }

    @Override
    @Nonnull
    public IConnectorSettings createConnector(@Nonnull Direction side) {
        return new FluidConnectorSettings(side);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new FluidChannelSettings();
    }
}
