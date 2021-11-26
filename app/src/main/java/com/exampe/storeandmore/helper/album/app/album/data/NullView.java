
package com.exampe.storeandmore.helper.album.app.album.data;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.helper.album.api.widget.Widget;
import com.exampe.storeandmore.helper.album.app.Contract;
import com.exampe.storeandmore.helper.album.util.AlbumUtils;
import com.exampe.storeandmore.helper.album.util.SystemBar;

public class NullView extends Contract.NullView implements View.OnClickListener {

    private final Activity mActivity;

    private final Toolbar mToolbar;
    private final TextView mTvMessage;
    private final AppCompatButton mBtnTakeImage;

    public NullView(Activity activity, Contract.NullPresenter presenter) {
        super(activity, presenter);
        this.mActivity = activity;
        this.mToolbar = activity.findViewById(R.id.toolbar);
        this.mTvMessage = activity.findViewById(R.id.tv_message);
        this.mBtnTakeImage = activity.findViewById(R.id.btn_camera_image);

        this.mBtnTakeImage.setOnClickListener(this);

    }

    @Override
    public void setupViews(Widget widget) {
        mToolbar.setBackgroundColor(widget.getToolBarColor());

        int statusBarColor = widget.getStatusBarColor();
        Drawable navigationIcon = getDrawable(R.drawable.album_ic_back_white);
        if (widget.getUiStyle() == Widget.STYLE_LIGHT) {
            if (SystemBar.setStatusBarDarkFont(mActivity, true)) {
                SystemBar.setStatusBarColor(mActivity, statusBarColor);
            } else {
                SystemBar.setStatusBarColor(mActivity, getColor(R.color.albumColorPrimaryBlack));
            }

            AlbumUtils.setDrawableTint(navigationIcon, getColor(R.color.albumIconDark));
            setHomeAsUpIndicator(navigationIcon);
        } else {
            SystemBar.setStatusBarColor(mActivity, statusBarColor);
            setHomeAsUpIndicator(navigationIcon);
        }
        SystemBar.setNavigationBarColor(mActivity, widget.getNavigationBarColor());

        Widget.ButtonStyle buttonStyle = widget.getButtonStyle();
        ColorStateList buttonSelector = buttonStyle.getButtonSelector();
        mBtnTakeImage.setSupportBackgroundTintList(buttonSelector);

        if (buttonStyle.getUiStyle() == Widget.STYLE_LIGHT) {
            Drawable drawable = mBtnTakeImage.getCompoundDrawables()[0];
            AlbumUtils.setDrawableTint(drawable, getColor(R.color.albumIconDark));
            mBtnTakeImage.setCompoundDrawables(drawable, null, null, null);

            AlbumUtils.setDrawableTint(drawable, getColor(R.color.albumIconDark));

            mBtnTakeImage.setTextColor(getColor(R.color.albumFontDark));

        }
    }

    @Override
    public void setMessage(int message) {
        mTvMessage.setText(message);
    }

    @Override
    public void setMakeImageDisplay(boolean display) {
        mBtnTakeImage.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setMakeVideoDisplay(boolean display) {

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_camera_image) {
            getPresenter().takePicture();
        }
    }
}