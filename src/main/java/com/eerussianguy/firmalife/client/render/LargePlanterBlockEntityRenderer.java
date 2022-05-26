package com.eerussianguy.firmalife.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;

import com.eerussianguy.firmalife.common.blockentities.LargePlanterBlockEntity;
import com.eerussianguy.firmalife.common.util.Plantable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class LargePlanterBlockEntityRenderer implements BlockEntityRenderer<LargePlanterBlockEntity>
{
    @Override
    public void render(LargePlanterBlockEntity planter, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLight, int combinedOverlay)
    {
        Plantable plant = planter.getPlantable(0);
        if (plant == null) return;

        poseStack.pushPose();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(plant.getTexture(planter.getGrowth(0)));
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());

        renderCross(0.125f, 0.875f, 0.3125f, 1.0625f, poseStack, buffer, combinedLight, combinedOverlay, sprite, 0f, 0f, 1f, 1f);

        poseStack.popPose();
    }

    public static void renderCross(float x, float z, float y1, float y2, PoseStack poseStack, VertexConsumer buffer, int combinedLight, int combinedOverlay, TextureAtlasSprite sprite, float u1, float v1, float u2, float v2)
    {
        for (float[] ver : getCrossHalfVertices(x, y1, x, z, y2, z, u1, v1, u2, v2))
        {
            renderTexturedVertex(poseStack, buffer, combinedLight, combinedOverlay, ver[0], ver[1], ver[2], sprite.getU(ver[3] * 16), sprite.getV(16 - (ver[4] * 16)), -1.414f, 0f, -1.414f);
        }

        for (float[] ver : getCrossHalfVertices(x, y1, z, z, y2, x, u1, v1, u2, v2))
        {
            renderTexturedVertex(poseStack, buffer, combinedLight, combinedOverlay, ver[0], ver[1], ver[2], sprite.getU(ver[3] * 16), sprite.getV(16 - (ver[4] * 16)), 1.414f, 0f, 1.414f);
        }
    }

    public static float[][] getCrossHalfVertices(float x1, float y1, float z1, float x2, float y2, float z2, float u1, float v1, float u2, float v2)
    {
        return new float[][] {
            {x1, y1, z1, u1, v1},
            {x2, y1, z2, u2, v1},
            {x2, y2, z2, u2, v2},
            {x1, y2, z1, u1, v2},

            {x2, y1, z2, u2, v1},
            {x1, y1, z1, u1, v1},
            {x1, y2, z1, u1, v2},
            {x2, y2, z2, u2, v2}
        };
    }

    public static void renderTexturedVertex(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ)
    {
        buffer.vertex(poseStack.last().pose(), x, y, z)
            .color(1f, 1f, 1f, 1f)
            .uv(u, v)
            .uv2(packedLight)
            .overlayCoords(packedOverlay)
            .normal(poseStack.last().normal(), normalX, normalY, normalZ)
            .endVertex();
    }
}