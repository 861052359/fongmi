package com.fongmi.android.tv.player.exo;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

/**
 * Disk pre-cache helper.
 *
 * <p>Route A: FongMi's private {@code DiskPreloadManager} is not present in the public Media3
 * AAR set (only {@code DefaultPreloadManager} / {@code PreCacheHelper} exist). Preload is a
 * no-op so the app can compile and run; HTTP playback still works without ahead-of-time cache.
 */
public class PreCache {

    public void start(ExoPlayer player, MediaItem mediaItem) {
        // no-op without DiskPreloadManager
    }

    public void stop() {
        // no-op
    }

    public void release() {
        stop();
    }
}
