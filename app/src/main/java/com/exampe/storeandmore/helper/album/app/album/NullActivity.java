package com.exampe.storeandmore.helper.album.app.album;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.helper.album.Action;
import com.exampe.storeandmore.helper.album.Album;
import com.exampe.storeandmore.helper.album.api.widget.Widget;
import com.exampe.storeandmore.helper.album.app.Contract;
import com.exampe.storeandmore.helper.album.app.album.data.NullView;
import com.exampe.storeandmore.helper.album.mvp.BaseActivity;

public class NullActivity extends BaseActivity implements Contract.NullPresenter {

    private static final String KEY_OUTPUT_IMAGE_PATH = "KEY_OUTPUT_IMAGE_PATH";

    public static String parsePath(Intent intent) {
        return intent.getStringExtra(KEY_OUTPUT_IMAGE_PATH);
    }

    private Widget mWidget;
    private int mQuality = 1;
    private long mLimitDuration;
    private long mLimitBytes;

    private Contract.NullView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity_null);

        mView = new NullView(this, this);

        Bundle argument = getIntent().getExtras();
        assert argument != null;
        int function = argument.getInt(Album.KEY_INPUT_FUNCTION);
        boolean hasCamera = argument.getBoolean(Album.KEY_INPUT_ALLOW_CAMERA);

        mQuality = argument.getInt(Album.KEY_INPUT_CAMERA_QUALITY);
        mLimitDuration = argument.getLong(Album.KEY_INPUT_CAMERA_DURATION);
        mLimitBytes = argument.getLong(Album.KEY_INPUT_CAMERA_BYTES);

        mWidget = argument.getParcelable(Album.KEY_INPUT_WIDGET);
        mView.setupViews(mWidget);
        mView.setTitle(mWidget.getTitle());

        switch (function) {
            case Album.FUNCTION_CHOICE_IMAGE: {
                mView.setMessage(R.string.album_not_found_image);
                mView.setMakeVideoDisplay(false);
                break;
            }
            /*case Album.FUNCTION_CHOICE_VIDEO: {
                mView.setMessage(R.string.album_not_found_video);
                mView.setMakeImageDisplay(false);
                break;
            }*/
            case Album.FUNCTION_CHOICE_ALBUM: {
                mView.setMessage(R.string.album_not_found_album);
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }

        if (!hasCamera) {
            mView.setMakeImageDisplay(false);
            mView.setMakeVideoDisplay(false);
        }
    }

    @Override
    public void takePicture() {
        Album.camera(this)
                .image()
                .onResult(mCameraAction)
                .start();
    }

    @Override
    public void takeVideo() {

    }

    private final Action<String> mCameraAction = new Action<String>() {
        @Override
        public void onAction(@NonNull String result) {
            Intent intent = new Intent();
            intent.putExtra(KEY_OUTPUT_IMAGE_PATH, result);
            setResult(RESULT_OK, intent);
            finish();
        }
    };
}
