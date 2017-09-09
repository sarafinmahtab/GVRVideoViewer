package com.sarafinmahtab.gvrvideoviewer;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private VrVideoView mVrVideoView;
    private SeekBar mSeekBar;

    private boolean mIsPaused;
    private boolean mIsMuted;

    private static final String STATE_PROGRESS = "state_progress";
    private static final String STATE_DURATION = "state_duration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        VideoLoaderTask mBackgroundVideoLoaderTask = new VideoLoaderTask();
        mBackgroundVideoLoaderTask.execute();
    }

    private void initViews() {
        mVrVideoView = (VrVideoView) findViewById(R.id.video_view);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        Button mVolumeButton = (Button) findViewById(R.id.btn_volume);

        mVolumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onVolumeToggleClicked();
            }
        });

        mVrVideoView.setEventListener(new ActivityEventListener());
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void playPause() {
        if(mIsPaused) {
            mVrVideoView.playVideo();
        } else {
            mVrVideoView.pauseVideo();
        }

        mIsPaused = !mIsPaused;
    }

    private void onVolumeToggleClicked() {
        mIsMuted = !mIsMuted;
        mVrVideoView.setVolume(mIsMuted ? 0.0f : 1.0f);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            mVrVideoView.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class ActivityEventListener extends VrVideoEventListener {
        @Override
        public void onLoadSuccess() {
            super.onLoadSuccess();

            mSeekBar.setMax((int) mVrVideoView.getDuration());
            mVrVideoView.pauseVideo();
            mIsPaused = true;
        }

        @Override
        public void onLoadError(String errorMessage) {
            super.onLoadError(errorMessage);
        }

        @Override
        public void onClick() {
            playPause();
        }

        @Override
        public void onNewFrame() {
            super.onNewFrame();

            mSeekBar.setProgress((int) mVrVideoView.getCurrentPosition());
        }

        @Override
        public void onCompletion() {
//          //Restart the video, allowing it to loop
            mVrVideoView.seekTo(0);
            mVrVideoView.pauseVideo();
            mIsPaused = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVrVideoView.pauseRendering();
        mIsPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVrVideoView.resumeRendering();
        mIsPaused = false;
    }

    @Override
    protected void onDestroy() {
        mVrVideoView.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(STATE_PROGRESS, mVrVideoView.getCurrentPosition());
        outState.putLong(STATE_DURATION, mVrVideoView.getDuration());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progress = savedInstanceState.getLong(STATE_PROGRESS);

        mVrVideoView.seekTo(progress);
        mSeekBar.setMax((int) savedInstanceState.getLong(STATE_DURATION));
        mSeekBar.setProgress((int) progress);
    }

    private class VideoLoaderTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            VrVideoView.Options options = new VrVideoView.Options();
            options.inputType = VrVideoView.Options.TYPE_MONO;
            loadVideo(options);
            return true;
        }
    }

    private void loadVideo(VrVideoView.Options options) {
        try {
            mVrVideoView.loadVideoFromAsset("seaturtle.mp4", options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
