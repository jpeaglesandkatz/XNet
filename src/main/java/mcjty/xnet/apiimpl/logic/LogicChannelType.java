package mcjty.xnet.apiimpl.logic;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LogicChannelType implements IChannelType {

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
        return LogicChannelSettings.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IChannelSettings> getStreamCodec() {
        return LogicChannelSettings.STREAM_CODEC;
    }

    @Override
    public MapCodec<? extends IConnectorSettings> getConnectorCodec() {
        return LogicConnectorSettings.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IConnectorSettings> getConnectorStreamCodec() {
        return LogicConnectorSettings.STREAM_CODEC;
    }

    @Override
    public boolean supportsBlock(@Nonnull Level world, @Nonnull BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    @Nonnull
    public IConnectorSettings createConnector(@Nonnull Direction side) {
        return new LogicConnectorSettings(AbstractConnectorSettings.DEFAULT_SETTINGS, side);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new LogicChannelSettings();
    }
}
