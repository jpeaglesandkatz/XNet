package mcjty.xnet.modules.cables;

import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;

public enum CableColor implements StringRepresentable {
    BLUE("blue", "dyeBlue"),
    RED("red", "dyeRed"),
    YELLOW("yellow", "dyeYellow"),
    GREEN("green", "dyeGreen"),
    ROUTING("routing", null);

    public static final CableColor[] VALUES = CableColor.values();

    private final String name;
    private final String dye;

    CableColor(String name, String dye) {
        this.name = name;
        this.dye = dye;
    }

    @Override
    @Nonnull
    public String getSerializedName() {
        return name;
    }

    public String getDye() {
        return dye;
    }
}
