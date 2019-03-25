import 'dart:async';

import 'package:flutter/services.dart';

class Offering {
  String key;
  String activeProductIdentifier;
  String freeTrialPeriod;
  String description;
  double price;
  String currencyCode;
  String sku;
  String subscriptionPeriod;
  String title;
  String type;

  Offering({this.key, this.activeProductIdentifier,
    this.freeTrialPeriod, this.description, this.price, this.currencyCode, this.sku,
    this.title, this.type,
  });

  Offering.fromJson(Map<String, dynamic> data) {
    key = data['key'];
    activeProductIdentifier = data['activeProductIdentifier'];
    freeTrialPeriod = data['freeTrialPeriod'];
    description = data['description'];
    price = data['price'];
    currencyCode = data['currencyCode'];
    sku = data['sku'];
    title = data['title'];
    type = data['type'];
  }

  Map<String, dynamic> toJson() {
    return {
      'key': key,
      'activeProductIdentifier': activeProductIdentifier,
      'freeTrialPeriod': freeTrialPeriod,
      'description': description,
      'price': price,
      'currencyCode': currencyCode,
      'sku': sku,
      'subscriptionPeriod': subscriptionPeriod,
      'title': title,
      'type': type,
    };
  }
}

class Entitlement {
  String key;
  List<Offering> offerings;

  Entitlement.fromJson(Map<String, dynamic> data) {
    key = data['key'];
    final o = data['offerings'] as List;
    offerings = o.map((j) => Offering.fromJson(j)).toList();
  }
}

class Revenuecat {
  static const MethodChannel _channel = const MethodChannel('revenuecat');

  static Future<void> setDebugLogsEnabled(bool enabled) async {
    await _channel.invokeMethod(
        'setDebugLogsEnabled', <String, dynamic>{'enabled': enabled});
  }

  static Future<void> configure({String apiKey, String appUserID}) async {
    await _channel.invokeMethod('configure',
        <String, dynamic>{'apiKey': apiKey, 'appUserID': appUserID});
  }

  static Future<List<Entitlement>> getEntitlements() async {
    final data = await _channel.invokeMethod(
        'getEntitlements', {}) as List;
    return data.map((j) => Entitlement.fromJson(j)).toList();
  }
}
