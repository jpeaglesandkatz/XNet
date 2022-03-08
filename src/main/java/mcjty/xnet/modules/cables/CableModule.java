package mcjty.xnet.modules.cables;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.blocks.*;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock.CableBlockType;
import mcjty.xnet.modules.cables.client.ClientSetup;
import mcjty.xnet.modules.cables.client.GuiConnector;
import mcjty.xnet.setup.Registration;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.xnet.setup.Registration.*;

public class CableModule implements IModule {

    public static final RegistryObject<NetCableBlock> NETCABLE = BLOCKS.register("netcable", () -> new NetCableBlock(CableBlockType.CABLE));
    public static final RegistryObject<Item> NETCABLE_RED = ITEMS.register("netcable_red", () -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.RED));
    public static final RegistryObject<Item> NETCABLE_GREEN = ITEMS.register("netcable_green", () -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.GREEN));
    public static final RegistryObject<Item> NETCABLE_BLUE = ITEMS.register("netcable_blue", () -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.BLUE));
    public static final RegistryObject<Item> NETCABLE_YELLOW = ITEMS.register("netcable_yellow", () -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.YELLOW));
    public static final RegistryObject<Item> NETCABLE_ROUTING = ITEMS.register("netcable_routing", () -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.ROUTING));

    public static final RegistryObject<ConnectorBlock> CONNECTOR = BLOCKS.register("connector", () -> new ConnectorBlock(CableBlockType.CONNECTOR));
    public static final RegistryObject<Item> CONNECTOR_RED = ITEMS.register("connector_red", () -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.RED));
    public static final RegistryObject<Item> CONNECTOR_GREEN = ITEMS.register("connector_green", () -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.GREEN));
    public static final RegistryObject<Item> CONNECTOR_BLUE = ITEMS.register("connector_blue", () -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.BLUE));
    public static final RegistryObject<Item> CONNECTOR_YELLOW = ITEMS.register("connector_yellow", () -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.YELLOW));
    public static final RegistryObject<Item> CONNECTOR_ROUTING = ITEMS.register("connector_routing", () -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.ROUTING));

    public static final RegistryObject<AdvancedConnectorBlock> ADVANCED_CONNECTOR = BLOCKS.register("advanced_connector", () -> new AdvancedConnectorBlock(CableBlockType.ADVANCED_CONNECTOR));
    public static final RegistryObject<Item> ADVANCED_CONNECTOR_RED = ITEMS.register("advanced_connector_red", () -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.RED));
    public static final RegistryObject<Item> ADVANCED_CONNECTOR_GREEN = ITEMS.register("advanced_connector_green", () -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.GREEN));
    public static final RegistryObject<Item> ADVANCED_CONNECTOR_BLUE = ITEMS.register("advanced_connector_blue", () -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.BLUE));
    public static final RegistryObject<Item> ADVANCED_CONNECTOR_YELLOW = ITEMS.register("advanced_connector_yellow", () -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.YELLOW));
    public static final RegistryObject<Item> ADVANCED_CONNECTOR_ROUTING = ITEMS.register("advanced_connector_routing", () -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.ROUTING));

    public static final RegistryObject<BlockEntityType<?>> TYPE_CONNECTOR = TILES.register("connector", () -> BlockEntityType.Builder.of(ConnectorTileEntity::new, CONNECTOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<?>> TYPE_ADVANCED_CONNECTOR = TILES.register("advanced_connector", () -> BlockEntityType.Builder.of(AdvancedConnectorTileEntity::new, ADVANCED_CONNECTOR.get()).build(null));

    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_CONNECTOR = CONTAINERS.register("connector", GenericContainer::createContainerType);

    public static final TagKey<Item> TAG_CABLES = TagKey.create(Registry.ITEM.key(), new ResourceLocation(XNet.MODID, "cables"));
    public static final TagKey<Item> TAG_CONNECTORS = TagKey.create(Registry.ITEM.key(), new ResourceLocation(XNet.MODID, "connectors"));
    public static final TagKey<Item> TAG_ADVANCED_CONNECTORS = TagKey.create(Registry.ITEM.key(), new ResourceLocation(XNet.MODID, "advanced_connectors"));

    public CableModule() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::modelInit);
        });
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiConnector.register();
        });
        ClientSetup.initClient();
    }

    @Override
    public void initConfig() {

    }
}
