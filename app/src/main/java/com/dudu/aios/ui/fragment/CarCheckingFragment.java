package com.dudu.aios.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.aios.ui.view.VehicleCheckResultView;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.FileUtils;
import com.dudu.carChecking.CarCheckingProxy;
import com.dudu.carChecking.VideoTextureView;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.event.Events;
import com.dudu.commonlib.umeng.ClickEvent;
import com.dudu.commonlib.utils.File.KeyConstants;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.monitor.tirepressure.TirePressureManager;
import com.dudu.obd.FaultCodesEvent;
import com.dudu.obd.ShowFaultPageEvent;
import com.dudu.persistence.driving.FaultCode;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.voice.semantic.engine.SemanticEngine;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.ObservableFactory;
import com.dudu.workflow.driving.CarStatusUtils;
import com.dudu.workflow.obd.CarCheckType;
import com.dudu.workflow.obd.FaultCodeFlow;
import com.dudu.workflow.obd.OBDStream;
import com.dudu.workflow.obd.VehicleConstants;
import com.dudu.workflow.tpms.TPMSInfo;
import com.umeng.analytics.MobclickAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 行车自检
 * Created by Robi on 2016-03-10 16:29.
 */
public class CarCheckingFragment extends RBaseFragment implements View.OnClickListener, TirePressureManager.ITireDataListener {
    private Logger logger = LoggerFactory.getLogger("car.CarCheckingFragment");
    private VideoTextureView carTypeVideo;
    private VideoTextureView.PlayListener playListener;
    private int[] icons = {R.drawable.vehicle_fine_bg, R.drawable.vehicle_problem_bg};
    private VehicleCheckResultView engineVehicleCheckResultView, gearboxVehicleCheckResultView, absVehicleCheckResultView, wsbVehicleCheckResultView, rsrVehicleCheckResultView;
    private TextView tvEnginePrompt, tvGearboxPrompt, tvAbsPrompt, tvWsbPrompt, tvSrsPrompt;
    private ImageView iconEnginePrompt, iconGearboxPrompt, iconAbsPrompt, iconWsbPrompt, iconSrsPrompt;
    private LinearLayout engineContainer, gearboxContainer, absContainer, wsbContainer, srsContainer;
    private ImageButton buttonBack;
    private ImageView buttonCarChecking;
    private ImageView buttonVehicleReplace;
    private ImageView buttonTire;
    private TextView batteryVoltageTextView;
    private TextView textFuelOilRatioTextView;
    private String vehicleType;
    private RelativeLayout carTypeVideoContainer;
    private Subscription subscription;
    private Subscription voltageSubscription;
    private Subscription oilRatioSubscription;
    private Subscription obdWorkStartSubscription;


    @DebugLog
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_car_checking;
    }

    @Override
    protected void initViewData() {
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
    }

    public void initListener() {
        buttonBack.setOnClickListener(this);
        buttonTire.setOnClickListener(this);

        engineContainer.setOnClickListener(this);
        gearboxContainer.setOnClickListener(this);
        absContainer.setOnClickListener(this);
        wsbContainer.setOnClickListener(this);
        srsContainer.setOnClickListener(this);

        buttonCarChecking.setOnClickListener(this);
        buttonVehicleReplace.setOnClickListener(this);

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
    }

    @DebugLog
    @Override
    protected void initView(View rootView) {
        engineVehicleCheckResultView = (VehicleCheckResultView) mViewHolder.v(R.id.engine_vehicleCheckResult);
        gearboxVehicleCheckResultView = (VehicleCheckResultView) mViewHolder.v(R.id.gearbox_vehicleCheckResult);
        absVehicleCheckResultView = (VehicleCheckResultView) mViewHolder.v(R.id.abs_vehicleCheckResult);
        wsbVehicleCheckResultView = (VehicleCheckResultView) mViewHolder.v(R.id.wsb_vehicleCheckResult);
        rsrVehicleCheckResultView = (VehicleCheckResultView) mViewHolder.v(R.id.srs_vehicleCheckResult);
        carTypeVideoContainer = (RelativeLayout) mViewHolder.gV(R.id.anim_container);
        tvEnginePrompt = (TextView) mViewHolder.v(R.id.engine_prompt_text);
        tvGearboxPrompt = (TextView) mViewHolder.v(R.id.gearbox_prompt_text);
        tvAbsPrompt = (TextView) mViewHolder.v(R.id.abs_prompt_text);
        tvWsbPrompt = (TextView) mViewHolder.v(R.id.wsb_prompt_text);
        tvSrsPrompt = (TextView) mViewHolder.v(R.id.srs_prompt_text);

        iconEnginePrompt = (ImageView) mViewHolder.v(R.id.engine_prompt_icon);
        iconGearboxPrompt = (ImageView) mViewHolder.v(R.id.gearbox_prompt_icon);
        iconAbsPrompt = (ImageView) mViewHolder.v(R.id.abs_prompt_icon);
        iconWsbPrompt = (ImageView) mViewHolder.v(R.id.wsb_prompt_icon);
        iconSrsPrompt = (ImageView) mViewHolder.v(R.id.srs_prompt_icon);

        engineContainer = (LinearLayout) mViewHolder.v(R.id.engine_container);
        gearboxContainer = (LinearLayout) mViewHolder.v(R.id.gearbox_container);
        absContainer = (LinearLayout) mViewHolder.v(R.id.abs_container);
        wsbContainer = (LinearLayout) mViewHolder.v(R.id.wsb_container);
        srsContainer = (LinearLayout) mViewHolder.v(R.id.srs_container);

        buttonCarChecking = (ImageButton) mViewHolder.v(R.id.button_vehicle_clear);
        buttonVehicleReplace = (ImageButton) mViewHolder.v(R.id.button_vehicle_replace);
        buttonTire = (ImageButton) mViewHolder.v(R.id.button_tire);
        buttonBack = (ImageButton) mViewHolder.v(R.id.button_back);
        batteryVoltageTextView = (TextView) mViewHolder.v(R.id.battery_voltage);
        textFuelOilRatioTextView = (TextView) mViewHolder.v(R.id.text_fuel_oil_ratio);

        initListener();
        initData();
        playListener = new VideoTextureView.PlayListener() {
            @Override
            public void onPlayEnd() {
                logger.debug("车辆自检动画出场动画的结束。。");
                File file;
                file = new File(VehicleConstants.SYSTEM_ANIMATION_DIR + "/" + VehicleConstants.VEHICLE_CATEGORY_DIR + "/" + vehicleType, VehicleConstants.VEHICLE_CYCLE_MP4);
                if (!file.exists()) {
                    file = new File(FileUtils.getAnimDir() + "/" + VehicleConstants.VEHICLE_CATEGORY_DIR + "/" + vehicleType, VehicleConstants.VEHICLE_CYCLE_MP4);
                }
                logger.debug("车辆自检循环动画的目录:" + file.getPath() + (file.exists() ? "存在" : "不存在"));
                if (file.exists()) {
                    carTypeVideo.setFilePath(file.getPath());
                    carTypeVideo.setFps(25);
                    carTypeVideo.setLoopMode(true);
                    carTypeVideo.startPlay();
                }
            }

            @Override
            public void onPlayStart() {
                Observable.timer(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
                    if (carTypeVideo != null) {
                        logger.debug("车辆自检动画的执行方法：--onPlayStart()");
                        carTypeVideo.setVisibility(View.VISIBLE);
                    }
                }, throwable -> logger.error("mAnimationView.startAnim", throwable));

            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.engine_container:
            case R.id.gearbox_container:
            case R.id.abs_container:
            case R.id.wsb_container:
            case R.id.srs_container:
                MobclickAgent.onEvent(getContext(), ClickEvent.CLICK_SHOWFAULTS.getEventId());
                showFaultCodesMessages(false, null);
                break;
            case R.id.button_vehicle_clear:
                MobclickAgent.onEvent(getContext(), ClickEvent.CLICK_CLEARFAULTS.getEventId());
                showFaultCodesMessages(true, null);
                break;
            case R.id.button_vehicle_replace:
                MobclickAgent.onEvent(getContext(), ClickEvent.CLICK_GOTO4S.getEventId());
                goTo4s();
                break;
            case R.id.button_tire:
                goToTire();
                break;
            case R.id.button_back:
                CarCheckingProxy.getInstance().cancelChecking();
                VoiceManagerProxy.getInstance().stopSpeaking();
                VoiceManagerProxy.getInstance().onStop();
                mBaseActivity.showMain();
                return;
        }

    }

    private void goToTire() {
        replaceFragment(FragmentConstants.TIRE_FRAGMENT);
    }

    /**
     * 打开故障码页面（正在检查故障码、清除故障码或者没故障码时不打开）
     *
     * @param clearCodes 是否立即清除故障码
     * @param faultInfo  故障类型（用于播报语音）
     */
    private void showFaultCodesMessages(boolean clearCodes, String faultInfo) {
        if (!CarCheckingProxy.getInstance().isCheckingFaults() && !CarCheckingProxy.getInstance().isClearingFault()) {
            DataFlowFactory.getDrivingFlow()
                    .getAllFaultCodes()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(faultCodeList -> {
                        showVehicleAnimationFragment(clearCodes, faultInfo, FaultCodeFlow.getFaultCode(faultCodeList), false);
                    }, throwable -> logger.error("showFaultCodesMessages", throwable));
        }
    }

    /**
     * 显示故障页
     *
     * @param clearCodes    是否要立即清除故障码
     * @param faultInfo     故障类型(用于播报,不包括胎压)
     * @param faultCodes    故障类型(用于播放动画,包括胎压)
     * @param askClearCodes 是否需要播报语音“询问是否清除故障码”(自检后自动打开的需要，手动点击的不需要)
     */
    private void showVehicleAnimationFragment(boolean clearCodes, String faultInfo, String[] faultCodes, boolean askClearCodes) {
        logger.debug("showVehicleAnimationFragment: clearCodes," + clearCodes + " faultInfo," + faultInfo + " faultCodes," + faultCodes + " askClearCodes," + askClearCodes);

        VoiceManagerProxy.getInstance().stopSpeaking();
        VoiceManagerProxy.getInstance().onStop();

        Bundle bundle = new Bundle();
        bundle.putStringArray(VehicleConstants.VEHICLE, faultCodes);
        bundle.putBoolean(VehicleConstants.CLEAR_CODES, clearCodes);
        bundle.putBoolean(VehicleConstants.ASK_CLEAR_CODES, askClearCodes);
        bundle.putString(VehicleConstants.VEHICLE_FAULT_INFO, faultInfo);
        FragmentConstants.TEMP_ARGS = bundle;
        replaceFragment(FragmentConstants.VEHICLE_ANIMATION_FRAGMENT);
    }

    @DebugLog
    @Override
    public void onAdd() {
        super.onAdd();

    }

    private void stopPromptAnim(ImageView imageView, TextView textView) {
        imageView.clearAnimation();
        textView.clearAnimation();
    }

    private void starPromptAnim(ImageView imageView, TextView textView) {
        Animation alphaAnimation = new AlphaAnimation(0.2f, 0.9f);
        alphaAnimation.setDuration(2000);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        imageView.startAnimation(alphaAnimation);
        textView.startAnimation(alphaAnimation);
    }

    @DebugLog
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @DebugLog
    @Override
    public void onPause() {
        super.onPause();
        onHide();
        stopCarTypeVideo();
    }

    @DebugLog
    @Override
    public void onResume() {
        super.onResume();
        logger.debug("onResume()...");
        initAboutAnim();

    }

    @DebugLog
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        logger.debug("onAttach()...");
    }

    private void stopCarTypeVideo() {
        if (carTypeVideo != null) {
            carTypeVideo.setPlayListener(null);
            carTypeVideo.stopPlay();
            carTypeVideo.setVisibility(View.GONE);
            carTypeVideo = null;
        }
        if (carTypeVideoContainer.getChildCount() > 0) {
            carTypeVideoContainer.removeAllViews();
        }
    }

    @DebugLog
    @Override
    public void onShow() {
        super.onShow();
        logger.debug("fragment running onShow()");
        initAboutAnim();
        initData();
        //查询轮胎信息
        TirePressureManager.instance().addTireDataListener(this);
        TirePressureManager.instance().queryTireDatas(true);
    }

    private void initData() {
        SemanticEngine.getProcessor().switchSemanticType(SceneType.CAR_CHECKING);
        obdWorkStart();
        reflashContainorColor();
    }

    private void initAboutAnim() {
        carTypeVideo = new VideoTextureView(getActivity());
        carTypeVideoContainer.addView(carTypeVideo);
        checkCarcategory();
    }

    private void setAllVehicleCategoryViewEnable(boolean enable) {
        engineContainer.setEnabled(enable);
        gearboxContainer.setEnabled(enable);
        absContainer.setEnabled(enable);
        wsbContainer.setEnabled(enable);
        srsContainer.setEnabled(enable);
    }

    private void setButtonEnable(boolean enable) {
        buttonCarChecking.setEnabled(enable);
        buttonVehicleReplace.setEnabled(enable);
        if (!enable) {
            buttonCarChecking.setBackgroundResource(R.drawable.button_vehicle_clear_clicked);
            buttonVehicleReplace.setBackgroundResource(R.drawable.button_vehicle_replace_clicked);
        } else {
            buttonCarChecking.setBackgroundResource(R.drawable.button_vehicle_clear_selector);
            buttonVehicleReplace.setBackgroundResource(R.drawable.button_vehicle_replace_selector);
        }
    }

    private void obdWorkStart() {
        if (obdWorkStartSubscription != null) {
            obdWorkStartSubscription.unsubscribe();
        }
        obdWorkStartSubscription = CarStatusUtils.isFired()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fired -> {
                    if (fired) {
                        reflashData(SharedPreferencesUtil.getBooleanValue(getContext(), KeyConstants.KEY_SHOULD_GET_ACC_VOLTAGE, false));
                        startChecking();
                    } else {
                        VoiceManagerProxy.getInstance().startSpeaking(
                                CommonLib.getInstance().getContext().getString(R.string.fire_before_carcheck), TTSType.TTS_DO_NOTHING, false);
                    }
                }, throwable -> logger.error("obdWorkStart", throwable));
    }

    private void initAnimData(String category) {
        vehicleType = category;
        File file;
        file = new File(VehicleConstants.SYSTEM_ANIMATION_DIR + "/" + VehicleConstants.VEHICLE_CATEGORY_DIR + "/" + category, VehicleConstants.VEHICLE_APPEAR_MP4);
        if (!file.exists()) {
            file = new File(FileUtils.getAnimDir() + "/" + VehicleConstants.VEHICLE_CATEGORY_DIR + "/" + category, VehicleConstants.VEHICLE_APPEAR_MP4);
        }
        logger.debug("文件的目录：" + file.getPath() + "--" + file.exists());
        if (file.exists()) {
            carTypeVideo.setFilePath(file.getPath());
            carTypeVideo.setFps(25);
            carTypeVideo.setLoopMode(false);
            Observable.timer(300, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
                        if (playListener != null && carTypeVideo != null) {
                            carTypeVideo.startPlay(playListener);
                            logger.debug("开始播放动画。。。");
                        }
                    }
                    , throwable -> logger.error("mAnimationView.startAnim", throwable));
        }
    }

    public void reflashData(boolean monitorAccVoltage) {
        try {
            if (voltageSubscription != null) {
                voltageSubscription.unsubscribe();
            }
            voltageSubscription = ObservableFactory.getDrivingFlow()
                    .getVoltageStream(monitorAccVoltage)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(batteryVoltage -> batteryVoltageTextView.setText(batteryVoltage + "V")
                            , throwable -> logger.error("reflashData", throwable));
            if (oilRatioSubscription != null) {
                oilRatioSubscription.unsubscribe();
            }
            oilRatioSubscription = OBDStream.getInstance().oilRatioStream()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(oilRatio -> textFuelOilRatioTextView.setText(oilRatio)
                            , throwable -> logger.error("reflashData", throwable));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onEventMainThread(Events.VoltageTypeChangeEvent event) {
        reflashData(event.isAccVoltage());
    }


    @Override
    public void onHide() {
        super.onHide();
        logger.debug("fragment running onHide()");
        stopCarTypeVideo();
        SemanticEngine.getProcessor().switchSemanticType(SceneType.HOME);
        //移出轮胎数据监听
        TirePressureManager.instance().removeTireDataListener(this);
        ObservableFactory.getDrivingFlow().stopGetAdcVoltage();
    }

    public void onEventMainThread(FaultCodesEvent event) {
        switch (event.getCarCheckType()) {
            case ECM:
                changeTextType(event.getStartOrStop(), tvEnginePrompt, iconEnginePrompt);
                showContainerColor(event.getCheckFaultCodeResult() == FaultCodesEvent.CHECK_CODES_RESULT_HAS_CODES, tvEnginePrompt, iconEnginePrompt, engineContainer, engineVehicleCheckResultView);
                changeTextView(tvEnginePrompt, event.getCheckFaultCodeResult() == FaultCodesEvent.CHECK_CAR_IS_RUNNING);
                startVehicleAnim(event.getStartOrStop(), event.getCheckFaultCodeResult(), engineVehicleCheckResultView);
                break;
            case TCM:
                changeTextType(event.getStartOrStop(), tvGearboxPrompt, iconGearboxPrompt);
                showContainerColor(event.getCheckFaultCodeResult() == FaultCodesEvent.CHECK_CODES_RESULT_HAS_CODES, tvGearboxPrompt, iconGearboxPrompt, gearboxContainer, gearboxVehicleCheckResultView);
                changeTextView(tvGearboxPrompt, event.getCheckFaultCodeResult() == FaultCodesEvent.CHECK_CAR_IS_RUNNING);
                startVehicleAnim(event.getStartOrStop(), event.getCheckFaultCodeResult(), gearboxVehicleCheckResultView);
                break;
            case ABS:
                changeTextType(event.getStartOrStop(), tvAbsPrompt, iconAbsPrompt);
                showContainerColor(event.getCheckFaultCodeResult() == FaultCodesEvent.CHECK_CODES_RESULT_HAS_CODES, tvAbsPrompt, iconAbsPrompt, absContainer, absVehicleCheckResultView);
                changeTextView(tvAbsPrompt, event.getCheckFaultCodeResult() == FaultCodesEvent.CHECK_CAR_IS_RUNNING);
                startVehicleAnim(event.getStartOrStop(), event.getCheckFaultCodeResult(), absVehicleCheckResultView);
                break;
            case SRS:
                changeTextType(event.getStartOrStop(), tvSrsPrompt, iconSrsPrompt);
                showContainerColor(event.getCheckFaultCodeResult() == FaultCodesEvent.CHECK_CODES_RESULT_HAS_CODES, tvSrsPrompt, iconSrsPrompt, srsContainer, rsrVehicleCheckResultView);
                changeTextView(tvSrsPrompt, event.getCheckFaultCodeResult() == FaultCodesEvent.CHECK_CAR_IS_RUNNING);
                startVehicleAnim(event.getStartOrStop(), event.getCheckFaultCodeResult(), rsrVehicleCheckResultView);
                break;
        }

    }

    private void changeTextType(int startOrStop, TextView textView, ImageView imageView) {
        if (startOrStop == FaultCodesEvent.CHECK_CODES_START) {
            starPromptAnim(imageView, textView);
        } else {
            stopPromptAnim(imageView, textView);
        }
    }

    private void showContainerColor(boolean showRed, TextView textView, ImageView imageView, View container, VehicleCheckResultView vehicleCheckResultView) {
        if (showRed) {
            vehicleCheckResultView.setProgressColor(getResources().getColor(R.color.red));
            textView.setText(getString(R.string.check_details));
            imageView.setImageResource(icons[1]);
            container.setEnabled(true);
        } else {
            vehicleCheckResultView.setProgressColor(getResources().getColor(R.color.blue));
            textView.setText(getString(R.string.device_fine));
            imageView.setImageResource(icons[0]);
            container.setEnabled(false);
        }
    }

    private void changeTextView(TextView textView, boolean isPause) {
        textView.setText(getString(isPause ? R.string.checking_pause : R.string.check_details));
    }

    private void startVehicleAnim(int isStartOrStop, int checkFaultCodeResult, VehicleCheckResultView vehicleCheckResultView) {
        if (isStartOrStop == FaultCodesEvent.CHECK_CODES_START) {
            vehicleCheckResultView.startAnim(100);
        } else if (isStartOrStop == FaultCodesEvent.CHECKING_RESUME) {
            vehicleCheckResultView.resumeAnim(100);
        } else {
            switch (checkFaultCodeResult) {
                case FaultCodesEvent.CHECK_CAR_IS_RUNNING:
                    vehicleCheckResultView.pauseAnim();
                    break;
                case FaultCodesEvent.CHECK_CODES_RESULT_HAS_CODES:
                    vehicleCheckResultView.pauseAndContinueAnim(100, 1);
                    break;
                default:
                    vehicleCheckResultView.pauseAndContinueAnim(100, 0);
            }
        }
    }

    public void onEventMainThread(Events.CarCheckingStartEvent event) {
        setAllVehicleCategoryViewEnable(false);
        setButtonEnable(false);
        resetContainor();
    }

    public void onEventMainThread(ShowFaultPageEvent event) {
        setButtonEnable(true);
        if (event.getFaultCodes() != null && event.getFaultCodes().length > 0) {
            showVehicleAnimationFragment(false, event.getFaultInfo(), event.getFaultCodes(), true);
        }
    }


    private void goTo4s() {
        if (!CarCheckingProxy.getInstance().isCheckingFaults() && !CarCheckingProxy.getInstance().isClearingFault()) {
            subscription = DataFlowFactory.getDrivingFlow()
                    .getAllFaultCodes()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(faultCodes -> {
                        subscription.unsubscribe();
                        if (faultCodes.size() > 0) {
                            showRepairFaultCodeFragment();
                        }
                    }, throwable -> logger.error("goTo4s", throwable));
        }
    }

    private void showRepairFaultCodeFragment() {
        VoiceManagerProxy.getInstance().stopSpeaking();
        VoiceManagerProxy.getInstance().onStop();
        Bundle args = new Bundle();
        FragmentConstants.TEMP_ARGS = args;
        args.putBoolean(VehicleConstants.FROM_FAULT_LIST, false);
        replaceFragment(FragmentConstants.REPAIR_FAULT_CODE_FRAGMENT);
    }

    private void startChecking() {
        Bundle bundle = FragmentConstants.TEMP_ARGS;
        if (bundle != null && bundle.getBoolean(VehicleConstants.START_CHECKING, false)) {
            logger.debug("直接进入。。");
            bundle.putBoolean(VehicleConstants.START_CHECKING, false);
            FragmentConstants.TEMP_ARGS = bundle;
            if (CarCheckingProxy.getInstance().isCheckingFaults()) {
                reflashCheckedContainorColor();
            }
            CarCheckingProxy.getInstance().requestCarTypeAndStartCarchecking(false);
        }
    }

    private void reflashContainorColor() {
        setButtonEnable(true);
        DataFlowFactory.getDrivingFlow()
                .getAllFaultCodes()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(faultCodes -> {
                            List<Integer> allFaultTypes = FaultCodeFlow.initCarTypes();
                            for (int i = 0; i < faultCodes.size(); i++) {
                                FaultCode faultCode = faultCodes.get(i);
                                changeContainerColor(faultCode.getCarCheckType(), true);
                                for (int j = 0; j < allFaultTypes.size(); j++) {
                                    int aFaultType = allFaultTypes.get(j);
                                    if (aFaultType == faultCode.getCarCheckType()) {
                                        allFaultTypes.remove(j);
                                    }
                                }
                            }
                            for (int faultCode : allFaultTypes) {
                                changeContainerColor(faultCode, false);
                            }
                        }
                        , throwable -> logger.error("reflashContainorColor", throwable));
    }

    private void reflashCheckedContainorColor() {
        logger.debug("reflashCheckedContainorColor:");
        List<CarCheckType> checkedCarCheckTypeList = CarCheckingProxy.getInstance().getCarCheckTypeCheckedList();
        for (CarCheckType carCheckType : checkedCarCheckTypeList) {
            logger.debug("carCheckType:" + carCheckType);
            DataFlowFactory.getDrivingFlow()
                    .getFaultCodes(carCheckType)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(faultCode -> {
                                logger.debug("faultCode" + faultCode);
                                changeContainerColor(faultCode.getCarCheckType(), faultCode.getFaultCode().length > 0);
                            }
                            , throwable -> logger.error("reflashContainorColor", throwable));
        }

        List<CarCheckType> carCheckTypeList = CarCheckingProxy.getInstance().getCarCheckTypeList();
        logger.debug("carCheckTypeList:" + carCheckTypeList);
        for (CarCheckType carCheckType : carCheckTypeList) {
            logger.debug("carCheckType:" + carCheckType);
            switch (carCheckType) {
                case ECM:
                    logger.debug("ECM:carCheckType:" + carCheckType);
                    tvEnginePrompt.setText("");
                    iconEnginePrompt.setImageBitmap(null);
                    engineVehicleCheckResultView.reset();
                    break;
                case TCM:
                    logger.debug("TCM:carCheckType:" + carCheckType);
                    tvGearboxPrompt.setText("");
                    iconGearboxPrompt.setImageBitmap(null);
                    gearboxVehicleCheckResultView.reset();
                    break;
                case ABS:
                    logger.debug("ABS:carCheckType:" + carCheckType);
                    iconAbsPrompt.setImageBitmap(null);
                    absVehicleCheckResultView.reset();
                    tvAbsPrompt.setText("");
                    break;
                case SRS:
                    logger.debug("SRS:carCheckType:" + carCheckType);
                    iconSrsPrompt.setImageBitmap(null);
                    rsrVehicleCheckResultView.reset();
                    tvSrsPrompt.setText("");
                    break;
            }
        }
    }

    private void resetContainor() {
        tvEnginePrompt.setText("");
        tvGearboxPrompt.setText("");
        tvAbsPrompt.setText("");
        tvWsbPrompt.setText("");
        tvSrsPrompt.setText("");
        iconEnginePrompt.setImageBitmap(null);
        iconGearboxPrompt.setImageBitmap(null);
        iconAbsPrompt.setImageBitmap(null);
        iconWsbPrompt.setImageBitmap(null);
        iconSrsPrompt.setImageBitmap(null);
        engineVehicleCheckResultView.reset();
        gearboxVehicleCheckResultView.reset();
        absVehicleCheckResultView.reset();
        wsbVehicleCheckResultView.reset();
        rsrVehicleCheckResultView.reset();
    }

    private void changeContainerColor(int faultType, boolean showRed) {
        switch (faultType) {
            case FaultCode.ECM:
                showContainerColor(showRed, tvEnginePrompt, iconEnginePrompt, engineContainer, engineVehicleCheckResultView);
                break;
            case FaultCode.TCM:
                showContainerColor(showRed, tvGearboxPrompt, iconGearboxPrompt, gearboxContainer, gearboxVehicleCheckResultView);
                break;
            case FaultCode.ABS:
                showContainerColor(showRed, tvAbsPrompt, iconAbsPrompt, absContainer, absVehicleCheckResultView);
                break;
            case FaultCode.WSB:
                showContainerColor(showRed, tvWsbPrompt, iconWsbPrompt, wsbContainer, wsbVehicleCheckResultView);
                break;
            case FaultCode.SRS:
                showContainerColor(showRed, tvSrsPrompt, iconSrsPrompt, srsContainer, rsrVehicleCheckResultView);
                break;
        }
    }

    private void checkCarcategory() {
        DataFlowFactory.getUserMessageFlow().obtainUserMessage()
                .map(userMessage -> userMessage.getVehicleModel())
                .subscribe(category -> {
                            initAnimData(category != null ? category : VehicleConstants.CATEGORY_MVP);
                        }
                        , throwable -> {
                            initAnimData(VehicleConstants.CATEGORY_MVP);
                            logger.error("checkCarTypeAndSetToObd", throwable);
                        });

    }

    @Override
    public void onTireData(boolean change1, boolean change2, boolean change3, boolean change4, TPMSInfo info1, TPMSInfo info2, TPMSInfo info3, TPMSInfo info4) {
//        if (info1 == null && info2 == null && info3 == null && info4 == null) {
//            logger.debug("收到-->无胎压数据.{}", change);
//        } else {
//            logger.debug("收到-->轮胎数据. {}", change);
//        }
    }
}
