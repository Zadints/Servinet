package org.example.servinet.infrastructure.api;

public class MapApi {

    // Mértodo que genera el contenido HTML del mapa listo para pasarlo al WebView
    public static void cargarMapaAltoTrujillo(javafx.scene.web.WebView webView) {
        javafx.scene.web.WebEngine webEngine = webView.getEngine();
        String htmlMapa = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='utf-8'/>" +
                "    <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "    <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "    <style> body, html { height: 100%; margin: 0; } #map { height: 100%; width: 100%; }</style>" +
                "</head>" +
                "<body>" +
                "    <div id='map'></div>" +
                "    <script>" +
                "        var map = L.map('map').setView([-8.1118, -78.9743], 14);" +
                "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "            maxZoom: 19," +
                "            attribution: '&copy; OpenStreetMap contributors'" +
                "        }).addTo(map);" +
                "        var marker = L.marker([-8.1118, -78.9743]).addTo(map)" +
                "            .bindPopup('<b>Zona de Cobertura: Alto Trujillo</b><br>Instalación disponible.').openPopup();" +
                "    </script>" +
                "</body>" +
                "</html>";
        webEngine.loadContent(htmlMapa);
    }
}
