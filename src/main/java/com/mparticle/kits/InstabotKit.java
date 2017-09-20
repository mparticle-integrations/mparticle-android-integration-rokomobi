package com.mparticle.kits;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.mparticle.MPEvent;
import com.mparticle.MParticle;
import com.rokolabs.sdk.tools.ThreadUtils;

import java.util.List;
import java.util.Map;

import io.instabot.sdk.Instabot;
import io.instabot.sdk.Settings;
import io.instabot.sdk.account.Account;
import io.instabot.sdk.analytics.Analytics;
import io.instabot.sdk.analytics.Event;
import io.instabot.sdk.push.Push;
import io.instabot.sdk.push.PushConstants;
import io.instabot.sdk.push.PushNotificationHelper;

public class InstabotKit extends KitIntegration implements KitIntegration.EventListener, KitIntegration.PushListener, KitIntegration.AttributeListener, InstabotProvider {

    @Override
    protected List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) {
        Instabot.start(settings.get("apiKey"), context, new Instabot.CallbackStart() {
            @Override
            public void start() {
                Instabot.getSettings().setInstabotActivity("io.instabot.INSTABOT_ACTIVITY");
            }
        });
        return null;
    }

    @Override
    public Object getInstance() {
        return this;
    }

    @Override
    public Instabot instabot() {
        return Instabot.getInstance();
    }

    @Override
    public String getName() {
        return "Instabot";
    }

    @Override
    public List<ReportingMessage> setOptOut(boolean optedOut) {
        return null;
    }

    @Override
    public List<ReportingMessage> leaveBreadcrumb(String s) {
        return null;
    }

    @Override
    public List<ReportingMessage> logError(String s, Map<String, String> map) {
        return null;
    }

    @Override
    public List<ReportingMessage> logException(Exception e, Map<String, String> map, String s) {
        return null;
    }

    @Override
    public List<ReportingMessage> logEvent(MPEvent mpEvent) {
        Log.e(getName(), mpEvent.getEventName());
        Analytics.addEvents(new Event(mpEvent.getEventName()));
        return null;
    }

    @Override
    public List<ReportingMessage> logScreen(String s, Map<String, String> map) {
        return null;
    }

    @Override
    public boolean willHandlePushMessage(Intent intent) {
        return intent != null && intent.getStringExtra("rkMsgId") != null;
    }

    @Override
    public void onPushMessageReceived(final Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bundle.containsKey(PushConstants.EXTRA_OVERLAY_ID)) {
                    PushNotificationHelper.pushNotificationSent(bundle.getString(PushConstants.EXTRA_OVERLAY_ID));
                    PushNotificationHelper.showDefaultNotification(context, bundle);
                } else if (bundle.containsKey(PushConstants.EXTRA_CONVERSATION_ID)) {
                    PushNotificationHelper.showInstabot(bundle.getString(PushConstants.EXTRA_CONVERSATION_ID));
                } else {
                    PushNotificationHelper.showDefaultNotification(context, bundle);
                }
            }
        });
    }

    @Override
    public boolean onPushRegistration(String instanceId, String senderId) {
        Settings preferences = Instabot.getSettings();
        preferences.edit().putString("deviceToken", instanceId).apply();
        Push.register(getContext(), instanceId);
        Push.start(senderId);
        return false;
    }

    @Override
    public void setUserAttribute(String name, String value) {
        Account.setUserCustomProperty(name, value);
    }

    @Override
    public void setUserAttributeList(String s, List<String> list) {
    }

    @Override
    public boolean supportsAttributeLists() {
        return false;
    }

    @Override
    public void setAllUserAttributes(Map<String, String> map, Map<String, List<String>> map1) {
    }

    @Override
    public void removeUserAttribute(String name) {
        Account.setUserCustomProperty(name, null);
    }

    @Override
    public void setUserIdentity(MParticle.IdentityType identityType, String identityToken) {
        if(identityType.equals(MParticle.IdentityType.CustomerId) || identityType.equals(MParticle.IdentityType.Email)) {
            Account.setUser(identityToken);
        }
    }

    @Override
    public void removeUserIdentity(MParticle.IdentityType identityType) {
        if(identityType.equals(MParticle.IdentityType.CustomerId) || identityType.equals(MParticle.IdentityType.Email)) {
            Account.logout();
        }
    }

    @Override
    public List<ReportingMessage> logout() {
        Account.logout();
        return null;
    }
}