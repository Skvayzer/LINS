package com.example.mp11.myvideoplayer;

import com.example.mp11.VideoPlayer;


public interface HpLib_RendererBuilder {
    void buildRenderers(VideoPlayer player);
    void cancel();
}
