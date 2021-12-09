package mcjty.xnet.modules.cables.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.api.xnet.tiles.IConnectorTile;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.facade.IFacadeSupport;
import mcjty.xnet.modules.facade.MimicBlockSupport;
import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import mcjty.xnet.setup.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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

import static mcjty.lib.api.container.DefaultContainerProvider.empty;
import static mcjty.xnet.modules.cables.CableModule.TYPE_CONNECTOR;

public class ConnectorTileEntity extends GenericTileEntity implements IFacadeSupport, IConnectorTile {

    private final MimicBlockSupport mimicBlockSupport = new MimicBlockSupport();

    private int energy = 0;
    private int inputFromSide[] = new int[] { 0, 0, 0, 0, 0, 0 };

    // Count the number of redstone pulses we got
    private int pulseCounter;
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };

    private byte enabled = 0x3f;

    private final LazyOptional<SidedHandler>[] sidedStorages;

    private final Block[] cachedNeighbours = new Block[OrientationTools.DIRECTION_VALUES.length];

    @GuiValue
    private String name = "";

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level.isClientSide && getMimicBlock() != null) {
            level.setBlockAndUpdate(worldPosition, getBlockState());
        }
    }

    @Cap(type = CapType.CONTAINER)
    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Connector")
            .containerSupplier(empty(CableModule.CONTAINER_CONNECTOR, this)));

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
        super.onDataPacket(net, packet);

        if (level.isClientSide) {
            ModelDataManager.requestModelDataRefresh(this);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
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
        setChanged();
        level.neighborChanged(worldPosition.relative(side), this.getBlockState().getBlock(), this.worldPosition);
    }

    public void setEnabled(Direction direction, boolean e) {
        if (e) {
            enabled |= 1 << direction.ordinal();
        } else {
            enabled &= ~(1 << direction.ordinal());
        }
        setChanged();
        Block block = getBlockState().getBlock();
        if (block instanceof GenericCableBlock) {
            level.setBlock(worldPosition, ((GenericCableBlock) block).calculateState(level, worldPosition, getBlockState()), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
        }
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
            if (getBlockPos().relative(facing).equals(neighbor)) {
                Block newblock = level.getBlockState(neighbor).getBlock();
                if (newblock != cachedNeighbours[facing.ordinal()]) {
                    cachedNeighbours[facing.ordinal()] = newblock;
                    WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);
                    worldBlob.markNetworkDirty(worldBlob.getNetworkAt(getBlockPos()));
                }
                return;
            }
        }
    }

    @Override
    public void load(CompoundNBT tagCompound) {
        super.load(tagCompound);
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
    public void loadInfo(CompoundNBT tagCompound) {
        super.loadInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        name = info.getString("name");
        if (info.contains("enabled")) {
            enabled = info.getByte("enabled");
        } else {
            enabled = 0x3f;
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundNBT tagCompound) {
        super.saveAdditional(tagCompound);
        tagCompound.putInt("energy", energy);
        tagCompound.putIntArray("inputs", inputFromSide);
        mimicBlockSupport.writeToNBT(tagCompound);
        tagCompound.putInt("pulse", pulseCounter);
        for (int i = 0 ; i < 6 ; i++) {
            tagCompound.putByte("p" + i, (byte) powerOut[i]);
        }
    }

    @Override
    public void writeClientDataToNBT(CompoundNBT tagCompound) {
        mimicBlockSupport.writeToNBT(tagCompound);
    }

    @Override
    public void readClientDataFromNBT(CompoundNBT tagCompound) {
        mimicBlockSupport.readFromNBT(tagCompound);
    }

    @Override
    public void saveInfo(CompoundNBT tagCompound) {
        super.saveInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putString("name", name);
        info.putByte("enabled", enabled);
    }

    public void setConnectorName(String n) {
        this.name = n;
        setChanged();
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


    public static final Key<Integer> PARAM_FACING = new Key<>("facing", Type.INTEGER);
    public static final Key<Boolean> PARAM_ENABLED = new Key<>("enabled", Type.BOOLEAN);
    @ServerCommand
    public static final Command<?> CMD_ENABLE = Command.<ConnectorTileEntity>create("connector.enable",
            (te, playerEntity, params) -> {
                int f = params.get(PARAM_FACING);
                boolean e = params.get(PARAM_ENABLED);
                te.setEnabled(OrientationTools.DIRECTION_VALUES[f], e);
            });

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
