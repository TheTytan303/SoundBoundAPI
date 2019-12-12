package edu.ciesla.main_service.database.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "playlist", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_Playlist"}))
public class Playlist {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID_Playlist", nullable = false, unique = true)
    int id;

    @Column(name = "title", nullable = false)
    String title;

    @ManyToMany
    @JoinTable(name="playlist_entry", joinColumns = @JoinColumn(name="Playlist"), inverseJoinColumns = @JoinColumn(name = "song"))
    List<Song> songs = new ArrayList<>();

    @ManyToMany(mappedBy = "playlists")
    Set<User> owners= new HashSet<>();

    @OneToMany(mappedBy = "queue")
    Set<Room> roomsQueue = new HashSet<>();

    @OneToMany(mappedBy = "history")
    Set<Room> roomsHistory = new HashSet<>();

    public PlaylistShort getPlaylistShort(){
        return new PlaylistShort(this.id,this.title);
    }

    //-------------------------------------------------------------------Overrides:


    public Playlist() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<Room> getRoomsQueue() {
        return roomsQueue;
    }

    public void setRoomsQueue(Set<Room> roomsQueue) {
        this.roomsQueue = roomsQueue;
    }

    public Set<Room> getRoomsHistory() {
        return roomsHistory;
    }

    public void setRoomsHistory(Set<Room> roomsHistory) {
        this.roomsHistory = roomsHistory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public Set<User> getOwners() {
        return owners;
    }

    public void setOwners(Set<User> owners) {
        this.owners = owners;
    }

    @Override
    public String toString() {
        return "["+id+"]"+title;
    }

    private class PlaylistShort{
        int id;
        String name;
        public PlaylistShort(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
