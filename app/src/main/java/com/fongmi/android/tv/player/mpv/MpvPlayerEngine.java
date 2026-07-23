package com.fongmi.android.tv.player.mpv;

import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;

import com.fongmi.android.tv.bean.Sub;
import com.fongmi.android.tv.player.engine.PlayerEngine;
import com.fongmi.android.tv.player.media.PlaySpec;

/**
 * MPV engine stub for builds without {@code androidx.media3.mpvplayer}.
 *
 * <p>{@link #isAvailable()} is always false, so {@link com.fongmi.android.tv.player.engine.PlayerEngineFactory}
 * only constructs Exo. Methods below throw if invoked accidentally.
 */
public class MpvPlayerEngine implements PlayerEngine {

    private static final String MSG = "MPV engine is not bundled (missing androidx.media3.mpvplayer)";

    public MpvPlayerEngine(int decode, Player.Listener listener) {
        throw new UnsupportedOperationException(MSG);
    }

    public static boolean isAvailable() {
        return MpvUtil.isAvailable();
    }

    @Override
    public Type getType() {
        return Type.MPV;
    }

    @Override
    public Player getPlayer() {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void release() {
    }

    @Override
    public Player rebuild() {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public boolean setDecode(int decode) {
        return false;
    }

    @Override
    public void start(PlaySpec spec, long startPositionMs) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public boolean isVod() {
        return false;
    }

    @Override
    public boolean addSubtitle(Sub sub) {
        return false;
    }

    @Override
    public String getErrorMessage(PlaybackException e) {
        return MSG;
    }

    @Override
    public ErrorAction handleError(PlaybackException e) {
        return ErrorAction.FATAL;
    }
}
