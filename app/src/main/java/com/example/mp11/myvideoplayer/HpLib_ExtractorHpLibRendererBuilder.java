package com.example.mp11.myvideoplayer;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;

import com.example.mp11.VideoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

public class HpLib_ExtractorHpLibRendererBuilder implements HpLib_RendererBuilder {

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;

    private final Context context;
    private final String userAgent;
    private final Uri uri;

    public HpLib_ExtractorHpLibRendererBuilder(Context context, String userAgent, Uri uri) {
        this.context = context;
        this.userAgent = userAgent;
        this.uri = uri;
    }

    @Override
    public void buildRenderers(VideoPlayer player) {
        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        Handler mainHandler = player.getMainHandler();

        // Build the video and audio renderers.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mainHandler, null);
        DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator,
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);
        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context,
                sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000);
        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,MediaCodecSelector.DEFAULT,null,true);
        // Invoke the callback.
        TrackRenderer[] renderers = new TrackRenderer[VideoPlayer.RENDERER_COUNT];
        renderers[VideoPlayer.TYPE_VIDEO] = videoRenderer;
        renderers[VideoPlayer.TYPE_AUDIO] = audioRenderer;
        player.onRenderers(renderers, bandwidthMeter);
    }

    @Override
    public void cancel() {
        // Do nothing.
    }

}
