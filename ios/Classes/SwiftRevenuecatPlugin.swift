import Flutter
import UIKit
import Purchases

public class SwiftRevenuecatPlugin: NSObject, FlutterPlugin {
  private var registrar: FlutterPluginRegistrar!

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "revenuecat", binaryMessenger: registrar.messenger())
    let instance = SwiftRevenuecatPlugin(registrar)
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  init(_ registrar: FlutterPluginRegistrar) {
    this.registrar = registrar
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? Dictionary<String, Any>
    switch call.method {
      case "setDebugLogsEnabled":
        handleSetDebugLogs(arguments, result)
      case "configure":

      default:
        result(FlutterMethodNotImplemented)
    }
  }

  private func handleSetDebugLogs(_ args: Dictionary<String, Any>, result: @escaping FlutterResult) {
    let enabled = args["enabled"]
    Purchases.debugLogsEnabled = enabled
    result(nil)
  }

  private func handleConfigure(_ args: Dictionary<String, Any>, result: @escaping FlutterResult) {
    let apiKey = args["apiKey"]
    let appUserID = args["appUserID"]
    Purchases.configure(withAPIKey: apiKey, appUserID: appUserID)
    result(nil)
  }

  private func handleGetEntitlements(_ args: Dictionary<String, Any>, result: @escaping FlutterResult) {
    Purchases.shared.entitlements { (entitlements, error) in
      if error != nil {
        result(FlutterError.init(code: error.code, message: error.message, details: nil))
        return
      }

      result(nil)
    }
  }
}
