const MeasurementModule = (function() {
    let measureControl;

    function init(map) {
        measureControl = new L.Control.Measure({
            position: 'bottomleft',
            primaryLengthUnit: 'meters',
            secondaryLengthUnit: 'kilometers',
            primaryAreaUnit: 'sqmeters',
            activeColor: '#3d8bfd',
            completedColor: '#3d8bfd'
        });
        map.addControl(measureControl);
    }

    return { init };
})();