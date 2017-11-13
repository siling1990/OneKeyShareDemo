package com.stone.onkeyshare.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.stone.onkeyshare.R;
import com.stone.onkeyshare.entity.ShareInfo;
import com.stone.onkeyshare.listener.SharePlatformListener;
import com.stone.onkeyshare.utils.Log;
import com.stone.onkeyshare.utils.ShareUtil;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.stone.onkeyshare.utils.ShareUtil.RESULT_CANCEL;
import static com.stone.onkeyshare.utils.ShareUtil.RESULT_ERROR;
import static com.stone.onkeyshare.utils.ShareUtil.RESULT_SUCCESS;

/**
 * Created by Stone on 2017/9/22.
 */
public class ShareActivity extends Activity {

    private final static String TAG = "ShareActivity";
    private final static String HOST = "https://lmapp.jd.com";
    private final static int SHARE_BIG_IMAGE_SIZE_MAX = 5 * 1024 * 1024;
    private final static int THUMB_DATA_MAX = 32 * 1024;
    private final static int THUMB_IMAGE_SIZE = 120;
    private ShareInfo mShareInfo;
    private int mAction;
    private int mSharedResult;
    private String mSharedMsg;
    private String mSharedUrl;
    private String mSharedChannel;
    private String mTrans;
    private byte[] mThumbData;
    private String mSelectedChannel;
    private GridView gridView;
    private SimpleAdapter saImageItems;

    private BaseUiListener mBaseUiListener;
    private Runnable mShareRunnable;
    public static SharePlatformListener sharePlatformListener;

    private Activity thisActivity;
    private Bitmap mBitmapSubLogo;
    private Bitmap mBitmapProduct;

    private Bitmap mShareBigBitmap;
    private String mBitmapSavedPath;

    private RelativeLayout mRootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        initData(intent);
        thisActivity = this;
        if (mAction == ShareUtil.ACTION_BACK) {
            //TODO
            mTrans = intent.getStringExtra(ShareUtil.SHARE_ACT_TRANS);
            mSharedResult = intent.getIntExtra(ShareUtil.SHARE_ACT_RESULT, 0);
            setSharedResult(mSharedResult, mTrans, mSharedMsg);

            finish();
            return;
        }

        if (mShareInfo == null && mShareInfo.getShareImageInfo() == null) {
            finish();
            return;
        }

        setContentView(R.layout.share_activity);

        mRootView = (RelativeLayout) findViewById(R.id.share_activity);
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mSelectedChannel)) {
                    finish();
                }
            }
        });


        mSelectedChannel = mShareInfo.getChannels();

        if (TextUtils.isEmpty(mSelectedChannel) && mAction != ShareUtil.ACTION_PANEL) {
            return;
        }
        if (mSelectedChannel.startsWith(ShareUtil.Wechat) && ((TextUtils.isEmpty(mShareInfo.getIconUrl()) && mShareInfo.getShareImageInfo() == null)||"jdunion_tran".equals(mShareInfo.getEventName())) && !TextUtils.isEmpty(mShareInfo.getSummary())) {//只分享文字只支持微信
            doShareMessage();
            return;
        }
        setShareInfo();

        if (mAction == ShareUtil.ACTION_OPEN) {
            if (mShareInfo.getShareImageInfo() != null && !TextUtils.isEmpty(mShareInfo.getShareImageInfo().directPath)) {//图片存在分享图片
                mBitmapSavedPath = mShareInfo.getShareImageInfo().directPath;
                doShareImage();
            } else {
                doShare();
            }
        } else if (mAction == ShareUtil.ACTION_PANEL) {
            showPanel();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (QQUtil.getTencentInstance() != null) {
            QQUtil.getTencentInstance().onActivityResultData(
                    requestCode, resultCode, intent, mBaseUiListener);
        }
    }

    private void initData(Intent intent) {
        mAction = intent.getIntExtra(ShareUtil.SHARE_ACT_ACTION, 0);
        mBaseUiListener = new BaseUiListener();

        if (intent.hasExtra(ShareUtil.SHARE_ACT_MSG)) {
            mSharedMsg = intent.getStringExtra(ShareUtil.SHARE_ACT_MSG);
        }
        if (intent.hasExtra(ShareUtil.SHAREINFO)) {
            mShareInfo = intent.getParcelableExtra(ShareUtil.SHAREINFO);
        }
        if (intent.hasExtra(ShareUtil.SHARE_BYTES)) {
            mThumbData = intent.getByteArrayExtra(ShareUtil.SHARE_BYTES);
        }
    }

    public void setSharedResult(int result, String transaction, String msg) {
        mSharedResult = result;
        mSharedMsg = msg;
        splitTransaction(transaction);
        String message="";
        switch(result){
            case RESULT_CANCEL:
                if(sharePlatformListener!=null){
                    sharePlatformListener.onCancel(mSharedChannel);
                }else{
                    message="分享取消";
                }
                break;
            case RESULT_ERROR:
                if(sharePlatformListener!=null){
                    sharePlatformListener.onError(mSharedChannel,mSharedMsg);
                }else{
                    message="分享失败";
                }
                break;
            case RESULT_SUCCESS:
                if(sharePlatformListener!=null){
                    sharePlatformListener.onComplete(mSharedChannel);
                }else{
                    message="分享成功";
                }
                break;
            default:message="分享错误";
        }
        if(!TextUtils.isEmpty(message)){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        finish();

    }

    private void splitTransaction(String transaction) {
        String[] arr = ShareUtil.splitTransaction(transaction);
        mSharedUrl = ShareUtil.urlDecode(arr[0]);
        mSharedChannel = arr[1];
    }

    private void setShareInfo() {
        // 若为空设置默认值
        if (TextUtils.isEmpty(mShareInfo.getTitle())) {
            mShareInfo.setTitle(getString(R.string.app_name));
        }
        if (TextUtils.isEmpty(mShareInfo.getIconUrl())) {
            mShareInfo.setIconUrl("http://img30.360buyimg.com/uba/jfs/t10315/48/1236285364/7170/9648068a/59ddeecbNdcf5bfce.png");
        }
        if (TextUtils.isEmpty(mShareInfo.getSummary())) {
            mShareInfo.setSummary(getString(R.string.share_defaut_summary));
        }
        if (TextUtils.isEmpty(mShareInfo.getWxcontent())) {
            mShareInfo.setWxcontent(mShareInfo.getSummary());
        }
        if (TextUtils.isEmpty(mShareInfo.getWxMomentsContent())) {
            mShareInfo.setWxMomentsContent(mShareInfo.getSummary());
        }
        if (TextUtils.isEmpty(mShareInfo.getNormalText())) {
            mShareInfo.setNormalText(mShareInfo.getTitle() + " " +
                    mShareInfo.getSummary() + " " +
                    ShareUtil.getShareUrl(mShareInfo.getUrl(), "ShareMore"));
        }

        // 由于 http://3.cn/Ceo4yH 链接失效，需要替换成 http://sq.jd.com/NvQBpa
        // 暂时直接用新链接代替老链接
        String oldUrl = "3.cn/Ceo4yH";
        String newUrl = "sq.jd.com/NvQBpa";
        if(TextUtils.isEmpty(mShareInfo.getUrl())){
            mShareInfo.setUrl(HOST);
        }
        mShareInfo.setNormalText(mShareInfo.getNormalText().replace(oldUrl, newUrl));
        mShareInfo.setTransaction(ShareUtil.urlEncode(mShareInfo.getUrl()));

    }

    private void showPanel() {
        showPanelAnimation(R.layout.share_layout);

        findViewById(R.id.share_layout_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        List<HashMap<String, Object>> shareList = new ArrayList<HashMap<String, Object>>();

        String[] platforms = {ShareUtil.Wechat, ShareUtil.WechatMoments, ShareUtil.QQ, ShareUtil.Copy};
        for (String plat : platforms) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("platform", plat);
            String name = "";
            if (plat != null) {
                name = plat.toLowerCase();
            }
            int resId = ShareUtil.getBitmapRes(this, "ssdk_oks_classic_" + name);
            if (resId > 0) {
                map.put("ItemImage", resId);
            } else {
                map.put("ItemImage", resId);
            }
            resId = ShareUtil.getStringRes(this, "ssdk_" + name);
            if (resId > 0) {
                map.put("ItemText", this.getResources().getString(resId));
            } else {
                map.put("ItemText", "");
            }
            shareList.add(map);
        }
        GridView gridView = (GridView) findViewById(R.id.share_gridView);
        saImageItems = new SimpleAdapter(this, shareList, R.layout.share_item, new String[]{"ItemImage", "ItemText"}, new int[]{R.id.imageView1, R.id.textView1});
        gridView.setAdapter(saImageItems);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, Object> item = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
                String platform = (String) item.get("platform");

                if(!TextUtils.isEmpty(platform)){
                    mSelectedChannel=platform;
                }else{
                    //TO DO
                    dealResult();
                    return ;
                }
                if (mShareInfo.getShareImageInfo() != null && !TextUtils.isEmpty(mShareInfo.getShareImageInfo().directPath)) {//图片存在分享图片
                    mBitmapSavedPath = mShareInfo.getShareImageInfo().directPath;
                    doShareImage();
                } else {
                    doShare();
                }
            }
        });
    }

    private void doShare() {

        if (ShareUtil.Wechat.equals(mSelectedChannel)) {
            if (WeixinUtil.check()) {
                mShareRunnable = new Runnable() {
                    @Override
                    public void run() {
                        WeixinUtil.doWXShare(mShareInfo, true, mThumbData);
                    }
                };
                checkPermission();
            } else {
                shareBlock();
            }
        } else if (ShareUtil.WechatMoments.equals(mSelectedChannel)) {
            if (WeixinUtil.check()) {
                mShareRunnable = new Runnable() {
                    @Override
                    public void run() {
                        WeixinUtil.doWXShare(mShareInfo, false, mThumbData);
                    }
                };
                checkPermission();
            } else {
                shareBlock();
            }
        } else if (ShareUtil.QQ.equals(mSelectedChannel)) {
            if (QQUtil.check()) {
                mBaseUiListener.transaction = mShareInfo.getTransaction()
                        + ShareUtil.SEPARATOR_SIGN + ShareUtil.QQ;
                QQUtil.shareToQQ(thisActivity, mShareInfo, mBaseUiListener);
            } else {
                shareBlock();
            }
        }

    }

    private void doShareImage() {

        if (mShareBigBitmap == null) {
            mShareBigBitmap = getSavedBitmap(mBitmapSavedPath);
        }
        if (ShareUtil.Wechat.equals(mSelectedChannel)) {
            if (WeixinUtil.check()) {
                setBigImgThumbData();
                WeixinUtil.doWXShare(mShareInfo, true, mThumbData, mShareBigBitmap);
            } else {
                shareBlock();
            }
        } else if (ShareUtil.WechatMoments.equals(mSelectedChannel)) {
            if (WeixinUtil.check()) {
                setBigImgThumbData();
                WeixinUtil.doWXShare(mShareInfo, false, mThumbData, mShareBigBitmap);
            } else {
                shareBlock();
            }
        } else if (ShareUtil.QQ.equals(mSelectedChannel)) {
            if (QQUtil.check()) {
                mBaseUiListener.transaction = mShareInfo.getTransaction()
                        + ShareUtil.SEPARATOR_SIGN + ShareUtil.QQ;
                QQUtil.shareToQQ(thisActivity, mShareInfo, mBitmapSavedPath, mBaseUiListener);
            } else {
                shareBlock();
            }
        }

    }

    private void doShareMessage() {

        IWXAPI wxApi = WeixinUtil.getWXApi();

        WXTextObject textObject = new WXTextObject();
        textObject.text = mShareInfo.getSummary();

        WXMediaMessage mediaMessage = new WXMediaMessage();
        mediaMessage.mediaObject = textObject;
        mediaMessage.description = mShareInfo.getSummary();

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = mediaMessage;
        boolean isScene = true;
        if (mSelectedChannel.equals(ShareUtil.WechatMoments)) {
            isScene = false;
        }

        if (isScene) {
            req.transaction = mShareInfo.getTransaction() + "##" + ShareUtil.Wechat;
            req.scene = 0;
        } else {
            req.transaction = mShareInfo.getTransaction() + "##" + ShareUtil.WechatMoments;
            req.scene = 1;
        }

        wxApi.sendReq(req);
    }

    private Bitmap getSavedBitmap(String imagePath) {
        if (TextUtils.isEmpty(imagePath))
            return null;
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(imagePath));
            byte[] data = (new FileService()).readInputStream(fis);
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            if (Log.D) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    private void checkPermission() {
        if (PermissionHelper.hasGrantedExternalStorage(
                thisActivity,
                PermissionHelper.generateBundle("share", "ShareActivity", "checkPermission"),
                new PermissionHelper.PermissionResultCallBack() {
                    @Override
                    public void onGranted() {
                        shareToDo();
                    }

                    @Override
                    public void onDenied() {
                        finish();
                    }

                    @Override
                    public void onCanceled() {
                        finish();
                    }

                    @Override
                    public void onIgnored() {
                        finish();
                    }

                    @Override
                    public void onOpenSetting() {

                    }
                }
        )) {
            shareToDo();
        }
    }

    private void shareToDo() {
        if (mShareRunnable == null) return;

        if (!isThumbDataWrong()) {
            mShareRunnable.run();
        } else if (TextUtils.isEmpty(mShareInfo.getImageUrl())) {
            shareWithDefaultThumbData();
        } else {
            shareWithNetThumbData();
        }
    }

    private void shareWithDefaultThumbData() {
//        int tmp = (mShareInfo.getEventFrom() != null ? mShareInfo.getEventFrom() : "")
//                .equals(ClickConstant.CLICK_SHARE_VALUE_HB)
//                ? R.drawable.share_wx_hb
//                : R.drawable.share_default_icon;
        int tmp = R.drawable.ic_launcher;
        Drawable drawable = ContextCompat.getDrawable(thisActivity, tmp);
        if (drawable == null) return;
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int rate = 100;
        while (isThumbDataWrong() && rate > 0) {
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, rate, baos);
            mThumbData = baos.toByteArray();
            rate = rate - 15;
        }

        mShareRunnable.run();
    }

    private void shareWithNetThumbData() {
        //使用应用自带的网络组建下载
        // mThumbData
    }

    private void shareBlock() {
        mSharedResult = ShareUtil.RESULT_BLOCK;
        mSharedMsg = "check failed";
        dealResult();
        finish();
    }

    private void dealResult() {
        //TODO
        setSharedResult(RESULT_ERROR,mSelectedChannel,"分享失败！");
    }

    private void setBigImgThumbData() {
        Bitmap bitmap;
        if (null == mShareBigBitmap)
            return;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mShareBigBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = mShareBigBitmap.getHeight() / 120;
            bitmap = BitmapFactory.decodeStream(bais, null, options);
        } catch (Exception e) {
            if (Log.D) {
                e.printStackTrace();
            }
            //分享默认图，建议自定义
            bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
        }

        if (bitmap == null) return;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int rate = 100;
        while (isThumbDataWrong() && rate > 0) {
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, rate, baos);
            mThumbData = baos.toByteArray();
            rate = rate - 15;
        }
    }

    private boolean isThumbDataWrong() {
        return mThumbData == null || mThumbData.length == 0 || mThumbData.length > THUMB_DATA_MAX;
    }

    private void showPanelAnimation(int id) {
        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_SELF, 0);
        animation.setFillAfter(true);
        animation.setDuration(200);

        View view = getLayoutInflater().inflate(id, null);

        mRootView.addView(view);

        view.startAnimation(animation);
    }

    private class BaseUiListener implements IUiListener {
        public String transaction; // 分享事务ID，作为回调凭证

        @Override
        public void onComplete(Object obj) {
            setSharedResult(RESULT_SUCCESS, RESULT_SUCCESS+"##"+ShareUtil.QQ, "success");
        }

        @Override
        public void onError(UiError e) {
            setSharedResult(RESULT_ERROR, RESULT_ERROR+"##"+ShareUtil.QQ, e.errorMessage);
        }

        @Override
        public void onCancel() {
            setSharedResult(RESULT_CANCEL, RESULT_CANCEL+"##"+ShareUtil.QQ, "cancel");
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mSharedResult != 0) {
            if (mSharedResult == RESULT_SUCCESS) {
                finish();
            } else {
                dealResult();
                return;
            }
        }

        if (mRootView.getChildCount() == 0 && TextUtils.isEmpty(mSelectedChannel)) {
            finish();
            return;
        }
        if (!TextUtils.isEmpty(mSelectedChannel)) {
            if (mSelectedChannel.equals(ShareUtil.Wechat) || mSelectedChannel.equals(ShareUtil.WechatMoments)) {
                finish();
            }
        }
    }
}
