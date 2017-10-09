# YouTubePlayer
Multiplatform YouTube player with media keys support
![main_img](https://github.com/GSilidis/YouTubePlayer/blob/master/img/main.png?raw=true)
## Features
##### Cross-platform
Supports Windows, Linux and Mac OS <br>
![multiplatform_img](https://github.com/GSilidis/YouTubePlayer/blob/master/img/multiplatform.png?raw=true)
##### Use media keys for playback control.
Control playback using media keys (play, stop, etc.) on your keyboard
##### Search for lyrics for currently playing song
![lyrics_img](https://github.com/GSilidis/YouTubePlayer/blob/master/img/lyrics.png?raw=true)
##### Modify and save locally existing youtube playlists
![playlist_img](https://github.com/GSilidis/YouTubePlayer/blob/master/img/playlist.png?raw=true)
## Requirements
* Java 1.8
* Windows 7 SP1 or newer; Internet Explorer 11 must be installed
* Linux (only x86 or x86_64); Mozilla Firefox, XULRunner 1.8.x - 1.9.x, 3.6.x, 10.x, 24.x and 31.x 
(but not 2.x nor other unlisted versions), WebKitGTK+ 1.0 and newer
* Mac OS X 10.5 or newer
## Building notice
You need to compile SWT library for your platform as jar file and put it into lib/org.eclipse.swt.*.jar 
in your working directory.<br>
Valid names for SWT jar:
* org.eclipse.swt.cocoa.macosx.x86_64-4.3.jar
* org.eclipse.swt.gtk.linux.x86_64-4.3.jar
* org.eclipse.swt.gtk.linux.x86-4.3.jar
* org.eclipse.swt.win32.win32.x86_64-4.3.jar
* org.eclipse.swt.win32.win32.x86-4.3.jar
## Download
[Download](http://gsilidis.heliohost.org/shared/player/)
## Third-party components
This application uses next third party components:
* DJ Native Swing (<http://djproject.sourceforge.net/ns/>) for web browser component
* JNativeHook (<https://github.com/kwhat/jnativehook>) for media keys events
* ChartLyrics API (<http://www.chartlyrics.com/api.aspx>) as source for lyrics
* YouTube JS API (https://developers.google.com/youtube/iframe_api_reference) for playback
