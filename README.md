# Rakibul Video Studio — Android

A mobile-first video editor inspired by the editing workflow shown in the supplied reference video. The app name is **Rakibul Video Studio**.

## Included editor features
- Vertical mobile-first editor UI
- Multi-video timeline with real thumbnail frames
- Drag trim handles on both clip edges
- Split at playhead
- Reorder clips
- Delete clips
- Video preview with automatic clip sequencing
- Multiple audio/music/SFX tracks
- Audio track deletion and per-track export gain
- Text overlays
- Animated CTA/emoji stickers: Like, Follow, Subscribe, Comment, Share, Click Here, etc.
- Pop, Bounce, Wiggle and typing-style sticker motion
- Lightning/electric flash effect with adjustable intensity
- Flash, vivid, warm, cool, mono and vintage looks
- Brightness and contrast controls
- Speed and volume controls
- Crop presets: 16:9, 9:16, 1:1, 4:5, 4:3
- 720p, 1080p and 4K export
- Multiple audio sequences are mixed during export
- Sticker/text overlays are rendered into the exported video
- Internet permission is enabled so future online sticker/music packs can be connected without redesigning the app
- Image/photo editing is intentionally excluded

## Build
The repository contains a GitHub Actions workflow at `.github/workflows/build-android.yml`.

Push to `main` and GitHub Actions builds Debug and Release APKs. Create a tag such as `v1.0.0` to publish the APKs as a GitHub Release.

The workflow installs Gradle 8.9 and Android SDK 35 automatically, so a local Gradle wrapper is not required for GitHub Actions.

## Media engine
The editor uses AndroidX Media3 Transformer/ExoPlayer. Media3 Composition supports combining video sequences with one or more audio sequences and applying video/audio effects.
