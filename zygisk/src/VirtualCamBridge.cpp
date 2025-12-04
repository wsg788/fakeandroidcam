#include "VirtualCamBridge.h"

#include <android/log.h>
#include <fstream>
#include <sstream>

namespace {
constexpr const char *kTag = "VirtualCamBridge";

std::string hash_text(const std::string &text) {
    std::hash<std::string> hasher;
    return std::to_string(hasher(text));
}

AppRouting make_route_from(const std::smatch &match) {
    AppRouting route;
    if (match.size() >= 3) {
        route.mode = match[1].str();
        route.asset = match[2].str();
    }
    return route;
}
} // namespace

VirtualCamBridge::VirtualCamBridge(std::string config_path)
    : config_path_(std::move(config_path)) {
    reload();
}

void VirtualCamBridge::reload() {
    std::ifstream file(config_path_);
    if (!file.good()) {
        __android_log_print(ANDROID_LOG_WARN, kTag, "Config %s missing; using defaults", config_path_.c_str());
        per_app_.clear();
        config_hash_.clear();
        return;
    }

    std::stringstream buffer;
    buffer << file.rdbuf();
    parse(buffer.str());
}

bool VirtualCamBridge::should_hook(const std::string &package) const {
    if (per_app_.find("*") != per_app_.end()) {
        return true;
    }
    return per_app_.find(package) != per_app_.end();
}

AppRouting VirtualCamBridge::routing_for(const std::string &package) const {
    if (auto it = per_app_.find(package); it != per_app_.end()) {
        return it->second;
    }
    if (auto all = per_app_.find("*"); all != per_app_.end()) {
        return all->second;
    }
    return {"video", default_asset_};
}

std::string VirtualCamBridge::current_config_hash() const { return config_hash_; }

void VirtualCamBridge::parse(const std::string &text) {
    config_hash_ = hash_text(text);
    per_app_.clear();

    std::regex default_asset_regex("\\\"default_asset\\\"\\s*:\\s*\\\"([^\\"]+)\\\"");
    if (std::smatch match; std::regex_search(text, match, default_asset_regex) && match.size() > 1) {
        default_asset_ = match[1].str();
    }

    std::regex fallback_regex("\\\"fallback_image\\\"\\s*:\\s*\\\"([^\\"]+)\\\"");
    if (std::smatch match; std::regex_search(text, match, fallback_regex) && match.size() > 1) {
        fallback_image_ = match[1].str();
    }

    std::regex per_app_regex("\\\"([^\\"]+)\\\"\\s*:\\s*\\{[^}]*\\\"mode\\\"\\s*:\\s*\\\"([^\\"]*)\\\"[^}]*\\\"asset\\\"\\s*:\\s*\\\"([^\\"]*)\\\"");
    auto begin = std::sregex_iterator(text.begin(), text.end(), per_app_regex);
    auto end = std::sregex_iterator();
    for (auto it = begin; it != end; ++it) {
        const auto &match = *it;
        if (match.size() < 4) {
            continue;
        }
        AppRouting route;
        route.mode = match[2].str();
        route.asset = match[3].str();
        per_app_[match[1].str()] = route;
    }

    __android_log_print(ANDROID_LOG_INFO, kTag, "Loaded %zu routing entries", per_app_.size());
}
