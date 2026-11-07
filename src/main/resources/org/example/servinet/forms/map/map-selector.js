
document.addEventListener('DOMContentLoaded', () => {

    const coordinatesElement = document.getElementById('coordinates');

    if (typeof ol === 'undefined') {
        coordinatesElement.textContent =
            'Error: OpenLayers no está disponible.';
        return;
    }

    // Límites aproximados de Alto Trujillo.
    const allowedBounds = [
        -79.08,
        -8.12,
        -78.94,
        -7.96
    ];

    const allowedExtent = ol.proj.transformExtent(
        allowedBounds,
        'EPSG:4326',
        'EPSG:3857'
    );

    function isInsideAllowedArea(latitude, longitude) {
        return (
            longitude >= allowedBounds[0] &&
            latitude >= allowedBounds[1] &&
            longitude <= allowedBounds[2] &&
            latitude <= allowedBounds[3]
        );
    }

    const initialCoordinates = ol.proj.fromLonLat([
        -79.01,
        -8.06
    ]);

    let marker = null;
    let selectedLatitude = null;
    let selectedLongitude = null;
    let lastValidPosition = null;

    // Enviar coordenadas a Java y cerrar la ventana.
    function sendLocationToJava(latitude, longitude) {

        if (!window.javaBridge) {
            console.error('JavaBridge no está disponible.');
            coordinatesElement.textContent =
                'Error: no se pudo conectar con Java.';
            return;
        }

        try {
            console.log('Enviando coordenadas a Java...');

            window.javaBridge.onLocationSelected(
                latitude,
                longitude
            );

            console.log('Coordenadas enviadas correctamente.');

        } catch (error) {
            console.error('Error enviando coordenadas:', error);
        }
    }

    // Fuente del marcador.
    const markerSource = new ol.source.Vector();

    // Capa del marcador.
    const markerLayer = new ol.layer.Vector({
        source: markerSource,
        style: new ol.style.Style({
            image: new ol.style.Circle({
                radius: 9,
                fill: new ol.style.Fill({
                    color: '#007bff'
                }),
                stroke: new ol.style.Stroke({
                    color: '#ffffff',
                    width: 3
                })
            })
        })
    });

    // Capa OpenStreetMap.
    const osmLayer = new ol.layer.Tile({
        source: new ol.source.OSM()
    });

    // Vista del mapa.
    const view = new ol.View({
        center: initialCoordinates,
        zoom: 14,
        minZoom: 12,
        maxZoom: 19,
        extent: allowedExtent,
        constrainOnlyCenter: false
    });

    // Crear mapa.
    const map = new ol.Map({
        target: 'map',
        layers: [
            osmLayer,
            markerLayer
        ],
        view: view
    });

    window.map = map;

    // Actualizar coordenadas seleccionadas.
    function updateCoordinates(position) {

        const lonLat = ol.proj.toLonLat(position);

        const longitude = lonLat[0];
        const latitude = lonLat[1];

        if (!isInsideAllowedArea(latitude, longitude)) {
            return false;
        }

        selectedLatitude = latitude;
        selectedLongitude = longitude;

        lastValidPosition = position.slice();

        coordinatesElement.textContent =
            'Latitud: ' + latitude.toFixed(7) +
            ' | Longitud: ' + longitude.toFixed(7);

        return true;
    }

    // Desactivar desplazamiento predeterminado.
    const dragPan = map.getInteractions().getArray().find(
        interaction => interaction instanceof ol.interaction.DragPan
    );

    if (dragPan) {
        dragPan.setActive(false);
    }

    // Desplazar mapa con clic derecho.
    const rightDragPan = new ol.interaction.DragPan({
        condition: event =>
            event.originalEvent.button === 2
    });

    map.addInteraction(rightDragPan);

    // Evitar menú contextual.
    map.getViewport().addEventListener('contextmenu', event => {
        event.preventDefault();
    });

    // Seleccionar ubicación con clic izquierdo.
    map.on('click', event => {

        if (event.originalEvent.button !== 0) {
            return;
        }

        const position = event.coordinate;
        const lonLat = ol.proj.toLonLat(position);

        if (!isInsideAllowedArea(lonLat[1], lonLat[0])) {
            coordinatesElement.textContent =
                'La ubicación está fuera del área permitida.';
            return;
        }

        // Crear o mover marcador.
        if (marker === null) {

            marker = new ol.Feature({
                geometry: new ol.geom.Point(position)
            });

            markerSource.addFeature(marker);

        } else {

            marker.getGeometry().setCoordinates(position);

        }

        // Actualizar coordenadas.
        if (!updateCoordinates(position)) {
            return;
        }

        console.log('Ubicación seleccionada:');
        console.log('Latitud:', selectedLatitude);
        console.log('Longitud:', selectedLongitude);

        // Enviar a Java. Java cerrará la ventana.
        sendLocationToJava(
            selectedLatitude,
            selectedLongitude
        );
    });

    // Permitir arrastrar el marcador.
    const modifyInteraction = new ol.interaction.Modify({
        source: markerSource
    });

    map.addInteraction(modifyInteraction);

    // Validar posición después de arrastrar.
    modifyInteraction.on('modifyend', event => {

        const feature = event.features.item(0);

        if (!feature) {
            return;
        }

        const position = feature.getGeometry().getCoordinates();

        if (!updateCoordinates(position)) {

            if (lastValidPosition !== null) {
                feature.getGeometry().setCoordinates(
                    lastValidPosition.slice()
                );
            }

            return;
        }

        // Enviar también la ubicación ajustada.
        sendLocationToJava(
            selectedLatitude,
            selectedLongitude
        );
    });

    // Exponer coordenadas para JavaFX.
    window.getSelectedLocation = function() {

        if (
            selectedLatitude === null ||
            selectedLongitude === null
        ) {
            return null;
        }

        return {
            latitude: selectedLatitude,
            longitude: selectedLongitude
        };
    };

    // Actualizar tamaño del mapa.
    window.invalidateMapSize = function() {
        map.updateSize();
    };

    window.mapReady = true;

    window.addEventListener('resize', () => {
        map.updateSize();
    });

});
