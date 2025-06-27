const GISAdvanced = (function() {
    // Variables de estado
    let map;
    let layerControl;
	let drawTool;
    let drawnItems = L.featureGroup();
    let currentClientId = null;
    let baseLayers = {};
    let overlayLayers = {};

    // Inicialización
    function init() {
        initMap();
        initControls();
        setupEventListeners();
        loadClientsFromAPI();
        
        // Inicializar módulos
        if (window.LayersModule) LayersModule.init();
        if (window.MeasurementModule) MeasurementModule.init(map);
    }

    // Configuración del mapa base
    function initMap() {
        map = L.map('map', {
            fullscreenControl: false,
			zoomControl: false,
            fullscreenControlOptions: { position: 'topleft' },
            preferCanvas: true,
			minZoom: 3,
			maxZoom: 22
        }).setView([40.4168, -3.7038], 6);

		baseLayers.OSM = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
		    attribution: '© OpenStreetMap contributors',
		    maxZoom: 22
		}).addTo(map);

		baseLayers.Satellite = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
		    attribution: 'Tiles © Esri',
		    maxZoom: 22
		});

        L.control.scale({imperial: false}).addTo(map);
    }

    // Controles básicos
	function initControls() {
	    // Añadir control de capas
	    if (!layerControl) {
	        layerControl = L.control.layers(baseLayers, overlayLayers, {
	            collapsed: false,
	            position: 'topleft'
	        }).addTo(map);
	    }

	    // Añadir controles de edición y medición con Geoman
	    map.pm.addControls({
	        position: 'topleft',
	        drawCircle: true,
	        drawCircleMarker: false,
	        drawMarker: true,
	        drawPolygon: true,
	        drawPolyline: true,
	        drawRectangle: true,
	        editMode: true,
	        dragMode: true,
	        cutPolygon: false,
	        removalMode: true
	    });

	    // Medir automáticamente cuando se dibuja
		map.on('pm:create', e => {
		    const layer = e.layer;

		    // Medición visual por segmentos
		    L.MeasurePath.measure(layer);

		    // Si es línea o polígono, calcular y mostrar longitud total
		    if (layer instanceof L.Polyline || layer instanceof L.Polygon) {
		        const latlngs = layer.getLatLngs().flat();
		        const lengthMeters = L.GeometryUtil.length(latlngs);
		        const lengthKm = (lengthMeters / 1000).toFixed(2);

		        const popupText = `Longitud total: ${lengthKm} km`;

		        layer.bindPopup(popupText).openPopup(); // Muestra popup al terminar
		    }
		});

	}


    function handleDrawCreated(e) {
        const layer = e.layer;
        drawnItems.addLayer(layer);
        console.log('Feature creada:', layer.toGeoJSON());
    }

    function setupEventListeners() {
        document.getElementById("searchClient")?.addEventListener("input", filterClients);
        document.getElementById('togglePanelBtn')?.addEventListener('click', toggleSidePanel);
        document.getElementById('closePanelBtn')?.addEventListener('click', toggleSidePanel);
        document.getElementById('togglePanelIcon')?.addEventListener('click', toggleSidePanel);
		
		
    }

    function toggleSidePanel() {
        const panel = document.getElementById('sidePanel');
        if (!panel) return;
        
        panel.classList.toggle('hidden');
        const icon = document.getElementById('togglePanelIcon');
        if (icon) {
            icon.innerHTML = panel.classList.contains('hidden') ? 
                '<i class="bi bi-list"></i>' : '<i class="bi bi-x-lg"></i>';
        }
    }

    function filterClients() {
        const q = this.value.toLowerCase();
        document.querySelectorAll("#clientsContainer .client-card").forEach(card => {
            const clientName = card.querySelector('.client-name')?.textContent.toLowerCase();
            card.style.display = clientName?.includes(q) ? "" : "none";
        });
    }

    function loadClientsFromAPI() {
        const loadingOverlay = createLoadingOverlay();
        const mapArea = document.querySelector('.gis-map-area');
        if (!mapArea) return;
        
        mapArea.appendChild(loadingOverlay);
        
        fetch('/api/clients')
            .then(response => {
                if (!response.ok) throw new Error('Error al cargar clientes');
                return response.json();
            })
            .then(clients => {
                if (window.ClientsModule?.renderClients) {
                    ClientsModule.renderClients(clients);
                }
            })
            .catch(err => {
                console.error('Error cargando clientes:', err);
                alert('Error al cargar clientes: ' + err.message);
            })
            .finally(() => {
                loadingOverlay.remove();
            });
    }

    function createLoadingOverlay() {
        const overlay = document.createElement('div');
        overlay.className = 'loading-overlay';
        overlay.innerHTML = '<div class="spinner"></div>';
        return overlay;
    }
	
	

    // API Pública
    return {
        init,
        addGeoJsonLayer: function(geojson, options = {}) {
            const defaultOptions = {
                pointToLayer: (feature, latlng) => {
                    return L.circleMarker(latlng, {
                        radius: 8,
                        fillColor: "#ff9f43",
                        color: "#fff",
                        weight: 2,
                        opacity: 1,
                        fillOpacity: 0.8,
                        ...options.pointStyle
                    });
                },
                style: (feature) => {
                    const baseStyle = feature.geometry.type === 'LineString' ? {
                        color: "#4dabf5",
                        weight: 4,
                        opacity: 0.7
                    } : {
                        color: "#2e8bc0",
                        weight: 2,
                        opacity: 0.7
                    };
                    return {...baseStyle, ...options.featureStyle};
                },
                onEachFeature: (feature, layer) => {
                    if (feature.properties) {
                        let popupContent = `<b>${feature.properties.name || 'Elemento'}</b>`;
                        for (const key in feature.properties) {
                            if (key !== 'name') {
                                popupContent += `<br>${key}: ${feature.properties[key]}`;
                            }
                        }
                        layer.bindPopup(popupContent);
                    }
                    if (options.onEachFeature) {
                        options.onEachFeature(feature, layer);
                    }
                }
            };

            const layer = L.geoJSON(geojson, defaultOptions);
            layer.addTo(map);
            return layer;
        },
        removeGeoJsonLayer: function(layer) {
            if (layer && map.hasLayer(layer)) {
                map.removeLayer(layer);
            }
        },
        fitBounds: function(bounds) {
            map.fitBounds(bounds, { padding: [50, 50] });
        },
        getMap: function() {
            return map;
        },
		toggleLayerPanel: function() {
		    const layerPanel = document.getElementById('layerPanel');
		    if (layerPanel) {
		        const isNowVisible = !layerPanel.classList.contains('active');

		        // ✅ Alternar visibilidad
		        layerPanel.classList.toggle('active');

		        // ✅ Añadir estado al historial solo si se está abriendo
		        if (isNowVisible) {
		            NavigationModule.goToLayerPanel();
		        }
		    }

        },
		loadClientProjects: function(clientId) {
		    currentClientId = clientId;
		    const loadingOverlay = createLoadingOverlay();
		    const mapArea = document.querySelector('.gis-map-area');
		    if (!mapArea) return;

		    mapArea.appendChild(loadingOverlay);

		    // ✅ Añadido return
		    return fetch(`/api/clients/${clientId}/projects`)
		        .then(response => {
		            if (!response.ok) throw new Error('Error al cargar proyectos');
		            return response.json();
		        })
		        .then(projects => {
		            if (window.ProjectsModule?.fillClientProjects) {
		                ProjectsModule.fillClientProjects(projects);
		            }
		            return projects; // ✅ opcionalmente devuelve los datos
		        })
		        .catch(err => {
		            console.error('Error cargando proyectos:', err);
		            alert('Error al cargar proyectos: ' + err.message);
		        })
		        .finally(() => {
		            loadingOverlay.remove();
		        });
        },
        reloadClients: function() {
            const loadingOverlay = createLoadingOverlay();
            const mapArea = document.querySelector('.gis-map-area');
            if (!mapArea) return;
            
            mapArea.appendChild(loadingOverlay);
            
            setTimeout(() => {
                loadClientsFromAPI();
                loadingOverlay.remove();
            }, 800);
        }
    };
})();

window.GISDashboard = GISAdvanced;

// Inicialización al cargar el DOM
document.addEventListener('DOMContentLoaded', () => {
    // Exportar al ámbito global si es necesario
	GISAdvanced.init();
	NavigationModule.init();
});