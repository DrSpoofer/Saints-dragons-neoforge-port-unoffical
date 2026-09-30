package com.leon.saintsdragons.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public final class DragonParticleRenderTypes {
    /**
     * Standard alpha blending with depth testing, but without writing the
     * particle quad into the depth buffer. This lets overlapping glow layers
     * remain independently visible while still being hidden by world geometry.
     */
    public static final ParticleRenderType TRANSLUCENT_NO_DEPTH_WRITE = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.setShader(DragonParticleShaders::getImpactGlowShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            return new StateRestoringBufferBuilder(StateRestoringBufferBuilder.buffer(),
                    VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return "SAINTS_DRAGONS_TRANSLUCENT_NO_DEPTH_WRITE";
        }
    };

    /**
     * 1.21 removed {@code ParticleRenderType#end}. The particle engine builds the
     * returned buffer after all particles of this type are rendered, so drawing and
     * restoring the render state here reproduces the original end() callback. The
     * mesh is drawn directly and {@code null} is returned so it is not drawn twice.
     */
    private static final class StateRestoringBufferBuilder extends BufferBuilder {
        private static ByteBufferBuilder sharedBuffer;

        private StateRestoringBufferBuilder(ByteBufferBuilder buffer, VertexFormat.Mode mode, VertexFormat format) {
            super(buffer, mode, format);
        }

        private static ByteBufferBuilder buffer() {
            if (sharedBuffer == null) {
                sharedBuffer = new ByteBufferBuilder(786432);
            }
            return sharedBuffer;
        }

        @Override
        public MeshData build() {
            MeshData mesh = super.build();
            if (mesh != null) {
                BufferUploader.drawWithShader(mesh);
            }
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            return null;
        }
    }

    private DragonParticleRenderTypes() {
    }
}
