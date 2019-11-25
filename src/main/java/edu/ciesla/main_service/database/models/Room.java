package edu.ciesla.main_service.database.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_room"}))
public class Room {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID_room", nullable = false, unique = true)
    String id;

    @Column(name = "name", nullable = false)
    String name;

    @OneToMany
    @JoinTable(name="playlist_entry", joinColumns = @JoinColumn(name="Playlist"), inverseJoinColumns = @JoinColumn(name = "song"))
    List<Song> playing = new ArrayList<>();

    @ManyToMany
    @JoinTable(name="playlist_entry", joinColumns = @JoinColumn(name="Playlist"), inverseJoinColumns = @JoinColumn(name = "song"))
    List<Song> songs = new ArrayList<>();

    @Column(name = "duration")
    int duration;
}
