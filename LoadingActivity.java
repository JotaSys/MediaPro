package cu.ij.jotalab.mediapro;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingActivity extends AppCompatActivity {


    private ProgressBar pbLoading;
    private int progresStatus = 0;
    private Handler handler = new Handler();
    TextView progressNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        pbLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        progressNumber = (TextView) findViewById(R.id.tv_progressNumber);


        //Color color = new Color();
        //color.parseColor(String.valueOf(R.color.colorAccent));
        //pbLoading.setBackgroundColor(R.color.colorAccent);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progresStatus < 100)
                {
                    progresStatus+=1;
                    //SystemClock.sleep(20);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            pbLoading.setProgress(progresStatus);
                            progressNumber.setText(progresStatus +"%");
                        }
                    });
                    try {
                        Thread.sleep(20);
                    }
                    catch (InterruptedException ie)
                    {ie.printStackTrace();}
                }
                Intent goToShield = new Intent(LoadingActivity.this,ShieldActivity.class);
                startActivity(goToShield);
                finish();

            }
        }).start();


    }
}
