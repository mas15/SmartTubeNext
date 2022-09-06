package com.liskovsoft.smartyoutubetv2.common.app.models.playback.controller;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.FormatItem;

import java.io.InputStream;
import java.util.List;

public interface PlaybackEngineController {
    int BACKGROUND_MODE_DEFAULT = 0;
    int BACKGROUND_MODE_SOUND = 1;
    int BACKGROUND_MODE_PIP = 2;
    int BACKGROUND_MODE_PLAY_BEHIND = 3;
    int BUFFER_LOW = 0;
    int BUFFER_MEDIUM = 1;
    int BUFFER_HIGH = 2;
    int ZOOM_MODE_DEFAULT = AspectRatioFrameLayout.RESIZE_MODE_FIT;
    int ZOOM_MODE_FIT_WIDTH = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;
    int ZOOM_MODE_FIT_HEIGHT = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
    int ZOOM_MODE_FIT_BOTH = AspectRatioFrameLayout.RESIZE_MODE_ZOOM;
    int ZOOM_MODE_STRETCH = AspectRatioFrameLayout.RESIZE_MODE_FILL;
    float ASPECT_RATIO_DEFAULT = 0;
    float ASPECT_RATIO_1_1 = 1f;
    float ASPECT_RATIO_4_3 = 1.33f;
    float ASPECT_RATIO_5_4 = 1.25f;
    float ASPECT_RATIO_16_9 = 1.77f;
    float ASPECT_RATIO_16_10 = 1.6f;
    float ASPECT_RATIO_21_9 = 2.33f;
    float ASPECT_RATIO_64_27 = 2.37f;
    float ASPECT_RATIO_221_1 = 2.21f;
    float ASPECT_RATIO_235_1 = 2.35f;
    float ASPECT_RATIO_239_1 = 2.39f;
    void openDash(InputStream dashManifest);
    void openDashUrl(String dashManifestUrl);
    void openHlsUrl(String hlsPlaylistUrl);
    void openUrlList(List<String> urlList);
    long getPositionMs();
    void setPositionMs(long positionMs);
    long getLengthMs();
    void setPlayWhenReady(boolean play);
    boolean getPlayWhenReady();
    boolean isPlaying();
    boolean isLoading();
    List<FormatItem> getVideoFormats();
    List<FormatItem> getAudioFormats();
    List<FormatItem> getSubtitleFormats();
    void setFormat(FormatItem option);
    FormatItem getVideoFormat();
    boolean isEngineInitialized();
    void restartEngine();
    void reloadPlayback();
    void setBackgroundMode(int type);
    int getBackgroundMode();
    boolean isInPIPMode();
    boolean containsMedia();
    void setSpeed(float speed);
    float getSpeed();
    void setVolume(float volume);
    float getVolume();
    void setVideoZoomMode(int mode);
    int getVideoZoomMode();
    void setVideoAspectRatio(float mode);
}
