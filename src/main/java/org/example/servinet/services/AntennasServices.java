package org.example.servinet.services;

import org.example.servinet.domain.entities.antenna.Antenna;
import org.example.servinet.domain.entities.antenna.LogAntenna;

import java.util.*;

import static java.util.Arrays.stream;

public class AntennasServices {

    private static List<Antenna> listAntennas = new ArrayList<Antenna>();
    private static List<LogAntenna> logsAntennas = new ArrayList<LogAntenna>();

    public static List<Antenna> getAllAntennas(){
        if (listAntennas.isEmpty()) {
            return null;
        }

        return listAntennas;
    }

    public static Antenna getAntenna(String idAntenna){

        if (idAntenna == null) {
            return null;
        }

        if (listAntennas.isEmpty()) {
            return null;
        }

        Antenna result = listAntennas.stream()
                .filter(antena -> antena.getId() == idAntenna)
                .findFirst()
                .orElse(null);

        return result;

    }


    public static boolean addAntenna(Antenna newAntenna){


        return  true;
    }
}
