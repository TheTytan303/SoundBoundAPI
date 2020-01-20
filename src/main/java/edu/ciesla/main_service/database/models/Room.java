package edu.ciesla.main_service.database.models;

import edu.ciesla.main_service.database.HibernateConfig;
import edu.ciesla.main_service.database.models.exceptions.AlreadyInDatabase;
import edu.ciesla.main_service.database.models.exceptions.Unauthorized;
import edu.ciesla.main_service.database.models.exceptions.Unidentified;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "room", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_room"}))
public class Room {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID_room", nullable = false, unique = true)
    Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "token")
    private Integer token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Host")
    private User host;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "playing")
    Song playing;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Queue")
    Playlist queue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "History")
    Playlist history;

    @ManyToMany(fetch = FetchType.EAGER)
    //@JoinTable(name="room_owners", joinColumns = @JoinColumn(name="user"), inverseJoinColumns = @JoinColumn(name = "room"))
    @JoinTable(name = "room_owners", joinColumns = @JoinColumn(name="room"), inverseJoinColumns = @JoinColumn(name = "user"))
    Set<User> owners = new HashSet<>();
    //@Column(name = "duration")
    //int duration;


    public Room(){
    }


    //-------------------------------------------------------------------Public:
    public void addOwner(int token, User user) throws Unidentified, AlreadyInDatabase {
        if(this.token == 0){
            throw new Unidentified("owner token not generated");
        }
        if(this.token != token){
            throw new Unidentified("invalid owner token");
        }
        if(this.isOwner(user)){
            throw new AlreadyInDatabase("this User is already Owner of this Playlist");
        }
        this.owners.add(user);
        this.update();
    }
    public boolean isOwner(User owner){
        for(User u: this.getOwners()){
            if(u.getId() == owner.getId()){
                return true;
            }
        }
        return false;
    }
    public boolean isHost(User u){
        return u.getId()==this.host.getId();
    }
    public JSONObject JSON(){
        JSONObject returnVale = new JSONObject();
        returnVale.put("id",this.id);
        returnVale.put("name",this.name);
        returnVale.put("host",this.host.nickname);
        returnVale.put("Queue",this.queue.getPlaylistShortJSON());
        if(playing!=null){
            returnVale.put("playing",this.playing.JSON());
        }else{
            returnVale.put("playing","none");
        }
        returnVale.put("History",this.history.getPlaylistShortJSON());
        return returnVale;
    }
    public int getOwnerToken(){
        this.generateToken();
        this.update();
        return this.token;
    }
    public void vote(User user, Song song) throws Unauthorized {
        if(!this.isOwner(user)){
            throw new Unauthorized("this User can not vote");
        }
        String sql = "SELECT * FROM `room_owners` WHERE `room` = "+this.id+" AND `user` = " +user.getId();
        ArrayList<Room_owner> returnVale = new ArrayList<>();
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        NativeQuery query = session.createNativeQuery(sql);
        query.addEntity(Room_owner.class);
        returnVale.addAll(query.list());
        transaction.commit();
        session.close();
        Room_owner ro = returnVale.get(0);
        ro.song = song;
        ro.update();
    }
    public Map<User, Song> getVotes(){
        String sql = "SELECT * FROM `room_owners` WHERE `room` = "+this.id;
        ArrayList<Room_owner> room_owners = new ArrayList<>();
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        NativeQuery query = session.createNativeQuery(sql);
        query.addEntity(Room_owner.class);
        room_owners.addAll(query.list());
        transaction.commit();
        session.close();
        Map<User, Song> returnVale = new HashMap<>();
        for(Room_owner ro: room_owners){
            returnVale.put(ro.user, ro.song);
        }
        return returnVale;
    }
    public void skip(){
        Song playing = this.getPlaying();
        if(playing != null){
            this.getHistory().addSong(List.of(playing));
        }
        if(this.getQueue().getSongs().size() != 0){
            this.setPlaying(this.queue.getAndRemove(0));
        }else{
            this.playing = null;
        }
        this.update();
    }
    //-------------------------------------------------------------------Private:
    private void update(){
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction tc = session.beginTransaction();
        session.update(this);
        session.flush();
        tc.commit();
        session.close();
    }
    private void generateToken(){
        Random random = new Random(System.currentTimeMillis());
        this.token = random.nextInt();
        if(this.token < 0) this.token *= (-1);
        this.token = this.token % 9000;
        this.token += 1000;
    }
    //-------------------------------------------------------------------Overrides:
    @Override
    public String toString() {
        return "["+id+"]"+"'"+name+"' -|- "+host.toString();
    }
    //-------------------------------------------------------------------Static:
    public static Room addRoom(User host, String name){
        Room returnVale = new Room();
        returnVale.history = Playlist.addPlaylist(name+"_H", host);
        returnVale.queue = Playlist.addPlaylist(name+"_Q",host);
        returnVale.name = name;
        returnVale.host = host;
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(returnVale);
        session.flush();
        tx.commit();
        session.close();
        return returnVale;
    }
    public static Room getRoom(int id){
        Room returnVale;
        String sql = "SELECT * FROM `room`";
        sql=sql.concat(" WHERE");
        sql = sql.concat(" `ID_room` =" + id);
        try{
            returnVale = getCommand(sql).get(0);
        }catch (Exception e){
            return null;
        }
        return returnVale;
    }

    private static ArrayList<Room> getCommand(String sqlCommand){
        ArrayList<Room> returnVale = new ArrayList<>();
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        NativeQuery query = session.createNativeQuery(sqlCommand);
        query.addEntity(Room.class);
        returnVale.addAll(query.list());
        transaction.commit();
        session.close();
        return returnVale;
    }
    public Set<User> getOwners() {
        return owners;
    }
    public Playlist getQueue() {
        return queue;
    }
    public Playlist getHistory() {
        return history;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
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
