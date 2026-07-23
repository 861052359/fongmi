package com.fongmi.android.tv.player.mpv;

/**
 * MPV helpers.
 *
 * <p>Route A: {@code androidx.media3.mpvplayer} is not shipped with the public Media3 AAR set
 * used by this fork. MPV is therefore always unavailable at compile/runtime until a real
 * mpvplayer library is added.
 */
public final class MpvUtil {

    private MpvUtil() {
    }

    public static boolean isAvailable() {
        return false;
    }
}
