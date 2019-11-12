package edu.ciesla.main_service.database.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlist", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_Playlist"}))
public class Playlist {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID_Playlist", nullable = false, unique = true)
    String id;

    @Column(name = "title", nullable = false)
    String title;

    @ManyToMany
    @JoinTable(name="playlist_entry", joinColumns = @JoinColumn(name="Playlist"), inverseJoinColumns = @JoinColumn(name = "song"))
    List<Song> songs = new ArrayList<>();

    @Column(name = "duration")
    int duration;



}
