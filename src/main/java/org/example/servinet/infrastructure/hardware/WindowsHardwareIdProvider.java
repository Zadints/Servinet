package org.example.servinet.infrastructure.hardware;

import org.example.servinet.application.port.out.HardwareIdProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WindowsHardwareIdProvider implements HardwareIdProvider {

    @Override
    public String getId() {
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
