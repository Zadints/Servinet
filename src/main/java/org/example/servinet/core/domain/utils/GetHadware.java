package org.example.servinet.core.domain.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GetHadware {

    public static String id() {
        try {
            Process process = new ProcessBuilder(
                    "powershell",
                    "-Command",
                    "(Get-CimInstance Win32_ComputerSystemProduct).UUID"
            ).start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (!line.isEmpty() && !line.equalsIgnoreCase("UUID")) {
                        return line;
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("No se pudo obtener el Hardware ID", e);
        }

        return null;
    }
}
