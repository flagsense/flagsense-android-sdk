package com.flagsense.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.flagsense.android.Flagsense;
import com.flagsense.android.enums.Environment;
import com.flagsense.android.model.FSFlag;
import com.flagsense.android.services.FlagsenseService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FlagsenseService flagsenseService = Flagsense.serviceBuilder()
                .application(this.getApplication())
                .sdkId("")
                .sdkSecret("")
                .environment(Environment.PROD)
//                .userId("a123jh")
                .build();

        TextView tv1 = findViewById(R.id.textview1);
        tv1.setText("Hola");
        FSFlag<Boolean> fsFlag = new FSFlag<>("", "off", false);

        flagsenseService.setMaxInitializationWaitTime(5000L);
        flagsenseService.waitForInitializationComplete();
        System.out.println("without user set: " + flagsenseService.booleanVariation(fsFlag).getKey());
        flagsenseService.setFSUser("a123jh");
        System.out.println("with user set: " + flagsenseService.booleanVariation(fsFlag).getKey());

        String key = flagsenseService.booleanVariation(fsFlag).getKey();
        flagsenseService.recordEvent(fsFlag, "checkout-click-5");

        tv1.setText(key);
    }
}
