const ProjectsModule = (function() {
    let currentGeoJsonLayer = null;
    let projectMarkers = null;  // LayerGroup para todos los marcadores

    function init() {
        // Crea el grupo al arrancar
        const map = GISDashboard.getMap();
        projectMarkers = L.layerGroup().addTo(map);
    }

	function createProjectCard(project) {
	    const div = document.createElement('div');
	    div.className = 'project-card';

	    const phaseColor = {
	        'Planning': '#ffc107',
	        'Construction': '#28a745',
	        'Completed': '#007bff',
	        '': '#6c757d'
	    };

	    const color = phaseColor[project.phase] || '#6c757d';

		const civilWorks = project.civilworks?.toLocaleString() ?? '0';
		const homes = project.hp?.toLocaleString() ?? '0';
		const contracts = project.total_contracts?.toLocaleString() ?? '0';


	    div.innerHTML = `
	        <div class="card-header" style="background:${color}">
	            ${project.phase || 'Sin fase'}
	        </div>
	        <div class="card-body">
	            <div class="card-title">${project.name}</div>
	            <div class="card-sub">${project.subcontractor || 'Sin subcontrata'}</div>
				<div class="card-metrics">
				    <div><i class="bi bi-rulers"></i> ${civilWorks} m</div>
				    <div><i class="bi bi-house-door"></i> ${homes} homes</div>
				    <div><i class="bi bi-file-earmark-text"></i> ${contracts} contratos</div>
				</div>

	        </div>
	    `;

		div.onclick = () => {
		    // Guardar el clientId en el estado de navegación
		    const clientId = project.client_id ?? NavigationModule.getLastClientId?.();
		    if (clientId) {
		        // Actualizar URL y estado antes de abrir layers
		        history.pushState({ view: 'projects', clientId }, '', `#projects-${clientId}`);
		        lastClientId = clientId;
		    }
		    
		    if (window.GISDashboard?.toggleLayerPanel) {
		        GISDashboard.toggleLayerPanel();
		    }
		};


	    return div;
	}


    function fillClientProjects(projects) {
		clearFloatingCards(); // Limpia proyectos anteriores
        const map = GISDashboard.getMap();

        // 1) Limpia marcadores anteriores
        projectMarkers.clearLayers();
		if (window.LayersModule?.clearBuildingsCluster) {
		        LayersModule.clearBuildingsCluster();
		    }
        // 2) Construye nuevos marcadores y bounds
        const bounds = L.latLngBounds([]);
        projects.forEach(project => {
            if (project.lat != null && project.lng != null) {
                const card = createProjectCard(project);
                const marker = L.marker([project.lat, project.lng], {
                    icon: L.divIcon({
                        className: 'project-map-wrapper',
                        html: card.outerHTML,
                        iconSize: [200, 80],
                        iconAnchor: [100, 40]
                    })
                });

                // 3) Al hacer click en el marcador:
                marker.on('click', () => {
                    // Limpia todos los cards
                    projectMarkers.clearLayers();
                    // Cierra panel lateral de clientes y abre el de capas
                    document.getElementById("sidePanel")?.classList.remove("active");
                    document.getElementById("layerPanel")?.classList.add("active");

                    // Carga el proyecto en el mapa
                    openProjectInMap(project.id);
                });

                projectMarkers.addLayer(marker);
                bounds.extend([project.lat, project.lng]);
            } else {
                console.warn(`Proyecto sin coordenadas: ${project.name}`);
            }
        });

        // 4) Centrar el mapa
		if (bounds.isValid()) {
		        map.flyToBounds(bounds, {
		            padding: [50, 50],
		            duration: 1.5,
		            easeLinearity: 0.25
		        });
		    }
    }
	function clearProjectView() {
	    const map = GISDashboard.getMap();

	    // 🔹 1. Limpia los markers flotantes
	    if (projectMarkers) projectMarkers.clearLayers();

	    // 🔹 2. Elimina la capa GeoJSON de civil works si está en el mapa
	    if (currentGeoJsonLayer && map.hasLayer(currentGeoJsonLayer)) {
	        map.removeLayer(currentGeoJsonLayer);
	        currentGeoJsonLayer = null;
	    }

	    // 🔹 3. Elimina la capa de buildings si LayersModule la tiene
	    if (LayersModule.clearBuildingsLayer) {
	        LayersModule.clearBuildingsLayer(); // Debes tener esta función
	    }
	}


	function openProjectInMap(projectId) {
	    const loadingOverlay = document.createElement('div');
	    loadingOverlay.className = 'loading-overlay';
	    loadingOverlay.innerHTML = '<div class="spinner"></div>';
	    document.querySelector('.gis-map-area').appendChild(loadingOverlay);

	    // Limpia capas anteriores (muy importante)
	    if (LayersModule.clearBuildingsLayer) LayersModule.clearBuildingsLayer();
	    if (ProjectsModule.clearProjectView) ProjectsModule.clearProjectView(); // opcional si no rompe

	    // Cargar Civil Works
	    LayersModule.loadCivilWorksLayer(projectId)
	        .then(layer => {
	            if (layer) {
	                currentGeoJsonLayer = layer;
	                GISDashboard.fitBounds(layer.getBounds());
	            }
	        })
	        .catch(err => {
	            console.error('Error al cargar civil works:', err);
	            alert('Error al cargar datos de civil works: ' + err.message);
	        });

	    // Cargar Buildings (forzado siempre)
	    LayersModule.loadBuildingsLayer(projectId)
	        .catch(err => {
	            console.error('Error al cargar buildings:', err);
	            alert('Error al cargar datos de buildings: ' + err.message);
	        })
	        .finally(() => {
	            loadingOverlay.remove();
	        });
	}


    // Exponer métodos
	return {
	    init,
	    fillClientProjects,
	    openProjectInMap,
	    clearProjectView
	};

})();

// Tras inicializar el mapa, arranca el módulo
document.addEventListener('DOMContentLoaded', () => {
    const waitForGISDashboard = setInterval(() => {
        if (window.GISDashboard?.getMap) {
            clearInterval(waitForGISDashboard);
            ProjectsModule.init();
        }
    }, 100);
});
window.ProjectsModule = ProjectsModule;
