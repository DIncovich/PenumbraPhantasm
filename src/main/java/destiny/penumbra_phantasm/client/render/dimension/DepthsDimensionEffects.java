package destiny.penumbra_phantasm.client.render.dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import destiny.penumbra_phantasm.PenumbraPhantasm;
import destiny.penumbra_phantasm.server.util.DepthsSkyLightning;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class DepthsDimensionEffects extends DimensionSpecialEffects {
    public static final ResourceLocation DEPTHS_DIMENSION_EFFECTS = new ResourceLocation(PenumbraPhantasm.MODID, "depths_dimension_effects");

    private static final ResourceLocation[] SILHOUETTE_TEXTURES = new ResourceLocation[]{
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/titan_1.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/titan_2.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/titan_3.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/debris_1.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/debris_2.png"),
            new ResourceLocation(PenumbraPhantasm.MODID, "textures/environment/depths/debris_3.png")
    };

    private static final float CYLINDER_RADIUS = 96F;
    private static final float HORIZON_LIFT = -5F;
    private static final float TWO_PI = (float) (Math.PI * 2D);
    private static final float TITAN_WIDTH = 16F;
    private static final float TITAN_HEIGHT = 16F;
    private static final float DEBRIS_WIDTH = 16F;
    private static final float DEBRIS_HEIGHT = 16F;
    private static final float FLASH_RADIUS_SCALE = 1.5F;
    private static final int FLASH_SEGMENTS = 24;

    private final VertexBuffer skyBuffer;
    private final VertexBuffer dynamicTexturedBuffer;
    private final VertexBuffer dynamicColorBuffer;

    private long silhouetteSeed = Long.MIN_VALUE;
    private List<Silhouette> silhouettes = List.of();

    public DepthsDimensionEffects() {
        super(Float.NaN, true, SkyType.NONE, false, false);
        this.skyBuffer = DarkWorldDimensionEffects.createLightSky();
        this.dynamicTexturedBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.dynamicColorBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(0F, 0F, 0F, 1F);
        RenderSystem.setShader(GameRenderer::getPositionShader);

        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        FogRenderer.levelFogColor();
        RenderSystem.setShaderFogStart(CYLINDER_RADIUS * 4F);
        RenderSystem.setShaderFogEnd(CYLINDER_RADIUS * 4.5F);

        this.renderSilhouettes(level, partialTick, poseStack, projectionMatrix);

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.depthMask(true);
        return true;
    }

    private void renderSilhouettes(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix) {
        this.ensureSilhouettes(level);
        this.renderFlashes(level, partialTick, poseStack, projectionMatrix);
        this.renderSpriteQuads(poseStack, projectionMatrix);
    }

    private void ensureSilhouettes(ClientLevel level) {
        long seed = DepthsSkyLightning.getSkySeed(level);
        if (seed == this.silhouetteSeed) {
            return;
        }

        RandomSource random = RandomSource.create(seed);
        int count = Mth.nextInt(random, DepthsSkyLightning.MIN_SILHOUETTE_COUNT, DepthsSkyLightning.MAX_SILHOUETTE_COUNT);
        float origin = random.nextFloat() * TWO_PI;
        List<Silhouette> placed = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            float azimuth = origin + TWO_PI * i / count;
            int textureIndex = random.nextInt(SILHOUETTE_TEXTURES.length);
            boolean titan = textureIndex < 3;
            float scale = Mth.lerp(random.nextFloat(), 0.85F, 1.15F);
            float width = scale * (titan ? TITAN_WIDTH : DEBRIS_WIDTH);
            float height = scale * (titan ? TITAN_HEIGHT : DEBRIS_HEIGHT);
            float yLift = Mth.lerp(random.nextFloat(), HORIZON_LIFT - 2F, HORIZON_LIFT + 4F);
            float rotation = titan ? 0F : (random.nextFloat() - 0.5F) * 0.4F;
            Vec3 center = new Vec3(Mth.cos(azimuth) * CYLINDER_RADIUS, yLift + height, Mth.sin(azimuth) * CYLINDER_RADIUS);
            placed.add(new Silhouette(center, azimuth, width, height, rotation, textureIndex));
        }

        this.silhouettes = placed;
        this.silhouetteSeed = seed;
    }

    private void renderFlashes(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        boolean drawing = false;

        ActiveFlash flash = this.getActiveFlash(level, partialTick);
        if (flash != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            drawing = true;

            Vec3 flashCenter = new Vec3(
                    Mth.cos(flash.event.azimuth()) * flash.event.radius(),
                    DepthsSkyLightning.FLASH_Y,
                    Mth.sin(flash.event.azimuth()) * flash.event.radius()
            );
            Basis basis = getUprightBasis(flashCenter, 0F);
            float radius = 16F * FLASH_RADIUS_SCALE;
            this.addFlashDisc(bufferBuilder, flashCenter, basis, radius, flash.alpha);
        }

        if (!drawing) {
            return;
        }

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
        this.dynamicColorBuffer.bind();
        this.dynamicColorBuffer.upload(renderedBuffer);
        this.dynamicColorBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();
    }

    private void renderSpriteQuads(PoseStack poseStack, Matrix4f projectionMatrix) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        for (int textureIndex = 0; textureIndex < SILHOUETTE_TEXTURES.length; textureIndex++) {
            boolean drawing = false;

            for (Silhouette silhouette : this.silhouettes) {
                if (silhouette.textureIndex != textureIndex) {
                    continue;
                }

                if (!drawing) {
                    RenderSystem.setShaderTexture(0, SILHOUETTE_TEXTURES[textureIndex]);
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                    drawing = true;
                }

                Basis basis = getUprightBasis(silhouette.center, silhouette.rotation);
                this.addSilhouetteQuad(bufferBuilder, silhouette.center, basis, silhouette.width, silhouette.height);
            }

            if (!drawing) {
                continue;
            }

            BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
            this.dynamicTexturedBuffer.bind();
            this.dynamicTexturedBuffer.upload(renderedBuffer);
            this.dynamicTexturedBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
            VertexBuffer.unbind();
        }
    }

    private ActiveFlash getActiveFlash(ClientLevel level, float partialTick) {
        long skySeed = DepthsSkyLightning.getSkySeed(level);
        double skyTime = level.getGameTime() + partialTick;
        long slot = (long) Math.floor(skyTime / DepthsSkyLightning.FLASH_PERIOD);
        ActiveFlash active = null;

        for (long currentSlot = slot - 1L; currentSlot <= slot; currentSlot++) {
            DepthsSkyLightning.FlashEvent event = DepthsSkyLightning.createFlash(skySeed, currentSlot);
            if (event == null) {
                continue;
            }

            double eventTime = (skyTime - event.startTick()) / event.duration();
            if (eventTime < 0.0D || eventTime > 1.0D) {
                continue;
            }

            float alpha = event.peak() * (1F - (float) eventTime);
            if (alpha > 0F && (active == null || alpha > active.alpha)) {
                active = new ActiveFlash(event, alpha);
            }
        }

        return active;
    }

    private void addFlashDisc(BufferBuilder bufferBuilder, Vec3 center, Basis basis, float radius, float alpha) {
        Vec3 flashCenter = center.add(center.normalize().scale(-0.25D));

        for (int i = 0; i < FLASH_SEGMENTS; i++) {
            float fromAngle = TWO_PI * i / FLASH_SEGMENTS;
            float toAngle = TWO_PI * (i + 1) / FLASH_SEGMENTS;
            Vec3 from = flashCenter.add(basis.right.scale(Mth.cos(fromAngle) * radius)).add(basis.up.scale(Mth.sin(fromAngle) * radius));
            Vec3 to = flashCenter.add(basis.right.scale(Mth.cos(toAngle) * radius)).add(basis.up.scale(Mth.sin(toAngle) * radius));
            bufferBuilder.vertex((float) flashCenter.x, (float) flashCenter.y, (float) flashCenter.z).color(1F, 1F, 1F, alpha).endVertex();
            bufferBuilder.vertex((float) from.x, (float) from.y, (float) from.z).color(1F, 1F, 1F, 0F).endVertex();
            bufferBuilder.vertex((float) to.x, (float) to.y, (float) to.z).color(1F, 1F, 1F, 0F).endVertex();
        }
    }

    private void addSilhouetteQuad(BufferBuilder bufferBuilder, Vec3 center, Basis basis, float width, float height) {
        Vec3 horizontal = basis.right.scale(width);
        Vec3 vertical = basis.up.scale(height);
        Vec3 topLeft = center.subtract(horizontal).add(vertical);
        Vec3 bottomLeft = center.subtract(horizontal).subtract(vertical);
        Vec3 bottomRight = center.add(horizontal).subtract(vertical);
        Vec3 topRight = center.add(horizontal).add(vertical);
        bufferBuilder.vertex((float) topLeft.x, (float) topLeft.y, (float) topLeft.z).color(0F, 0F, 0F, 1F).uv(0F, 0F).endVertex();
        bufferBuilder.vertex((float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z).color(0F, 0F, 0F, 1F).uv(0F, 1F).endVertex();
        bufferBuilder.vertex((float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z).color(0F, 0F, 0F, 1F).uv(1F, 1F).endVertex();
        bufferBuilder.vertex((float) topRight.x, (float) topRight.y, (float) topRight.z).color(0F, 0F, 0F, 1F).uv(1F, 0F).endVertex();
    }

    private static Basis getUprightBasis(Vec3 center, float rotation) {
        Vec3 towardCamera = center.scale(-1D).normalize();
        Vec3 up = new Vec3(0D, 1D, 0D);
        Vec3 right = up.cross(towardCamera).normalize();
        up = towardCamera.cross(right).normalize();

        if (rotation != 0F) {
            double cos = Mth.cos(rotation);
            double sin = Mth.sin(rotation);
            Vec3 rotatedRight = right.scale(cos).add(up.scale(sin));
            Vec3 rotatedUp = up.scale(cos).subtract(right.scale(sin));
            return new Basis(rotatedRight, rotatedUp);
        }

        return new Basis(right, up);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float v) {
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int i, int i1) {
        return false;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return true;
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        return false;
    }

    private record Basis(Vec3 right, Vec3 up) {
    }

    private record Silhouette(Vec3 center, float azimuth, float width, float height, float rotation, int textureIndex) {
    }

    private record ActiveFlash(DepthsSkyLightning.FlashEvent event, float alpha) {
    }
}
