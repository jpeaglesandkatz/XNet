package mcjty.xnet.modules.cables.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import mcjty.xnet.XNet;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class CableModelLoader implements IGeometryLoader<CableModelLoader.CableModelGeometry> {

    public static void register(ModelEvent.RegisterGeometryLoaders event) {
        event.register("cableloader", new CableModelLoader());
    }


    @Override
    public CableModelGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return new CableModelGeometry();
    }

    public static class CableModelGeometry implements IUnbakedGeometry<CableModelGeometry> {

        // @todo 1.19.3
        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
            return new GenericCableBakedModel();
        }

        // @todo 1.19.3
//        @Override
//        public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
//            List<Material> materials = new ArrayList<>();
//            materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/connector_side")));
//
//            for (int i = 0 ; i <= 4 ; i++) {
//                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/advanced_connector")));
//                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/connector")));
//                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_corner_netcable")));
//                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_cross_netcable")));
//                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_end_netcable")));
//                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_netcable")));
//                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_none_netcable")));
//                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_three_netcable")));
//            }
//            return materials;
//        }
    }
}
