package com.example.zhwaterwave;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

/**
 * com.example.zhwaterwave.ZHwaterWave
 * 
 * @author zenghui <br/>
 *         create at 2015-6-6 下午8:08:42
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ZHwaterWave view = (ZHwaterWave) findViewById(R.id.progressView);
        view.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
