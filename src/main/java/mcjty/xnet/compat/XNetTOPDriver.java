package mcjty.xnet.compat;

import mcjty.lib.compat.theoneprobe.McJtyLibTOPDriver;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.rftoolsbase.api.xnet.keys.NetworkId;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.cables.blocks.ConnectorBlock;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.controller.ControllerSetup;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.modules.facade.FacadeSetup;
import mcjty.xnet.modules.router.RouterSetup;
import mcjty.xnet.modules.router.blocks.TileEntityRouter;
import mcjty.xnet.modules.wireless.WirelessRouterSetup;
import mcjty.xnet.modules.wireless.blocks.TileEntityWirelessRouter;
import mcjty.xnet.multiblock.BlobId;
import mcjty.xnet.multiblock.ColorId;
import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XNetTOPDriver implements TOPDriver {

    public static final XNetTOPDriver DRIVER = new XNetTOPDriver();

    private final Map<ResourceLocation, TOPDriver> drivers = new HashMap<>();

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        Block block = blockState.getBlock();
        ResourceLocation id = block.getRegistryName();
        if (!drivers.containsKey(id)) {
            if (block == CableSetup.NETCABLE.get() || block == FacadeSetup.FACADE.get()) {
                drivers.put(id, new CableDriver());
            } else if (block instanceof ConnectorBlock) {
                drivers.put(id, new ConnectorDriver());
            } else if (block == ControllerSetup.CONTROLLER.get()) {
                drivers.put(id, new ControllerDriver());
            } else if (block == RouterSetup.ROUTER.get()) {
                drivers.put(id, new RouterDriver());
            } else if (block == WirelessRouterSetup.WIRELESS_ROUTER.get()) {
                drivers.put(id, new WirelessRouterDriver());
            } else {
                drivers.put(id, new DefaultDriver());
            }
        }
        TOPDriver driver = drivers.get(id);
        if (driver != null) {
            driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class DefaultDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class CableDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockState(data.getPos()).getBlock(), (GenericCableBlock block) -> {
                WorldBlob worldBlob = XNetBlobData.get(world).getWorldBlob(world);

                if (mode == ProbeMode.DEBUG) {
                    BlobId blobId = worldBlob.getBlobAt(data.getPos());
                    if (blobId != null) {
                        probeInfo.text(TextStyleClass.LABEL + "Blob: " + TextStyleClass.INFO + blobId.getId());
                    }

                    ColorId colorId = worldBlob.getColorAt(data.getPos());
                    if (colorId != null) {
                        probeInfo.text(TextStyleClass.LABEL + "Color: " + TextStyleClass.INFO + colorId.getId());
                    }
                }

                Set<NetworkId> networks = worldBlob.getNetworksAt(data.getPos());
                for (NetworkId network : networks) {
                    if (mode == ProbeMode.DEBUG) {
                        probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + network.getId() + ", V: " +
                                worldBlob.getNetworkVersion(network));
                    } else {
                        probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + network.getId());
                    }
                }

                ConsumerId consumerId = worldBlob.getConsumerAt(data.getPos());
                if (consumerId != null) {
                    probeInfo.text(TextStyleClass.LABEL + "Consumer: " + TextStyleClass.INFO + consumerId.getId());
                }
            }, "Bad block!");
        }
    }

    private static class ConnectorDriver extends CableDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getTileEntity(data.getPos()), (ConnectorTileEntity te) -> {
                String name = te.getConnectorName();
                if (!name.isEmpty()) {
                    probeInfo.text(TextStyleClass.LABEL + "Name: " + TextStyleClass.INFO + name);
                }
            }, "Bad tile entity!");
        }
    }

    private static class ControllerDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getTileEntity(data.getPos()), (TileEntityController te) -> {
                WorldBlob worldBlob = XNetBlobData.get(world).getWorldBlob(world);

                NetworkId networkId = te.getNetworkId();
                if (networkId != null) {
                    if (mode == ProbeMode.DEBUG) {
                        probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + networkId.getId() + ", V: " +
                                worldBlob.getNetworkVersion(networkId));
                    } else {
                        probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + networkId.getId());
                    }
                }

                if (mode == ProbeMode.DEBUG) {
                    String s = "";
                    for (NetworkId id : te.getNetworkChecker().getAffectedNetworks()) {
                        s += id.getId() + " ";
                        if (s.length() > 15) {
                            probeInfo.text(TextStyleClass.LABEL + "InfNet: " + TextStyleClass.INFO + s);
                            s = "";
                        }
                    }
                    if (!s.isEmpty()) {
                        probeInfo.text(TextStyleClass.LABEL + "InfNet: " + TextStyleClass.INFO + s);
                    }
                }
                if (blockState.get(TileEntityController.ERROR)) {
                    probeInfo.text(TextStyleClass.ERROR + "Too many controllers on network!");
                }

                if (mode == ProbeMode.DEBUG) {
                    BlobId blobId = worldBlob.getBlobAt(data.getPos());
                    if (blobId != null) {
                        probeInfo.text(TextStyleClass.LABEL + "Blob: " + TextStyleClass.INFO + blobId.getId());
                    }
                    ColorId colorId = worldBlob.getColorAt(data.getPos());
                    if (colorId != null) {
                        probeInfo.text(TextStyleClass.LABEL + "Color: " + TextStyleClass.INFO + colorId.getId());
                    }

                    probeInfo.text(TextStyleClass.LABEL + "Color mask: " + te.getColors());
                }
            }, "Bad tile entity!");
        }
    }

    private static class RouterDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getTileEntity(data.getPos()), (TileEntityRouter te) -> {
                XNetBlobData blobData = XNetBlobData.get(world);
                WorldBlob worldBlob = blobData.getWorldBlob(world);
                Set<NetworkId> networks = worldBlob.getNetworksAt(data.getPos());
                for (NetworkId networkId : networks) {
                    probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + networkId.getId());
                    if (mode != ProbeMode.EXTENDED) {
                        break;
                    }
                }
                if (blockState.get(TileEntityController.ERROR)) {
                    probeInfo.text(TextStyleClass.ERROR + "Too many channels on router!");
                } else {
                    probeInfo.text(TextStyleClass.LABEL + "Channels: " + TextStyleClass.INFO + te.getChannelCount());
                }

                if (mode == ProbeMode.DEBUG) {
                    BlobId blobId = worldBlob.getBlobAt(data.getPos());
                    if (blobId != null) {
                        probeInfo.text(TextStyleClass.LABEL + "Blob: " + TextStyleClass.INFO + blobId.getId());
                    }
                    ColorId colorId = worldBlob.getColorAt(data.getPos());
                    if (colorId != null) {
                        probeInfo.text(TextStyleClass.LABEL + "Color: " + TextStyleClass.INFO + colorId.getId());
                    }
                }
            }, "Bad tile entity!");
        }
    }

    private static class WirelessRouterDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getTileEntity(data.getPos()), (TileEntityWirelessRouter te) -> {
                XNetBlobData blobData = XNetBlobData.get(world);
                WorldBlob worldBlob = blobData.getWorldBlob(world);
                Set<NetworkId> networks = worldBlob.getNetworksAt(data.getPos());
                for (NetworkId networkId : networks) {
                    probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + networkId.getId());
                    if (mode != ProbeMode.EXTENDED) {
                        break;
                    }
                }
                if (blockState.get(TileEntityController.ERROR)) {
                    probeInfo.text(TextStyleClass.ERROR + "Missing antenna!");
                } else {
//            probeInfo.text(TextStyleClass.LABEL + "Channels: " + TextStyleClass.INFO + getChannelCount());
                }

                if (mode == ProbeMode.DEBUG) {
                    BlobId blobId = worldBlob.getBlobAt(data.getPos());
                    if (blobId != null) {
                        probeInfo.text(TextStyleClass.LABEL + "Blob: " + TextStyleClass.INFO + blobId.getId());
                    }
                    ColorId colorId = worldBlob.getColorAt(data.getPos());
                    if (colorId != null) {
                        probeInfo.text(TextStyleClass.LABEL + "Color: " + TextStyleClass.INFO + colorId.getId());
                    }
                }
            }, "Bad tile entity!");
        }
    }
}
