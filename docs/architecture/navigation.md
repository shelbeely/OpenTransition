# Navigation

*This page is under development.*

OpenTransition uses Android Navigation Component for navigation flow.

Key navigation patterns:
- Single Activity architecture (MainActivity)
- Fragment-based screens
- Type-safe arguments with SafeArgs
- Global actions for common navigation

## Main Navigation Destinations

- **Home** - Dashboard with recent photos and audio
- **Gallery** - Browse photos and audio recordings
- **Milestones** - Track important events
- **RecordAudio** - Voice recording screen
- **Settings** - App configuration

Users can navigate to the RecordAudioFragment from the main add button (➕) to record voice samples.

See [Architecture Overview](overview.md) for more details.
