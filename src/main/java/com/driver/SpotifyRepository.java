package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        //create the user with given name and number
        User user = new User(name,mobile);
        users.add(user);
        userPlaylistMap.put(user,new ArrayList<>());

        return user;
    }

    public Artist createArtist(String name) {
        //create the artist with given name
        Artist artist = new Artist(name);
        artists.add(artist);
        artistAlbumMap.put(artist,new ArrayList<>());

        return artist;
    }
    //returns the artist reference if exists
    //else returns null
    public Artist checkIfArtistExists(String artistName){
        for(Artist artist:artists){
            if(artistName.equals(artist.getName())){
                return artist;
            }
        }
        return null;
    }
    public Album createAlbum(String title, String artistName) {
        //If the artist does not exist, first create an artist with given name
        //Create an album with given title and artist
        Artist artist=checkIfArtistExists(artistName);
        if(artist==null){
            artist=createArtist(artistName);
        }
        Album album = new Album(title);
        albums.add(album);
        artistAlbumMap.get(artist).add(album); //add the album to that artist's album list
        albumSongMap.put(album,new ArrayList<>());
        return album;
    }

    //returns reference of new Album if found
    //else returns null
    public Album checkIfAlbumExists(String albumName){
        for(Album album:albums){
            if(album.getTitle().equals(albumName)){
                return album;
            }
        }
        return null;
    }
    public Song createSong(String title, String albumName, int length) throws Exception{
        //If the album does not exist in database, throw "Album does not exist" exception
        //Create and add the song to respective album

        Album album = checkIfAlbumExists(albumName);
        if(album==null)
            throw new Exception("Album does not exist");
        Song song = new Song(title,length);
        songs.add(song);
        albumSongMap.get(album).add(song);
        songLikeMap.put(song,new ArrayList<>());
        return song;
    }

    //adds all songs with desired length to a list and returns it
    public List<Song> getSongsWithGivenLength(int length){
        List<Song> listOfSongs = new ArrayList<>();
        for(Song song:songs){
            if(song.getLength()==length){
                listOfSongs.add(song);
            }
        }
        return listOfSongs;
    }
    //returns the reference of user if user found
    //else returns null
    public User checkIfUserExists(String mobile){
        for(User user:users){
            if(user.getMobile().equals(mobile)){
                return user;
            }
        }
        return null;
    }
    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {

        //Create a playlist with given title and add all songs having the given length in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception
        User user = checkIfUserExists(mobile);
        if(user==null)
            throw new Exception("User does not exist");
        //if user exists
        Playlist playlist = new Playlist(title);
        List<Song> listOfsongs=getSongsWithGivenLength(length);
        playlistSongMap.put(playlist,listOfsongs);
        playlistListenerMap.put(playlist,new ArrayList<>());
        playlistListenerMap.get(playlist).add(user);
        creatorPlaylistMap.put(user,playlist);
        userPlaylistMap.get(user).add(playlist);
        playlists.add(playlist);
        return playlist;

    }

    //adds the songs whose title matches with given title to songlist
    public void getSongsWithGivenTitle(String title,List<Song> songlist){
        for(Song song:songs){
            if(song.getTitle().equals(title)){
                if(!songlist.contains(song))
                    songlist.add(song);
            }
        }
    }
    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //Create a playlist with given title and add all songs having the given titles in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception
        User user = checkIfUserExists(mobile);
        if(user==null)
            throw new Exception("User does not exist");
        Playlist playlist = new Playlist(title);
        List<Song> listOfsongs = new ArrayList<>();
        for(String s:songTitles) {
            getSongsWithGivenTitle(s,listOfsongs);
        }
        playlistSongMap.put(playlist,listOfsongs);
        playlistListenerMap.put(playlist,new ArrayList<>());
        playlistListenerMap.get(playlist).add(user);
        creatorPlaylistMap.put(user,playlist);
        userPlaylistMap.get(user).add(playlist);
        playlists.add(playlist);
        return playlist;
    }

    //returns the playlist's reference if playlist exists
    //else returns null
    public Playlist checkIfPlaylistExists(String playlistTitle){

        for(Playlist playlist:playlists){
            if(playlist.getTitle().equals(playlistTitle)){
                return playlist;
            }
        }
        return null;
    }
    //checks if user is creator of the desired playlist or not
    public boolean checkIfUserIsACreator(User user,Playlist playlist){
        if(creatorPlaylistMap.containsKey(user)){
            return creatorPlaylistMap.get(user).equals(playlist);
        }
        return false;
    }
    //checks if user is a listener of the playlist
    public boolean checkIfUserisAListener(User user,Playlist playlist){
        return playlistListenerMap.get(playlist).contains(user);
    }
    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {

        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updating
        Playlist playlist=null;
        User user = null;
        playlist = checkIfPlaylistExists(playlistTitle);
        user=checkIfUserExists(mobile);
        if(playlist==null)
            throw new Exception("Playlist does not exist");
        if(user==null)
            throw new Exception("User does not exist");

        boolean isUserAListenerOrACreator=checkIfUserIsACreator(user,playlist) || checkIfUserisAListener(user,playlist);

        if(isUserAListenerOrACreator)
            return playlist;

        playlistListenerMap.get(playlist).add(user);
        userPlaylistMap.get(user).add(playlist);

        return playlist;
    }

    //returns the reference of the song whose title matches with desired songtitle
    //else returns null if no match found
    public Song checkIfSongExists(String songTitle){

        for(Song song:songs){
            if(song.getTitle().equals(songTitle))
                return song;
        }
        return null;
    }
    public Song likeSong(String mobile, String songTitle) throws Exception {
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        //If the user does not exist, throw "User does not exist" exception
        //If the song does not exist, throw "Song does not exist" exception
        //Return the song after updating

        User user = null;
        Song song = null;
        int songLikes=0;
        int artistLikes=0;
        user=checkIfUserExists(mobile);
        song=checkIfSongExists(songTitle);

        if(user==null)
            throw new Exception("User does not exist");
        if(song==null)
            throw new Exception("Song does not exist");

        //i need the artist the song belongs to for that i need the below stuffs:
        //i need to get the album the song belongs to
        //then i need to get the artist that album belongs to
        Album album = getAlbumOfTheSong(song);
        Artist artist = getArtistOfTheAlbum(album); //artist of the song

        if(songLikeMap.containsKey(song)){
            if(!(songLikeMap.get(song).contains(user))){
                songLikeMap.get(song).add(user);
                songLikes=song.getLikes()+1;
                artistLikes=artist.getLikes()+1;
                song.setLikes(songLikes);
                artist.setLikes(artistLikes);
            }
        }
        else{
            songLikeMap.put(song,new ArrayList<>());
            songLikeMap.get(song).add(user);
            songLikes=song.getLikes()+1;
            artistLikes=artist.getLikes()+1;
            song.setLikes(songLikes);
            artist.setLikes(artistLikes);

        }
        return song;
    }
    public Artist getArtistOfTheAlbum(Album album){
        for(Artist artist:artistAlbumMap.keySet()){
            if(artistAlbumMap.get(artist).contains(album)){
                return artist;
            }
        }
        return null;
    }
    public Album getAlbumOfTheSong(Song song){
        for(Album album: albumSongMap.keySet()){
            if(albumSongMap.get(album).contains(song)){
                return album;
            }
        }
        return null;
    }
    public String mostPopularArtist() {
        //Return the artist name with maximum likes
        String artistName="";
        int max=0;
        for(Artist artist:artists){
            if(artist.getLikes()>max){
                max=artist.getLikes();
                artistName=artist.getName();
            }
        }
        return artistName;
    }

    public String mostPopularSong() {

        //return name of most popular song
        String songName="";
        int max=0;
        for(Song song:songs){
            if(song.getLikes()>max){
                max=song.getLikes();
                songName=song.getTitle();
            }
        }
        return songName;
    }
}