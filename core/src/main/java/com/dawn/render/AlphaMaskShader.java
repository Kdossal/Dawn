package com.dawn.render;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/** SpriteBatch shader that outputs white with source texture alpha (for highlight masks). */
public final class AlphaMaskShader extends ShaderProgram {
    private static final String VERTEX =
            """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
            void main() {
                v_color = a_color;
                v_texCoords = a_texCoord0;
                gl_Position = u_projTrans * a_position;
            }
            """;

    private static final String FRAGMENT =
            """
            #ifdef GL_ES
            #define LOWP lowp
            precision mediump float;
            #else
            #define LOWP
            #endif
            varying LOWP vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            void main() {
                float a = texture2D(u_texture, v_texCoords).a * v_color.a;
                gl_FragColor = vec4(1.0, 1.0, 1.0, a);
            }
            """;

    public AlphaMaskShader() {
        super(VERTEX, FRAGMENT);
        if (!isCompiled()) {
            throw new IllegalStateException("AlphaMaskShader compile error: " + getLog());
        }
    }
}
