package gov.ismonnet.cardhelp.activity;

import android.os.Bundle;

import java.util.Set;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Inject @Activity Set<LifeCycle> lifecycleListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleListeners.forEach(LifeCycle::onCreate);
    }

    @Override
    protected void onStart() {
        super.onStart();
        lifecycleListeners.forEach(LifeCycle::onStart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifecycleListeners.forEach(LifeCycle::onResume);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lifecycleListeners.forEach(LifeCycle::onPause);
    }

    @Override
    protected void onStop() {
        super.onStop();
        lifecycleListeners.forEach(LifeCycle::onStop);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        lifecycleListeners.forEach(LifeCycle::onRestart);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifecycleListeners.forEach(LifeCycle::onDestroy);
    }
}
