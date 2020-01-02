package edu.ciesla.main_service.database.models.exceptions;

public class Unauthorized extends Exception{
    public Unauthorized(String message){
        super(message);
    }
}
