package mcjty.xnet.setup;

public class GuiProxy {

    // @todo 1.14
//    public static final int GUI_MANUAL_XNET = 0;
//    public static final int GUI_CONTROLLER = 1;
//    public static final int GUI_CONNECTOR = 2;
//    public static final int GUI_ROUTER = 3;
//    public static final int GUI_WIRELESS_ROUTER = 4;
//    public static final String SHIFT_MESSAGE = "<Press Shift>";
//
//    @Override
//    public Object getServerGuiElement(int guiid, PlayerEntity entityPlayer, World world, int x, int y, int z) {
//        if (guiid == GUI_MANUAL_XNET) {
//            return null;
//        }
//        BlockPos pos = new BlockPos(x, y, z);
//        Block block = world.getBlockState(pos).getBlock();
//        if (block instanceof GenericBlock) {
//            GenericBlock<?, ?> genericBlock = (GenericBlock<?, ?>) block;
//            TileEntity te = world.getTileEntity(pos);
//            return genericBlock.createServerContainer(entityPlayer, te);
//        } else if (block instanceof ConnectorBlock) {
//            return new EmptyContainer(entityPlayer, null);
//        }
//        return null;
//    }
//
//    @Override
//    public Object getClientGuiElement(int guiid, PlayerEntity entityPlayer, World world, int x, int y, int z) {
//        if (guiid == GUI_MANUAL_XNET) {
//            return new GuiXNetManual(GuiXNetManual.MANUAL_XNET);
//        }
//        BlockPos pos = new BlockPos(x, y, z);
//        Block block = world.getBlockState(pos).getBlock();
//        if (block instanceof GenericBlock) {
//            GenericBlock<?, ?> genericBlock = (GenericBlock<?, ?>) block;
//            TileEntity te = world.getTileEntity(pos);
//            return genericBlock.createClientGui(entityPlayer, te);
//        } else if (block instanceof ConnectorBlock) {
//            TileEntity te = world.getTileEntity(pos);
//            return new GuiConnector((ConnectorTileEntity) te, new EmptyContainer(entityPlayer, null));
//        }
//        return null;
//    }
}
