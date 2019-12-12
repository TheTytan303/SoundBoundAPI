package edu.ciesla.main_service.database.models;


import edu.ciesla.main_service.database.HibernateConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "song", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_song"}))
public class Song {

    @Id
    @Column(name = "ID_song", nullable = false, unique = true)
    String id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "artist")
    String artist;

    @Column(name = "duration")
    int duration;

    @OneToMany(mappedBy = "playing")
    Set<Room> rooms = new HashSet<>();

    @ManyToMany(mappedBy = "songs")
    Set<Playlist>  usingPlaylists= new HashSet<>();

    //-------------------------------------------------------------------Overrides:


    @Override
    public String toString() {
        return "["+duration+"]"+"'"+title+"' by "+artist;
    }

    //-------------------------------------------------------------------STATIC:
    public static ArrayList<Song> getSongs(int ...ids){
        ArrayList<Song> returnVale;
        String sql = "SELECT * FROM `song`";
        if(ids.length>0){
            sql=sql.concat(" WHERE");
            for (int value : ids) {
                sql = sql.concat(" `ID_song` =" + value + " OR");
            }
            sql=sql.substring(0,sql.length()-3);
        }

        try{
            returnVale = getCommand(sql);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return returnVale;
    }

    private static ArrayList<Song> getCommand(String sqlCommand){
        ArrayList<Song> returnVale = new ArrayList<>();
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        NativeQuery query = session.createNativeQuery(sqlCommand);
        query.addEntity(Song.class);
        returnVale.addAll(query.list());
        transaction.commit();
        return returnVale;
    }




    //-------------------------------------------------------------------Getters/Setters::

    public void setRooms(Set<Room> rooms) {
        this.rooms = rooms;
    }

    public Set<Playlist> getUsingPlaylists() {
        return usingPlaylists;
    }

    public void setUsingPlaylists(Set<Playlist> usingPlaylists) {
        this.usingPlaylists = usingPlaylists;
    }

    public Set<Room> getRooms() {
        return rooms;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
}
