package mcjty.xnet.modules.cables.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mcjty.lib.client.BaseGeometry;
import mcjty.xnet.XNet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CableModelLoader implements IGeometryLoader<CableModelLoader.CableModelGeometry> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(XNet.MODID, "cableloader");

    public static void register(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ID, new CableModelLoader());
    }


    @Override
    public CableModelGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return new CableModelGeometry();
    }

    public static class CableModelGeometry extends BaseGeometry<CableModelGeometry> {

        @Override
        public BakedModel bake() {
            return new GenericCableBakedModel();
        }

        @Override
        public Collection<Material> getMaterials() {
            List<Material> materials = new ArrayList<>();
            materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(XNet.MODID, "block/connector_side")));

            for (int i = 0 ; i <= 4 ; i++) {
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(XNet.MODID, "block/cable"+i+"/advanced_connector")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(XNet.MODID, "block/cable"+i+"/connector")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(XNet.MODID, "block/cable"+i+"/normal_corner_netcable")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(XNet.MODID, "block/cable"+i+"/normal_cross_netcable")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(XNet.MODID, "block/cable"+i+"/normal_end_netcable")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(XNet.MODID, "block/cable"+i+"/normal_netcable")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(XNet.MODID, "block/cable"+i+"/normal_none_netcable")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(XNet.MODID, "block/cable"+i+"/normal_three_netcable")));
            }
            return materials;
        }
    }
}
