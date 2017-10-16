package com.stone.onkeyshare.listener;

/**
 * Created by Stone on 2017/9/27.
 */
public interface SharePlatformListener {
    void onComplete(String platformName);
    void onError(String platformName, String errMsg);
    void onCancel(String platformName);

}
