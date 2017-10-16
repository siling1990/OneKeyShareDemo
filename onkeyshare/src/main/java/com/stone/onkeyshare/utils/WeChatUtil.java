package com.stone.onkeyshare.utils;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by Stone on 2017/10/16.
 */
public class WeChatUtil {

    private static final String APP_ID="";
    private static IWXAPI wxApi;

    public static IWXAPI getWXApi(){
        wxApi= WXAPIFactory.createWXAPI(ShareUtil.getInstance().getApplicationContext(),APP_ID,true);
        wxApi.registerApp(APP_ID);
        return wxApi;
    }
}
