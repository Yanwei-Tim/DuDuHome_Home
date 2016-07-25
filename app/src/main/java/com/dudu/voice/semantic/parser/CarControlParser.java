package com.dudu.voice.semantic.parser;

import com.dudu.android.launcher.utils.Constants;
import com.dudu.voice.semantic.bean.BrightnessBean;
import com.dudu.voice.semantic.bean.CmdBean;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.bean.VolumeBean;
import com.dudu.voice.semantic.constant.SemanticConstant;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 * Created by 赵圣琪 on 2015/12/28.
 */
public class CarControlParser {

    public static SemanticBean parseCarControlBean(JSONObject semantic, String text) {
        try {
            if (!semantic.isNull(SemanticConstant.REQUEST)) {
                JSONObject request = semantic.getJSONObject(SemanticConstant.REQUEST);
                if (!request.isNull(SemanticConstant.PARAM)) {
                    JSONObject param = request.getJSONObject(SemanticConstant.PARAM);
                    if (!param.isNull(SemanticConstant.VOLUME_CN)) {
                        VolumeBean bean = new VolumeBean();
                        bean.setService(SemanticConstant.SERVICE_VOLUME);
                        bean.setHasResult(true);
                        bean.setText(text);
                        bean.setOperation(param.optString(SemanticConstant.VOLUME_CN));
                        return bean;
                    }

                    CmdBean bean = new CmdBean();
                    bean.setService(SemanticConstant.SERVICE_CMD);
                    bean.setHasResult(true);
                    bean.setText(text);

                    if (text.contains(Constants.TAKE_PICTURE)
                            || param.optString(SemanticConstant.ACTION_CN).equals(Constants.TAKE_PICTURE)) {
                        bean.setTarget(Constants.TAKE_PICTURE);
                        return bean;
                    }

                    if (text.contains(SemanticConstant.RECORD_CN)) {
                        bean.setAction(param.optString(SemanticConstant.ACTION_CN));
                        bean.setTarget(SemanticConstant.RECORD_CN);
                        return bean;
                    }

                    if (text.contains(Constants.SPEECH)) {
                        bean.setTarget(Constants.SPEECH);
                        return bean;
                    }

                    if (text.contains(Constants.NAVIGATION) || text.contains(SemanticConstant.DOMAIN_MAP)
                            || text.contains(Constants.WIFI) || text.contains(Constants.WIFI_CN)) {

                        bean.setTarget(param.optString(SemanticConstant.OBJECT_CN));
                        bean.setAction(param.optString(SemanticConstant.ACTION_CN));
                        return bean;

                    }


                    if (text.contains(Constants.BACK)) {
                        bean.setTarget(Constants.BACK);
                        return bean;
                    }

                    if (text.contains(Constants.EXIT) || param.optString(SemanticConstant.ACTION_CN).equals(Constants.EXIT)) {
                        bean.setTarget(Constants.EXIT);
                        return bean;
                    }

                    if (!param.isNull(SemanticConstant.BRIGHTNESS_CN)) {
                        BrightnessBean brightnessBean = new BrightnessBean();
                        brightnessBean.setHasResult(true);
                        brightnessBean.setText(text);
                        brightnessBean.setAction(param.optString(SemanticConstant.BRIGHTNESS_CN));
                        brightnessBean.setService(SemanticConstant.SERVICE_BRIGHTNESS);
                        return brightnessBean;
                    }


                    bean.setAction(param.optString(SemanticConstant.ACTION));
                    bean.setTarget(param.optString(SemanticConstant.TARGET));

                    if (!param.isNull(SemanticConstant.ACTION_CN)) {
                        bean.setAction(param.optString(SemanticConstant.ACTION_CN));
                    }

                    if (!param.isNull(SemanticConstant.OBJECT_CN)) {
                        bean.setTarget(param.optString(SemanticConstant.OBJECT_CN));
                    }

                    return bean;
                }
            }
        } catch (JSONException e) {
            // 忽略
        }

        return SemanticBean.getDefaultBean(text, getDomain(semantic));
    }

    private static String getDomain(JSONObject semantic) {
        try {
            return semantic.getJSONObject("request").getString("domain");
        } catch (Exception e) {
            LoggerFactory.getLogger("voice").error("解析json出错： " + e.getMessage());
        }

        return "";
    }

}
