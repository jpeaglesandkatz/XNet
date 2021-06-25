package mcjty.xnet.modules.controller.client;

import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.keys.SidedPos;
import mcjty.xnet.modules.cables.blocks.ConnectorBlock;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import net.minecraft.client.Minecraft;

import java.util.Map;

import static mcjty.xnet.modules.controller.blocks.TileEntityController.*;

public class ConnectorEditorPanel extends AbstractEditorPanel {

    private final int channel;
    private final SidedPos sidedPos;
    private final boolean advanced;

    @Override
    protected void update(String tag, Object value) {
        data.put(tag, value);
        TypedMap.Builder builder = TypedMap.builder();
        int i = 0;
        builder.put(PARAM_POS, sidedPos.getPos())
            .put(PARAM_SIDE, sidedPos.getSide().ordinal())
            .put(PARAM_CHANNEL, channel);
        performUpdate(builder, i, TileEntityController.CMD_UPDATECONNECTOR);
    }

    public ConnectorEditorPanel(Panel panel, Minecraft mc, GuiController gui, int channel, SidedPos sidedPos) {
        super(panel, mc, gui);
        this.channel = channel;
        this.sidedPos = sidedPos;
        advanced = ConnectorBlock.isAdvancedConnector(mc.level, sidedPos.getPos().relative(sidedPos.getSide()));
    }

    @Override
    public boolean isAdvanced() {
        return advanced;
    }

    public void setState(IConnectorSettings settings) {
        for (Map.Entry<String, Widget<?>> entry : components.entrySet()) {
            entry.getValue().enabled(settings.isEnabled(entry.getKey()));
        }
    }
}
