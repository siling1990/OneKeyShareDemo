package com.stone.onkeyshare.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.stone.onkeyshare.utils.Log;
import com.stone.onkeyshare.utils.ShareUtil;
import com.stone.onkeyshare.utils.ToastUtils;
import com.stone.onkeyshare.utils.WeChatUtil;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

/**
 * Created by Stone on 2017/9/22.
 */
public class WXhandlerActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXEntryActivity";

    private IWXAPI wxApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        wxApi = WeChatUtil.getWXApi();
        Intent ait = getIntent();
        if (null == ait) {
            finish();
            return;
        }
        wxApi.handleIntent(getIntent(), this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        wxApi.handleIntent(intent, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onReq(BaseReq req) {
        if (Log.D) {
            Log.d(TAG, " onReq -->> ");
        }
       // WeiXinManager.getInstance().receivedReq(req);
    }

    @Override
    public void onResp(BaseResp resp) {
        if (Log.D) {
            Log.d(TAG, " onResp -->> resp.errCode :  " + resp.errCode);
        }

        switch (resp.getType()) {
            case ConstantsAPI.COMMAND_SENDAUTH:
                handleWXLoginResp(resp);
                break;

            case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX:
                //发出分享时设定的此值，作为判定是分享回调的依据
                if (TextUtils.isEmpty(resp.transaction)) break;

                int result = -1;

                switch (resp.errCode) {
                    case BaseResp.ErrCode.ERR_OK: // 分享成功
                        result = ShareUtil.RESULT_SUCCESS;
                        break;

                    case BaseResp.ErrCode.ERR_USER_CANCEL: // 分享取消
                        result = ShareUtil.RESULT_CANCEL;
                        break;

                    case BaseResp.ErrCode.ERR_AUTH_DENIED: // 分享失败
                        result = ShareUtil.RESULT_ERROR;
                        break;

                    default:
                        ToastUtils.showToast(resp.errCode + ":" + resp.errStr);
                }
                ShareUtil.backShareActivity(this, result, resp.transaction, resp.errStr);
                break;

            default:
                break;
        }

        finish();
    }

    private void handleWXLoginResp(BaseResp resp) {
        SendAuth.Resp sResp = (SendAuth.Resp) resp;

        if(Log.D) {
            Log.d("JD_Smith", "Type: " + sResp.getType());
            Log.d("JD_Smith", "Code: " + sResp.code);
            Log.d("JD_Smith", "State: " + sResp.state);
            Log.d("JD_Smith", "ErrCode: " + sResp.errCode);
        }

        Bundle pBundle = new Bundle();
        pBundle.putInt("type", sResp.getType());
        pBundle.putString("code", sResp.code);
        pBundle.putString("state", sResp.state);
        pBundle.putInt("errCode", sResp.errCode);

        Intent pIntent = new Intent(Configuration.BROADCAST_FROM_WXLOGIN);
        pIntent.putExtras(pBundle);
        sendBroadcast(pIntent, Configuration.SLEF_BROADCAST_PERMISSION);

        if(Log.D) {
            Log.e("JD_Smith", "Broadcast has been send.");
        }
    }


}
