package com.richify.goobucks.util;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by thomaslin on 10/03/2018.
 *
 */

public class BackgroundBitmapCache {

    private LruCache<Integer, Bitmap> mBackgroundsCache;
    private static BackgroundBitmapCache instance;

    public static  BackgroundBitmapCache getInstance() {
        if (instance == null) {
            instance = new BackgroundBitmapCache();
            instance.init();
        }
        return instance;
    }

    private void init() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 5;

        mBackgroundsCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToBackgroundMemoryCache(Integer key, Bitmap bitmap) {
        if (getBitmapFromBackgroundMemoryCache(key) == null) {
            mBackgroundsCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromBackgroundMemoryCache(Integer key) {
        return mBackgroundsCache.get(key);
    }
}

