package org.example.servinet.core.application.usecase;

import org.example.servinet.core.domain.entities.User;
import org.example.servinet.core.domain.utils.GetHadware;
import org.example.servinet.infrastructure.database.models.UserModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StartupUseCase {
    public static boolean checkAutomaticLogin() {
        return SessionUseCase.automaticUserLogin(GetHadware.id());
        //agregar tema de logs aqui de session
    }

}
