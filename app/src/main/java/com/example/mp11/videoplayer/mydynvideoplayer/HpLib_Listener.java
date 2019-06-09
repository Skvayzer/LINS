package com.example.mp11.videoplayer.mydynvideoplayer;

import com.example.mp11.activities.DynVideoPlayer;

import java.io.Serializable;


public interface HpLib_Listener extends Serializable {
    void player_created(DynVideoPlayer videoPlayer);
    //void click_next(Hplib_Tracker hplib_tracker);
    //void click_previous(Hplib_Tracker hplib_tracker);
    void click_cast();
}
