package mcjty.xnet.blocks.cables;

import mcjty.xnet.config.ConfigSetup;

import static mcjty.xnet.blocks.cables.NetCableSetup.TYPE_ADVANCED_CONNECTOR;

public class AdvancedConnectorTileEntity extends ConnectorTileEntity {

    public AdvancedConnectorTileEntity() {
        super(TYPE_ADVANCED_CONNECTOR);
    }

    @Override
    public int getMaxEnergy() {
        return ConfigSetup.maxRfAdvancedConnector.get();
    }
}
