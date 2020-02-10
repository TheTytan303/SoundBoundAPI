package edu.ciesla.main_service.database.models;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import edu.ciesla.main_service.database.HibernateConfig;
import edu.ciesla.main_service.database.models.exceptions.NoIdFound;
import edu.ciesla.main_service.database.models.exceptions.Unauthorized;
import edu.ciesla.main_service.database.models.exceptions.Unidentified;
import edu.ciesla.main_service.database.models.exceptions.WrongInputData;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "user", uniqueConstraints=@UniqueConstraint(columnNames = {"ID_user"}))
public class User {
    static String SERVER_PASS = "82CQCZxcDw";
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "ID_user", unique = true, nullable = false)
    int id;

    @Column(name = "nickname", nullable = false)
    String nickname;

    @Column(name = "user_token")
    @GeneratedValue()
    String user_token;

    @Column(name = "password")
    private String password;

    @ManyToMany(mappedBy = "owners", fetch = FetchType.LAZY)
    Set<Playlist> playlists= new HashSet<>();

    @ManyToMany(mappedBy = "owners")
    Set<Room> rooms = new HashSet<>();

    public User(){}


    public User(String nickname, String password){
        this.nickname =nickname;
        this.password = BCrypt.hashpw(password,BCrypt.gensalt(12));
    }

    private boolean checkPw(String password)throws JWTDecodeException {
        return BCrypt.checkpw(password,this.password);
    }

    private String generateToken(){
        Algorithm algoritmHS = Algorithm.HMAC256(SERVER_PASS);
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HMAC256");
        map.put("type", "JWT");

        this.user_token = JWT.create().withIssuer("SnB").withHeader(map).withClaim("id",this.id).sign(algoritmHS);
        return user_token;
    }
    public static User identifyUser(String token) throws Unidentified, JWTDecodeException {
        Algorithm algorithm = Algorithm.HMAC256(SERVER_PASS);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("SnB").build();
        DecodedJWT jwt = verifier.verify(token);
        int id = jwt.getClaim("id").asInt();
        User user = getUser(id).get(0);
        if(user == null){
            throw new Unidentified("user Unidnetified");
        }
        return user;
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

    public static User addUser(String nickname, String password) throws WrongInputData {
        if(password == null || nickname == null){
            throw new WrongInputData("no required data found");
        }
        User returnVale = new User(nickname,password);
        Session session = HibernateConfig.getSessionFactory().openSession();
        Transaction tx;
        tx = session.beginTransaction();
        session.save(returnVale);
        tx.commit();
        //if(exception != null) throw exception;
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

    public static String authorize(int id, String password) throws NoIdFound, Unauthorized {
        User user = getUser(id).get(0);
        if(user == null){
            throw new NoIdFound("No user with id ["+id+"] found");
        }
        try{
            if(user.checkPw(password)){
                return user.generateToken();
            }else {
                throw new Unauthorized("Invalid user password");
            }
        } catch(JWTDecodeException e){
            throw new Unauthorized("Invalid JWT");
        }
    }


    //-------------------------------------------------------------------Getters/Setters::

    public int getId() {
        return id;
    }
    public Set<Playlist> getPlaylists(){
        return this.playlists;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
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
}