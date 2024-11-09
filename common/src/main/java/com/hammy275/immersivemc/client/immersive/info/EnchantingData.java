package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.book.BookRenderable;
import com.hammy275.immersivemc.client.immersive.book.ClientBookData;
import com.hammy275.immersivemc.client.immersive.book.WrittenBookHelpers;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import com.hammy275.immersivemc.common.compat.apotheosis.ApothStats;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ETableStorage;
import com.hammy275.immersivemc.common.util.PosRot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.hammy275.immersivemc.common.immersive.CommonBookData.textStackScaleSize;

public class EnchantingData {

    public final ETableData weakData = new ETableData();
    public final ETableData midData = new ETableData();
    public final ETableData strongData = new ETableData();
    public ApothStats apothStats = ApothStats.EMPTY;

    protected ClientBookData bookData = null;

    public boolean hasAnyEnchantments() {
        return strongData.isPresent() || midData.isPresent() || weakData.isPresent();
    }

    @Nullable
    public ClientBookData getBookData(BuiltImmersiveInfo<EnchantingData> info) {
        if (bookData == null && Apoth.apothImpl.enchantModuleEnabled()) {
            bookData = new ClientBookData();
            bookData.renderables.add(new WrittenBookHelpers.BookTextRenderer(leftPage -> {
                if (leftPage) {
                    boolean hasItem = !info.getItem(0).isEmpty();
                    return FormattedText.composite(
                            Component.literal("\n"),
                            translate("gui.apotheosis.enchant.eterna").withStyle(ChatFormatting.GREEN),
                            Component.literal("\n"),
                            makeStatComponent(apothStats.eterna(), 50f, hasItem, false).withStyle(ChatFormatting.GREEN),
                            Component.literal("\n\n"),
                            translate("gui.apotheosis.enchant.quanta").withStyle(ChatFormatting.RED),
                            Component.literal("\n"),
                            makeStatComponent(apothStats.quanta(), 100f, hasItem, true).withStyle(ChatFormatting.RED),
                            Component.literal("\n\n"),
                            translate("gui.apotheosis.enchant.arcana").withStyle(ChatFormatting.DARK_PURPLE),
                            Component.literal("\n"),
                            makeStatComponent(apothStats.arcana(), 100f, hasItem, true).withStyle(ChatFormatting.DARK_PURPLE),
                            Component.literal("\n\n"),
                            translate("gui.apotheosis.enchant.rectification").withStyle(ChatFormatting.YELLOW),
                            Component.literal("\n"),
                            makeStatComponent(apothStats.rectification(), 100f, hasItem, true).withStyle(ChatFormatting.YELLOW)
                    );
                } else {
                    return Component.EMPTY;
                }
            }, textStackScaleSize * 1.5f, new Vec3(0.45, 4d/3d, 0)));
            bookData.renderables.add(new ApothBarBookRenderable(() -> apothStats.eterna() / 50f, new Vec3(1, 0.5, 0),
                    0f, 197f, false, info));
            bookData.renderables.add(new ApothBarBookRenderable(() -> apothStats.quanta() / 100f, new Vec3(1, 0.1, 0),
                    5f, 202f, false, info));
            bookData.renderables.add(new ApothBarBookRenderable(() -> apothStats.arcana() / 100f, new Vec3(1, -0.3, 0),
                    10f, 207f, false, info));
            bookData.renderables.add(new ApothBarBookRenderable(() -> apothStats.rectification() / 100f, new Vec3(1, -0.7, 0),
                    15f, 20f, true, info));
        } else if (bookData != null && !Apoth.apothImpl.enchantModuleEnabled()) {
            bookData = null;
        }
        return bookData;
    }

    /**
     * Basic conversion of Apotheosis translatable keys to Zenith if Zenith is loaded
     * @param key Apotheosis key
     * @return Zenith key
     */
    private MutableComponent translate(String key) {
        if (Platform.isModLoaded("zenith")) {
            return Component.translatable(key.replaceFirst("apotheosis", "zenith"));
        }
        return Component.translatable(key);
    }

    private MutableComponent makeStatComponent(float stat, float max, boolean hasItem, boolean isPercent) {
        String end = isPercent ? "%" : "";
        if (!hasItem) {
            return Component.literal("??.??%s/??.??%s".formatted(end, end)).withStyle(ChatFormatting.OBFUSCATED);
        }
        return Component.literal("%.2f%s/%.2f%s".formatted(stat, end, max, end));
    }

    public static class ETableData {
        public int levelsNeeded;
        public List<Component> textPreviews = new ArrayList<>();

        public boolean isPresent() {
            return levelsNeeded > -1;
        }

        public void set(ETableStorage.SlotData data) {
            this.textPreviews.clear();
            if (data.enchantmentHints().isEmpty()) {
                this.textPreviews.add(Component.literal("???"));
            } else {
                for (int i = 0; i < data.enchantmentHints().size(); i++) {
                    Enchantment ench = Enchantment.byId(data.enchantmentHints().get(i));
                    if (ench != null) {
                        this.levelsNeeded = data.xpLevel();
                        String end = i == data.enchantmentHints().size() - 1 ? "...?" : "";
                        this.textPreviews.add(Component.literal(ench.getFullname(data.enchantmentHintLevels().get(i)).getString() + end));
                    } else {
                        this.levelsNeeded = -1;
                        break;
                    }
                }
            }
        }
    }

    private record ApothBarBookRenderable(Supplier<Float> amountFullSupplier, Vec3 offset,
                                          float startYEmpty, float startYFull, boolean useOurImageForFull,
                                          BuiltImmersiveInfo<EnchantingData> info) implements BookRenderable {

        private static ResourceLocation fullLocation = null;
        private static final ResourceLocation emptyLocation = new ResourceLocation(ImmersiveMC.MOD_ID, "apoth_bars.png");
        private static final float barMaxX = 109f;
        private static final float maxXY = 255f;

        @Override
        public void render(PoseStack stack, ClientBookData data, boolean leftPage, int light, PosRot bookPosRot) {
            if (leftPage || info.getItem(0).isEmpty()) return;
            if (fullLocation == null) {
                String modId = Platform.isModLoaded("zenith") ? "zenith" : "apotheosis";
                fullLocation = new ResourceLocation(modId, "textures/gui/enchanting_table.png");
            }
            renderBar(stack, light, amountFullSupplier.get());
        }

        private void renderBar(PoseStack stack, int light, float fullAmount) {
            // Most UIs render the empty bar, then draw the full one on top.
            // Here we need to draw part of the empty and part of the full instead to prevent z-fighting issues.
            stack.pushPose();
            stack.mulPose(Vector3f.YP.rotation((float) Math.PI));
            float size = 1f / (109f * 4f);
            stack.scale(size, size, size);
            renderBarPart(false, stack, light, 0, startYFull, fullAmount);
            renderBarPart(true, stack, light, barMaxX * fullAmount, startYEmpty, fullAmount);
            stack.popPose();
        }

        private void renderBarPart(boolean isEmpty, PoseStack stack, int light, float startX, float startY, float fullAmount) {
            if ((!isEmpty && fullAmount <= 0) || (isEmpty && 1 - fullAmount <= 0)) return;
            ResourceLocation barLoc = !isEmpty && !useOurImageForFull ? fullLocation : emptyLocation;
            VertexConsumer consumer =
                    Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(barLoc));
            PoseStack.Pose lastPose = stack.last();
            Matrix4f pose = lastPose.pose();
            Matrix3f normal = lastPose.normal();

            float minImageU = startX / maxXY;
            float maxImageU = (startX + barMaxX * (isEmpty ? 1 - fullAmount : fullAmount)) / maxXY;
            float minImageV = startY / maxXY;
            float maxImageV = (startY + 4) / maxXY;

            float minX = isEmpty ? barMaxX * fullAmount : 0f;
            float minY = 0f;
            float maxX = isEmpty ? barMaxX : barMaxX * fullAmount;
            float maxY = 16f; // Normal bar sizing would be 4f, but want it to be taller

            consumer.vertex(pose, minX, minY, 0)
                    .color(255, 255, 255, 255)
                    .uv(minImageU, maxImageV)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(light)
                    .normal(normal, 0, 1, 0)
                    .endVertex();
            consumer.vertex(pose, maxX, minY, 0)
                    .color(255, 255, 255, 255)
                    .uv(maxImageU, maxImageV)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(light)
                    .normal(normal, 0, 1, 0)
                    .endVertex();
            consumer.vertex(pose, maxX, maxY, 0)
                    .color(255, 255, 255, 255)
                    .uv(maxImageU, minImageV)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(light)
                    .normal(normal, 0, 1, 0)
                    .endVertex();
            consumer.vertex(pose, minX, maxY, 0)
                    .color(255, 255, 255, 255)
                    .uv(minImageU, minImageV)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(light)
                    .normal(normal, 0, 1, 0)
                    .endVertex();
        }

        @Override
        public Vec3 getStartOffset(ClientBookData data, boolean leftPage, PosRot bookPosRot) {
            return offset;
        }
    }
}
