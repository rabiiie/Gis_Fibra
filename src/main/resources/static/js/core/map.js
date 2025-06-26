class GISCore {
  constructor(containerId, config = {}) {
    this.map = L.map(containerId, {
      center: config.center || [40.4168, -3.7038],
      zoom: config.zoom || 12,
      layers: [
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '© OpenStreetMap'
        })
      ]
    });

    this._initBaseControls();
  }

  _initBaseControls() {
    // Control de escala
    L.control.scale({ position: 'bottomleft' }).addTo(this.map);
    
    // Fullscreen
    this.map.addControl(new L.Control.Fullscreen({
      title: { 'false': 'Pantalla completa', 'true': 'Salir' }
    }));

    // Geolocalización
    this.map.addControl(L.control.locate({
      drawCircle: false,
      position: 'bottomleft'
    }));
  }

  // Método para añadir capas con auto-fit
  addLayer(layer, fitBounds = true) {
    this.map.addLayer(layer);
    if (fitBounds && layer.getBounds) {
      this.map.fitBounds(layer.getBounds());
    }
    return this; // Para chaining
  }
}