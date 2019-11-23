package mcjty.xnet.compat.rftoolscontrol;

public class RFToolsControlSupport {

    // @todo 1.14
//    public static final Opcode DO_CHANNEL = Opcode.builder()
//            .id("xnet:channel")
//            .description(
//                    TextFormatting.GREEN + "Operation: channel (XNet)",
//                    "turn on or off an XNet channel")
//            .opcodeOutput(SINGLE)
//            .parameter(ParameterDescription.builder().name("controller").type(PAR_SIDE).description("XNet controller adjacent to (networked) block").build())
//            .parameter(ParameterDescription.builder().name("channel").type(PAR_INTEGER).description("Channel number (starting at 0)").build())
//            .parameter(ParameterDescription.builder().name("enabled").type(PAR_BOOLEAN).description("Enable or disable").build())
//            .icon(0, 5, XNet.MODID + ":textures/gui/guielements.png")
//            .runnable(((processor, program, opcode) -> {
//                BlockSide inv = processor.evaluateSideParameterNonNull(opcode, program, 0);
//                int channel = processor.evaluateIntParameter(opcode, program, 1);
//                if (channel < 0 || channel > 7) {
//                    processor.log("Wrong channel!");
//                    return POSITIVE;
//                }
//                boolean enable = processor.evaluateBoolParameter(opcode, program, 2);
//                TileEntity te = processor.getTileEntityAt(inv);
//                if (te instanceof TileEntityController) {
//                    TileEntityController controller = (TileEntityController) te;
//                    controller.getChannels()[channel].setEnabled(enable);
//                    controller.markAsDirty();
//                }
//                return POSITIVE;
//            }))
//            .build();
//    public static final Opcode TEST_COLOR = Opcode.builder()
//            .id("xnet:read_color")
//            .description(
//                    TextFormatting.GREEN + "Test: color (XNet)",
//                    "test if a certain color is enabled in",
//                    "an XNet controller")
//            .opcodeOutput(YESNO)
//            .parameter(ParameterDescription.builder().name("controller").type(PAR_SIDE).description("XNet controller adjacent to (networked) block").build())
//            .parameter(ParameterDescription.builder().name("mask").type(PAR_INTEGER).description("Color mask (bit combination of colors)").build())
//            .icon(1, 5, XNet.MODID + ":textures/gui/guielements.png")
//            .runnable(((processor, program, opcode) -> {
//                BlockSide inv = processor.evaluateSideParameterNonNull(opcode, program, 0);
//                int mask = processor.evaluateIntParameter(opcode, program, 1);
//                TileEntity te = processor.getTileEntityAt(inv);
//                if (te instanceof TileEntityController) {
//                    TileEntityController controller = (TileEntityController) te;
//                    return controller.matchColor(mask) ? POSITIVE : NEGATIVE;
//                }
//                return NEGATIVE;
//            }))
//            .build();
//
//    public static class GetOpcodeRegistry implements Function<IOpcodeRegistry, Void> {
//        @Nullable
//        @Override
//        public Void apply(IOpcodeRegistry registry) {
//            registry.register(DO_CHANNEL);
//            registry.register(TEST_COLOR);
//            return null;
//        }
//    }
}
