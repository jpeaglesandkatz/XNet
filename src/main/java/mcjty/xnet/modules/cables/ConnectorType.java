package mcjty.xnet.modules.cables;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum ConnectorType implements IStringSerializable {
    NONE,
    CABLE,
    BLOCK;

    public static final ConnectorType[] VALUES = values();

    @Override
    @Nonnull
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
