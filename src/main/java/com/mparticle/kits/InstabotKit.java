package com.mparticle.kits;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.mparticle.MPEvent;
import com.mparticle.MParticle;
import com.mparticle.kits_core.KitIntegration;
import com.mparticle.kits_core.ReportingMessage;

import java.util.LinkedList;
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
import io.instabot.sdk.tools.ThreadUtils;

public class InstabotKit extends AbstractKitIntegration implements KitIntegration.EventListener, KitIntegration.PushListener, KitIntegration.AttributeListener, InstabotProvider {

    @Override
    public List<ReportingMessage> onKitCreate(Map<String, String> settings, Context context) {
        String apiKey = settings.get("apiKey");
        if(KitUtils.isEmpty(apiKey)){
            throw new IllegalArgumentException("Instabot initialization was failed. Please set \"apiKey\".");
        }

        Instabot.start(apiKey, context);
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

        List<ReportingMessage> messages = new LinkedList<ReportingMessage>();
        messages.add(ReportingMessageImpl.fromEvent(this, mpEvent));
        return messages;
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
                    PushNotificationHelper.showInstabot(bundle.getLong(PushConstants.EXTRA_CONVERSATION_ID), context, bundle);
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
        return true;
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
