package com.example.mp11.myvideoplayer;

import com.example.mp11.VideoPlayer;

/**
 * Created by kunal.bhatia on 05-05-2016.
 */
public interface HpLib_RendererBuilder {
    void buildRenderers(VideoPlayer player);
    void cancel();
}
