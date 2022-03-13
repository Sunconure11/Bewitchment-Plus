package dev.mrsterner.bewitchmentplus.client.renderer;

import dev.mrsterner.bewitchmentplus.BewitchmentPlus;
import dev.mrsterner.bewitchmentplus.client.model.GobletItemModel;
import dev.mrsterner.bewitchmentplus.common.block.blockentity.GobletBlockEntity;
import dev.mrsterner.bewitchmentplus.common.item.GobletBlockItem;
import dev.mrsterner.bewitchmentplus.common.registry.BWPObjects;
import moriyashiine.bewitchment.common.registry.BWObjects;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.renderer.RendererAccessImpl;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.screen.PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;

public class GobletBlockItemRenderer implements BlockEntityRenderer<GobletBlockEntity>, BuiltinItemRendererRegistry.DynamicItemRenderer {
    public static final SpriteIdentifier BLOOD = new SpriteIdentifier(BLOCK_ATLAS_TEXTURE, new Identifier(BewitchmentPlus.MODID, "block/goblet_fluid"));
    public static final SpriteIdentifier HONEY = new SpriteIdentifier(BLOCK_ATLAS_TEXTURE, new Identifier(BewitchmentPlus.MODID, "block/honey_fluid"));
    public static final SpriteIdentifier UNICORN = new SpriteIdentifier(BLOCK_ATLAS_TEXTURE, new Identifier(BewitchmentPlus.MODID, "block/goblet_fluid_unicorn"));
    private static final float EDGE_SIZE = 1f / 16f;
    private static final float INNER_SIZE = 1f - (EDGE_SIZE * 2f);
    public GobletItemModel gobletItemModel;

    public GobletBlockItemRenderer() {
        gobletItemModel = new GobletItemModel(GobletItemModel.getTexturedModelData().createModel());
    }

    public Identifier getGobletTexture(GobletBlockItem itemStack) {
        return new Identifier(BewitchmentPlus.MODID, BWPObjects.SILVER_GOBLET.asItem() == itemStack.asItem() ? "textures/block/silver_goblet.png" : BWPObjects.GOLD_GOBLET.asItem() == itemStack.asItem() ? "textures/block/gold_goblet.png" : "textures/block/netherite_goblet.png");
    }

    @Override
    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0F, 1.2F, 0F);
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180));
        VertexConsumer ivertexbuilder1 = ItemRenderer.getItemGlintConsumer(vertexConsumers, this.gobletItemModel.getLayer(getGobletTexture((GobletBlockItem) stack.getItem())), false, stack.hasGlint());
        gobletItemModel.render(matrices, ivertexbuilder1, light, overlay, 1, 1, 1, 1);
        matrices.pop();
        if (stack.hasNbt()) {
            var nbt = stack.getNbt();
            if (nbt.contains("BlockEntityTag")) {
                matrices.push();
                matrices.scale(0.25f, 0.25f, 0.25f);
                matrices.translate(-0.5F, 0.75F, 0.5F);
                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180));
                renderFluid(matrices, vertexConsumers, light, overlay, null, stack);
                matrices.pop();
            }
        }
    }

    @Override
    public void render(GobletBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.getStack(0).isEmpty()) {
            matrices.push();
            matrices.scale(0.25f, 0.25f, 0.25f);
            matrices.translate(1.5, 0.9, 1.5);
            renderFluid(matrices, vertexConsumers, light, overlay, entity, null);
            matrices.pop();
        }
        VertexConsumer ivertexbuilder1 = ItemRenderer.getItemGlintConsumer(vertexConsumers, this.gobletItemModel.getLayer(getGobletTexture(entity.getGoblet())), false, false);
        matrices.translate(0.5, 1.5, 0.5);
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180));
        gobletItemModel.render(matrices, ivertexbuilder1, light, overlay, 1, 1, 1, 1);
    }


    private void renderFluid(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, @Nullable GobletBlockEntity entity, @Nullable ItemStack itemStack) {
        matrices.push();
        var variant = FluidVariant.of(Fluids.WATER);
        var handler = FluidVariantRendering.getHandlerOrDefault(variant.getFluid());
        var sprite = handler.getSprites(variant)[0];
        var flipped = handler.fillsFromTop(variant);
        var luminance = variant.getFluid().getDefaultState().getBlockState().getLuminance();
        var renderer = RendererAccessImpl.INSTANCE.getRenderer();
        var consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
        var builder = renderer.meshBuilder();
        var emitter = builder.getEmitter();
        var newColor = ColorHelper.swapRedBlueIfNeeded(0x3f76e4);
        if (entity != null) {
            newColor = ColorHelper.swapRedBlueIfNeeded(entity.color);
            sprite = entity.color == 0xff0000 ? BLOOD.getSprite() : entity.color == 0xff9500 ? HONEY.getSprite() : entity.color == 0x9cb7b8 ? UNICORN.getSprite() : handler.getSprites(variant)[0];
        } else if (itemStack != null && itemStack.hasNbt()) {
            var nbt = itemStack.getNbt();
            if (nbt.contains("BlockEntityTag")) {
                var slots = DefaultedList.ofSize(1, ItemStack.EMPTY);
                Inventories.readNbt(nbt.getCompound("BlockEntityTag"), slots);
                newColor = ColorHelper.swapRedBlueIfNeeded(slots.get(0).getItem() == BWObjects.BOTTLE_OF_BLOOD ? 0xff0000 : slots.get(0).getItem() == Items.HONEY_BOTTLE ? 0xff9500 : slots.get(0).getItem() == BWPObjects.UNICORN_BLOOD ? 0x9cb7b8 : 0x3f76e4);
                sprite = slots.get(0).getItem() == BWObjects.BOTTLE_OF_BLOOD ? BLOOD.getSprite() : slots.get(0).getItem() == Items.HONEY_BOTTLE ? HONEY.getSprite() : slots.get(0).getItem() == BWPObjects.UNICORN_BLOOD ? UNICORN.getSprite() : handler.getSprites(variant)[0];
            }
        }

        emitFluidFace(emitter, sprite, newColor, flipped, Direction.UP, 1f, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.DOWN, 1f, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.NORTH, 1f, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.EAST, 1f, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.SOUTH, 1f, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.WEST, 1f, 0f);

        var mesh = builder.build();
        var newLight = (light & 0xFFFF_0000) | (Math.max((light >> 4) & 0xF, luminance) << 4);
        renderMesh(mesh, matrices, consumer, newLight, overlay);
        matrices.pop();
    }

    private void emitFluidFace(QuadEmitter emitter, Sprite sprite, int color, boolean flipped, Direction direction, float height, float depth) {
        var minU = sprite.getMinU();
        var minV = sprite.getMinV();

        var uMult = sprite.getMaxU() - minU;
        var vMult = sprite.getMaxV() - minV;

        var bottomleft = flipped ? (1f - EDGE_SIZE - (height * INNER_SIZE)) : EDGE_SIZE;
        var right = 1f - EDGE_SIZE;
        var top = flipped ? (1f - EDGE_SIZE) : (EDGE_SIZE + (height * INNER_SIZE));
        var deep = EDGE_SIZE + (depth * INNER_SIZE);

        emitter.square(direction, bottomleft, bottomleft, right, top, deep);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_ROTATE_NONE);
        emitter.spriteColor(0, color, color, color, color);
        emitter.sprite(0, 0, minU + bottomleft * uMult, minV + (1f - top) * vMult);
        emitter.sprite(1, 0, minU + bottomleft * uMult, minV + (1f - bottomleft) * vMult);
        emitter.sprite(2, 0, minU + right * uMult, minV + (1f - bottomleft) * vMult);
        emitter.sprite(3, 0, minU + right * uMult, minV + (1f - top) * vMult);
        emitter.emit();
    }

    private void renderMesh(Mesh mesh, MatrixStack matrices, VertexConsumer consumer, int light, int overlay) {
        var quadList = ModelHelper.toQuadLists(mesh);
        for (List<BakedQuad> bakedQuads : quadList) {
            for (BakedQuad bq : bakedQuads) {
                float[] brightness = new float[]{1f, 1f, 1f, 1f};
                int[] lights = new int[]{light, light, light, light};
                consumer.quad(matrices.peek(), bq, brightness, 1f, 1f, 1f, lights, overlay, true);
            }
        }
    }
}
