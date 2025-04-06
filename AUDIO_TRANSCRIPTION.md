# Audio Transcription in Claudine

This feature adds audio transcription capabilities to Claudine using the ElevenLabs API.

## Setup

1. Sign up for an ElevenLabs account at https://elevenlabs.io
2. Get your API key from the ElevenLabs dashboard
3. Set the API key in your environment:

```bash
export ELEVENLABS_API_KEY=your_key_goes_here
```

## Testing the Transcription Feature

A standalone test application is provided to demonstrate the audio transcription functionality.

### Run the Test Application

```bash
# Build the project first
./gradlew build

# Run the test application
java -cp build/libs/claudine-jvm-0.1-SNAPSHOT.jar com.xemantic.ai.claudine.TranscribeAudioTestKt
```

The test application will:
1. Download a sample public domain audio file (Martin Luther King Jr.'s "I Have a Dream" speech)
2. Save it to a temporary location
3. Transcribe the audio using the ElevenLabs API
4. Display the transcription results

## Using the Transcription Feature in Claudine

Once Claudine is running, you can use the audio transcription feature by asking Claudine to transcribe an audio file. For example:

```
[me]> Can you transcribe the audio file at /path/to/my/audio.mp3?
```

Claude will use the TranscribeAudio tool to process the request.

## Supported Audio Formats

ElevenLabs supports the following audio formats:
- MP3
- MP4
- MPEG
- MPG
- M4A
- WAV
- WEBM

## Language Support

The ElevenLabs API can auto-detect the language in the audio, but you can also specify a language code (e.g., 'en', 'fr', 'de') for better accuracy.

## Note on API Usage

The ElevenLabs API is a paid service with usage limits. Check your ElevenLabs dashboard for your current usage and limits.