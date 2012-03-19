# Music Moderator

Music Moderator is a mobile web application that implements an upvote/downvote system for tracks in a music library and  a listening interface that takes into account current vote counts. It is written using [Play Framework][1] and [jQuery Mobile][2].

### Features

* Mobile web interface
* Voting system for tracks in a music library
* Listen interface featuring HTML5 audio player with Flash fallback which plays tracks according to current vote counts
* Application reads ID3 tags and displays artworks where available
* Filesystem monitoring for the `public/tracks` folder with tracklist and vote count updates on changes

### Installation

* Install Play Framework
* Copy mp3 files to the `public/tracks` folder
* Execute `play run .`
* Open browser at `http://<server host>:9000` to view and vote tracks and at `http://<server host>:9000/player` to open the listening interface

[1]: http://www.playframework.org/
[2]: http://jquerymobile.com/
