package mcjty.xnet.apiimpl.logic;

import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public boolean supportsBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nullable Direction side) {
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
