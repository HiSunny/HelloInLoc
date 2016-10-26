package utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import sunny.example.indoorlocation.SettingActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by on 2016/3/22.
 */
public class ObtainOffLineLearnStepData implements SensorEventListener {

	//alpha��ͨ�˲�ϵ��
    public float accThreshold = 0.65f, k_wein = 45f, alpha = 0.25f;
    //mScale��ͼ����ϵ��
    private static float mScale;
    private static int mOffsetDegree;
    Context context;
    TextView tvstepCount, tvstepLength, tvdegree, tvcoordinate;
    float[] mAccValues = new float[3];
    float[] mMagValues = new float[3];
    float[] mValues = new float[3];
    float[] R = new float[9];
    float[] I = new float[9];
    //mAcc�ϼ��ٶ�
    float mAcc = 0, mMaResult = 0;
    float mMaxVal = 0f, mMinVal = 0f, mStepLength = 0f;
    int maLength = 5, stepState = 0, stepCount = 0;
    int degreeDisplay;
    DecimalFormat decimalF = new DecimalFormat("#.00");//����С�������λ
    float offset, degree;
    float mDistanceOnMap;
    /**
     * ������*/
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    
    /**
     * Acc��Mag����ʱ�䣨���룩*/
    private long mLastTimeAcc;
    private long mCurTimeAcc;
    private long mLastTimeMag;
    private long mCurTimeMag;
    
    //�洢����
    private ArrayList<Float> mStepLengthList = new ArrayList<>();
    //�洢
    private ArrayList<Integer> mDegreeList = new ArrayList<>();
/*��ȡ����������*/
    public ObtainOffLineLearnStepData(Context context, TextView stepCount, TextView stepLength, TextView mMMapStepLength, TextView mDegree, float[] coordPoint1, float[] coordPoint2) {
        this.context = context;
        this.tvstepCount = stepCount;
        this.tvstepLength = stepLength;
        this.tvdegree = mDegree;
        this.tvcoordinate = mMMapStepLength;
        mDistanceOnMap = (float) Math.sqrt(Math.pow(coordPoint1[0] - coordPoint2[0], 2) + Math.pow(coordPoint1[1] - coordPoint2[1], 2));

        //���ò�Ƶ��ֵ��Winbergϵ��
        initData();
        //��ȡ���ٶȴ��������شŴ�����ʵ��
        loadSystemService();
    }

    /*SettingActivity�п��Ը��Ĳ�Ƶ��ֵ��Winbergϵ��*/
    private void initData() {
        accThreshold = SettingActivity.mFrequencyThreshold;
        k_wein = SettingActivity.k_wein;
    }

    private void loadSystemService() {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
    //����ϵ��
    public static float getStepScale() {
        return mScale;
    }

    public static int getOffsetDegree() {
        return mOffsetDegree;
    }


   

    public void obtainStep() {
        mStepLengthList.clear();
        mDegreeList.clear();
        mLastTimeAcc = System.currentTimeMillis();
        mLastTimeMag = System.currentTimeMillis();
        /*boolean android.hardware.SensorManager.registerListener(SensorEventListener listener, 
         * Sensor sensor, int rateUs)*/
        //SENSOR_DELAY_GAME 30hz����45Hz֮�� һ��38hzһ����38��SENSOR_DELAY_GAME
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    
//////////��
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mCurTimeAcc = System.currentTimeMillis();
            //����ʱ����ֵ40ms��ȡһ�β��������� ���Ѳ����洢��List�� ������̫������Ϊ80try100try200
            if (mCurTimeAcc - mLastTimeAcc > 100) {
            	getStepAccCountLengthInfo(event.values.clone());//event.values.clone()��ȡ���ٶȵ�ֵ float[]
                mLastTimeAcc = mCurTimeAcc;
            }
        }
 //////////       ////�ų�������
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mCurTimeMag = System.currentTimeMillis();
            //40��Ϊ80��Ϊ100try200
            if (mCurTimeMag - mLastTimeMag > 100) {
                getAzimuthDegree(event.values.clone());//��ȡ��λ��float[]
                mLastTimeMag = mCurTimeMag;
            }
        }
    }//stepCount mStepLength ��ȡ�����Ͳ���
    private void getStepAccCountLengthInfo(float[] acc) {
        mAccValues = acc; 
        //��ϼ��ٶ�
        mAcc = (float) (Math.sqrt(Math.pow(mAccValues[0], 2) + Math.pow(mAccValues[1], 2) + Math.pow(mAccValues[2], 2)) - 9.794);
       // public static float movingAverage(float acc, int length)
        //length�����ٶȵ�ƽ��ֵ��length�������ȣ�
        //accThreshold���ٶȵ���ֵֻ��Ϊ�ж��Ƿ�ʼ�߶�
        //�ϼ��ٶȵ�ƽ��ֵ
        mMaResult = MovingAverage.movingAverage(mAcc, maLength);
       //stepState == 0��δ�˶�����ǡ������һ��׼������һ�������ʾ��δ�˶�
        if (stepState == 0 && mMaResult > accThreshold) {
            stepState = 1;
        }
        //stepstateΪ1ʱ����ʾ��ʼ���߲ɼ��û����߸ò������еļ��ٶȵ����ֵ
        //�ڸò������в��ϸ��¼��ٶ����ֵ
        if (stepState == 1 && mMaResult > mMaxVal) { //find peak
            mMaxVal = mMaResult;
        }
      //stepstateΪ1ʱ����ʾ�Ѿ��ɼ����û����߸ò������еļ��ٶȵ����ֵ
        if (stepState == 1 && mMaResult <= 0) {
            stepState = 2;
        }
        //���߸ò��Ĺ����еļ��ٶȵ���Сֵ//�ڸò������в��ϸ��¼��ٶ���Сֵ
        if (stepState == 2 && mMaResult < mMinVal) { //find bottom
            mMinVal = mMaResult;
        }
        //stepState == 2 �ϼ��ٶȴ��ک� �Ʋ����  ����Winberg���㲽��
        if (stepState == 2 && mMaResult >= 0) {
            stepCount++;
            mStepLength = (float) (k_wein * Math.pow(mMaxVal - mMinVal, 1.0 / 4));
            mStepLengthList.add(mStepLength);
            mMaxVal = mMinVal = stepState = 0;
        }

        //TextView��ʾ�����Ͳ���
        stepViewShow();
    }

    public void stepViewShow() {
        tvstepCount.setText("Step Count : " + stepCount);
        tvstepLength.setText("Step Length : " + decimalF.format(mStepLength) + " cm");
    }

    private void getAzimuthDegree(float[] MagClone) {
		/*
		 * get the azimuth degree of the pedestrian.
		 */
        mMagValues = lowPassFilter(MagClone, mMagValues);
        if (mAccValues == null || mMagValues == null) return;
        boolean sucess = SensorManager.getRotationMatrix(R, I, mAccValues, mMagValues);
        if (sucess) {
        	
        	//float[] android.hardware.SensorManager.getOrientation(float[] R, float[] values)
        	//������ת����mValues���㷽λ��
        	/*values[0]: azimuth, rotation around the Z axis. 
			values[1]: pitch, rotation around the X axis. 
			values[2]: roll, rotation around the Y axis. */
            SensorManager.getOrientation(R, mValues);
            //������ת��Ϊ�Ƕ�double java.lang.Math.toDegrees(double angrad)
           // Log.i("sunny", "degree1 = " + Math.toDegrees(mValues[0]));
            //ֻ����values[0]ƫ����
            degree = (int) (Math.toDegrees(mValues[0]) + 360) % 360; // translate into (0, 360).
           // Log.i("sunny", "degree2 = " + degree);
            degree = ((int) (degree + 2)) / 5 * 5; // the value of degree is multiples of 5.
          //  Log.i("sunny", "degree3 = " + degree);
           // Log.i("sunny", "offset = " + offset);

            degreeDisplay = (int) degree;
            //��ȡƫ���Ǵ洢��List��
            mDegreeList.add(degreeDisplay);
        }
    }

    //��ͨ�˲���
    protected float[] lowPassFilter(float[] input, float[] output) {
		/*
		 * low pass filter algorithm implement.
		 */
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + alpha * (input[i] - output[i]);
        }
        return output;
    }

    //ֹͣ���� ����ƽ�������������ڵ�ͼ�ϵ�ƽ������
    public void stopStep() {
		/*
		 * stop listening for sensor and recording step information.
		 */
        mSensorManager.unregisterListener(this);
        //����ʵ��ƽ������
        float sum = 0;
        for (float item : mStepLengthList) {
            sum += item;
        }
        float averageLength = sum / stepCount;
        float averageMapLength = mDistanceOnMap / stepCount;
        //����ϵ�� ��ͼ�ϵ�ƽ������/ʵ��ƽ������
        mScale = averageMapLength / averageLength;
        //��ʾƽ������
        tvstepLength.setText("avg Length : " + decimalF.format(averageLength) + " cm");
       // Log.i("steplength", "sum = " + sum + ", averageLength =" + averageLength + ", disOnMap = " + mDistanceOnMap + ", averageMapLength = " + averageMapLength + ", mScale = " +mScale);

        for (int item : mDegreeList) {
            sum += item;
        }
        mOffsetDegree = (int) (sum / mDegreeList.size());

    }
    
}