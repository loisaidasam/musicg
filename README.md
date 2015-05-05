# musicg

Lightweight Java API for audio analysing, Android compatible

Automatically exported from [code.google.com/p/musicg](https://code.google.com/p/musicg/)

## About

musicg is a lightweight audio analysis library, written in Java, with the purpose of extracting both high level and low level audio features.
This API allows developers to extract audio features and operate audio data like reading, cutting and trimming easily from an inputstream. It also provides tools for digital signal processing, renders the wavform or spectrogram for research and development purpose.

The API is Android compatible.

## Application Demo

[WhistleAPI Demo for Android](https://code.google.com/p/musicg/downloads/list) in Downloads here

[Surprise!](https://market.android.com/details?id=com.whistleapp) on Android Market: Listen for a whistle and responses with user defined image presentation and sound playing.

## Current features

- Clap Api - Detect whether the input audio is a clap
- Whistle Api - Detect whether the input audio is a whistle
- Read PCM WAVE Headers
- Read audio data
- Trim the audio data
- Save the edited audio file
- Read amplitude-time domain data
- Read frequency-time domain data
- Render audio wave form image (Requires [Java 2D](http://download.oracle.com/javase/6/docs/technotes/guides/2d/index.html) & [Java Image I/O](http://download.oracle.com/javase/1.4.2/docs/guide/imageio/), Android non-compatible)
- Render audio spectrogram image (Requires [Java 2D](http://download.oracle.com/javase/6/docs/technotes/guides/2d/index.html) & [Java Image I/O](http://download.oracle.com/javase/1.4.2/docs/guide/imageio/), Android non-compatible)

## Documentation

- [musicg page](https://sites.google.com/site/musicgapi/): This site contains technical documents and examples on how to use musicg in your code.

## Discussion group

- [Discussion group](https://groups.google.com/forum/?fromgroups#!forum/musicg-api): Discuss musicg here.
