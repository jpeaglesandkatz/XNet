package mcjty.xnet.modules.cables.blocks;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IValue;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.api.xnet.tiles.IConnectorTile;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.facade.IFacadeSupport;
import mcjty.xnet.modules.facade.MimicBlockSupport;
import mcjty.xnet.setup.Config;
import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.xnet.modules.cables.CableSetup.TYPE_CONNECTOR;

public class ConnectorTileEntity extends GenericTileEntity implements IFacadeSupport, IConnectorTile {

    public static final String CMD_ENABLE = "connector.enable";
    public static final Key<Integer> PARAM_FACING = new Key<>("facing", Type.INTEGER);
    public static final Key<Boolean> PARAM_ENABLED = new Key<>("enabled", Type.BOOLEAN);

    private MimicBlockSupport mimicBlockSupport = new MimicBlockSupport();

    private int energy = 0;
    private int inputFromSide[] = new int[] { 0, 0, 0, 0, 0, 0 };
    private String name = "";

    // Count the number of redstone pulses we got
    private int pulseCounter;
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };

    private byte enabled = 0x3f;

    private LazyOptional<SidedHandler>[] sidedStorages;

    private Block[] cachedNeighbours = new Block[OrientationTools.DIRECTION_VALUES.length];

    public static final Key<String> VALUE_NAME = new Key<>("name", Type.STRING);

    @Override
    public IValue<?>[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_NAME, this::getConnectorName, this::setConnectorName),
        };
    }

    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Connector")
            .containerSupplier((windowId,player) -> new GenericContainer(CableSetup.CONTAINER_CONNECTOR.get(), windowId, EmptyContainer.CONTAINER_FACTORY, getPos(), ConnectorTileEntity.this)));

    public ConnectorTileEntity() {
        this(TYPE_CONNECTOR.get());
    }

    protected ConnectorTileEntity(TileEntityType<?> type) {
        super(type);
        sidedStorages = new LazyOptional[OrientationTools.DIRECTION_VALUES.length];
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            sidedStorages[direction.ordinal()] = LazyOptional.of(() -> createSidedHandler(direction));
        }

    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        BlockState oldMimicBlock = mimicBlockSupport.getMimicBlock();
        byte oldEnabled = enabled;

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            if (enabled != oldEnabled || mimicBlockSupport.getMimicBlock() != oldMimicBlock) {
                ModelDataManager.requestModelDataRefresh(this);
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
            }
        }
    }

    public int getPowerOut(Direction side) {
        return powerOut[side.ordinal()];
    }

    public void setPowerOut(Direction side, int powerOut) {
        if (powerOut > 15) {
            powerOut = 15;
        }
        if (this.powerOut[side.ordinal()] == powerOut) {
            return;
        }
        this.powerOut[side.ordinal()] = powerOut;
        markDirty();
        world.neighborChanged(pos.offset(side), this.getBlockState().getBlock(), this.pos);
    }

    public void setEnabled(Direction direction, boolean e) {
        if (e) {
            enabled |= 1 << direction.ordinal();
        } else {
            enabled &= ~(1 << direction.ordinal());
        }
        markDirtyClient();
    }

    public boolean isEnabled(Direction direction) {
        return (enabled & (1 << direction.ordinal())) != 0;
    }

    @Override
    public BlockState getMimicBlock() {
        return mimicBlockSupport.getMimicBlock();
    }

    public void setMimicBlock(BlockState mimicBlock) {
        mimicBlockSupport.setMimicBlock(mimicBlock);
        markDirtyClient();
    }

    @Override
    public void setPowerInput(int powered) {
        if (powerLevel == 0 && powered > 0) {
            pulseCounter++;
        }
        super.setPowerInput(powered);
    }

    @Override
    public int getPulseCounter() {
        return pulseCounter;
    }

    // Optimization to only increase the network if there is an actual block change
    public void possiblyMarkNetworkDirty(@Nonnull BlockPos neighbor) {
        for (Direction facing : OrientationTools.DIRECTION_VALUES) {
            if (getPos().offset(facing).equals(neighbor)) {
                Block newblock = world.getBlockState(neighbor).getBlock();
                if (newblock != cachedNeighbours[facing.ordinal()]) {
                    cachedNeighbours[facing.ordinal()] = newblock;
                    WorldBlob worldBlob = XNetBlobData.get(world).getWorldBlob(world);
                    worldBlob.markNetworkDirty(worldBlob.getNetworkAt(getPos()));
                }
                return;
            }
        }
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        energy = tagCompound.getInt("energy");
        inputFromSide = tagCompound.getIntArray("inputs");
        if (inputFromSide.length != 6) {
            inputFromSide = new int[] { 0, 0, 0, 0, 0, 0 };
        }
        mimicBlockSupport.readFromNBT(tagCompound);
        pulseCounter = tagCompound.getInt("pulse");
        for (int i = 0 ; i < 6 ; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    public void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        name = info.getString("name");
        if (info.contains("enabled")) {
            enabled = info.getByte("enabled");
        } else {
            enabled = 0x3f;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putInt("energy", energy);
        tagCompound.putIntArray("inputs", inputFromSide);
        mimicBlockSupport.writeToNBT(tagCompound);
        tagCompound.putInt("pulse", pulseCounter);
        for (int i = 0 ; i < 6 ; i++) {
            tagCompound.putByte("p" + i, (byte) powerOut[i]);
        }
        return tagCompound;
    }

    @Override
    public void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putString("name", name);
        info.putByte("enabled", enabled);
    }

    @Override
    public int getPowerLevel() {
        return powerLevel;
    }

    public void setConnectorName(String n) {
        this.name = n;
        markDirtyClient();
    }


    public String getConnectorName() {
        return name;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        if (this.energy != energy) {
            if (energy < 0) {
                energy = 0;
            }
            this.energy = energy;
            markDirtyQuick();
        }
    }

    public void setEnergyInputFrom(Direction from, int rate) {
        if (inputFromSide[from.ordinal()] != rate) {
            inputFromSide[from.ordinal()] = rate;
            markDirtyQuick();
        }
    }

    public int getMaxEnergy() {
        return Config.maxRfConnector.get();
    }

    private int receiveEnergyInternal(Direction from, int maxReceive, boolean simulate) {
        if (from == null) {
            return 0;
        }
        int m = inputFromSide[from.ordinal()];
        if (m > 0) {
            int toreceive = Math.min(maxReceive, m);
            int newenergy = energy + toreceive;
            if (newenergy > getMaxEnergy()) {
                toreceive -= newenergy - getMaxEnergy();
                newenergy = getMaxEnergy();
            }
            if (!simulate && energy != newenergy) {
                energy = newenergy;
                inputFromSide[from.ordinal()] = 0;
                markDirtyQuick();
            }
            return toreceive;
        }
        return 0;
    }

    private int getEnergyStoredInternal() {
        return energy;
    }

    private int getMaxEnergyStoredInternal() {
        return getMaxEnergy();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(GenericCableBlock.FACADEID, getMimicBlock())
                .build();
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_ENABLE.equals(command)) {
            int f = params.get(PARAM_FACING);
            boolean e = params.get(PARAM_ENABLED);
            setEnabled(OrientationTools.DIRECTION_VALUES[f], e);
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            if (side == null) {
                return LazyOptional.empty();
            } else {
                return sidedStorages[side.ordinal()].cast();
            }
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    private SidedHandler createSidedHandler(Direction facing) {
        return new SidedHandler(facing);
    }

    class SidedHandler implements IEnergyStorage {

        private final Direction facing;

        public SidedHandler(Direction facing) {
            this.facing = facing;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return ConnectorTileEntity.this.receiveEnergyInternal(facing, maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return ConnectorTileEntity.this.getEnergyStoredInternal();
        }

        @Override
        public int getMaxEnergyStored() {
            return ConnectorTileEntity.this.getMaxEnergyStoredInternal();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }

}
