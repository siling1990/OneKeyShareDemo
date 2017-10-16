package com.stone.onkeyshare.utils;

import android.widget.Toast;

/**
 * Created by Stone on 2017/10/16.
 */
public class ToastUtils {
    public static void showToast(String toast){
        Toast.makeText(ShareUtil.getInstance().getApplicationContext(),toast,Toast.LENGTH_SHORT).show();
    }
    public static void showToast(int resourceId){
        Toast.makeText(ShareUtil.getInstance().getApplicationContext(),ShareUtil.getInstance().getApplication().getString(resourceId),Toast.LENGTH_SHORT).show();
    }
}
