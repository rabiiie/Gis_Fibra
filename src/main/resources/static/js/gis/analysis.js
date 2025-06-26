const SpatialAnalysisModule = (function() {
    let map;

    function init(baseMap) {
        map = baseMap;
    }

    function buffer(layer, distance) {
        if (typeof turf !== 'undefined') {
            const buffered = turf.buffer(layer.toGeoJSON(), distance);
            return L.geoJSON(buffered, {
                style: {
                    color: '#ff7800',
                    weight: 2,
                    opacity: 0.7,
                    fillOpacity: 0.2
                }
            });
        }
        console.error('Turf.js no está cargado');
        return null;
    }

    return {
        init,
        buffer,
        // Puedes añadir más funciones: intersect, nearest, etc.
    };
})();