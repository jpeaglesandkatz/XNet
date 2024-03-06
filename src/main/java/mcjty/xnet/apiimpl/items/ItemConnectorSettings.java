package mcjty.xnet.apiimpl.items;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import mcjty.lib.varia.ItemStackList;
import mcjty.lib.varia.JSonTools;
import mcjty.rftoolsbase.api.xnet.channels.IControllerContext;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings;
import mcjty.xnet.XNet;
import mcjty.xnet.apiimpl.Constants;
import mcjty.xnet.apiimpl.EnumStringTranslators;
import mcjty.xnet.utils.TagUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static mcjty.xnet.apiimpl.Constants.TAG_ADVANCED_NEEDED;
import static mcjty.xnet.apiimpl.Constants.TAG_BLACKLIST;
import static mcjty.xnet.apiimpl.Constants.TAG_COUNT;
import static mcjty.xnet.apiimpl.Constants.TAG_EXTRACT;
import static mcjty.xnet.apiimpl.Constants.TAG_EXTRACT_AMOUNT;
import static mcjty.xnet.apiimpl.Constants.TAG_EXTRACT_MODE;
import static mcjty.xnet.apiimpl.Constants.TAG_FILTER_INDEX;
import static mcjty.xnet.apiimpl.Constants.TAG_FLT;
import static mcjty.xnet.apiimpl.Constants.TAG_FILTER_IDX;
import static mcjty.xnet.apiimpl.Constants.TAG_ITEM_MODE;
import static mcjty.xnet.apiimpl.Constants.TAG_META;
import static mcjty.xnet.apiimpl.Constants.TAG_META_MODE;
import static mcjty.xnet.apiimpl.Constants.TAG_MODE;
import static mcjty.xnet.apiimpl.Constants.TAG_NBT;
import static mcjty.xnet.apiimpl.Constants.TAG_NBT_MODE;
import static mcjty.xnet.apiimpl.Constants.TAG_PRIORITY;
import static mcjty.xnet.apiimpl.Constants.TAG_SPEED;
import static mcjty.xnet.apiimpl.Constants.TAG_STACK;
import static mcjty.xnet.apiimpl.Constants.TAG_STACK_MODE;
import static mcjty.xnet.apiimpl.Constants.TAG_TAGS;
import static mcjty.xnet.apiimpl.Constants.TAG_TAGS_MODE;

public class ItemConnectorSettings extends AbstractConnectorSettings {

    public static final ResourceLocation iconGuiElements = new ResourceLocation(XNet.MODID, "textures/gui/guielements.png");

    public static final int FILTER_SIZE = 18;

    public enum ItemMode {
        INS,
        EXT
    }

    public enum StackMode {
        SINGLE,
        STACK,
        COUNT
    }

    public enum ExtractMode {
        FIRST,
        RND,
        ORDER
    }

    @Getter
    private ItemMode itemMode = ItemMode.INS;
    @Getter
    private ExtractMode extractMode = ExtractMode.FIRST;
    @Getter
    private int speed = 2;
    @Getter
    private StackMode stackMode = StackMode.SINGLE;
    private boolean tagsMode = false;
    private boolean metaMode = false;
    private boolean nbtMode = false;
    private boolean blacklist = false;
    @Nullable private Integer priority = 0;
    @Nullable private Integer count = null;
    @Nullable private Integer extractAmount = null;

    private final ItemStackList filters = ItemStackList.create(FILTER_SIZE);
    @Getter
    private int filterIndex = -1;

    // Cached matcher for items
    private Predicate<ItemStack> matcher = null;

    public ItemConnectorSettings(@Nonnull Direction side) {
        super(side);
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return switch (itemMode) {
            case INS -> new IndicatorIcon(iconGuiElements, 0, 70, 13, 10);
            case EXT -> new IndicatorIcon(iconGuiElements, 13, 70, 13, 10);
        };
    }

    @Override
    @Nullable
    public String getIndicator() {
        return null;
    }

    @Override
    public void createGui(IEditorGui gui) {
        advanced = gui.isAdvanced();
        String[] speeds = advanced ? Constants.ADVANCED_SPEEDS : Constants.SPEEDS;

        sideGui(gui);
        colorsGui(gui);
        redstoneGui(gui);
        gui.nl()
                .choices(TAG_MODE, "Insert or extract mode", itemMode, ItemMode.values())
                .shift(5)
                .choices(TAG_STACK, "Single item, stack, or count", stackMode, StackMode.values());

        if (stackMode == StackMode.COUNT && itemMode == ItemMode.EXT) {
            gui
                    .integer(TAG_EXTRACT_AMOUNT, "Amount of items to extract|per operation", extractAmount, 30, 64);
        }

        gui
                .shift(10)
                .choices(TAG_SPEED, "Number of ticks for each operation", Integer.toString(speed * 5), speeds)
                .nl();

        gui
                .label("Pri").integer(TAG_PRIORITY, "Insertion priority", priority, 36).shift(5)
                .label("#")
                .integer(TAG_COUNT, itemMode == ItemMode.EXT ? "Amount in destination inventory|to keep" : "Max amount in destination|inventory", count, 30);

        if (itemMode == ItemMode.EXT) {
            gui
                    .shift(5)
                    .choices(TAG_EXTRACT, "Extract mode (first available,|random slot or round robin)", extractMode, ExtractMode.values());
        }

        gui
                .nl()

                .toggleText(TAG_BLACKLIST, "Enable blacklist mode", "BL", blacklist).shift(0)
                .toggleText(TAG_TAGS, "Tag matching", "Tags", tagsMode).shift(0)
                .toggleText(TAG_META, "Metadata matching", "Meta", metaMode).shift(0)
                .toggleText(TAG_NBT, "NBT matching", "NBT", nbtMode).shift(0)
                .choices(TAG_FILTER_IDX, "Filter Index", getFilterIndexString(), "<Off>", "1", "2", "3", "4")
                .nl();
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            gui.ghostSlot(TAG_FLT + i, filters.get(i));
        }
    }

    private String getFilterIndexString() {
        if (filterIndex == -1) {
            return "<Off>";
        } else {
            return Integer.toString(filterIndex);
        }
    }

    public Predicate<ItemStack> getMatcher(IControllerContext context) {
        if (matcher == null) {
            ItemStackList filterList = ItemStackList.create();
            for (ItemStack stack : filters) {
                if (!stack.isEmpty()) {
                    filterList.add(stack);
                }
            }
            Predicate<ItemStack> filterMatcher = getIndexFilterMatcher(context);
            if (filterList.isEmpty()) {
                if (filterMatcher != null) {
                    matcher = filterMatcher;
                } else {
                    matcher = itemStack -> true;
                }
            } else {
                ItemFilterCache filterCache = new ItemFilterCache(metaMode, tagsMode, blacklist, nbtMode, filterList);
                if (filterMatcher != null) {
                    matcher = stack -> filterMatcher.test(stack) || filterCache.match(stack);
                } else {
                    matcher = filterCache::match;
                }
            }
        }
        return matcher;
    }

    @Nullable
    private Predicate<ItemStack> getIndexFilterMatcher(IControllerContext context) {
        if (filterIndex == -1) {
            return null;
        }
        return s -> context.getIndexedFilter(filterIndex-1).test(s);
    }


    @Nonnull
    public Integer getPriority() {
        return priority == null ? 0 : priority;
    }

    @Nullable
    public Integer getCount() {
        return count;
    }

    public int getExtractAmount() {
        return extractAmount == null ? 1 : extractAmount;
    }

    private static final Set<String> INSERT_TAGS = ImmutableSet.of(TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3", TAG_COUNT, TAG_PRIORITY, TAG_TAGS, TAG_META, TAG_NBT, TAG_BLACKLIST);
    private static final Set<String> EXTRACT_TAGS = ImmutableSet.of(TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3", TAG_COUNT, TAG_TAGS, TAG_META, TAG_NBT, TAG_BLACKLIST, TAG_STACK, TAG_SPEED, TAG_EXTRACT, TAG_EXTRACT_AMOUNT);

    @Override
    public boolean isEnabled(String tag) {
        if (tag.startsWith(TAG_FLT)) {
            return true;
        }
        if (tag.equals(TAG_FACING)) {
            return advanced;
        }
        if (itemMode == ItemMode.INS) {
            return INSERT_TAGS.contains(tag);
        } else {
            return EXTRACT_TAGS.contains(tag);
        }
    }

    public void setSpeed(int speed) {
        this.speed = speed != 0 ? speed : 4;
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        itemMode = ItemMode.valueOf(((String)data.get(TAG_MODE)).toUpperCase());
        Object emode = data.get(TAG_EXTRACT);
        if (emode == null) {
            extractMode = ExtractMode.FIRST;
        } else {
            extractMode = ExtractMode.valueOf(((String) emode).toUpperCase());
        }
        stackMode = StackMode.valueOf(((String)data.get(TAG_STACK)).toUpperCase());
        setSpeed(Integer.parseInt((String) data.get(TAG_SPEED)) / 5);

        String idx = (String) data.get(TAG_FILTER_IDX);
        this.filterIndex = "<Off>".equalsIgnoreCase(idx) ? -1 : Integer.parseInt(idx);
        tagsMode = Boolean.TRUE.equals(data.get(TAG_TAGS));
        metaMode = Boolean.TRUE.equals(data.get(TAG_META));
        nbtMode = Boolean.TRUE.equals(data.get(TAG_NBT));

        blacklist = Boolean.TRUE.equals(data.get(TAG_BLACKLIST));
        priority = (Integer) data.get(TAG_PRIORITY);
        count = (Integer) data.get(TAG_COUNT);
        extractAmount = (Integer) data.get(TAG_EXTRACT_AMOUNT);
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            filters.set(i, (ItemStack) data.get(TAG_FLT+i));
        }
        matcher = null;
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject object = new JsonObject();
        super.writeToJsonInternal(object);
        setEnumSafe(object, TAG_ITEM_MODE, itemMode);
        setEnumSafe(object, TAG_EXTRACT_MODE, extractMode);
        setEnumSafe(object, TAG_STACK_MODE, stackMode);
        object.add(TAG_TAGS_MODE, new JsonPrimitive(tagsMode));
        object.add(TAG_META_MODE, new JsonPrimitive(metaMode));
        object.add(TAG_NBT_MODE, new JsonPrimitive(nbtMode));
        object.add(TAG_PRIORITY, new JsonPrimitive(blacklist));
        setIntegerSafe(object, TAG_PRIORITY, priority);
        setIntegerSafe(object, TAG_EXTRACT_AMOUNT, extractAmount);
        setIntegerSafe(object, TAG_COUNT, count);
        setIntegerSafe(object, TAG_SPEED, speed);
        setIntegerSafe(object, TAG_FILTER_INDEX, filterIndex);
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            if (!filters.get(i).isEmpty()) {
                object.add(TAG_FLT + i, JSonTools.itemStackToJson(filters.get(i)));
            }
        }
        if (speed == 1) {
            object.add(TAG_ADVANCED_NEEDED, new JsonPrimitive(true));
        }

        return object;
    }

    @Override
    public void readFromJson(JsonObject object) {
        super.readFromJsonInternal(object);
        itemMode = getEnumSafe(object, TAG_ITEM_MODE, EnumStringTranslators::getItemMode);
        extractMode = getEnumSafe(object, TAG_EXTRACT_MODE, EnumStringTranslators::getExtractMode);
        stackMode = getEnumSafe(object, TAG_STACK_MODE, EnumStringTranslators::getStackMode);
        tagsMode = getBoolSafe(object, TAG_TAGS_MODE);
        metaMode = getBoolSafe(object, TAG_META_MODE);
        nbtMode = getBoolSafe(object, TAG_NBT_MODE);
        blacklist = getBoolSafe(object, TAG_PRIORITY);
        priority = getIntegerSafe(object, TAG_PRIORITY);
        extractAmount = getIntegerSafe(object, TAG_EXTRACT_AMOUNT);
        count = getIntegerSafe(object, TAG_COUNT);
        speed = getIntegerNotNull(object, TAG_SPEED);
        if (object.has(TAG_FILTER_INDEX)) {
            filterIndex = getIntegerNotNull(object, TAG_FILTER_INDEX);
        } else {
            filterIndex = -1;
        }
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            if (object.has(TAG_FLT + i)) {
                filters.set(i, JSonTools.jsonToItemStack(object.get(TAG_FLT + i).getAsJsonObject()));
            } else {
                filters.set(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        itemMode = ItemMode.values()[tag.getByte(TAG_ITEM_MODE)];
        extractMode = ExtractMode.values()[tag.getByte(TAG_EXTRACT_MODE)];
        stackMode = StackMode.values()[tag.getByte(TAG_STACK_MODE)];
        if (tag.contains(TAG_SPEED)) { // Reverse old|new speed tag. Now actual tag is "speed" to unification between types
            setSpeed(tag.getInt(TAG_SPEED));
        } else {
            setSpeed(tag.getInt("spd")); // TODO: 06.03.2024 backward compatibility. DELETE THIS after 1.20.4_neo branch
        }
        if (tag.contains(TAG_FILTER_INDEX)) {
            filterIndex = tag.getInt(TAG_FILTER_INDEX);
        } else {
            filterIndex = -1;
        }
        tagsMode = tag.getBoolean(TAG_TAGS_MODE);
        metaMode = tag.getBoolean(TAG_META_MODE);
        nbtMode = tag.getBoolean(TAG_NBT_MODE);
        blacklist = tag.getBoolean(TAG_PRIORITY);
        priority = TagUtils.getIntOrNull(tag, TAG_PRIORITY);
        extractAmount = TagUtils.getIntOrNull(tag, TAG_EXTRACT_AMOUNT);
        count = TagUtils.getIntOrNull(tag, TAG_COUNT);
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            if (tag.contains(TAG_FLT + i)) {
                CompoundTag itemTag = tag.getCompound(TAG_FLT + i);
                filters.set(i, ItemStack.of(itemTag));
            } else {
                filters.set(i, ItemStack.EMPTY);
            }
        }
        matcher = null;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putByte(TAG_ITEM_MODE, (byte) itemMode.ordinal());
        tag.putByte(TAG_EXTRACT_MODE, (byte) extractMode.ordinal());
        tag.putByte(TAG_STACK_MODE, (byte) stackMode.ordinal());
        tag.putInt(TAG_SPEED, speed);
        tag.putInt(TAG_FILTER_INDEX, filterIndex);
        tag.putBoolean(TAG_TAGS_MODE, tagsMode);
        tag.putBoolean(TAG_META_MODE, metaMode);
        tag.putBoolean(TAG_NBT_MODE, nbtMode);
        tag.putBoolean(TAG_PRIORITY, blacklist);
        TagUtils.putIntIfNotNull(tag, TAG_PRIORITY, priority);
        TagUtils.putIntIfNotNull(tag, TAG_EXTRACT_AMOUNT, extractAmount);
        TagUtils.putIntIfNotNull(tag, TAG_COUNT, count);
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            if (!filters.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                filters.get(i).save(itemTag);
                tag.put(TAG_FLT + i, itemTag);
            }
        }
    }
}
