package org.example.servinet.domain.entities;

import java.util.ArrayList;
import java.util.List;

public class Pokemon {
    private final String NAME;
    private int vida;
    private final int[] TIPOS;
    private final TipoPokemon tipoPokemon;

    public Pokemon(String NAME, int vida, TipoPokemon tipoPokemon) {
        this.NAME = NAME;
        this.tipoPokemon = tipoPokemon;
        this.TIPOS = new int[2];
        this.vida = vida;
    }

    public TipoPokemon getTipoPokemon() {
        return tipoPokemon;
    }
}



class prueba{

    List<Pokemon> pokemonList = new ArrayList<>();



}

//Normal 1
//Fuego 2
//Hielo 3
//Agua 4

