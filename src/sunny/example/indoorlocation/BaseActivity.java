package sunny.example.indoorlocation;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


public class BaseActivity extends ActionBarActivity{

    //返回键
    final long backSpaceTimeIntervalMilliSecond = 1000;
    long lastTimeclickBack = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//
        /*Window android.app.Activity.getWindow()
Retrieve the current android.view.Window for the activity.
*///returns the current Window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        if (lastTimeclickBack <= 0) {
//            Toast.makeText(this, "再按一次后退键退出应用", Toast.LENGTH_SHORT).show();
//            lastTimeclickBack = System.currentTimeMillis();
//        } else {
//            long currentClickTime = System.currentTimeMillis();
//            if (currentClickTime - lastTimeclickBack < backSpaceTimeIntervalMilliSecond) {
//                finish();
//                //kill the process of the App
//                android.os.Process.killProcess(android.os.Process.myPid());
//            } else {
//                Toast.makeText(this, "再按一次后退键退出应用", Toast.LENGTH_SHORT).show();
//                lastTimeclickBack = System.currentTimeMillis();
//            }
//        }
//    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
