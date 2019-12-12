package edu.ciesla.main_service.database.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "room", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_room"}))
public class Room {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID_room", nullable = false, unique = true)
    String id;

    @Column(name = "name", nullable = false)
    String name;

    @Column
    @JoinColumn(name = "owner_token", nullable = true)
    String ownerToken;

    @ManyToOne
    @JoinColumn(name = "playing", nullable = true)
    Song playing;

    @ManyToOne
    @JoinColumn(name = "Queue", nullable = true)
    Playlist queue;

    @ManyToOne
    @JoinColumn(name = "History", nullable = true)
    Playlist history;

    @ManyToMany(mappedBy = "rooms")
    //@JoinTable(name = "room_owners", joinColumns = @JoinColumn(name="room"), inverseJoinColumns = @JoinColumn(name = "user"))
    Set<User> owners = new HashSet<>();
    //@Column(name = "duration")
    //int duration;


    public Room() {
    }

    public Set<User> getOwners() {
        return owners;
    }

    public void setOwners(Set<User> owners) {
        this.owners = owners;
    }

    public String getOwnerToken() {
        return ownerToken;
    }

    public void setOwnerToken(String ownerToken) {
        this.ownerToken = ownerToken;
    }

    public Playlist getQueue() {
        return queue;
    }

    public void setQueue(Playlist queue) {
        this.queue = queue;
    }

    public Playlist getHistory() {
        return history;
    }

    public void setHistory(Playlist history) {
        this.history = history;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Song getPlaying() {
        return playing;
    }

    public void setPlaying(Song playing) {
        this.playing = playing;
    }

}
