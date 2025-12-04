#pragma once

#include <regex>
#include <string>
#include <unordered_map>

struct AppRouting {
    std::string mode{"video"};
    std::string asset{"demo.mp4"};
};

class VirtualCamBridge {
  public:
    explicit VirtualCamBridge(std::string config_path = "/data/adb/virtualcam/config.json");

    void reload();
    bool should_hook(const std::string &package) const;
    AppRouting routing_for(const std::string &package) const;
    std::string current_config_hash() const;

  private:
    void parse(const std::string &text);

    std::string config_path_;
    std::string default_asset_{"demo.mp4"};
    std::string fallback_image_{"frame.png"};
    std::unordered_map<std::string, AppRouting> per_app_{};
    std::string config_hash_;
};
