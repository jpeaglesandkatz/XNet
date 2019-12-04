package mcjty.xnet.modules.cables.blocks;

import mcjty.xnet.config.ConfigSetup;

import static mcjty.xnet.modules.cables.CableSetup.TYPE_ADVANCED_CONNECTOR;

public class AdvancedConnectorTileEntity extends ConnectorTileEntity {

    public AdvancedConnectorTileEntity() {
        super(TYPE_ADVANCED_CONNECTOR.get());
    }

    @Override
    public int getMaxEnergy() {
        return ConfigSetup.maxRfAdvancedConnector.get();
    }
}
