package mcjty.xnet.apiimpl.items;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.ItemStackList;
import mcjty.lib.varia.JSonTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IControllerContext;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings;
import mcjty.xnet.XNet;
import mcjty.xnet.apiimpl.EnumStringTranslators;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class ItemConnectorSettings extends AbstractConnectorSettings {

    public static final ResourceLocation iconGuiElements = ResourceLocation.fromNamespaceAndPath(XNet.MODID, "textures/gui/guielements.png");

    public static final String TAG_MODE = "mode";
    public static final String TAG_STACK = "stack";
    public static final String TAG_EXTRACT_AMOUNT = "extract_amount";
    public static final String TAG_SPEED = "speed";
    public static final String TAG_EXTRACT = "extract";
    public static final String TAG_TAGS = "od";
    public static final String TAG_NBT = "nbt";
    public static final String TAG_META = "meta";
    public static final String TAG_PRIORITY = "priority";
    public static final String TAG_COUNT = "count";
    public static final String TAG_FILTER = "flt";
    public static final String TAG_FILTER_IDX = "fltIdx";
    public static final String TAG_BLACKLIST = "blacklist";

    public static final int FILTER_SIZE = 18;

    public enum ItemMode implements StringRepresentable {
        INS,
        EXT;

        public static final Codec<ItemMode> CODEC = StringRepresentable.fromEnum(ItemMode::values);
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ItemMode.class);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    public enum StackMode implements StringRepresentable {
        SINGLE,
        STACK,
        COUNT;

        public static final Codec<StackMode> CODEC = StringRepresentable.fromEnum(StackMode::values);
        public static final StreamCodec<RegistryFriendlyByteBuf, StackMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(StackMode.class);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    public enum ExtractMode implements StringRepresentable {
        FIRST,
        RND,
        ORDER;

        public static final Codec<ExtractMode> CODEC = StringRepresentable.fromEnum(ExtractMode::values);
        public static final StreamCodec<RegistryFriendlyByteBuf, ExtractMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ExtractMode.class);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    private ItemMode itemMode = ItemMode.INS;
    private ExtractMode extractMode = ExtractMode.FIRST;
    private int speed = 2;
    private StackMode stackMode = StackMode.SINGLE;
    private boolean tagsMode = false;
    private boolean metaMode = false;
    private boolean componentMode = false;
    private boolean blacklist = false;
    @Nullable private Integer priority = 0;
    @Nullable private Integer count = null;
    @Nullable private Integer extractAmount = null;

    private final ItemStackList filters = ItemStackList.create(FILTER_SIZE);
    private int filterIndex = -1;

    public static final MapCodec<ItemConnectorSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.fieldOf("base").forGetter(settings -> settings.settings),
            ItemMode.CODEC.fieldOf("itemMode").forGetter(settings -> settings.itemMode),
            ExtractMode.CODEC.fieldOf("extractMode").forGetter(settings -> settings.extractMode),
            StackMode.CODEC.fieldOf("stackMode").forGetter(settings -> settings.stackMode),
            Codec.INT.fieldOf("speed").forGetter(settings -> settings.speed),
            Codec.INT.fieldOf("filterIndex").forGetter(settings -> settings.filterIndex),
            Codec.BOOL.fieldOf("tagsMode").forGetter(settings -> settings.tagsMode),
            Codec.BOOL.fieldOf("metaMode").forGetter(settings -> settings.metaMode),
            Codec.BOOL.fieldOf("componentMode").forGetter(settings -> settings.componentMode),
            Codec.BOOL.fieldOf("blacklist").forGetter(settings -> settings.blacklist),
            Codec.INT.optionalFieldOf("priority").forGetter(settings -> Optional.ofNullable(settings.priority)),
            Codec.INT.optionalFieldOf("extractAmount").forGetter(settings -> Optional.ofNullable(settings.extractAmount)),
            Codec.INT.optionalFieldOf("count").forGetter(settings -> Optional.ofNullable(settings.count)),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("filters").forGetter(settings -> settings.filters)
    ).apply(instance, (base, itemMode, extractMode, stackMode, speed, filterIndex, tagsMode, metaMode, componentMode, blacklist, priority, extractAmount, count, filters) -> {
        ItemConnectorSettings settings = new ItemConnectorSettings(base, Direction.NORTH);
        settings.itemMode = itemMode;
        settings.extractMode = extractMode;
        settings.stackMode = stackMode;
        settings.speed = speed;
        settings.filterIndex = filterIndex;
        settings.tagsMode = tagsMode;
        settings.metaMode = metaMode;
        settings.componentMode = componentMode;
        settings.blacklist = blacklist;
        settings.priority = priority.orElse(null);
        settings.extractAmount = extractAmount.orElse(null);
        settings.count = count.orElse(null);
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            settings.filters.set(i, filters.get(i));
        }
        return settings;
    }));

    public static StreamCodec<RegistryFriendlyByteBuf, ItemConnectorSettings> STREAM_CODEC = CompositeStreamCodec.composite(
            BaseSettings.STREAM_CODEC, s -> s.settings,
            ItemMode.STREAM_CODEC, s -> s.itemMode,
            ExtractMode.STREAM_CODEC, s -> s.extractMode,
            StackMode.STREAM_CODEC, s -> s.stackMode,
            ByteBufCodecs.INT, s -> s.speed,
            ByteBufCodecs.INT, s -> s.filterIndex,
            ByteBufCodecs.BOOL, s -> s.tagsMode,
            ByteBufCodecs.BOOL, s -> s.metaMode,
            ByteBufCodecs.BOOL, s -> s.componentMode,
            ByteBufCodecs.BOOL, s -> s.blacklist,
            ByteBufCodecs.optional(ByteBufCodecs.INT), s -> Optional.ofNullable(s.priority),
            ByteBufCodecs.optional(ByteBufCodecs.INT), s -> Optional.ofNullable(s.extractAmount),
            ByteBufCodecs.optional(ByteBufCodecs.INT), s -> Optional.ofNullable(s.count),
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, s -> s.filters,
            (base, itemMode, extractMode, stackMode, speed, filterIndex, tagsMode, metaMode, componentMode, blacklist, priority, extractAmount, count, filters) -> {
                ItemConnectorSettings settings = new ItemConnectorSettings(base, Direction.NORTH);
                settings.itemMode = itemMode;
                settings.extractMode = extractMode;
                settings.stackMode = stackMode;
                settings.speed = speed;
                settings.filterIndex = filterIndex;
                settings.tagsMode = tagsMode;
                settings.metaMode = metaMode;
                settings.componentMode = componentMode;
                settings.blacklist = blacklist;
                settings.priority = priority.orElse(null);
                settings.extractAmount = extractAmount.orElse(null);
                settings.count = count.orElse(null);
                settings.filters.clear();
                settings.filters.addAll(filters);
                return settings;
            }
    );

    public ItemConnectorSettings(@Nonnull BaseSettings settings, @Nonnull Direction side) {
        super(settings, side);
    }

    // Cached matcher for items
    private Predicate<ItemStack> matcher = null;

    public ItemMode getItemMode() {
        return itemMode;
    }

    @Override
    public IChannelType getType() {
        return XNet.setup.itemChannelType;
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
        String[] speeds;
        if (advanced) {
            speeds = new String[] { "5", "10", "20", "60", "100", "200" };
        } else {
            speeds = new String[] { "10", "20", "60", "100", "200" };
        }

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
                .toggleText(TAG_NBT, "NBT matching", "NBT", componentMode).shift(0)
                .choices(TAG_FILTER_IDX, "Filter Index", getFilterIndexString(), "<Off>", "1", "2", "3", "4")
                .nl();
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            gui.ghostSlot(TAG_FILTER + i, filters.get(i));
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
                ItemFilterCache filterCache = new ItemFilterCache(metaMode, tagsMode, blacklist, componentMode, filterList);
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

    public StackMode getStackMode() {
        return stackMode;
    }


    public ExtractMode getExtractMode() {
        return extractMode;
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

    public int getSpeed() {
        return speed;
    }

    public int getFilterIndex() {
        return filterIndex;
    }

    private static final Set<String> INSERT_TAGS = ImmutableSet.of(TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3", TAG_COUNT, TAG_PRIORITY, TAG_TAGS, TAG_META, TAG_NBT, TAG_BLACKLIST);
    private static final Set<String> EXTRACT_TAGS = ImmutableSet.of(TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3", TAG_COUNT, TAG_TAGS, TAG_META, TAG_NBT, TAG_BLACKLIST, TAG_STACK, TAG_SPEED, TAG_EXTRACT, TAG_EXTRACT_AMOUNT);

    @Override
    public boolean isEnabled(String tag) {
        if (tag.startsWith(TAG_FILTER)) {
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
        speed = Integer.parseInt((String) data.get(TAG_SPEED)) / 5;
        if (speed == 0) {
            speed = 4;
        }
        String idx = (String) data.get(TAG_FILTER_IDX);
        this.filterIndex = "<Off>".equalsIgnoreCase(idx) ? -1 : Integer.parseInt(idx);
        tagsMode = Boolean.TRUE.equals(data.get(TAG_TAGS));
        metaMode = Boolean.TRUE.equals(data.get(TAG_META));
        componentMode = Boolean.TRUE.equals(data.get(TAG_NBT));

        blacklist = Boolean.TRUE.equals(data.get(TAG_BLACKLIST));
        priority = (Integer) data.get(TAG_PRIORITY);
        count = (Integer) data.get(TAG_COUNT);
        extractAmount = (Integer) data.get(TAG_EXTRACT_AMOUNT);
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            filters.set(i, (ItemStack) data.get(TAG_FILTER+i));
        }
        matcher = null;
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject object = new JsonObject();
        super.writeToJsonInternal(object);
        setEnumSafe(object, "itemmode", itemMode);
        setEnumSafe(object, "extractmode", extractMode);
        setEnumSafe(object, "stackmode", stackMode);
        object.add("tagsmode", new JsonPrimitive(tagsMode));
        object.add("metamode", new JsonPrimitive(metaMode));
        object.add("nbtmode", new JsonPrimitive(componentMode));
        object.add("blacklist", new JsonPrimitive(blacklist));
        setIntegerSafe(object, "priority", priority);
        setIntegerSafe(object, "extractamount", extractAmount);
        setIntegerSafe(object, "count", count);
        setIntegerSafe(object, "speed", speed);
        setIntegerSafe(object, "filterindex", filterIndex);
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            if (!filters.get(i).isEmpty()) {
                object.add("filter" + i, JSonTools.itemStackToJson(filters.get(i)));
            }
        }
        if (speed == 1) {
            object.add("advancedneeded", new JsonPrimitive(true));
        }

        return object;
    }

    @Override
    public void readFromJson(JsonObject object) {
        super.readFromJsonInternal(object);
        itemMode = getEnumSafe(object, "itemmode", EnumStringTranslators::getItemMode);
        extractMode = getEnumSafe(object, "extractmode", EnumStringTranslators::getExtractMode);
        stackMode = getEnumSafe(object, "stackmode", EnumStringTranslators::getStackMode);
        tagsMode = getBoolSafe(object, "tagsmode");
        metaMode = getBoolSafe(object, "metamode");
        componentMode = getBoolSafe(object, "nbtmode");
        blacklist = getBoolSafe(object, "blacklist");
        priority = getIntegerSafe(object, "priority");
        extractAmount = getIntegerSafe(object, "extractamount");
        count = getIntegerSafe(object, "count");
        speed = getIntegerNotNull(object, "speed");
        if (object.has("filterindex")) {
            filterIndex = getIntegerNotNull(object, "filterindex");
        } else {
            filterIndex = -1;
        }
        for (int i = 0 ; i < FILTER_SIZE ; i++) {
            if (object.has("filter" + i)) {
                filters.set(i, JSonTools.jsonToItemStack(object.get("filter" + i).getAsJsonObject()));
            } else {
                filters.set(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
    }
}
