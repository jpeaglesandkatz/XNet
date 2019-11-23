package mcjty.xnet.clientinfo;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.OrientationTools;
import mcjty.xnet.api.keys.SidedPos;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;

public class ConnectedBlockClientInfo {

    /// The position of the block we are connecting too
    @Nonnull
    private final SidedPos pos;
    /// The itemstack representing the block
    @Nonnull private final ItemStack connectedBlock;

    /// The name of the connector
    @Nonnull private final String name;

    /// The name of the block
    @Nonnull private final String blockName;

    public ConnectedBlockClientInfo(@Nonnull SidedPos pos, @Nonnull ItemStack connectedBlock, @Nonnull String name) {
        this.pos = pos;
        this.connectedBlock = connectedBlock;
        this.name = name;
        this.blockName = getStackUnlocalizedName(connectedBlock);
    }

    public ConnectedBlockClientInfo(@Nonnull PacketBuffer buf) {
        pos = new SidedPos(buf.readBlockPos(), OrientationTools.DIRECTION_VALUES[buf.readByte()]);
        connectedBlock = buf.readItemStack();
        name = NetworkTools.readStringUTF8(buf);
        blockName = NetworkTools.readStringUTF8(buf);
    }

    public void writeToBuf(@Nonnull PacketBuffer buf) {
        buf.writeBlockPos(pos.getPos());
        buf.writeByte(pos.getSide().ordinal());
        buf.writeItemStack(connectedBlock);
        NetworkTools.writeStringUTF8(buf, name);
        NetworkTools.writeStringUTF8(buf, blockName);
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getBlockUnlocName() {
        return blockName;
    }

    @Nonnull
    public SidedPos getPos() {
        return pos;
    }

    @Nonnull
    public ItemStack getConnectedBlock() {
        return connectedBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectedBlockClientInfo that = (ConnectedBlockClientInfo) o;

        if (!pos.equals(that.pos)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    private static String getStackUnlocalizedName(ItemStack stack) {
        CompoundNBT nbttagcompound = getSubCompound(stack, "display");

        if (nbttagcompound != null) {
            if (nbttagcompound.contains("Name", 8)) {
                return nbttagcompound.getString("Name");
            }

            if (nbttagcompound.contains("LocName", 8)) {
                return nbttagcompound.getString("LocName");
            }
        }

        return stack.getItem().getTranslationKey(stack) + ".name";
    }

    private static CompoundNBT getSubCompound(ItemStack stack, String key) {
        if (stack.getTag() != null && stack.getTag().contains(key, 10)) {
            return stack.getTag().getCompound(key);
        } else {
            return null;
        }
    }

}
