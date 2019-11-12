package edu.ciesla.main_service.database.models;

import edu.ciesla.main_service.database.HibernateConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;

import javax.persistence.*;
import java.util.ArrayList;

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

    public User(){}
    public String generateToken(){
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
        ArrayList<User> returnVale;
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
            e.printStackTrace();
            return null;
        }
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
        return returnVale;
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
}