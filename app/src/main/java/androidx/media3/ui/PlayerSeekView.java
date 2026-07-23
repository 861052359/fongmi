package androidx.media3.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;

/**
 * Lightweight stand-in for FongMi's private Media3 {@code PlayerSeekView}.
 *
 * <p>The public Media3 AAR set used by this fork does not ship this widget. This
 * implementation provides the methods the app calls: {@link #setPlayer(Player)},
 * {@link #getTimeBar()}, and a progress child with {@link R.id#exo_progress}.
 */
@UnstableApi
public class PlayerSeekView extends LinearLayout {

    private static final long MAX_UPDATE_INTERVAL_MS = 1000;

    private final TextView positionView;
    private final TextView durationView;
    private final DefaultTimeBar timeBar;
    private final StringBuilder formatBuilder;
    private final java.util.Formatter formatter;
    private final Runnable updateProgressAction;

    @Nullable private Player player;
    private boolean scrubbing;

    private final Player.Listener componentListener =
            new Player.Listener() {
                @Override
                public void onEvents(Player player, Player.Events events) {
                    if (events.containsAny(
                            Player.EVENT_PLAYBACK_STATE_CHANGED,
                            Player.EVENT_PLAY_WHEN_READY_CHANGED,
                            Player.EVENT_IS_PLAYING_CHANGED,
                            Player.EVENT_POSITION_DISCONTINUITY,
                            Player.EVENT_TIMELINE_CHANGED,
                            Player.EVENT_AVAILABLE_COMMANDS_CHANGED)) {
                        updateAll();
                    }
                }
            };

    private final TimeBar.OnScrubListener scrubListener =
            new TimeBar.OnScrubListener() {
                @Override
                public void onScrubStart(TimeBar timeBar, long position) {
                    scrubbing = true;
                    positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
                }

                @Override
                public void onScrubMove(TimeBar timeBar, long position) {
                    positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
                }

                @Override
                public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                    scrubbing = false;
                    if (!canceled && player != null && player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
                        player.seekTo(position);
                    }
                    updateAll();
                }
            };

    public PlayerSeekView(Context context) {
        this(context, null);
    }

    public PlayerSeekView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerSeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        formatBuilder = new StringBuilder();
        formatter = new java.util.Formatter(formatBuilder, java.util.Locale.getDefault());
        updateProgressAction = this::updateProgress;

        int pad = dp(4);
        positionView = createTimeText(context);
        durationView = createTimeText(context);

        timeBar = new DefaultTimeBar(context, attrs);
        timeBar.setId(R.id.exo_progress);
        timeBar.addListener(scrubListener);
        LayoutParams barParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        barParams.setMarginStart(dp(8));
        barParams.setMarginEnd(dp(8));

        addView(positionView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(timeBar, barParams);
        addView(durationView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        setPadding(getPaddingLeft(), Math.max(getPaddingTop(), pad), getPaddingRight(), Math.max(getPaddingBottom(), pad));
    }

    private TextView createTimeText(Context context) {
        TextView view = new TextView(context);
        view.setTextColor(Color.WHITE);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        view.setText(Util.getStringForTime(formatBuilder, formatter, 0));
        view.setIncludeFontPadding(false);
        return view;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }

    /** Progress bar used by playback UI for scrub listeners and key increments. */
    public TimeBar getTimeBar() {
        return timeBar;
    }

    public void setPlayer(@Nullable Player player) {
        if (this.player == player) return;
        if (this.player != null) {
            this.player.removeListener(componentListener);
        }
        this.player = player;
        if (player != null) {
            if (Looper.myLooper() != player.getApplicationLooper()) {
                throw new IllegalStateException("Must set player on application looper");
            }
            player.addListener(componentListener);
        }
        updateAll();
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateAll();
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(updateProgressAction);
        super.onDetachedFromWindow();
    }

    private void updateAll() {
        updateProgress();
    }

    private void updateProgress() {
        removeCallbacks(updateProgressAction);
        if (!isAttachedToWindow()) return;

        long position = 0;
        long buffered = 0;
        long duration = 0;
        boolean isPlaying = false;

        if (player != null) {
            position = player.getContentPosition();
            buffered = player.getContentBufferedPosition();
            duration = player.getContentDuration();
            isPlaying = player.isPlaying();
        }

        if (duration == C.TIME_UNSET) duration = 0;
        if (position == C.TIME_UNSET) position = 0;
        if (buffered == C.TIME_UNSET) buffered = 0;

        durationView.setText(Util.getStringForTime(formatBuilder, formatter, duration));
        if (!scrubbing) {
            positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
            timeBar.setPosition(position);
            timeBar.setBufferedPosition(buffered);
            timeBar.setDuration(duration);
        }

        long delayMs = MAX_UPDATE_INTERVAL_MS;
        if (isPlaying && !scrubbing) {
            long mediaDelay = timeBar.getPreferredUpdateDelay();
            long playbackDelay = 1000 - (position % 1000);
            if (playbackDelay < 200) playbackDelay += 1000;
            delayMs = Math.min(mediaDelay, playbackDelay);
            delayMs = Math.max(16, Math.min(delayMs, MAX_UPDATE_INTERVAL_MS));
        }
        postDelayed(updateProgressAction, delayMs);
    }
}
