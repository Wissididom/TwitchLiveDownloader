# This Repository is Archived

This Repository still uses the Kraken API (apart from GraphQL), so it won't work anymore. You can try to use the [new version](https://github.com/Wissididom/TwitchDownloader) written in NodeJS instead.

# Welcome to TwitchLiveDownloader!

This Tool is for downloading VODs and livestreams from https://www.twitch.tv/. Commandlinesyntax following below:

# Modes
## GUI-Mode
    TwitchLiveDownloader.jar gui twitch-channel-or-vod-url destination-file.extension
    TwitchLiveDownloader.jar gui twitch-channel-or-vod-url
    TwitchLiveDownloader.jar gui
    TwitchLiveDownloader.jar

## Download-Mode
    TwitchLiveDownloader.jar download twitch-channel-or-vod-url destination-file.extension

## Preview-Mode
    TwitchLiveDownloader.jar preview twitch-channel-or-vod-url

## Show-Link-Mode
    TwitchLiveDownloader.jar show_url twitch-channel-or-vod-url

## Copy-Link-Mode
    TwitchLiveDownloader.jar copy_url twitch-channel-or-vod-url

# Options
## Hide Usher-URL
    <TwitchLiveDownloader-command> --hide_url
    <TwitchLiveDownloader-command> -hu

## Hide FFMPEG-Banner
    <TwitchLiveDownloader-command> --hide_banner
    <TwitchLiveDownloader-command> -hb

## Wait for Streamer when he/she is offline
    <TwitchLiveDownloader-command> --wait_for
    <TwitchLiveDownloader-command> -wf

# Workflow

![Workflow](workflow.png)
