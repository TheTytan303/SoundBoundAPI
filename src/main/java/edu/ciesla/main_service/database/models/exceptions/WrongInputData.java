package edu.ciesla.main_service.database.models.exceptions;

public class WrongInputData extends Exception{
    public WrongInputData(String message){
        super(message);
    }
}