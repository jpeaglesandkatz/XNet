package mcjty.xnet.modules.cables.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import mcjty.xnet.XNet;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class CableModelLoader implements IModelLoader<CableModelLoader.CableModelGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public CableModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new CableModelGeometry();
    }

    public static class CableModelGeometry implements IModelGeometry<CableModelGeometry> {

        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            return new GenericCableBakedModel();
        }

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            List<RenderMaterial> materials = new ArrayList<>();
            materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(XNet.MODID, "block/connector_side")));

            for (int i = 0 ; i <= 4 ; i++) {
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(XNet.MODID, "block/cable"+i+"/advanced_connector")));
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(XNet.MODID, "block/cable"+i+"/connector")));
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_corner_netcable")));
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_cross_netcable")));
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_end_netcable")));
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_netcable")));
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_none_netcable")));
                materials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_three_netcable")));
            }
            return materials;
        }
    }
}
