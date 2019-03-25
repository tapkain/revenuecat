package com.tapkain.revenuecat;

import androidx.annotation.NonNull;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.revenuecat.purchases.Entitlement;
import com.revenuecat.purchases.Offering;
import com.revenuecat.purchases.PurchaserInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.PurchaseCompletedListener;
import com.revenuecat.purchases.interfaces.ReceiveEntitlementsListener;
import com.revenuecat.purchases.interfaces.ReceivePurchaserInfoListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RevenuecatPlugin implements MethodCallHandler {
    private final Registrar registrar;
    private final MethodChannel channel;

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "revenuecat");
        channel.setMethodCallHandler(new RevenuecatPlugin(registrar, channel));
    }

    private RevenuecatPlugin(Registrar registrar, MethodChannel channel) {
        this.registrar = registrar;
        this.channel = channel;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "setDebugLogsEnabled":
                handleSetDebugLogs(call, result);
                break;
            case "configure":
                handleConfigure(call, result);
                break;
            case "getEntitlements":
                handleGetEntitlements(call, result);
                break;
            case "makePurchase":
                handleMakePurchase(call, result);
                break;
            case "getPurchaserInfo":
                handleGetPurchaserInfo(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void handleGetPurchaserInfo(MethodCall call, final Result result) {
        Purchases.getSharedInstance().getPurchaserInfo(new ReceivePurchaserInfoListener() {
            @Override
            public void onReceived(@NonNull PurchaserInfo purchaserInfo) {
                Map<String, Object> info = purchaserInfoToMap(purchaserInfo);
                result.success(info);
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                result.error("GetPurchaserInfo", error.getMessage(), null);
            }
        });
    }

    private void handleMakePurchase(MethodCall call, final Result result) {
        Map<String, String> arguments = call.arguments();
        String sku = arguments.get("sku");
        String skuType = arguments.get("skuType");
        Purchases.getSharedInstance().makePurchase(registrar.activity(), sku, skuType, new PurchaseCompletedListener() {
            @Override
            public void onCompleted(@NonNull String sku, @NonNull PurchaserInfo purchaserInfo) {
                Map<String, Object> info = purchaserInfoToMap(purchaserInfo);
                info.put("sku", sku);
                result.success(info);
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                result.error("MakePurchase", error.getMessage(), null);
            }
        });
    }

    private void handleSetDebugLogs(MethodCall call, Result result) {
        Map<String, Boolean> arguments = call.arguments();
        boolean enabled = arguments.get("enabled");
        Purchases.setDebugLogsEnabled(enabled);
        result.success(null);
    }

    private void handleConfigure(MethodCall call, Result result) {
        Map<String, String> arguments = call.arguments();
        String apiKey = arguments.get("apiKey");
        String appUserID = arguments.get("appUserID");
        Purchases.configure(this.registrar.context(), apiKey, appUserID);
        result.success(null);
    }

    private void handleGetEntitlements(MethodCall call, final Result result) {
        Purchases.getSharedInstance().getEntitlements(new ReceiveEntitlementsListener() {
            @Override
            public void onReceived(@NonNull Map<String, Entitlement> entitlementMap) {
                List<Map<String, Object>> entitlementList = new ArrayList<>();
                for (Map.Entry<String, Entitlement> entry : entitlementMap.entrySet()) {
                    entitlementList.add(entitlementToMap(entry.getKey(), entry.getValue()));
                }
                result.success(entitlementList);
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                result.error("GetEntitlements", error.getMessage(), null);
            }
        });
    }

    private Map<String, Object> entitlementToMap(String key, Entitlement entitlement) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", key);

        List<Map<String, String>> offerings = new ArrayList<>();
        for (Map.Entry<String, Offering> entry : entitlement.getOfferings().entrySet()) {
            offerings.add(offeringToMap(entry.getKey(), entry.getValue()));
        }

        map.put("offerings", offerings);
        return map;
    }

    private Map<String, String> offeringToMap(String key, Offering offering) {
        Map<String, String> map = new HashMap<>();
        map.put("key", key);
        map.put("activeProductIdentifier", offering.getActiveProductIdentifier());
        map.put("freeTrialPeriod", offering.getSkuDetails().getFreeTrialPeriod());
        map.put("description", offering.getSkuDetails().getDescription());
        map.put("price", offering.getSkuDetails().getPrice());
        map.put("currencyCode", offering.getSkuDetails().getPriceCurrencyCode());
        map.put("sku", offering.getSkuDetails().getSku());
        map.put("subscriptionPeriod", offering.getSkuDetails().getSubscriptionPeriod());
        map.put("title", offering.getSkuDetails().getTitle());
        map.put("type", offering.getSkuDetails().getType());
        return map;
    }

    private Map<String, Object> purchaserInfoToMap(PurchaserInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("activeEntitlements", info.getActiveEntitlements());
        map.put("activeSubscriptions", info.getActiveSubscriptions());
        map.put("requestDate", info.getRequestDate());
        return map;
    }
}
