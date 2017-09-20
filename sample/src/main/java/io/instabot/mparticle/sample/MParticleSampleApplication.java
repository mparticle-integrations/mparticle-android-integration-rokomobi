package io.instabot.mparticle.sample;

import android.app.Application;

import com.mparticle.MParticle;

/**
 * Created by sobolev on 7/18/17.
 */

public class MParticleSampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MParticle.start(this);
    }
}
