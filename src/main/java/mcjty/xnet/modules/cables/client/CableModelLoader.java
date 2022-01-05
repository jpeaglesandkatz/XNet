package mcjty.xnet.modules.cables.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import mcjty.xnet.XNet;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class CableModelLoader implements IModelLoader<CableModelLoader.CableModelGeometry> {

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {

    }

    @Override
    @Nonnull
    public CableModelGeometry read(@Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents) {
        return new CableModelGeometry();
    }

    public static class CableModelGeometry implements IModelGeometry<CableModelGeometry> {

        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            return new GenericCableBakedModel();
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            List<Material> materials = new ArrayList<>();
            materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/connector_side")));

            for (int i = 0 ; i <= 4 ; i++) {
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/advanced_connector")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/connector")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_corner_netcable")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_cross_netcable")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_end_netcable")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_netcable")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_none_netcable")));
                materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_three_netcable")));
            }
            return materials;
        }
    }
}
