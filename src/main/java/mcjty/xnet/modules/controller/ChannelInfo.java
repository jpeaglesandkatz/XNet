package mcjty.xnet.modules.controller;

import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.XNet;
import mcjty.xnet.client.ConnectorInfo;
import mcjty.xnet.utils.TagUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static mcjty.xnet.apiimpl.Constants.TAG_ADVANCED;
import static mcjty.xnet.apiimpl.Constants.TAG_CONNECTORS;
import static mcjty.xnet.apiimpl.Constants.TAG_CONSUMER_ID;
import static mcjty.xnet.apiimpl.Constants.TAG_ENABLED;
import static mcjty.xnet.apiimpl.Constants.TAG_NAME;
import static mcjty.xnet.apiimpl.Constants.TAG_SIDE;
import static mcjty.xnet.apiimpl.Constants.TAG_TYPE;

public class ChannelInfo {

    public static final int MAX_CHANNELS = 8;

    private final IChannelType type;
    private final IChannelSettings channelSettings;
    private String channelName;
    private boolean enabled = true;

    private final Map<SidedConsumer, ConnectorInfo> connectors = new HashMap<>();

    public ChannelInfo(IChannelType type) {
        this.type = type;
        channelSettings = type.createChannel();
    }

    @Nonnull
    public String getChannelName() {
        return channelName == null ? "" : channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public IChannelType getType() {
        return type;
    }

    public IChannelSettings getChannelSettings() {
        return channelSettings;
    }

    public Map<SidedConsumer, ConnectorInfo> getConnectors() {
        return connectors;
    }

    public ConnectorInfo createConnector(SidedConsumer id, boolean advanced) {
        ConnectorInfo info = new ConnectorInfo(type, id, advanced);
        connectors.put(id, info);
        return info;
    }

    public void writeToNBT(CompoundTag tag) {
        channelSettings.writeToNBT(tag);
        tag.putBoolean(TAG_ENABLED, enabled);
        if (channelName != null && !channelName.isEmpty()) {
            tag.putString(TAG_NAME, channelName);
        }
        ListTag conlist = new ListTag();
        for (Map.Entry<SidedConsumer, ConnectorInfo> entry : connectors.entrySet()) {
            CompoundTag tc = new CompoundTag();
            ConnectorInfo connectorInfo = entry.getValue();
            connectorInfo.writeToNBT(tc);
            tc.putInt(TAG_CONSUMER_ID, entry.getKey().consumerId().id());
            tc.putInt(TAG_SIDE, entry.getKey().side().ordinal());
            tc.putString(TAG_TYPE, connectorInfo.getType().getID());
            tc.putBoolean(TAG_ADVANCED, connectorInfo.isAdvanced());
            conlist.add(tc);
        }
        tag.put(TAG_CONNECTORS, conlist);
    }

    public void readFromNBT(CompoundTag tag) {
        channelSettings.readFromNBT(tag);
        enabled = tag.getBoolean(TAG_ENABLED);
        channelName = TagUtils.getStringOrNull(tag, TAG_NAME);
        ListTag conlist = tag.getList(TAG_CONNECTORS, Tag.TAG_COMPOUND);
        for (int i = 0 ; i < conlist.size() ; i++) {
            CompoundTag tc = conlist.getCompound(i);
            String id = tc.getString(TAG_TYPE);
            IChannelType type = XNet.xNetApi.findType(id);
            if (type == null) {
                XNet.setup.getLogger().warn("Unsupported type " + id + "!");
                continue;
            }
            if (!getType().equals(type)) {
                XNet.setup.getLogger().warn("Trying to load a connector with non-matching type " + type + "!");
                continue;
            }
            ConsumerId consumerId = new ConsumerId(tc.getInt(TAG_CONSUMER_ID));
            Direction side = OrientationTools.DIRECTION_VALUES[tc.getInt(TAG_SIDE)];
            SidedConsumer key = new SidedConsumer(consumerId, side);
            boolean advanced = tc.getBoolean(TAG_ADVANCED);
            ConnectorInfo connectorInfo = new ConnectorInfo(type, key, advanced);
            connectorInfo.readFromNBT(tc);
            connectors.put(key, connectorInfo);
        }
    }
}
