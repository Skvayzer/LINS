package com.example.mp11.mydynvideoplayer;

import com.example.mp11.DynVideoPlayer;
import com.example.mp11.VideoPlayer;


public interface HpLib_RendererBuilder {
    void buildRenderers(DynVideoPlayer player);
    void cancel();
}
