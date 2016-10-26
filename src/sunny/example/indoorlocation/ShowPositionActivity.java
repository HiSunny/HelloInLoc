package sunny.example.indoorlocation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint; 
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import utils.CoordPoint;
import utils.ObtainStepData;


public class ShowPositionActivity extends BaseActivity{


    private Button mOffLineLearnButton;
    private Button mShowPositionButton;
    private Button mSettingsButton;
    private BottomButtonClickListener mBottomButtonClickListener;

    static int[] newCoords = new int[2];
    static int[] curTouchCoords = {0, 0};
    static float[] myCoords = new float[2];
    // Time variables
    final long updateItemMilliTime = 100;
    TimerTask mTimetask;
    private Bitmap mBackgroundMap;
    private Bitmap mMark;
    private Bitmap mResultBitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    private ImageView mIndoorMap;
    private TextView mSetpCount;
    private TextView mStepLength;
    private TextView mStepDegree;
    private TextView mStepCoordinate;
    private ObtainStepData mObtainStepData;
    private Button mStartButton;
    private Button mResetButton;
    private Button mStopButton;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showposition);


        initBottomButton(); 

        mIndoorMap = (ImageView)findViewById(R.id.image_map);

        //sMapId=0实验室
        if(SettingActivity.sMapId ==0) {
            mBackgroundMap = BitmapFactory.decodeResource(getResources(), R.drawable.lab);
        } else {
            mBackgroundMap = BitmapFactory.decodeResource(getResources(), R.drawable.hallway);
        }
        mMark = BitmapFactory.decodeResource(getResources(), R.drawable.red_mark);

        mResultBitmap = Bitmap.createBitmap(mBackgroundMap.getWidth(), mBackgroundMap.getHeight(), mBackgroundMap.getConfig());

        mCanvas = new Canvas(mResultBitmap);
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN); 
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(7);

    /*void android.graphics.Canvas.drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint)
	Draw the bitmap using the specified matrix.
     */
        mCanvas.drawBitmap(mBackgroundMap, new Matrix(), null);
//        mCanvas.drawBitmap(mMark, 150, 1480, mPaint);//暂定测试用起始点
        mIndoorMap.setImageBitmap(mResultBitmap);


        mSetpCount = (TextView)findViewById(R.id.text_view_step_count);
        mStepLength = (TextView) findViewById(R.id.text_view_step_length);
        mStepDegree = (TextView) findViewById(R.id.text_view_step_degree);
        mStepCoordinate = (TextView) findViewById(R.id.text_view_step_coordinate);

        //utils.ObtainStepData.ObtainStepData(Context context, TextView stepCount, 
        //TextView stepLength,TextView degree, TextView coordinate)
        //获取步数、步长、角度、坐标
        mObtainStepData = new ObtainStepData(this, mSetpCount, mStepLength, mStepDegree, mStepCoordinate);

        //特殊处理－－每次进入定位页面,默认调用一次ｒｅｓｅｔ
        mObtainStepData.correctStep();
        //清除上次测量的坐标
        mObtainStepData.clearPoints();
        //初始化行人的初始坐标
        mObtainStepData.initPoints();
        //步数、步长、角度等TextView都不显示
        mObtainStepData.stepViewGone();
        //开始测量
        mStartButton = (Button) findViewById(R.id.button_start);
        /**
         * obtainStepSetting
         * initPoints
         * obtainStep
         */
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//让步数、步长、角度等TextView都visible
                mObtainStepData.obtainStepSetting();
                mObtainStepData.initPoints();
                mObtainStepData.obtainStep();
            }
        });
        mResetButton = (Button) findViewById(R.id.button_reset);
        /**
         * correctStep
         * clearPoints
         * initPoints
         * stepViewgone
         */
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mObtainStepData.correctStep();
                mObtainStepData.clearPoints();
                mObtainStepData.initPoints();
                mObtainStepData.stepViewGone();
            }
        });
        mStopButton = (Button) findViewById(R.id.button_stop);
        /**
         * stop
         */
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mObtainStepData.stopStep();
            }
        });


        stepShowTaskSchedule(updateItemMilliTime);
    }

    public void stepShowTaskSchedule(long milliTime) {
        mTimer = new Timer();
        mTimetask = new TimerTask() {
            @Override
            public void run() {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       mCanvas.drawBitmap(mBackgroundMap, new Matrix(), null);
                       //获取移动一步的坐标
                       myCoords = ObtainStepData.getCurCoordsOfStep();
                       //public int[] convertTouchCoordinates(float[] coors)适应不同屏幕
                       //转换后的初始坐标需要达到190，1040
                       curTouchCoords = convertTouchCoordinates(myCoords);
                       drawTrajectory();
                       //////////不断更新mMark的位置
                       mCanvas.drawBitmap(mMark,curTouchCoords[0],curTouchCoords[1], mPaint);
                       mIndoorMap.setImageBitmap(mResultBitmap);
            }
        });//190,1040 curTouchCoords[0] curTouchCoords[1]
            }
        };
        mTimer.schedule(mTimetask, 0, milliTime);
    }

    //画轨迹， 
    private void drawTrajectory() {
        /*
         * draw the line of trajectory.
		 */ 
    	//获取坐标 ObtainStepData.getPoints()
        ArrayList<CoordPoint> tmpPoints = ObtainStepData.getPoints();
        for (int i = 0; i < tmpPoints.size() - 1; i++) {
        	/*public class CoordPoint {
    
    public float px;
    public float py;

    public CoordPoint(float px, float py) {
        this.px = px;
        this.py = py;
    }
}*/			//CoordPoint java.util.ArrayList.get(int index)第i个坐标CoordPoint
            CoordPoint startPoint = tmpPoints.get(i);
            //////
            startPoint = convertTouchCoordinates(startPoint);
            CoordPoint endPoint = tmpPoints.get(i + 1);
            endPoint = convertTouchCoordinates(endPoint);
            mCanvas.drawLine(startPoint.px, startPoint.py, endPoint.px, endPoint.py, mPaint);
        }
    }

    public int[] convertTouchCoordinates(float[] coors) {
		/*
		 * float[] : convert coordinate to fit on the screen of mobile.
		 */

        newCoords[0] = (int) (coors[0] * ((float) mCanvas.getWidth() / mIndoorMap.getRight()));
        newCoords[1] = (int) (coors[1] * ((float) mCanvas.getHeight() / mIndoorMap.getBottom()));

        Log.i("test", ((float) mCanvas.getWidth() / mIndoorMap.getRight()) + " " + ((float) mCanvas.getHeight() / mIndoorMap.getBottom()));
        return newCoords;
    }


    public CoordPoint convertTouchCoordinates(CoordPoint coors) {
		/*
		 * CoordPoint : convert coordinate to fit on the screen of mobile.
		 */
        float xtmp = coors.px * ((float) mCanvas.getWidth() / mIndoorMap.getRight());
        float ytmp = coors.py * ((float) mCanvas.getHeight() / mIndoorMap.getBottom());
        return new CoordPoint(xtmp, ytmp);
    }


    private void initBottomButton() {
        mOffLineLearnButton = (Button) findViewById(R.id.bt_offline_learn);
        mShowPositionButton = (Button) findViewById(R.id.bt_show_position);
        mSettingsButton = (Button) findViewById(R.id.bt_settings);

        resetBottom();
        mShowPositionButton.setTextColor(getResources().getColor(R.color.my_green));

        mBottomButtonClickListener = new BottomButtonClickListener();
        mSettingsButton.setOnClickListener(mBottomButtonClickListener);
        mOffLineLearnButton.setOnClickListener(mBottomButtonClickListener);
//        mSettingsButton.setOnClickListener(mBottomButtonClickListener);
    }

    private class BottomButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {


            switch (v.getId()) {

                case R.id.bt_settings:
                    startActivity(new Intent(ShowPositionActivity.this, SettingActivity.class));
                    finish();
                    break;
                case R.id.bt_offline_learn:
                    startActivity(new Intent(ShowPositionActivity.this, OffLineLearnActivity.class));
                    finish();
                    break;
            }
        }
    }


    private void resetBottom() {

        mShowPositionButton.setTextColor(getResources().getColor(android.R.color.black));
        mSettingsButton.setTextColor(getResources().getColor(android.R.color.black));
        mOffLineLearnButton.setTextColor(getResources().getColor(android.R.color.black));
    }
}
