package mcjty.xnet.modules.facade.client;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FacadeBakedModel implements IDynamicBakedModel {

    private VertexFormat format;
    private static TextureAtlasSprite spriteCable;

    public FacadeBakedModel(VertexFormat format) {
        this.format = format;
    }

    private static void initTextures() {
        if (spriteCable == null) {
            spriteCable = Minecraft.getInstance().getTextureMap().getAtlasSprite(XNet.MODID + ":block/facade");
        }
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        BlockState facadeId = extraData.getData(GenericCableBlock.FACADEID);
        if (facadeId == null) {
            return Collections.emptyList();
        }

        BlockState facadeState = facadeId.getBlockState();
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer != null && !facadeState.getBlock().canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
            return Collections.emptyList();
        }
        IBakedModel model = getModel(facadeState);
        try {
            return model.getQuads(state, side, rand);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private IBakedModel getModel(@Nonnull BlockState state) {
        initTextures();
        IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
        return model;
    }


    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        initTextures();
        return spriteCable;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

}
