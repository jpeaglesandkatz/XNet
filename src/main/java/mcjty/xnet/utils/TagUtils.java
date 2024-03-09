package mcjty.xnet.utils;

import net.minecraft.nbt.CompoundTag;

import static mcjty.xnet.apiimpl.Constants.TAG_PRIORITY;
import static mcjty.xnet.apiimpl.Constants.TAG_RATE;

public class TagUtils {

    public static Integer getIntOrNull(CompoundTag tag, String key) {
        if (key == null || key.isEmpty() || !tag.contains(key)) {
            return null;
        } else {
            return tag.getInt(TAG_RATE);
        }
    }

    public static String getStringOrNull(CompoundTag tag, String key) {
        if (key == null || key.isEmpty() || !tag.contains(key)) {
            return null;
        } else {
            return tag.getString(TAG_RATE);
        }
    }

    public static void putIntIfNotNull(CompoundTag tag, String key, Integer value) {
        if (key != null && !key.isEmpty() && value != null) {
            tag.putInt(TAG_PRIORITY, value);
        }
    }
}
