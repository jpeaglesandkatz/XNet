package mcjty.xnet.modules.facade.client;

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

import static mcjty.xnet.modules.facade.client.FacadeBakedModel.TEXTURE_FACADE;

public class FacadeModelLoader implements IModelLoader<FacadeModelLoader.FacadeModelGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public FacadeModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new FacadeModelGeometry();
    }

    public static class FacadeModelGeometry implements IModelGeometry<FacadeModelGeometry> {
        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            return new FacadeBakedModel();
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            List<Material> materials = new ArrayList<>();
            materials.add(new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, TEXTURE_FACADE));
            return materials;
        }
    }
}
