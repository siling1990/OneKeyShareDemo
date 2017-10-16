package com.stone.onkeyshare.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

import com.stone.onkeyshare.BuildConfig;
import com.stone.onkeyshare.activity.ShareActivity;
import com.stone.onkeyshare.entity.ShareInfo;
import com.stone.onkeyshare.listener.SharePlatformListener;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by Stone on 2017/9/22.
 */
public class ShareUtil {
    public static final String SHAREINFO = "shareInfo";
    public static final String SHARE_ACT_ACTION = "action";
    public static final String SHARE_ACT_RESULT = "result";
    public static final String SHARE_ACT_TRANS = "transaction";
    public static final String SHARE_ACT_MSG = "msg";
    public static final String SHARE_BYTES = "bytes";
    public static final String SHARE_ACTIVITY = "com.jd.union.share.SHARE_ACTIVITY";

    public static final String SEPARATOR_SIGN = "##";

    public static final int ACTION_PANEL = 1;
    public static final int ACTION_OPEN = 2;
    public static final int ACTION_BACK = 3;

    public static final int RESULT_SUCCESS = 11;
    public static final int RESULT_ERROR = 12;
    public static final int RESULT_CANCEL = 13;
    public static final int RESULT_BLOCK = 14;

    public static final String Wechat = "Wechat";
    public static final String WechatMoments = "WechatMoments";
    public static final String QQ = "QQ";
    public static final String Copy = "Email";
    public static final String ShareImg = "ShareImage";

    private static long mLastUsedTime;


  	private static volatile ShareUtil mInstance;;
    private Application mApplication;
    public Context context;

    public ShareUtil(){}

    public static ShareUtil getInstance(Context context){
        return new ShareUtil();
    }
 public static synchronized ShareUtil getInstance() {
        if(mInstance == null) {
            mInstance = new ShareUtil();
        }
        return mInstance;
    }
	 public synchronized void setApplication(Application var1) {
        if(this.mApplication == null) {
            this.mApplication = var1;
        }

    }

    public Application getApplication() {
        if(this.mApplication == null) {
            throw new NullPointerException("mApplication is null, should call setApplication() when application init");
        } else {
            return this.mApplication;
        }
    }

    public Context getApplicationContext() {
        if(this.mApplication == null) {
            throw new NullPointerException("mApplication is null, should call setApplication() when application init");
        } else {
            return this.mApplication;
        }
    }

    public static void init() {
        if(BuildConfig.DEBUG){
            Log.D=true;
        }
        WeChatUtil.getWXApi();
        QQUtil.getTencentInstance();
    }

    public static void share(Context context, ShareInfo shareInfo, SharePlatformListener sharePlatformListener) {
        startShareActivity(context, shareInfo, ACTION_OPEN,sharePlatformListener);
    }

    public static void sharePanel(Context context, ShareInfo shareInfo,SharePlatformListener sharePlatformListener) {
        startShareActivity(context, shareInfo, ACTION_PANEL,sharePlatformListener);
    }

    public static void share(Context context, ShareInfo shareInfo ) {
        startShareActivity(context, shareInfo, ACTION_OPEN,null);
    }

    public static void sharePanel(Context context, ShareInfo shareInfo ) {
        startShareActivity(context, shareInfo, ACTION_PANEL,null);
    }

    public static void startShareActivity(Context context, ShareInfo shareInfo, int action, SharePlatformListener sharePlatformListener) {
        if (!isColdDown()) {
            ShareActivity.sharePlatformListener=sharePlatformListener;
            init();
            Intent intent = new Intent();
            intent.setAction(SHARE_ACTIVITY);
            intent.putExtra(SHAREINFO, shareInfo);
            intent.putExtra(SHARE_ACT_ACTION, action);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);

        }
    }

    public static void backShareActivity(Context context, int result, String transaction, String msg) {
        Intent intent = new Intent(SHARE_ACTIVITY);
        intent.putExtra(SHARE_ACT_ACTION, ACTION_BACK);
        intent.putExtra(SHARE_ACT_RESULT, result);
        intent.putExtra(SHARE_ACT_TRANS, transaction);
        intent.putExtra(SHARE_ACT_MSG, msg);
        context.startActivity(intent);
    }

    public static String[] splitTransaction(String transaction) {
        if (TextUtils.isEmpty(transaction)) {
            return new String[]{"", ""};
        } else {
            String[] strs = transaction.split("##");
            return strs.length > 1 ? strs : new String[]{transaction, ""};
        }
    }

    public static String urlEncode(String string) {
        if (string == null) {
            return "";
        } else {
            try {
                string = URLEncoder.encode(string, "utf8");
            } catch (Exception var2) {
                if (Log.E) {
                    var2.printStackTrace();
                }
            }

            return string;
        }
    }

    public static String urlDecode(String string) {
        if (string == null) {
            return "";
        } else {
            try {
                string = URLDecoder.decode(string, "utf8");
            } catch (Exception var2) {
                if (Log.E) {
                    var2.printStackTrace();
                }
            }

            return string;
        }
    }

    public static String getShareUrl(String url, String resource) {
        url = addShareUrlParam(url, "utm_source", "androidapp");
        url = addShareUrlParam(url, "utm_medium", "appshare");
        url = addShareUrlParam(url, "utm_term", resource);
        return url;
    }

    public static String getShareUrlOnlyRes(String url, String resource) {
        url = addShareUrlParam(url, "utm_source", "androidapp");
        url = addShareUrlParam(url, "utm_medium", "appshare");
        url = addShareUrlParam(url, "utm_term", resource);
        return url;
    }

    public static String addShareUrlParam(String url, String key, String val) {
        if (!url.contains(key)) {
            url = url + (url.contains("?") ? "&" : "?");
            url = url + key + "=" + val;
        }

        return url;
    }

    public static boolean isColdDown() {
        if (mLastUsedTime > 0L && mLastUsedTime + 800L > System.currentTimeMillis()) {
            return true;
        } else {
            mLastUsedTime = System.currentTimeMillis();
            return false;
        }
    }

    public static int getBitmapRes(Context context, String name) {
        //得到application对象
        ApplicationInfo appInfo = context.getApplicationInfo();
        int resID = context.getResources().getIdentifier(name, "drawable", appInfo.packageName);
        return resID;
    }
    public static int getStringRes(Context context, String name) {
        //得到application对象
        ApplicationInfo appInfo = context.getApplicationInfo();
        int resID = context.getResources().getIdentifier(name, "string", appInfo.packageName);
        return resID;
    }

}
