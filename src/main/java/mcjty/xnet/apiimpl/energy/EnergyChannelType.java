package mcjty.xnet.apiimpl.energy;

import com.mojang.serialization.Codec;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyChannelType implements IChannelType {

    public static final Codec<EnergyChannelSettings> CODEC = Codec.unit(new EnergyChannelSettings());
    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyChannelSettings> STREAM_CODEC = StreamCodec.unit(new EnergyChannelSettings());

    @Override
    public String getID() {
        return "xnet.energy";
    }

    @Override
    public String getName() {
        return "Energy";
    }

    @Override
    public Codec<? extends IChannelSettings> getCodec() {
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
        if (world.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side) != null) {
            return true;
        }
//        if (te instanceof IInventory) {
//            return true;
//        }
        return false;
    }

    @Override
    @Nonnull
    public IConnectorSettings createConnector(@Nonnull Direction side) {
        return new EnergyConnectorSettings(side);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new EnergyChannelSettings();
    }
}
