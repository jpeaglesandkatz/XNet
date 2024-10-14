package mcjty.xnet.apiimpl.none;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoneChannelType implements IChannelType {

    @Override
    public String getID() {
        return "xnet.none";
    }

    @Override
    public String getName() {
        return "None";
    }

    @Override
    public MapCodec<? extends IChannelSettings> getCodec() {
        return NoneChannelSettings.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IChannelSettings> getStreamCodec() {
        return NoneChannelSettings.STREAM_CODEC;
    }

    @Override
    public MapCodec<? extends IConnectorSettings> getConnectorCodec() {
        return NoneConnectorSettings.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IConnectorSettings> getConnectorStreamCodec() {
        return NoneConnectorSettings.STREAM_CODEC;
    }

    @Override
    public boolean supportsBlock(@Nonnull Level world, @Nonnull BlockPos pos, @Nullable Direction side) {
        return false;
    }

    @Override
    @Nonnull
    public IConnectorSettings createConnector(@Nonnull Direction side) {
        return new NoneConnectorSettings(side);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new NoneChannelSettings();
    }
}
