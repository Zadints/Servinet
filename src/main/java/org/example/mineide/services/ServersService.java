package org.example.mineide.services;

import org.example.mineide.core.entities.Server;

import java.util.ArrayList;

public class ServersService {
    private static ArrayList<Server> ListServes = new ArrayList<Server>();

    public static void AddServer(Server newServer){
        //verificar que no se repita y evaluar datos que sean reales
        ListServes.add(newServer);
    }
    public static ArrayList<Server> getListServes(){
        return ListServes;
    }
    public static boolean DeleteServer(String id){
        return true;
    }
    /*
    private Server searchServer(){

    }*/
}
