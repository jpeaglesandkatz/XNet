package mcjty.xnet.modules.facade.client;

import mcjty.lib.client.AbstractDynamicBakedModel;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FacadeBakedModel extends AbstractDynamicBakedModel {

    public static final ResourceLocation TEXTURE_FACADE = new ResourceLocation(XNet.MODID, "block/facade");

    private static TextureAtlasSprite spriteCable;

    private static void initTextures() {
        if (spriteCable == null) {
            spriteCable = getTexture(TEXTURE_FACADE);
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
//        RenderType layer = MinecraftForgeClient.getRenderLayer();
//        if (layer != null && !facadeState.getBlock().canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
//            return Collections.emptyList();
//        }
        IBakedModel model = getModel(facadeState);
        try {
            return model.getQuads(state, side, rand, null);
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
    public TextureAtlasSprite getParticleTexture() {
        initTextures();
        return spriteCable;
    }
}
