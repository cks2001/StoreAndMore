/*
 * Copyright 2017 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exampe.storeandmore.helper.album.app.album.data;

import android.media.MediaPlayer;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.io.File;

import com.exampe.storeandmore.helper.album.AlbumFile;
import com.exampe.storeandmore.helper.album.Filter;
import com.exampe.storeandmore.helper.album.util.AlbumUtils;


public class PathConversion {

    private final Filter<Long> mSizeFilter;
    private final Filter<String> mMimeFilter;
    private final Filter<Long> mDurationFilter;

    public PathConversion(Filter<Long> sizeFilter, Filter<String> mimeFilter, Filter<Long> durationFilter) {
        this.mSizeFilter = sizeFilter;
        this.mMimeFilter = mimeFilter;
        this.mDurationFilter = durationFilter;
    }

    @WorkerThread
    @NonNull
    public AlbumFile convert(String filePath) {
        File file = new File(filePath);

        AlbumFile albumFile = new AlbumFile();
        albumFile.setPath(filePath);

        File parentFile = file.getParentFile();
        albumFile.setBucketName(parentFile.getName());

        String mimeType = AlbumUtils.getMimeType(filePath);
        albumFile.setMimeType(mimeType);
        long nowTime = System.currentTimeMillis();
        albumFile.setAddDate(nowTime);
        albumFile.setSize(file.length());
        int mediaType = 0;
        if (!TextUtils.isEmpty(mimeType)) {
            if (mimeType.contains("video"))
                mediaType = AlbumFile.TYPE_VIDEO;
            if (mimeType.contains("image"))
                mediaType = AlbumFile.TYPE_IMAGE;
        }
        albumFile.setMediaType(mediaType);

        if (mSizeFilter != null && mSizeFilter.filter(file.length())) {
            albumFile.setDisable(true);
        }
        if (mMimeFilter != null && mMimeFilter.filter(mimeType)) {
            albumFile.setDisable(true);
        }

        if (mediaType == AlbumFile.TYPE_VIDEO) {
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(filePath);
                player.prepare();
                albumFile.setDuration(player.getDuration());
            } catch (Exception ignored) {
            } finally {
                player.release();
            }

            if (mDurationFilter != null && mDurationFilter.filter(albumFile.getDuration())) {
                albumFile.setDisable(true);
            }
        }
        return albumFile;
    }

}