package com.stone.onkeyshare.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.stone.onkeyshare.R;
import com.stone.onkeyshare.entity.ShareInfo;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import java.util.ArrayList;

/**
 * Created by Stone on 2017/10/16.
 */
public class QQUtil {

    private static final String TAG = "QQUtil";

    private static final int QQ_SHARE_TITLE_LIMIT = 30; // 分享到QQ标题限制长度
    private static final int QQ_SHARE_SUMMARY_LIMIT = 30; // 分享到QQ摘要限制长度
    private static final int QZONE_SHARE_TITLE_LIMIT = 200; // 分享到QZone标题限制长度
    private static final int QZONE_SHARE_SUMMARY_LIMIT = 600; // 分享到QZone摘要限制长度

    /**
     * 唯一标识，应用的id，在QQ开放平台注册时获得
     */
    private static final String APP_ID = "";

    private static final String S_QQ_FRIENDS="qq_friends";
    private static final String S_QQ_ZONE="qq_zone";

    private static Tencent mTencent;

    /**
     * 获得Tencent实例
     *
     * @return
     */
    public static Tencent getTencentInstance() {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(APP_ID,ShareUtil.getInstance().getApplicationContext());
        }
        return mTencent;
    }

    /**
     * 检查是否可以分享，不能分享则给予提示
     *
     * @return
     */
    public static boolean check() {
        try {
            ShareUtil.getInstance().getApplication().getPackageManager()
                    .getApplicationInfo("com.tencent.mobileqq",
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            ToastUtils.showToast("");
            return false;
        }
    }

    /**
     * 分享到QQ好友
     *
     * @param activity
     * @param shareInfo
     * @param listener
     */
    public static void shareToQQ(Activity activity, ShareInfo shareInfo, IUiListener listener) {
        if (activity == null || shareInfo == null || listener == null) {
            return;
        }

        if (shareInfo.getTitle().length() > QQ_SHARE_TITLE_LIMIT) {
            shareInfo.setTitle(shareInfo.getTitle()
                    .substring(0, (QQ_SHARE_TITLE_LIMIT - 3)) + "...");
        }
        if (shareInfo.getSummary().length() > QQ_SHARE_SUMMARY_LIMIT) {
            shareInfo.setSummary(shareInfo.getSummary()
                    .substring(0, (QQ_SHARE_SUMMARY_LIMIT - 3)) + "...");
        }

        String title = shareInfo.getTitle();
        String summary = shareInfo.getSummary();
        String targetUrl = ShareUtil.getShareUrl(shareInfo.getShareUrl(), S_QQ_FRIENDS);

        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,
                getIconUrl(activity, shareInfo.getImageUrl()));
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, activity.getString(R.string.app_name));

        getTencentInstance().shareToQQ(activity, params, listener);
    }

    /**
     * 纯图片分享到QQ好友
     *
     * @param activity
     * @param shareInfo
     * @param imageLocalUrl
     * @param listener
     */
    public static void shareToQQ(Activity activity, ShareInfo shareInfo,
                                 String imageLocalUrl, IUiListener listener) {
        if (activity == null || shareInfo == null ||
                TextUtils.isEmpty(imageLocalUrl) || listener == null) {
            return;
        }

        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageLocalUrl);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, activity.getString(R.string.app_name));

        getTencentInstance().shareToQQ(activity, params, listener);
    }

    /**
     * 分享到QQ空间
     *
     * @param activity
     * @param shareInfo
     * @param listener
     */
    public static void shareToQZone(Activity activity, ShareInfo shareInfo, IUiListener listener) {
        if (activity == null || shareInfo == null || listener == null) {
            return;
        }

        if (shareInfo.getTitle().length() > QZONE_SHARE_TITLE_LIMIT) {
            shareInfo.setTitle(shareInfo.getTitle()
                    .substring(0, (QZONE_SHARE_TITLE_LIMIT - 3)) + "...");
        }
        if (shareInfo.getSummary().length() > QZONE_SHARE_SUMMARY_LIMIT) {
            shareInfo.setSummary(shareInfo.getSummary()
                    .substring(0, (QZONE_SHARE_SUMMARY_LIMIT - 3)) + "...");
        }

        ArrayList<String> arrayList = new ArrayList();
        arrayList.add(getIconUrl(activity, shareInfo.getImageUrl()));

        String title = shareInfo.getTitle();
        String summary = shareInfo.getSummary();
        String targetUrl= ShareUtil.getShareUrl(shareInfo.getShareUrl(), S_QQ_ZONE);

        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, arrayList);

        getTencentInstance().shareToQzone(activity, params, listener);
    }

    /**
     * 检查分享图片url，若为空则用默认的url
     *
     * @param activity
     * @param url
     */
    private static String getIconUrl(Activity activity, String url) {
        if (TextUtils.isEmpty(url)) {
            return activity.getString(R.string.share_default_iconurl);
        } else {
            return url;
        }
    }

    /**
     * QQ登录
     *
     * @param activity
     * @param mListener qq登录成功的处理回调接口
     *                  <p>
     *                  如果要成功接收到回调，需要在调用接口的fragment的onActivityResult方法中增加如下代码：
     *                  Tencent.onActivityResultData(requestCode,resultCode,data,mListener);
     */
    public static void qqOpenSDKLogin(Activity activity, IUiListener mListener) {
        // 如果已经登陆过，先注销
        hasAlreadyLogin(activity);
        try {
            getTencentInstance().login(activity, "all", mListener);
        } catch (Exception e) {

        }
    }

    /**
     * QQ登录,,带未安装提示
     *
     * @param activity
     * @param mListener qq登录成功的处理回调接口
     *                  <p>
     *                  如果要成功接收到回调，需要在调用接口的fragment的onActivityResult方法中增加如下代码：
     *                  Tencent.onActivityResultData(requestCode,resultCode,data,mListener);
     */
    public static void qqOpenSDKLoginWithToast(Activity activity, IUiListener mListener) {
        if (!checkQQInstall()) {
            return;
        }
        // 如果已经登陆过，先注销
        hasAlreadyLogin(activity);
        try {
            getTencentInstance().login(activity, "all", mListener);
        } catch (Exception e) {

        }
    }


    /**
     * QQ登录
     *
     * @param fragment
     * @param mListener qq登录成功的处理回调接口
     *                  <p>
     *                  如果要成功接收到回调，需要在调用接口的fragment的onActivityResult方法中增加如下代码：
     *                  Tencent.onActivityResultData(requestCode,resultCode,data,mListener);
     */
    public static void qqOpenSDKLogin(Fragment fragment, IUiListener mListener) {
        // 如果已经登陆过，先注销
        hasAlreadyLogin(fragment.getActivity());
        try {
            getTencentInstance().login(fragment, "all", mListener);
        } catch (Exception e) {

        }
    }

    /**
     * QQ登录,带未安装提示
     *
     * @param fragment
     * @param mListener qq登录成功的处理回调接口
     *                  <p>
     *                  如果要成功接收到回调，需要在调用接口的fragment的onActivityResult方法中增加如下代码：
     *                  Tencent.onActivityResultData(requestCode,resultCode,data,mListener);
     */
    public static void qqOpenSDKLoginWithToast(Fragment fragment, IUiListener mListener) {
        if (!checkQQInstall()) {
            return;
        }
        // 如果已经登陆过，先注销
        hasAlreadyLogin(fragment.getActivity());
        try {
            getTencentInstance().login(fragment, "all", mListener);
        } catch (Exception e) {

        }
    }

    /**
     * 检查是否已经使用QQ登录成功
     */
    private static void hasAlreadyLogin(Activity activity) {
        Tencent tencent = getTencentInstance();
        if (tencent.isSessionValid()) {
            tencent.logout(activity);
        }
    }

    private static boolean checkQQInstall() {
        try {
            ShareUtil.getInstance().getApplication().getPackageManager()
                    .getApplicationInfo("com.tencent.mobileqq",
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            ToastUtils.showToast(R.string.check_install_qq);
            return false;
        }
    }
}
