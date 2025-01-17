package edu.ciesla.main_service.database.models;


import edu.ciesla.main_service.database.HibernateConfig;
import edu.ciesla.main_service.database.models.exceptions.AlreadyInDatabase;
import edu.ciesla.main_service.database.models.exceptions.NoIdFound;
import edu.ciesla.main_service.database.models.exceptions.WrongInputData;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "song", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_song"}))
public class Song {

    @Id
    @Column(name = "ID_song", nullable = false, unique = true)
    String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "artist")
    private String artist;

    @Column(name = "duration")
    private Integer duration;

    @OneToMany(mappedBy = "playing")
    Set<Room> rooms = new HashSet<>();

    @OneToMany(mappedBy = "song")
    Set<PlaylistEntry>  usingPlaylists= new HashSet<>();


    public Song(){};

    public Song(String id, String title, String artist, Integer duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    public JSONObject JSON(){
        JSONObject retrunVale = new JSONObject();
        retrunVale.put("id", this.getId());
        retrunVale.put("title", this.getTitle());
        retrunVale.put("artist", this.getArtist());
        retrunVale.put("duration", this.getDuration());
        return retrunVale;
    }
    //-------------------------------------------------------------------Overrides:

    @Override
    public String toString() {
        return "["+id+"]"+"'"+title+"' -|- "+artist;
    }

    //-------------------------------------------------------------------STATIC:

    public static Song addSong(String id, String title, String artist, Integer duration) throws AlreadyInDatabase, WrongInputData {
        Song returnVale = new Song();
        returnVale.artist = artist;
        returnVale.id = id;
        returnVale.duration = duration;
        returnVale.title=title;
        if(id == null || id == "" || id == "null"){
            throw new WrongInputData("None song ID has been defined");
        }
        if(title == null || title == ""){
            throw new WrongInputData("None song Title has been defined");
        }
        if(duration == null || duration <=0){
            throw new WrongInputData("None song Duration has been defined or is less than '1'");
        }
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction tx;
        AlreadyInDatabase exception = null;
        try{
            tx = session.beginTransaction();
            session.save(returnVale);
            tx.commit();
        }catch (PersistenceException pe){
            exception = new AlreadyInDatabase("");
            exception.object = Song.getSong(returnVale.id).get(0);
        }
        finally {
            session.close();
        }
        if(exception != null) throw  exception;
        return returnVale;
    }
    public static List<Song> getSong(String ...ids){
        ArrayList<Song> returnVale = new ArrayList<>();
        if(ids.length == 0){
            return returnVale;
        }
        String sql = "SELECT * FROM `song`";
        if(ids.length>0){
            sql=sql.concat(" WHERE");
            for (String value : ids) {
                sql = sql.concat(" `ID_song` = '" + value + "' OR");
            }
            sql = sql.substring(0, sql.length() - 3);
        }
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction tx;
        Exception exception = null;
        try{
            tx = session.beginTransaction();
            NativeQuery query = session.createSQLQuery(sql);
            query.addEntity(Song.class);
            returnVale.addAll(query.list());
            tx.commit();
        }catch (Exception e){
            exception = e;
        }finally {
            session.close();
        }
        Map<String, Song> tmp = new HashMap<>();
        for(Song s: returnVale){
            tmp.put(s.id,s);
        }
        returnVale = new ArrayList<>();
        for(String id: ids){
            Song s = tmp.get(id);
            if(s != null)
            returnVale.add(s);
        }
        return returnVale;
    }
    //-------------------------------------------------------------------Getters/Setters::

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
/*

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
 */