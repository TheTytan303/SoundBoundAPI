package edu.ciesla.main_service.database.models;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import edu.ciesla.main_service.database.HibernateConfig;
import edu.ciesla.main_service.database.models.exceptions.Unidentified;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "user", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_user"}))
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID_user", unique = true, nullable = false)
    int id;

    @Column(name = "nickname", nullable = false)
    String nickname;

    @Column(name = "user_token", nullable = true)
    @GeneratedValue()
    String user_token;

    @Column(name = "password", nullable = true)
    String password;

    @ManyToMany(mappedBy = "owners")
    Set<Playlist> playlists= new HashSet<>();

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(name="room_owners", joinColumns = @JoinColumn(name="user"), inverseJoinColumns = @JoinColumn(name = "room"))
    Set<Room> rooms = new HashSet<>();

    public User(){}
    public User(String nickname, String password){
        this.nickname =nickname;
        this.password = BCrypt.hashpw(password,BCrypt.gensalt(12));
    }


    public boolean checkPw(String password){
        return BCrypt.checkpw(password,this.password);
    }
    public String generateToken(){
        Algorithm algoritmHS = Algorithm.HMAC256("82CQCZxcDw");
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HMAC256");
        map.put("type", "JWT");

        this.user_token = JWT.create().withIssuer("SnB").withHeader(map).withClaim("id",this.id).sign(algoritmHS);
        return user_token;
    }
    public String getToken() {
        return user_token;
    }




    //-------------------------------------------------------------------Overrides:


    @Override
    public String toString() {
        return "["+id+"]"+nickname;
    }

    //-------------------------------------------------------------------STATIC:
    public static ArrayList<User> getUser(int ...ids){
        ArrayList<User> returnVale = new ArrayList<>();
        if(ids.length == 0){
            return returnVale;
        }
        String sql = "SELECT * FROM `user`";
        if(ids.length>0){
            sql=sql.concat(" WHERE");
            for (int value : ids) {
                sql = sql.concat(" `ID_user` =" + value + " OR");
            }
            sql = sql.substring(0, sql.length() - 3);
        }
        try{
            returnVale = getCommand(sql);
        }catch (Exception e){
            return returnVale;
        }
        return returnVale;
    }

    public static User addUser(String nickname, String password) throws Exception {
        User returnVale = new User(nickname,password);
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction tx;
        Exception exception=null;
        try {
            tx = session.beginTransaction();
            session.save(returnVale);
            tx.commit();
        }catch (Exception e){
            exception = e;
        }finally {
            session.close();
        }
        if(exception != null) throw exception;
        return returnVale;
    }

    private static ArrayList<User> getCommand(String sqlCommand){
        ArrayList<User> returnVale = new ArrayList<>();
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        NativeQuery query = session.createNativeQuery(sqlCommand);
        query.addEntity(User.class);
        returnVale.addAll(query.list());
        transaction.commit();
        session.close();
        return returnVale;
    }

    public static String authorize(int id, String password) throws Exception {
        User user = getUser(id).get(0);
        if(user == null){
            throw new Exception("No user with id ["+id+"] found");
        }
        if(user.checkPw(password)){
            return user.generateToken();
        }else {
            throw new Exception("Invalid user password");
        }
    }

    public static User identifyUser(String token) throws Unidentified {
        Algorithm algorithm = Algorithm.HMAC256("82CQCZxcDw");
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("SnB").build();
        DecodedJWT jwt = verifier.verify(token);
        int id = jwt.getClaim("id").asInt();
        User user = getUser(id).get(0);
        if(user == null){
            throw new Unidentified("user Unidnetified");
        }
        return user;
    }
    //-------------------------------------------------------------------Getters/Setters::

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUser_token() {
        return user_token;
    }

    public void setUser_token(String user_token) {
        this.user_token = user_token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(Set<Playlist> playlists) {
        this.playlists = playlists;
    }

    public Set<Room> getRooms() {
        return rooms;
    }

    public void setRooms(Set<Room> rooms) {
        this.rooms = rooms;
    }
}