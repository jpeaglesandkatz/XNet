package mcjty.xnet.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.lib.client.CustomRenderTypes;
import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.cables.ConnectorType;
import mcjty.xnet.modules.cables.blocks.ConnectorBlock;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.facade.IFacadeSupport;
import mcjty.xnet.modules.facade.blocks.FacadeBlock;
import mcjty.xnet.setup.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.ArrayList;
import java.util.List;

import static mcjty.xnet.modules.cables.ConnectorType.BLOCK;
import static mcjty.xnet.modules.cables.ConnectorType.CABLE;

public class RenderWorldLastEventHandler {

    public static void tick(RenderWorldLastEvent evt) {
        Minecraft mc = Minecraft.getInstance();

        ItemStack heldItem = mc.player.getHeldItem(Hand.MAIN_HAND);
        if (!heldItem.isEmpty()) {
            if (heldItem.getItem() instanceof BlockItem) {
                if (((BlockItem) heldItem.getItem()).getBlock() instanceof GenericCableBlock) {
                    renderCables(evt, mc);
                }
            }
        }
    }

    private static void renderCables(RenderWorldLastEvent evt, Minecraft mc) {
        PlayerEntity p = mc.player;

        MatrixStack matrixStack = evt.getMatrixStack();
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(CustomRenderTypes.OVERLAY_LINES);

        World world = mc.world;

        matrixStack.push();

        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        Matrix4f positionMatrix = matrixStack.getLast().getPositionMatrix();

        for (int dx = -20 ; dx <= 20 ; dx++) {
            for (int dy = -20 ; dy <= 20 ; dy++) {
                for (int dz = -20 ; dz <= 20 ; dz++) {
                    BlockPos c = p.getPosition().add(dx, dy, dz);
                    BlockState state = world.getBlockState(c);
                    Block block = state.getBlock();
                    if (block instanceof FacadeBlock || block instanceof ConnectorBlock || block instanceof GenericCableBlock) {
                        TileEntity te = world.getTileEntity(c);
                        if (te instanceof IFacadeSupport) {
                            BlockState facadeId = ((IFacadeSupport) te).getMimicBlock();
                            if (((!Config.showNonFacadedCablesWhileSneaking.get()) || (!p.isShiftKeyDown())) && facadeId == null && !(block instanceof FacadeBlock)) {
                                continue;
                            }
                        } else if (!Config.showNonFacadedCablesWhileSneaking.get() || !p.isShiftKeyDown()) {
                            continue;
                        }
                        CableColor color = state.get(GenericCableBlock.COLOR);
                        float r = 0;
                        float g = 0;
                        float b = 0;
                        switch (color) {
                            case BLUE:
                                r = .4f;
                                g = .4f;
                                b = 1f;
                                break;
                            case RED:
                                r = 1f;
                                g = .4f;
                                b = .4f;
                                break;
                            case YELLOW:
                                r = 1f;
                                g = 1f;
                                b = .4f;
                                break;
                            case GREEN:
                                r = .4f;
                                g = 1f;
                                b = .4f;
                                break;
                            case ROUTING:
                                r = .7f;
                                g = .7f;
                                b = .7f;
                                break;
                        }
                        List<Rect> quads = getQuads(state);
                        for (Rect quad : quads) {
                            renderRect(builder, positionMatrix, quad, c, r, g, b, 0.5f);
                        }
                    }
                }
            }
        }

        matrixStack.pop();

        RenderSystem.disableDepthTest();
        buffer.finish(CustomRenderTypes.OVERLAY_LINES);
    }


    private static Vec3d v(double x, double y, double z) {
        return new Vec3d(x, y, z);
    }

    private static class Rect {
        public Vec3d v1;
        public Vec3d v2;
        public Vec3d v3;
        public Vec3d v4;

        public Rect(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }
    }

    private static List<Rect> getQuads(BlockState state) {
        ConnectorType north = state.get(GenericCableBlock.NORTH);
        ConnectorType south = state.get(GenericCableBlock.SOUTH);
        ConnectorType west = state.get(GenericCableBlock.WEST);
        ConnectorType east = state.get(GenericCableBlock.EAST);
        ConnectorType up = state.get(GenericCableBlock.UP);
        ConnectorType down = state.get(GenericCableBlock.DOWN);
        List<Rect> quads = new ArrayList<>();

        double o = .4;      // Thickness of the cable. .0 would be full block, .5 is infinitely thin.
        double p = .1;      // Thickness of the connector as it is put on the connecting block
        double q = .2;      // The wideness of the connector

        // For each side we either cap it off if there is no similar block adjacent on that side
        // or else we extend so that we touch the adjacent block:

        if (up == CABLE) {
            quads.add(new Rect(v(1 - o, 1, o), v(1 - o, 1, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, o)));
            quads.add(new Rect(v(o, 1, 1 - o), v(o, 1, o), v(o, 1 - o, o), v(o, 1 - o, 1 - o)));
            quads.add(new Rect(v(o, 1, o), v(1 - o, 1, o), v(1 - o, 1 - o, o), v(o, 1 - o, o)));
            quads.add(new Rect(v(o, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1, 1 - o), v(o, 1, 1 - o)));
        } else if (up == BLOCK) {
            quads.add(new Rect(v(1 - o, 1 - p, o), v(1 - o, 1 - p, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, o)));
            quads.add(new Rect(v(o, 1 - p, 1 - o), v(o, 1 - p, o), v(o, 1 - o, o), v(o, 1 - o, 1 - o)));
            quads.add(new Rect(v(o, 1 - p, o), v(1 - o, 1 - p, o), v(1 - o, 1 - o, o), v(o, 1 - o, o)));
            quads.add(new Rect(v(o, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - p, 1 - o), v(o, 1 - p, 1 - o)));

            quads.add(new Rect(v(1 - q, 1 - p, q), v(1 - q, 1, q), v(1 - q, 1, 1 - q), v(1 - q, 1 - p, 1 - q)));
            quads.add(new Rect(v(q, 1 - p, 1 - q), v(q, 1, 1 - q), v(q, 1, q), v(q, 1 - p, q)));
            quads.add(new Rect(v(q, 1, q), v(1 - q, 1, q), v(1 - q, 1 - p, q), v(q, 1 - p, q)));
            quads.add(new Rect(v(q, 1 - p, 1 - q), v(1 - q, 1 - p, 1 - q), v(1 - q, 1, 1 - q), v(q, 1, 1 - q)));

            quads.add(new Rect(v(q, 1 - p, q), v(1 - q, 1 - p, q), v(1 - q, 1 - p, 1 - q), v(q, 1 - p, 1 - q)));
            quads.add(new Rect(v(q, 1, q), v(q, 1, 1 - q), v(1 - q, 1, 1 - q), v(1 - q, 1, q)));
        } else {
            quads.add(new Rect(v(o,     1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, o),     v(o,     1 - o, o)));
        }

        if (down == CABLE) {
            quads.add(new Rect(v(1 - o, o, o), v(1 - o, o, 1 - o), v(1 - o, 0, 1 - o), v(1 - o, 0, o)));
            quads.add(new Rect(v(o, o, 1 - o), v(o, o, o), v(o, 0, o), v(o, 0, 1 - o)));
            quads.add(new Rect(v(o, o, o), v(1 - o, o, o), v(1 - o, 0, o), v(o, 0, o)));
            quads.add(new Rect(v(o, 0, 1 - o), v(1 - o, 0, 1 - o), v(1 - o, o, 1 - o), v(o, o, 1 - o)));
        } else if (down == BLOCK) {
            quads.add(new Rect(v(1 - o, o, o), v(1 - o, o, 1 - o), v(1 - o, p, 1 - o), v(1 - o, p, o)));
            quads.add(new Rect(v(o, o, 1 - o), v(o, o, o), v(o, p, o), v(o, p, 1 - o)));
            quads.add(new Rect(v(o, o, o), v(1 - o, o, o), v(1 - o, p, o), v(o, p, o)));
            quads.add(new Rect(v(o, p, 1 - o), v(1 - o, p, 1 - o), v(1 - o, o, 1 - o), v(o, o, 1 - o)));

            quads.add(new Rect(v(1 - q, 0, q), v(1 - q, p, q), v(1 - q, p, 1 - q), v(1 - q, 0, 1 - q)));
            quads.add(new Rect(v(q, 0, 1 - q), v(q, p, 1 - q), v(q, p, q), v(q, 0, q)));
            quads.add(new Rect(v(q, p, q), v(1 - q, p, q), v(1 - q, 0, q), v(q, 0, q)));
            quads.add(new Rect(v(q, 0, 1 - q), v(1 - q, 0, 1 - q), v(1 - q, p, 1 - q), v(q, p, 1 - q)));

            quads.add(new Rect(v(q, p, 1 - q), v(1 - q, p, 1 - q), v(1 - q, p, q), v(q, p, q)));
            quads.add(new Rect(v(q, 0, 1 - q), v(q, 0, q), v(1 - q, 0, q), v(1 - q, 0, 1 - q)));
        } else {
            quads.add(new Rect(v(o, o, o), v(1 - o, o, o), v(1 - o, o, 1 - o), v(o, o, 1 - o)));
        }

        if (east == CABLE) {
            quads.add(new Rect(v(1, 1 - o, 1 - o), v(1, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, 1 - o)));
            quads.add(new Rect(v(1, o, o), v(1, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, o, o)));
            quads.add(new Rect(v(1, 1 - o, o), v(1, o, o), v(1 - o, o, o), v(1 - o, 1 - o, o)));
            quads.add(new Rect(v(1, o, 1 - o), v(1, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, o, 1 - o)));
        } else if (east == BLOCK) {
            quads.add(new Rect(v(1 - p, 1 - o, 1 - o), v(1 - p, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, 1 - o)));
            quads.add(new Rect(v(1 - p, o, o), v(1 - p, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, o, o)));
            quads.add(new Rect(v(1 - p, 1 - o, o), v(1 - p, o, o), v(1 - o, o, o), v(1 - o, 1 - o, o)));
            quads.add(new Rect(v(1 - p, o, 1 - o), v(1 - p, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, o, 1 - o)));

            quads.add(new Rect(v(1 - p, 1 - q, 1 - q), v(1, 1 - q, 1 - q), v(1, 1 - q, q), v(1 - p, 1 - q, q)));
            quads.add(new Rect(v(1 - p, q, q), v(1, q, q), v(1, q, 1 - q), v(1 - p, q, 1 - q)));
            quads.add(new Rect(v(1 - p, 1 - q, q), v(1, 1 - q, q), v(1, q, q), v(1 - p, q, q)));
            quads.add(new Rect(v(1 - p, q, 1 - q), v(1, q, 1 - q), v(1, 1 - q, 1 - q), v(1 - p, 1 - q, 1 - q)));

            quads.add(new Rect(v(1 - p, q, 1 - q), v(1 - p, 1 - q, 1 - q), v(1 - p, 1 - q, q), v(1 - p, q, q)));
            quads.add(new Rect(v(1, q, 1 - q), v(1, q, q), v(1, 1 - q, q), v(1, 1 - q, 1 - q)));
        } else {
            quads.add(new Rect(v(1 - o, o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, 1 - o), v(1 - o, o, 1 - o)));
        }

        if (west == CABLE) {
            quads.add(new Rect(v(o, 1 - o, 1 - o), v(o, 1 - o, o), v(0, 1 - o, o), v(0, 1 - o, 1 - o)));
            quads.add(new Rect(v(o, o, o), v(o, o, 1 - o), v(0, o, 1 - o), v(0, o, o)));
            quads.add(new Rect(v(o, 1 - o, o), v(o, o, o), v(0, o, o), v(0, 1 - o, o)));
            quads.add(new Rect(v(o, o, 1 - o), v(o, 1 - o, 1 - o), v(0, 1 - o, 1 - o), v(0, o, 1 - o)));
        } else if (west == BLOCK) {
            quads.add(new Rect(v(o, 1 - o, 1 - o), v(o, 1 - o, o), v(p, 1 - o, o), v(p, 1 - o, 1 - o)));
            quads.add(new Rect(v(o, o, o), v(o, o, 1 - o), v(p, o, 1 - o), v(p, o, o)));
            quads.add(new Rect(v(o, 1 - o, o), v(o, o, o), v(p, o, o), v(p, 1 - o, o)));
            quads.add(new Rect(v(o, o, 1 - o), v(o, 1 - o, 1 - o), v(p, 1 - o, 1 - o), v(p, o, 1 - o)));

            quads.add(new Rect(v(0, 1 - q, 1 - q), v(p, 1 - q, 1 - q), v(p, 1 - q, q), v(0, 1 - q, q)));
            quads.add(new Rect(v(0, q, q), v(p, q, q), v(p, q, 1 - q), v(0, q, 1 - q)));
            quads.add(new Rect(v(0, 1 - q, q), v(p, 1 - q, q), v(p, q, q), v(0, q, q)));
            quads.add(new Rect(v(0, q, 1 - q), v(p, q, 1 - q), v(p, 1 - q, 1 - q), v(0, 1 - q, 1 - q)));

            quads.add(new Rect(v(p, q, q), v(p, 1 - q, q), v(p, 1 - q, 1 - q), v(p, q, 1 - q)));
            quads.add(new Rect(v(0, q, q), v(0, q, 1 - q), v(0, 1 - q, 1 - q), v(0, 1 - q, q)));
        } else {
            quads.add(new Rect(v(o, o, 1 - o), v(o, 1 - o, 1 - o), v(o, 1 - o, o), v(o, o, o)));
        }

        if (north == CABLE) {
            quads.add(new Rect(v(o, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, 0), v(o, 1 - o, 0)));
            quads.add(new Rect(v(o, o, 0), v(1 - o, o, 0), v(1 - o, o, o), v(o, o, o)));
            quads.add(new Rect(v(1 - o, o, 0), v(1 - o, 1 - o, 0), v(1 - o, 1 - o, o), v(1 - o, o, o)));
            quads.add(new Rect(v(o, o, o), v(o, 1 - o, o), v(o, 1 - o, 0), v(o, o, 0)));
        } else if (north == BLOCK) {
            quads.add(new Rect(v(o, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, 1 - o, p), v(o, 1 - o, p)));
            quads.add(new Rect(v(o, o, p), v(1 - o, o, p), v(1 - o, o, o), v(o, o, o)));
            quads.add(new Rect(v(1 - o, o, p), v(1 - o, 1 - o, p), v(1 - o, 1 - o, o), v(1 - o, o, o)));
            quads.add(new Rect(v(o, o, o), v(o, 1 - o, o), v(o, 1 - o, p), v(o, o, p)));

            quads.add(new Rect(v(q, 1 - q, p), v(1 - q, 1 - q, p), v(1 - q, 1 - q, 0), v(q, 1 - q, 0)));
            quads.add(new Rect(v(q, q, 0), v(1 - q, q, 0), v(1 - q, q, p), v(q, q, p)));
            quads.add(new Rect(v(1 - q, q, 0), v(1 - q, 1 - q, 0), v(1 - q, 1 - q, p), v(1 - q, q, p)));
            quads.add(new Rect(v(q, q, p), v(q, 1 - q, p), v(q, 1 - q, 0), v(q, q, 0)));

            quads.add(new Rect(v(q, q, p), v(1 - q, q, p), v(1 - q, 1 - q, p), v(q, 1 - q, p)));
            quads.add(new Rect(v(q, q, 0), v(q, 1 - q, 0), v(1 - q, 1 - q, 0), v(1 - q, q, 0)));
        } else {
            quads.add(new Rect(v(o, 1 - o, o), v(1 - o, 1 - o, o), v(1 - o, o, o), v(o, o, o)));
        }

        if (south == CABLE) {
            quads.add(new Rect(v(o, 1 - o, 1), v(1 - o, 1 - o, 1), v(1 - o, 1 - o, 1 - o), v(o, 1 - o, 1 - o)));
            quads.add(new Rect(v(o, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, o, 1), v(o, o, 1)));
            quads.add(new Rect(v(1 - o, o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, 1), v(1 - o, o, 1)));
            quads.add(new Rect(v(o, o, 1), v(o, 1 - o, 1), v(o, 1 - o, 1 - o), v(o, o, 1 - o)));
        } else if (south == BLOCK) {
            quads.add(new Rect(v(o, 1 - o, 1 - p), v(1 - o, 1 - o, 1 - p), v(1 - o, 1 - o, 1 - o), v(o, 1 - o, 1 - o)));
            quads.add(new Rect(v(o, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, o, 1 - p), v(o, o, 1 - p)));
            quads.add(new Rect(v(1 - o, o, 1 - o), v(1 - o, 1 - o, 1 - o), v(1 - o, 1 - o, 1 - p), v(1 - o, o, 1 - p)));
            quads.add(new Rect(v(o, o, 1 - p), v(o, 1 - o, 1 - p), v(o, 1 - o, 1 - o), v(o, o, 1 - o)));

            quads.add(new Rect(v(q, 1 - q, 1), v(1 - q, 1 - q, 1), v(1 - q, 1 - q, 1 - p), v(q, 1 - q, 1 - p)));
            quads.add(new Rect(v(q, q, 1 - p), v(1 - q, q, 1 - p), v(1 - q, q, 1), v(q, q, 1)));
            quads.add(new Rect(v(1 - q, q, 1 - p), v(1 - q, 1 - q, 1 - p), v(1 - q, 1 - q, 1), v(1 - q, q, 1)));
            quads.add(new Rect(v(q, q, 1), v(q, 1 - q, 1), v(q, 1 - q, 1 - p), v(q, q, 1 - p)));

            quads.add(new Rect(v(q, 1 - q, 1 - p), v(1 - q, 1 - q, 1 - p), v(1 - q, q, 1 - p), v(q, q, 1 - p)));
            quads.add(new Rect(v(q, 1 - q, 1), v(q, q, 1), v(1 - q, q, 1), v(1 - q, 1 - q, 1)));
        } else {
            quads.add(new Rect(v(o, o, 1 - o), v(1 - o, o, 1 - o), v(1 - o, 1 - o, 1 - o), v(o, 1 - o, 1 - o)));
        }

        return quads;
    }

    public static void renderRect(IVertexBuilder buffer, Matrix4f positionMatrix, Rect rect, BlockPos p, float r, float g, float b, float a) {
        buffer.pos(positionMatrix, (float)(p.getX() + rect.v1.x), (float)(p.getY() + rect.v1.y), (float)(p.getZ() + rect.v1.z)).color(r, g, b, a).endVertex();
        buffer.pos(positionMatrix, (float)(p.getX() + rect.v2.x), (float)(p.getY() + rect.v2.y), (float)(p.getZ() + rect.v2.z)).color(r, g, b, a).endVertex();
        buffer.pos(positionMatrix, (float)(p.getX() + rect.v2.x), (float)(p.getY() + rect.v2.y), (float)(p.getZ() + rect.v2.z)).color(r, g, b, a).endVertex();
        buffer.pos(positionMatrix, (float)(p.getX() + rect.v3.x), (float)(p.getY() + rect.v3.y), (float)(p.getZ() + rect.v3.z)).color(r, g, b, a).endVertex();
        buffer.pos(positionMatrix, (float)(p.getX() + rect.v3.x), (float)(p.getY() + rect.v3.y), (float)(p.getZ() + rect.v3.z)).color(r, g, b, a).endVertex();
        buffer.pos(positionMatrix, (float)(p.getX() + rect.v4.x), (float)(p.getY() + rect.v4.y), (float)(p.getZ() + rect.v4.z)).color(r, g, b, a).endVertex();
        buffer.pos(positionMatrix, (float)(p.getX() + rect.v4.x), (float)(p.getY() + rect.v4.y), (float)(p.getZ() + rect.v4.z)).color(r, g, b, a).endVertex();
        buffer.pos(positionMatrix, (float)(p.getX() + rect.v1.x), (float)(p.getY() + rect.v1.y), (float)(p.getZ() + rect.v1.z)).color(r, g, b, a).endVertex();
    }
}
