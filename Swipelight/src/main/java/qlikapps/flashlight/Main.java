package qlikapps.flashlight;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;


public class Main extends Activity implements SurfaceHolder.Callback {

    // camera
    public boolean isLightOn = false;
 //   public Camera camera;
    public Camera.Parameters p;
    public SurfaceHolder mHolder;
    static Camera camera = null;

    // preferences
    public boolean multitasking, flashOnLoad, isScreenLightOn, textLabels, volumeButtonsToggle;

    // UI
    public ImageButton centerButton, flashButton, screenButton, bothButton;
    public TextView topText, bottomText, rightText, leftText;
    public SeekBar backLightControl;
    public Dialog dialog;

    // System
    Context context = Main.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        textLabels = preferences.getBoolean("TEXT_LABELS", true);
        flashOnLoad = preferences.getBoolean("AUTO_FLASH", false);
        multitasking = preferences.getBoolean("MULTI_TASK", true);
        volumeButtonsToggle = preferences.getBoolean("VOLUME_TOGGLE", false);

        if (camera == null) {
            camera = Camera.open();
        }

        SurfaceView preview = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = preview.getHolder();
        mHolder.addCallback(this);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p = camera.getParameters();

        centerButton = (ImageButton) findViewById(R.id.centerButton); // center
        flashButton = (ImageButton) findViewById(R.id.flashButton); // top
        screenButton = (ImageButton) findViewById(R.id.screenButton); // bottom
        bothButton = (ImageButton) findViewById(R.id.bothButton); // right
        backLightControl = (SeekBar) findViewById(R.id.seekBar);
        topText = (TextView) findViewById(R.id.topText);
        bottomText = (TextView) findViewById(R.id.bottomText);
        rightText = (TextView) findViewById(R.id.rightText);
        leftText = (TextView) findViewById(R.id.leftText);

        if (flashOnLoad) {
            flashlightMode();
        }

        increaseScreenBrightness();

        backLightControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

                float backLightValue = (float) arg1 / 100;

                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                layoutParams.screenBrightness = backLightValue;
                getWindow().setAttributes(layoutParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {}

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {}
        });

        centerButton.setOnTouchListener(new OnSwipeTouchListener() {

            public void onSwipeTop() { flashlightMode(); }

            public void onSwipeRight() { bothMode(); }

            public void onSwipeLeft() { showSettings(); }

            public void onSwipeBottom() {
                screenlightMode();
            }

            public void onClick() { flashlightMode(); }

        });

        bothButton.setOnTouchListener(new OnSwipeTouchListener() {

            public void onSwipeTop() { flashlightMode(); }

            public void onSwipeRight() {}

            public void onSwipeLeft() { allModesOff(); }

            public void onSwipeBottom() {
                screenlightMode();
            }

            public void onClick() { allModesOff(); }
        });

        flashButton.setOnTouchListener(new OnSwipeTouchListener() {
            public void onSwipeTop() {}

            public void onSwipeRight() { bothMode(); }

            public void onSwipeLeft() { showSettings(); }

            public void onSwipeBottom() { allModesOff(); }

            public void onClick() { allModesOff(); }
        });

        screenButton.setOnTouchListener(new OnSwipeTouchListener() {
            public void onSwipeTop() { allModesOff(); }

            public void onSwipeRight() { bothMode(); }

            public void onSwipeLeft() { showSettings(); }

            public void onSwipeBottom() {}

            public void onClick() { allModesOff(); }
        });

    }

    /**
     * Returns center button, turns all light modes off
     */
    private void allModesOff() {
        if (isLightOn) {
            flashToggle();
        }

        if (isScreenLightOn) {
            screenToggle();
        }

        centerButton.setVisibility(View.VISIBLE);
        flashButton.setVisibility(View.INVISIBLE);
        screenButton.setVisibility(View.INVISIBLE);
        bothButton.setVisibility(View.INVISIBLE);

        backLightControl.setVisibility(View.INVISIBLE);

        if (textLabels) {
            topText.setVisibility(View.VISIBLE);
            bottomText.setVisibility(View.VISIBLE);
            rightText.setVisibility(View.VISIBLE);
            leftText.setVisibility(View.VISIBLE);
        } else {
            topText.setVisibility(View.INVISIBLE);
            bottomText.setVisibility(View.INVISIBLE);
            rightText.setVisibility(View.INVISIBLE);
            leftText.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * Turns on the torch
     */
    private void flashlightMode() {
        centerButton.setVisibility(View.INVISIBLE);
        flashButton.setVisibility(View.VISIBLE);
        screenButton.setVisibility(View.INVISIBLE);
        bothButton.setVisibility(View.INVISIBLE);

        backLightControl.setVisibility(View.INVISIBLE);

        if (!isLightOn) {
            flashToggle();
        }

        if (isScreenLightOn) {
            screenToggle();
        }

        if (textLabels) {
            topText.setVisibility(View.INVISIBLE);
            bottomText.setVisibility(View.VISIBLE);
            rightText.setVisibility(View.VISIBLE);
            leftText.setVisibility(View.VISIBLE);
        } else {
            topText.setVisibility(View.INVISIBLE);
            bottomText.setVisibility(View.INVISIBLE);
            rightText.setVisibility(View.INVISIBLE);
            leftText.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Makes the screen white and increases brightness
     */
    private void screenlightMode() {
        centerButton.setVisibility(View.INVISIBLE);
        flashButton.setVisibility(View.INVISIBLE);
        screenButton.setVisibility(View.VISIBLE);
        bothButton.setVisibility(View.INVISIBLE);

        backLightControl.setVisibility(View.VISIBLE);

        if (isLightOn) {
            flashToggle();
        }

        if (!isScreenLightOn) {
            screenToggle();
        }

        if (textLabels) {
            topText.setVisibility(View.VISIBLE);
            bottomText.setVisibility(View.INVISIBLE);
            rightText.setVisibility(View.VISIBLE);
            leftText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Turns the flashlight and screen light on
     */
    private void bothMode() {
        if (!isLightOn) {
            flashToggle();
        }

        if (!isScreenLightOn) {
            screenToggle();
        }

        centerButton.setVisibility(View.INVISIBLE);
        flashButton.setVisibility(View.INVISIBLE);
        screenButton.setVisibility(View.INVISIBLE);
        bothButton.setVisibility(View.VISIBLE);

        backLightControl.setVisibility(View.VISIBLE);

        if (textLabels) {
            topText.setVisibility(View.VISIBLE);
            bottomText.setVisibility(View.VISIBLE);
            rightText.setVisibility(View.INVISIBLE);
            leftText.setVisibility(View.VISIBLE);
        }
    }

    public void hideAllLabels(boolean hide) {

        if (hide) {
            bottomText.setVisibility(View.INVISIBLE);
            rightText.setVisibility(View.INVISIBLE);
            leftText.setVisibility(View.INVISIBLE);
            topText.setVisibility(View.INVISIBLE);
        } else {
            bottomText.setVisibility(View.VISIBLE);
            rightText.setVisibility(View.VISIBLE);
            leftText.setVisibility(View.VISIBLE);

            // make sure the text label doesn't pop up when the light is on -- bug fix
            if (isLightOn) {
                topText.setVisibility(View.INVISIBLE);
            } else {
                topText.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showSettings() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        dialog.setContentView(R.layout.settings_layout);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface arg0) {
                if (textLabels) {
                    hideAllLabels(false);
                } else {
                    hideAllLabels(true);
                }
            }
        });

        // left align dialog
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.LEFT;
        window.setAttributes(wlp);

        if (isScreenLightOn) {
            allModesOff();
        }
        hideAllLabels(true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();

        dialog.findViewById(R.id.autoFlashButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flashOnLoad) {
                    editor.putBoolean("AUTO_FLASH", false);
                } else {
                    editor.putBoolean("AUTO_FLASH", true);
                }

                editor.commit();

                displayPrefImages();
            }
        });

        dialog.findViewById(R.id.multitaskingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (multitasking) {
                    editor.putBoolean("MULTI_TASK", false);
                } else {
                    editor.putBoolean("MULTI_TASK", true);
                }

                editor.commit();

                displayPrefImages();
            }
        });

        dialog.findViewById(R.id.volumeToggleButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (volumeButtonsToggle) {
                    editor.putBoolean("VOLUME_TOGGLE", false);
                } else {
                    editor.putBoolean("VOLUME_TOGGLE", true);
                }

                editor.commit();

                displayPrefImages();
            }
        });

        dialog.findViewById(R.id.textTipsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textLabels) {
                    editor.putBoolean("TEXT_LABELS", false);
                } else {
                    editor.putBoolean("TEXT_LABELS", true);
                }

                editor.commit();

                displayPrefImages();
            }
        });

        // dialog dismissable by swipe left
        dialog.findViewById(R.id.totalSettingsLayout).setOnTouchListener(new OnSwipeTouchListener() {
            public void onSwipeLeft() { dialog.dismiss(); }
        });
        dialog.findViewById(R.id.autoFlashLayout).setOnTouchListener(new OnSwipeTouchListener() {
            public void onSwipeLeft() { dialog.dismiss(); }
        });
        dialog.findViewById(R.id.multitaskingLayout).setOnTouchListener(new OnSwipeTouchListener() {
            public void onSwipeLeft() { dialog.dismiss(); }
        });
        dialog.findViewById(R.id.volumeButtonsLayout).setOnTouchListener(new OnSwipeTouchListener() {
            public void onSwipeLeft() { dialog.dismiss(); }
        });
        dialog.findViewById(R.id.textTipsSettingLayout).setOnTouchListener(new OnSwipeTouchListener() {
            public void onSwipeLeft() { dialog.dismiss(); }
        });

        displayPrefImages();

        dialog.show();
    }

    private void increaseScreenBrightness() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 1f; // 0f - no backlight ... 1f - full backlight
        getWindow().setAttributes(lp);
    }

    private void displayPrefImages() {

        ImageButton textTipsPref = (ImageButton) dialog.findViewById(R.id.textTipsButton);
        ImageButton flashOnStartPref = (ImageButton) dialog.findViewById(R.id.autoFlashButton);
        ImageButton multitaskingPref = (ImageButton) dialog.findViewById(R.id.multitaskingButton);
        ImageButton volumeButtonsPref = (ImageButton) dialog.findViewById(R.id.volumeToggleButton);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        textLabels = preferences.getBoolean("TEXT_LABELS", true);
        flashOnLoad = preferences.getBoolean("AUTO_FLASH", false);
        multitasking = preferences.getBoolean("MULTI_TASK", true);
        volumeButtonsToggle = preferences.getBoolean("VOLUME_TOGGLE", false);

        if (textLabels) {
            textTipsPref.setBackgroundResource(R.drawable.round_texttips_on);
        } else {
            textTipsPref.setBackgroundResource(R.drawable.round_texttips_off);
        }

        if (flashOnLoad) {
            flashOnStartPref.setBackgroundResource(R.drawable.round_autoflash_on);
        } else {
            flashOnStartPref.setBackgroundResource(R.drawable.round_autoflash_off);
        }

        if (multitasking) {
            multitaskingPref.setBackgroundResource(R.drawable.round_multitask_on);
        } else {
            multitaskingPref.setBackgroundResource(R.drawable.round_multitask_off);
        }

        if (volumeButtonsToggle) {
            volumeButtonsPref.setBackgroundResource(R.drawable.round_volume_on);
        } else {
            volumeButtonsPref.setBackgroundResource(R.drawable.round_volume_off);
        }
    }

    public void screenToggle() {

        final LinearLayout splitModeLayout = (LinearLayout) findViewById(R.id.splitModeLayout);

        if (isScreenLightOn) {

            // Turn screen flash OFF
            isScreenLightOn = false;
            //splitModeLayout.setBackgroundColor(Color.BLACK);

            Integer colorFrom = getResources().getColor(android.R.color.white);
            Integer colorTo = getResources().getColor(android.R.color.black);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    splitModeLayout.setBackgroundColor((Integer)animator.getAnimatedValue());
                }
            });
            colorAnimation.start();

            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = 1; // 0f - no backlight ... 1f - full backlight
            getWindow().setAttributes(lp);

        } else {

            // Turn screen flash ON
            isScreenLightOn = true;
            //splitModeLayout.setBackgroundColor(Color.WHITE);

            backLightControl.setProgress(100);

            Integer colorFrom = getResources().getColor(android.R.color.black);
            Integer colorTo = getResources().getColor(android.R.color.white);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    splitModeLayout.setBackgroundColor((Integer)animator.getAnimatedValue());
                }
            });
            colorAnimation.start();

            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = 1f; // 0f - no backlight ... 1f - full backlight
            getWindow().setAttributes(lp);

        }

    }

    public void flashToggle() {

        Log.d("mtest", String.valueOf(isLightOn));
        try{
            if(isLightOn == false) {

                if(camera!=null)
                {

                    Log.d("mmtest", String.valueOf(isLightOn));
                    Camera.Parameters parameters = camera.getParameters();
                    camera.setParameters(parameters);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    isLightOn = false;

                    flashToggle();
                }

                else {
                    camera = Camera.open();
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                    camera.startPreview();
                    isLightOn = true;

                }
                //// remoteViews.setViewVisibility(R.id.button2, View.VISIBLE);
              //  localAppWidgetManager.updateAppWidget(componentName, remoteViews);
            } else {
                isLightOn = false;
                camera.stopPreview();
                camera.release();
                camera = null;

               // remoteViews.setViewVisibility(R.id.button1, View.VISIBLE);
               // remoteViews.setViewVisibility(R.id.button2, View.GONE);
              //  localAppWidgetManager.updateAppWidget(componentName, remoteViews);
            }
        } catch(Exception e) {
            Log.e("Error", ""+e);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event){
        int keyCode = event.getKeyCode();

        if(event.getAction() == KeyEvent.ACTION_DOWN) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (volumeButtonsToggle) {
                        toggleFlashlightMode();
                        return true;
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (volumeButtonsToggle) {
                        toggleFlashlightMode();
                        return true;
                    }
                    return true;

                default:
                    return super.dispatchKeyEvent(event);
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private void toggleFlashlightMode() {
        if (isLightOn) {
            allModesOff();
        } else {
            flashlightMode();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (multitasking && isLightOn) {

        } else {
            allModesOff();
            if(camera!=null) {
                camera.release();
                camera = null;
            }
        }
    }

    @Override
    protected void onPause() {

        if (multitasking && isLightOn) {

        } else {
            allModesOff();
            if(camera!=null) {
                camera.release();
                camera = null;
            }
        }

        /*if (camera != null) {
            if (multitasking && isLightOn) {
                allModesOff();
            }

            camera.release();
            camera = null;
        }*/

        if (dialog != null) {
            dialog.dismiss();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (multitasking && isLightOn) {

        } else {
            allModesOff();
            if(camera!=null) {
                camera.release();
                camera = null;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector = new GestureDetector(new GestureListener());

        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener implements GestureDetector.OnGestureListener {

            private static final int SWIPE_THRESHOLD = 10;
            private static final int SWIPE_VELOCITY_THRESHOLD = 1;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onClick();
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeBottom();
                            } else {
                                onSwipeTop();
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {

        }

        public void onSwipeBottom() {
        }

        public void onClick() {
        }
    }
}
