package mcjty.xnet.blocks.generic;

public class BakedModelLoader {/*implements ICustomModelLoader {

    public static final GenericCableModel GENERIC_MODEL = new GenericCableModel();
    public static final FacadeModel FACADE_MODEL = new FacadeModel();

    private static final Set<String> NAMES = ImmutableSet.of(
            ConnectorBlock.CONNECTOR,
            NetCableBlock.NETCABLE,
            FacadeBlock.FACADE);

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!modelLocation.getResourceDomain().equals(XNet.MODID)) {
            return false;
        }
        if (modelLocation instanceof ModelResourceLocation && ((ModelResourceLocation)modelLocation).getVariant().equals("inventory")) {
            return false;
        }
        return NAMES.contains(modelLocation.getResourcePath());
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        if (FacadeBlock.FACADE.equals(modelLocation.getResourcePath())) {
            return FACADE_MODEL;
        } else {
            return GENERIC_MODEL;
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
*/}
