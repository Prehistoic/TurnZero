# Changelog

All notable changes to TurnZero will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.1] - 2026-06-28

### Added
- Custom app logo and app icon with adaptive icon safe zone inset
- CHANGELOG.md following Keep a Changelog format
- AGENTS.md changelog update enforcement rules
- Release workflow now auto-extracts changelog sections for GitHub Releases

### Changed
- Top bar logo uses Image instead of Icon to preserve original colors and transparency

### Fixed
- Theme mode now persists correctly across app restarts
- Tool tiles in grid layout now have uniform height regardless of text length

## [0.2.0] - 2026-06-28

### Added
- About section in Settings with version info, changelog link, and update checker
- OSS licenses screen with auto-detected dependency data (powered by AboutLibraries)
- Remote update mechanism (GitHub Releases API polling, APK download, FileProvider install)
- Social media links (Web, Discord, GitHub) in About screen
- Custom app logo with adaptive icon support
- CI/CD workflows (PR build checks, tag-based releases, version bump enforcement)
- Pull request template and issue templates (bug report, feature request)
- Apache 2.0 license
- CHANGELOG.md (keepachangelog format)

### Changed
- Tools screen grid tiles now have uniform height regardless of text length
- About screen uses mihon-style row layout instead of pill buttons
- Update notification uses standard Android Toast

### Fixed
- Update checker no longer displays raw API URL on HTTP errors
- License screen properly handles nullable fields from AboutLibraries API

## [0.1.0] - 2026-06-18

### Added
- Initial project scaffold with multi-module Gradle setup (app, core:tools-api, core:ui, core:data)
- Jetpack Compose + Material 3 UI with dark/light theme support
- Six color theme presets (Purple, Ocean, Forest, Sunset, Crimson, Rose)
- Navigation host with bottom nav bar (Games, Favorites, Settings)
- Games screen with grid/list layout toggle
- Tools screen with grid/list layout toggle and FAB tool switcher
- Favorites screen with favoriting support
- Settings screen with Appearance section (theme mode, color theme, language, layout)
- Search in settings
- Yu-Gi-Oh! game with Life Counter and Rulings Searcher tools (placeholder)
- Dice Roller tool
- DataStore-backed user preferences
- Service Locator dependency provision
- Tool registry with game-to-tool filtering
