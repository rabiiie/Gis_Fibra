LayersModule = (function() {
    let activeLayer = null;
	let buildingsLayer = null;
    let currentProjectId = null;
	let buildingsCluster = null;
	let catastroLayer = null;
	let catastroToggleButton = null;
	let currentFilters = {
	  type: [], village_pop: [], spec: [],
	  dp_name: [], building_status: [], has_class: [],street_name: []
	};

    let availableValues = {};
    
    // Obtener valores únicos para una propiedad
	function getUniqueValues(property) {
	    if (!activeLayer) return [];
	    
	    const layers = activeLayer.getLayers();
	    const values = new Set();
	    
	    layers.forEach(layer => {
	        const value = layer.feature?.properties[property];
	        // Manejar valores null, undefined o vacíos
	        values.add(value || 'OTHER');
	    });
	    
	    return Array.from(values).sort();
	}

    // Inicialización
    function init() {
        initEventListeners();
        initLayerControls();
		initCatastroLayer();
		updateInitialVisibilityStates();
        console.log("Módulo de capas inicializado");
    }

    // Configurar eventos
    function initEventListeners() {
        // Botones del panel
        document.getElementById("layersMenuBtn")?.addEventListener("click", toggleLayerPanel);
        document.getElementById("closeLayerPanelBtn")?.addEventListener("click", toggleLayerPanel);
        document.getElementById("applyLayerStyleBtn")?.addEventListener("click", applyLayerStyle);
        document.getElementById("applyFiltersBtn")?.addEventListener("click", applyFilters);

        
		document.getElementById("layerFilterTarget")?.addEventListener('change', function() {
		    const target = this.value;
		    const groupBySelect = document.getElementById("groupBySelect");
		    
		    // Vaciar opciones actuales
		    groupBySelect.innerHTML = '';
		    
		    if (target === "civil-works") {
		        ['village_pop','type','spec'].forEach(opt => {
		            const option = document.createElement('option');
		            option.value = opt;
		            option.textContent = opt.toUpperCase();
		            groupBySelect.appendChild(option);
		        });
		    } else if (target === "buildings") {
		        ['village_pop','dp_name','building_status','has_class', 'street_name'].forEach(opt => {
		            const option = document.createElement('option');
		            option.value = opt;
		            option.textContent = opt.replace(/_/g, ' ').toUpperCase();
		            groupBySelect.appendChild(option);
		        });
		    }
		    
		    loadAllFilterOptions();
		});
		}

    // Inicializar controles de capa
	function initLayerControls() {
	    const layersContainer = document.getElementById("layersListContainer");
	    if (!layersContainer) return;

	    layersContainer.innerHTML = `
	        <div class="layers-panel">
	            <div class="layers-header">CAPAS</div>
	            
	            <div class="layer-control">
	                <button class="layer-btn active" data-layer="civil-works">
	                    <i class="bi bi-eye-fill"></i>
	                    <span class="layer-name">Obras Civiles</span>
	                    <span class="layer-count">44</span>
	                </button>
	                
	                <button class="layer-btn active" data-layer="buildings">
	                    <i class="bi bi-eye-fill"></i>
	                    <span class="layer-name">Edificios</span>
	                    <span class="layer-count">27</span>
	                </button>
	                
	                <button class="layer-btn" data-layer="catastro">
	                    <i class="bi bi-eye-slash-fill"></i>
	                    <span class="layer-name">Catastro</span>
	                </button>
	            </div>
	        </div>
	    `;

	    // Event listeners
	    document.querySelectorAll('.layer-btn').forEach(btn => {
	        btn.addEventListener('click', function() {
	            const layerType = this.dataset.layer;
	            const isActive = this.classList.toggle('active');
	            const icon = this.querySelector('i');
	            
	            // Alternar iconos de visibilidad
	            icon.classList.toggle('bi-eye-fill', isActive);
	            icon.classList.toggle('bi-eye-slash-fill', !isActive);
	            
	            // Cambiar visibilidad en el mapa
	            toggleLayerVisibility(layerType);
	        });
	    });
	}

	// Nueva función para alternar visibilidad
	function toggleLayerVisibility(layerType) {
	    const map = GISDashboard.getMap();
	    
	    switch(layerType) {
	        case 'buildings':
	            if (buildingsCluster) {
	                if (map.hasLayer(buildingsCluster)) {
	                    map.removeLayer(buildingsCluster);
	                } else {
	                    map.addLayer(buildingsCluster);
	                }
	            }
	            break;
	            
	        case 'civil-works':
	            if (activeLayer) {
	                if (map.hasLayer(activeLayer)) {
	                    map.removeLayer(activeLayer);
	                } else {
	                    map.addLayer(activeLayer);
	                }
	            }
	            break;
	            
	        case 'catastro':
	            if (window.catastroLayer) {
	                if (map.hasLayer(window.catastroLayer)) {
	                    map.removeLayer(window.catastroLayer);
	                } else {
	                    map.addLayer(window.catastroLayer);
	                }
	            }
	            break;
	    }
	}

	// Función para actualizar el icono del ojo
	function updateVisibilityIcon(button, layerType) {
	    const map = GISDashboard.getMap();
	    const icon = button.querySelector('i');
	    let isVisible = false;
	    
	    switch(layerType) {
	        case 'buildings':
	            isVisible = buildingsCluster && map.hasLayer(buildingsCluster);
	            break;
	        case 'civil-works':
	            isVisible = activeLayer && map.hasLayer(activeLayer);
	            break;
	        case 'catastro':
	            isVisible = window.catastroLayer && map.hasLayer(window.catastroLayer);
	            break;
	    }
	    
	    if (isVisible) {
	        icon.classList.replace("bi-eye-slash-fill", "bi-eye-fill");
	    } else {
	        icon.classList.replace("bi-eye-fill", "bi-eye-slash-fill");
	    }
	}


    // Cargar todas las opciones de filtro disponibles
	function loadAllFilterOptions() {
	    const layerTarget = document.getElementById("layerFilterTarget")?.value || "civil-works";

	    // Ocultar y vaciar TODO por defecto
	    const allFilterContainers = document.querySelectorAll('.filter-content');
	    allFilterContainers.forEach(c => {
	        c.classList.add("d-none");
	        c.innerHTML = "";
	    });

	    availableValues = {};

	    // Para civil-works
	    if (layerTarget === "civil-works" && activeLayer) {
	        ['type', 'village_pop', 'spec','dp_name','street_name'].forEach(prop => {
	            const container = document.getElementById(`filterControls_${prop}`);
	            if (!container) return;

	            // Añadir soporte para valores nulos/vacíos como "OTHER"
	            let values = getUniqueValues(prop).map(v => v || 'OTHER');
	            availableValues[prop] = values;
	            updateFilterControls(prop);
	            container.classList.remove("d-none");
	        });
	    }

	    // Para buildings
	    else if (layerTarget === "buildings" && buildingsCluster) {
	        // Filtros directos
	        ['village_pop', 'dp_name'].forEach(prop => {
	            const container = document.getElementById(`filterControls_${prop}`);
	            if (!container) return;

				let values = getUniqueValues(prop).map(v => v || 'OTHER');
	            availableValues[prop] = values;
	            updateFilterControls(prop);
	            container.classList.remove("d-none");
	        });

	        // Filtros de homes_info
	        ['building_status', 'has_class'].forEach(prop => {
	            const container = document.getElementById(`filterControls_${prop}`);
	            if (!container) return;

	            let values = getUniqueValuesFromHomes(prop).map(v => v || 'OTHER');
	            availableValues[prop] = values;
	            updateFilterControls(prop);
	            container.classList.remove("d-none");
	        });
	    }
	}


	function getUniqueValuesFromHomes(property) {
	    if (!buildingsCluster) return [];
	    
	    const values = new Set();
	    buildingsCluster.getLayers().forEach(layer => {
	        const homes = layer.feature?.properties?.homes_info || [];
	        homes.forEach(home => {
	            // Manejar valores nulos o vacíos
	            values.add(home[property] || 'OTHER');
	        });
	    });
	    return Array.from(values).sort();
	}
	
	function updateLegend() {
	    const legendContainer = document.getElementById("mapLegend");
	    if (!legendContainer) return;

	    const groupBy = document.getElementById("groupBySelect")?.value || 'village_pop';
	    const layerTarget = document.getElementById("layerFilterTarget")?.value || "civil-works";

	    let html = '';

	    if (layerTarget === "civil-works" && activeLayer) {
	        // Obtener valores únicos VISIBLES después de filtrar
	        const visibleValues = new Set();
	        const totals = {};

	        activeLayer.eachLayer(layer => {
	            const props = layer.feature?.properties;
	            if (!props || !passesAllFilters(props, currentFilters)) return;

	            const value = props[groupBy] || 'OTHER';
	            visibleValues.add(value);

	            // Calcular longitud
	            const length = parseFloat(String(props.length_m).replace(',', '.')) || 0;
	            totals[value] = (totals[value] || 0) + length;
	        });

	        // Generar HTML de la leyenda
	        html = Array.from(visibleValues).sort().map(val => {
	            const color = getColorFromString(val);
	            const total = (totals[val] || 0).toFixed(1).replace('.', ',');
	            return `
	                <div class="legend-item">
	                    <span class="legend-color" style="background:${color}"></span>
	                    <span>${val} – ${total} m</span>
	                </div>
	            `;
	        }).join('');
	    }
	    else if (layerTarget === "buildings" && buildingsCluster) {
	        const groupFilters = currentFilters[groupBy];
	        const values = Array.isArray(groupFilters) && groupFilters.length > 0
	            ? groupFilters
	            : (availableValues[groupBy] || []);

	        const counts = {};

	        buildingsCluster.getLayers().forEach(layer => {
	            const props = layer.feature.properties;
	            const homes = props.homes_info || [];

	            let match = false;

	            if (groupBy === 'building_status' || groupBy === 'has_class') {
	                for (const h of homes) {
	                    if (!Array.isArray(groupFilters) || groupFilters.length === 0 || groupFilters.includes(h[groupBy])) {
	                        counts[h[groupBy]] = (counts[h[groupBy]] || 0) + 1;
	                        match = true;
	                    }
	                }
	            } else {
	                const val = props[groupBy] || 'OTHER';
	                if (!Array.isArray(groupFilters) || groupFilters.length === 0 || groupFilters.includes(val)) {
	                    counts[val] = (counts[val] || 0) + 1;
	                    match = true;
	                }
	            }
	        });

	        html = values.map(val => {
	            const color = getColorFromString(val);
	            const count = counts[val] || 0;
	            return `
	                <div class="legend-item">
	                    <span class="legend-color" style="background:${color}"></span>
	                    <span>${val} – ${count} edificios</span>
	                </div>
	            `;
	        }).join('') || "<span>Sin filtros aplicados</span>";
	    }

	    legendContainer.innerHTML = html;
	}




	function computeLengthByCategory(layer, groupByKey = 'spec', lengthField = 'length_m', filters = currentFilters) {
	    const totals = {};

	    if (!layer) return totals;

	    layer.eachLayer(l => {
	        const props = l.feature?.properties;
	        if (!props) return;

	        // ✅ Solo contar si pasa todos los filtros activos
	        if (!passesAllFilters(props, filters)) return;

	        const key = props[groupByKey] || 'Undefined';

	        let raw = props[lengthField];
	        if (typeof raw === 'string') {
	            raw = raw.replace(',', '.');
	        }

	        const length = parseFloat(raw) || 0;

	        if (!totals[key]) totals[key] = 0;
	        totals[key] += length;
	    });

	    return totals;
	}

	function updateMapVisibility() {
	    const map = GISDashboard.getMap(); // ✅ asegurarse de obtener el mapa
	    const layerTarget = document.getElementById("layerFilterTarget")?.value || "civil-works";

		if (layerTarget === "buildings" && buildingsCluster) {
		    buildingsCluster.eachLayer(layer => {
		        const visible = checkFeatureVisibility(layer.feature, currentFilters, layerTarget);
		        if (visible) {
		            layer.setStyle({ opacity: 1, fillOpacity: 0.5 });
		            layer.setRadius(8);  // opcional: tamaño mayor si visible
		        } else {
		            layer.setStyle({ opacity: 0.1, fillOpacity: 0.05 });
		            layer.setRadius(4);  // opcional: tamaño menor si oculto
		        }
		    });
		}


	    if (layerTarget === "civil-works" && activeLayer) {
	        activeLayer.eachLayer(layer => {
	            const visible = checkFeatureVisibility(layer.feature, currentFilters, layerTarget);
	            layer.setStyle({
	                opacity: visible ? 1 : 0.1,
	                fillOpacity: visible ? 0.7 : 0.05
	            });
	        });
	    }

	    updateLegendCounters();
	}


	function updateLegendCounters() {
	    if (!buildingsCluster) return;
	    const map = GISDashboard.getMap();

	    const elHAS0 = document.querySelector(".legend-has0 .legend-count");
	    const elHAS1 = document.querySelector(".legend-has1 .legend-count");
	    if (!elHAS0 || !elHAS1) return; // salir si no están

	    let countHAS0 = 0;
	    let countHAS1 = 0;

	    buildingsCluster.eachLayer(layer => {
	        if (!map.hasLayer(layer)) return;

	        const homes = layer.feature.properties.homes_info || [];

	        for (const h of homes) {
	            if (h.has_class === 'HAS0') countHAS0++;
	            else if (h.has_class === 'HAS1') countHAS1++;
	        }
	    });

	    elHAS0.textContent = `${countHAS0} edificios`;
	    elHAS1.textContent = `${countHAS1} edificios`;
	}

	function updateFilterCount(property) {
	    const checkboxes = document.querySelectorAll(`#filterControls_${property} input[type="checkbox"]:not(.select-all-check)`);
	    const checked = Array.from(checkboxes).filter(cb => cb.checked).length;
	    const total = checkboxes.length;

	    const badge = document.getElementById(`count_${property}`);
	    if (badge) badge.textContent = `${checked}/${total}`;
	}


	function updateFilterControls(property) {
	    const filterContainer = document.getElementById(`filterControls_${property}`);
	    if (!filterContainer) {
	        console.error(`Contenedor de filtro no encontrado: filterControls_${property}`);
	        return;
	    }

	    const values = Array.isArray(availableValues[property]) ? availableValues[property] : [];

	    // ✅ Evitar errores si no está inicializado
	    if (!Array.isArray(currentFilters[property])) {
	        currentFilters[property] = [];
	    }

	    const allChecked = currentFilters[property].length === 0 || currentFilters[property].length === values.length;

	    let html = `
	        <div class="filter-header-inner">
	            <label class="select-all-label">
	                <input type="checkbox" class="select-all-check" 
	                       ${allChecked ? 'checked' : ''} data-prop="${property}">
	                <span>Todos</span>
	            </label>
	        </div>`;

	    values.forEach(value => {
	        const isActive = currentFilters[property].length === 0 || currentFilters[property].includes(value);
	        html += `
	        <div class="form-check">
	            <input class="form-check-input" type="checkbox" 
	                   id="filter_${property}_${value.replace(/\W+/g, '_')}" 
	                   value="${value}" ${isActive ? 'checked' : ''}
	                   data-prop="${property}">
	            <label class="form-check-label" for="filter_${property}_${value.replace(/\W+/g, '_')}">
	                ${value}
	            </label>
	        </div>`;
	    });

	    filterContainer.innerHTML = html;

	    // Evento para el checkbox "Todos"
	    const selectAll = filterContainer.querySelector('.select-all-check');
		selectAll?.addEventListener('change', function () {
		    const checkboxes = filterContainer.querySelectorAll(`input[type="checkbox"]:not(.select-all-check)`);
		    checkboxes.forEach(cb => cb.checked = this.checked);

		    if (this.checked) {
		        currentFilters[property] = [];  // Significa "todos"
		    } else {
		        currentFilters[property] = [];  // Ninguno seleccionado
		    }

		    applyFilters();
		});


	    // Evento para checkboxes individuales
	    const individualCheckboxes = filterContainer.querySelectorAll(`input[type="checkbox"]:not(.select-all-check)`);
	    individualCheckboxes.forEach(cb => {
	        cb.addEventListener('change', function () {
	            const selected = Array.from(individualCheckboxes)
	                .filter(cb => cb.checked)
	                .map(cb => cb.value);

	            currentFilters[property] = selected.length === values.length ? [] : selected;

	            // Actualiza el estado del checkbox "Todos"
	            selectAll.checked = selected.length === values.length;

	            updateMapVisibility(); // aplica filtros a los elementos del mapa
	            updateFilterCount(property);
				applyFilters(); // aplica filtros a la capa activa
	        });
	    });

		updateMapVisibility(); // aplica filtros a los elementos del mapa
	    updateFilterCount(property);
		applyFilters(); // aplica filtros a la capa activa
	}

	function applyFilters() {
	    const layerTarget = document.getElementById("layerFilterTarget")?.value || "civil-works";
	    const groupBy = document.getElementById("groupBySelect")?.value || 
	                   (layerTarget === "civil-works" ? 'type' : 'village_pop');

	    // 1. Aplicar estilos según el tipo de capa
	    if (layerTarget === "civil-works" && activeLayer) {
	        activeLayer.setStyle(feature => {
	            const isVisible = passesAllFilters(feature.properties, currentFilters);
	            return {
	                ...getLayerStyle(feature),
	                opacity: isVisible ? 1 : 0.2,
	                fillOpacity: isVisible ? 0.8 : 0.1,
	                weight: isVisible ? 3 : 1
	            };
	        });
	    } 
	    else if (layerTarget === "buildings" && buildingsCluster) {
	        applyBuildingsStyle(groupBy);
	    }

	    // 2. Forzar actualización de la leyenda
	    updateLegend();

	    // 3. Actualizar contadores (solo para buildings)
	    if (layerTarget === "buildings") {
	        updateLegendCounters();
	    }
	}
	// Funciones auxiliares
	function resetCurrentFilters(layerTarget) {
	    const civilWorksFilters = ['type', 'village_pop', 'spec', 'dp_name', 'street_name'];
	    const buildingsFilters = ['village_pop', 'dp_name', 'building_status', 'has_class'];
	    
	    const filtersToReset = layerTarget === "civil-works" ? civilWorksFilters : buildingsFilters;
	    
	    filtersToReset.forEach(filter => {
	        currentFilters[filter] = [];
	    });
	}

	function collectActiveFilters() {
	  const layerTarget = document.getElementById("layerFilterTarget")?.value || "civil-works";

	  const selectors = document.querySelectorAll(`.filter-group[data-layer="${layerTarget}"]`);
	  selectors.forEach(group => {
	    const key = group.dataset.filterKey;
	    if (!key) return;

	    const checkedValues = Array.from(group.querySelectorAll('input[type="checkbox"]:checked'))
	                               .map(cb => cb.value);
	    currentFilters[key] = checkedValues;
	  });
	}


	function applyCivilWorksStyle(groupBy) {
	    activeLayer.setStyle(feature => {
	        const isVisible = checkFeatureVisibility(feature, currentFilters, "civil-works");
	        
	        return {
	            ...getLayerStyle(feature),
	            opacity: isVisible ? 1 : 0.2,
	            fillOpacity: isVisible ? 0.8 : 0.1,
	            weight: isVisible ? 3 : 1
	        };
	    });
	}

	function applyBuildingsStyle(groupBy) {
	  if (!window.baseBuildingsGeojson || !buildingsCluster) return;

	  const map = GISDashboard.getMap();

	  // Eliminar todos los subcapas del cluster
	  buildingsCluster.clearLayers();

	  // Crear capa filtrada
	  const filteredLayer = L.geoJSON(window.baseBuildingsGeojson, {
	    filter: feature => checkFeatureVisibility(feature, currentFilters, "buildings"),

	    pointToLayer: (feature, latlng) => {
	      let value;

	      const homes = feature.properties?.homes_info || [];

	      if (['building_status', 'has_class'].includes(groupBy)) {
	        value = homes.find(h => h[groupBy])?.[groupBy] || 'OTHER';
	      } else {
	        value = feature.properties?.[groupBy] || 'OTHER';
	      }

	      const color = getColorFromString(value);
	      const zoom = map.getZoom();
	      const radius = zoom >= 17 ? 6 : zoom >= 15 ? 4 : 2;

	      return L.circleMarker(latlng, {
	        radius: radius,
	        fillColor: color,
	        color: "#000",
	        weight: 1,
	        opacity: 1,
	        fillOpacity: 0.85
	      });
	    },

	    onEachFeature: bindPopup
	  });

	  // Añadir al cluster solo los visibles
	  buildingsCluster.addLayer(filteredLayer);

	  // Asegurarse de que está en el mapa
	  if (!map.hasLayer(buildingsCluster)) {
	    map.addLayer(buildingsCluster);
	  }

	  // Actualizar leyenda y contadores
	  updateLegend();
	  updateLegendCounters();
	}


	function checkFeatureVisibility(feature, filters, layerTarget) {
	    const props = feature.properties;
	    
	    if (layerTarget === "civil-works") {
	        return ['type', 'village_pop', 'spec', 'dp_name', 'street_name'].every(prop =>
	            Array.isArray(filters[prop]) &&
	            (filters[prop].length === 0 || filters[prop].includes(props[prop]))
	        );
	    }

	    if (layerTarget === "buildings") {
	        const directPass = ['village_pop', 'dp_name'].every(prop =>
	            Array.isArray(filters[prop]) &&
	            (filters[prop].length === 0 || filters[prop].includes(props[prop]))
	        );
	        
	        if (!directPass) return false;

	        const homes = props.homes_info || [];
	        return ['building_status', 'has_class'].every(prop =>
	            Array.isArray(filters[prop]) &&
	            (filters[prop].length === 0 ||
	             homes.some(home => home[prop] && filters[prop].includes(home[prop])))
	        );
	    }

	    return true;
	}
	
	function updateInitialVisibilityStates() {
	    const map = GISDashboard.getMap();
	    
	    document.querySelectorAll('.layer-visibility-btn').forEach(btn => {
	        const layerType = btn.dataset.layer;
	        const icon = btn.querySelector('i');
	        
	        let isVisible = false;
	        switch(layerType) {
	            case 'buildings':
	                isVisible = buildingsCluster && map.hasLayer(buildingsCluster);
	                break;
	            case 'civil-works':
	                isVisible = activeLayer && map.hasLayer(activeLayer);
	                break;
	            case 'catastro':
	                isVisible = window.catastroLayer && map.hasLayer(window.catastroLayer);
	                break;
	        }
	        
	        if (isVisible) {
	            icon.classList.replace("bi-eye-slash-fill", "bi-eye-fill");
	        } else {
	            icon.classList.replace("bi-eye-fill", "bi-eye-slash-fill");
	        }
	    });
	}

	
	function passesAllFilters(props, filters) {
	    const layerTarget = document.getElementById("layerFilterTarget")?.value || "civil-works";
	    
	    if (layerTarget === "civil-works") {
	        return ['type', 'village_pop', 'spec', 'dp_name', 'street_name'].every(prop => 
	            !filters[prop] || filters[prop].length === 0 || filters[prop].includes(props[prop])
	        );
	    }
	    
	    if (layerTarget === "buildings") {
	        // Filtros directos
	        const directPass = ['village_pop', 'dp_name'].every(prop => 
	            !filters[prop] || filters[prop].length === 0 || filters[prop].includes(props[prop])
	        );
	        
	        if (!directPass) return false;
	        
	        // Filtros de homes_info
	        const homes = props.homes_info || [];
	        return ['building_status', 'has_class'].every(prop => 
	            !filters[prop] || filters[prop].length === 0 || 
	            homes.some(home => home[prop] && filters[prop].includes(home[prop]))
	        );
	    }
	    
	    return true;
	}
	
    // Mostrar/ocultar panel
    function toggleLayerPanel() {
        const panel = document.getElementById("layerPanel");
        panel?.classList.toggle("active");
        console.log("Panel de capas toggled", panel?.classList.contains("active"));
    }
	
	function toggleFilterSection(property) {
	    const contentId = `filterControls_${property}`;
	    const content = document.getElementById(contentId);
	    
	    if (!content) {
	        console.error(`Elemento no encontrado: #${contentId}`);
	        return;
	    }
	    
	    content.classList.toggle("collapsed");
	    console.log(`Sección ${property} toggled. Estado collapsed:`, content.classList.contains("collapsed"));
	}


    // Cargar capa GeoJSON
    async function loadCivilWorksLayer(projectId, filters = {}) {
        try {
            currentProjectId = projectId;
            
            const url = new URL('/api/civil-works/geojson', window.location.origin);
            url.searchParams.append('projectId', projectId);
            
            // Añadir filtros
            Object.entries(filters).forEach(([key, value]) => {
                if (value) url.searchParams.append(key, value);
            });

            const response = await fetch(url);
            if (!response.ok) throw new Error('Error al cargar datos');
            
            const data = await response.json();
            const geojson = typeof data.geojson === 'string' ? 
                JSON.parse(data.geojson) : data.geojson;

            // Eliminar capa anterior
            if (activeLayer) {
                GISDashboard.removeGeoJsonLayer(activeLayer);
            }

            // Crear nueva capa
            activeLayer = GISDashboard.addGeoJsonLayer(geojson, {
                style: getLayerStyle,
                onEachFeature: bindPopup
            });

            // Ajustar vista del mapa
            GISDashboard.fitBounds(activeLayer.getBounds());
            
            // Resetear filtros
            currentFilters = {
                type: [],
                village_pop: [],
                spec: [],
				dp_name: [],
				street_name: []
            };
			await loadAllFilterOptions();
			applyFilters();
            
            return activeLayer;
        } catch (error) {
            console.error("Error al cargar capa:", error);
            throw error;
        }
		
    }
	
	async function loadBuildingsLayer(projectId, filters = {}) {
	  try {
	    currentProjectId = projectId;

	    const url = new URL('/api/buildings/geojson', window.location.origin);
	    url.searchParams.append('projectId', projectId);
	    Object.entries(filters).forEach(([key, value]) => {
	      if (value) url.searchParams.append(key, value);
	    });

	    const response = await fetch(url);
	    if (!response.ok) throw new Error('Error al cargar datos');

	    const data = await response.json();
	    const geojson = typeof data.geojson === 'string' ? JSON.parse(data.geojson) : data.geojson;

	    // Eliminar capa anterior si existe
	    if (buildingsCluster) {
	      GISDashboard.getMap().removeLayer(buildingsCluster);
	    }

	    // Crear grupo de clusters
	    buildingsCluster = L.markerClusterGroup({
	      showCoverageOnHover: false,
	      maxClusterRadius: 40
	    });

	    // Crear capa GeoJSON de puntos
	    const geojsonLayer = L.geoJSON(geojson, {
	      pointToLayer: (feature, latlng) => L.circleMarker(latlng, getLayerStyle(feature)),
	      onEachFeature: bindPopup
	    });

	    buildingsCluster.addLayer(geojsonLayer);
	    GISDashboard.getMap().addLayer(buildingsCluster);
		// 🔄 Actualizar estilo de los puntos al cambiar el zoom
		GISDashboard.getMap().on("zoomend", () => {
		  if (!buildingsCluster) return;

		  const groupBy = document.getElementById("groupBySelect")?.value || 'village_pop';

		  buildingsCluster.eachLayer(layer => {
		    const feature = layer.feature;
		    const isVisible = checkFeatureVisibility(feature, currentFilters, "buildings");

		    const homes = feature.properties?.homes_info || [];
		    let value;
		    if (['building_status', 'has_class'].includes(groupBy)) {
		      value = homes.find(h => h[groupBy])?.[groupBy] || 'OTHER';
		    } else {
		      value = feature.properties?.[groupBy] || 'OTHER';
		    }

		    const color = getColorFromString(value);

		    layer.setStyle({
		      fillColor: color,
		      color: "#000",
		      weight: isVisible ? 2 : 0.5,
		      opacity: isVisible ? 1 : 0,
		      fillOpacity: isVisible ? 0.85 : 0
		    });

		    if (layer.setRadius) {
		      layer.setRadius(isVisible ? 8 : 0);  // 0 = ocultar completamente
		    }

		    if (layer._path) {
		      layer._path.setAttribute('pointer-events', isVisible ? 'auto' : 'none');
		    }
		  });

		  updateLegend();
		  updateLegendCounters();
		});

	    // Ajustar vista
	    if (geojsonLayer.getBounds().isValid()) {
	      GISDashboard.fitBounds(geojsonLayer.getBounds());
	    }

	    // Cargar filtros y aplicar estilos
	    currentFilters = { village_pop: [], dp_name: [], building_status: [], has_class: [] };
	    await loadAllFilterOptions();
	    const groupBy = document.getElementById("groupBySelect")?.value || 'village_pop';
	    applyBuildingsStyle(groupBy);
	    updateLegendCounters();
		window.baseBuildingsGeojson = geojson;  // Guardamos los datos base para reutilizarlos


	  } catch (err) {
	    console.error("Error al cargar capa buildings:", err);
	  }
	}
	
	function clearBuildingsLayer() {
	  if (buildingsCluster) {
	    GISDashboard.getMap().removeLayer(buildingsCluster);
	    buildingsCluster = null;
	  }
	}



	function getLayerStyle(feature) {
	    const groupBy = document.getElementById("groupBySelect")?.value || 'village_pop';
	    const layerTarget = document.getElementById("layerFilterTarget")?.value || "civil-works";
	    
		if (layerTarget === "buildings" && feature.geometry.type === 'Point') {
		    const homes = feature.properties.homes_info || [];
		    const status = homes.find(h => h.building_status)?.building_status || 'undefined';
		    const zoom = GISDashboard.getMap().getZoom();
		    const radius = zoom >= 17 ? 6 : zoom >= 15 ? 4 : 2;

		    return {
		        radius,
		        fillColor: getStatusColor(status),
		        color: '#333',
		        weight: 1,
		        opacity: 0.8,
		        fillOpacity: 0.4,
		        interactive: true
		    };
		}

	    
	    // Para Civil Works (líneas/polígonos)
	    const value = feature.properties[groupBy] || 'default';
	    const featureType = feature.properties.type || feature.geometry.type;
	    
	    const baseStyle = {
	        weight: 8,
	        opacity: 0,
	        fillOpacity: 0,
	        color: '#ffffff00',
	        interactive: true,
	        className: 'click-buffer'
	    };

	    const visibleLineStyle = {
	        weight: 3,
	        color: getColorFromString(value),
	        dashArray: featureType === 'LineString' ? '0' : null,
	        opacity: 1,
	        fillOpacity: 0.8
	    };

	    return {
	        ...baseStyle,
	        ...visibleLineStyle
	    };
	}
	
	function getStatusColor(status) {
	    const map = {
	        "planning": "#ffc107",
	        "construction": "#007bff",
	        "completed": "#28a745",
	        "undefined": "#6c757d",
			"Obstruction": "#dc3545",
			"Open": "#17a2b8",
	    };

	    return map[status?.toLowerCase()] || "#6c757d";
	}



	function getColorFromString(str) {
	    if (!str || typeof str !== 'string') return '#777777';

	    // Normalizar para quitar acentos y caracteres raros
	    str = str.normalize("NFD").replace(/[\u0300-\u036f]/g, "") // elimina acentos
	             .replace(/ß/g, 'ss')                               // reemplaza ß
	             .replace(/[^a-zA-Z0-9_-]/g, '');                   // elimina símbolos raros

	    const palette = [
	        "#FF5733", "#33FF57", "#3357FF", "#F3FF33", "#FF33F3",
	        "#33FFF3", "#8A2BE2", "#FF8C00", "#00CED1", "#FFD700",
	        "#7CFC00", "#FF4500", "#9370DB", "#00FA9A", "#FF6347",
	        "#40E0D0", "#FF69B4", "#1E90FF", "#FFA07A", "#20B2AA"
	    ];

	    let hash = 0;
	    for (let i = 0; i < str.length; i++) {
	        hash = str.charCodeAt(i) * (i + 1) + ((hash << 5) - hash);
	    }

	    const index = Math.abs(hash) % palette.length;
	    return palette[index];
	}


	function bindPopup(feature, layer) {
	    const props = feature.properties;

	    if (feature.geometry.type === 'Point') {
	        // Popup para edificios (Buildings)
	        let content = `<div class="popup-info">`;
	        content += `<h4>Building: ${props.building_id || 'N/A'}</h4>`;
	        content += `<ul>`;
	        content += `<li><strong>Street:</strong> ${props.street_name || '-'}</li>`;
	        content += `<li><strong>DP:</strong> ${props.dp_name || '-'}</li>`;
	        content += `<li><strong>village_pop:</strong> ${props.village_pop || '-'}</li>`;
	        content += `<li><strong>Total Homes:</strong> ${props.total_homes || 0}</li>`;
	        content += `</ul>`;

	        if (props.homes_info && Array.isArray(props.homes_info)) {
	            content += `<hr><h5>Homes Info:</h5>`;
	            props.homes_info.forEach((home, index) => {
	                content += `<p><strong>Home ${index + 1}: ${home.home_id || 'N/A'}</strong></p>`;
	                content += `<ul>`;
	                content += `<li><strong>Anschlussstatus:</strong> ${home.anschlussstatus || '-'}</li>`;
	                content += `<li><strong>Aufgabenstatus:</strong> ${home.aufgabenstatus || '-'}</li>`;
	                content += `<li><strong>building Status:</strong> ${home.building_status || '-'}</li>`;
	                content += `<li><strong>HAS Class:</strong> ${home.has_class || '-'}</li>`;
	                content += `<li><strong>PHA:</strong> ${home.pha ? 'Yes' : 'No'}</li>`;
	                content += `</ul>`;
	            });
	        }

	        content += `</div>`;
	        layer.bindPopup(content);
	    } else {
	        // Popup para Civil Works
	        let content = `<div class="popup-info">`;
	        content += `<h4>Civil Work</h4>`;
	        content += `<ul>`;
	        content += `<li><strong>Type:</strong> ${props.type || '-'}</li>`;
	        content += `<li><strong>village_pop:</strong> ${props.village_pop || '-'}</li>`;
	        content += `<li><strong>Spec:</strong> ${props.spec || '-'}</li>`;
	        content += `<li><strong>Status:</strong> ${props.status || '-'}</li>`;
	        content += `<li><strong>Length:</strong> ${props.length_m || '-'} m</li>`;
	        content += `<li><strong>Start Date:</strong> ${props.start_date || '-'}</li>`;
	        content += `<li><strong>End Date:</strong> ${props.end_date || '-'}</li>`;
	        content += `<li><strong>Contractor:</strong> ${props.contractor || '-'}</li>`;
	        content += `<li><strong>Project ID:</strong> ${props.project_id || '-'}</li>`;
	        content += `<li><strong>Comments:</strong> ${props.comments || '-'}</li>`;
	        content += `</ul>`;
	        
	        // Mostrar todos los campos adicionales que no son los principales
	        const excludedFields = ['type', 'village_pop', 'spec', 'status', 'length_m', 
	                              'start_date', 'end_date', 'contractor', 'project_id', 'comments'];
	        
	        const additionalFields = Object.keys(props)
	            .filter(key => !excludedFields.includes(key) && props[key] !== undefined && props[key] !== null);
	        
	        if (additionalFields.length > 0) {
	            content += `<hr><h5>Additional Info:</h5><ul>`;
	            additionalFields.forEach(field => {
	                content += `<li><strong>${field}:</strong> ${props[field]}</li>`;
	            });
	            content += `</ul>`;
	        }
	        
	        content += `</div>`;
	        layer.bindPopup(content);
	    }

	    // Estilo hover solo para geometrías no puntuales
	    if (feature.geometry.type !== 'Point') {
	        layer.on('mouseover', function() {
	            this.setStyle({
	                weight: 5,
	                color: '#ff9800',
	                fillColor: '#ffcc00',
	                fillOpacity: 1
	            });
	            this.bringToFront();
	        });

	        layer.on('mouseout', function() {
	            this.setStyle(getLayerStyle(feature));
	        });
	    }
	}

    // Aplicar estilo
	function applyLayerStyle() {
	  const map = GISDashboard.getMap();

	  // 1. Reaplica estilo a civil-works si está activa
	  if (activeLayer) {
	    activeLayer.setStyle(getLayerStyle);
	  }

	  if (buildingsCluster) {
	    // Mantén el mismo groupBy que uses en filtros
	    const groupBy = document.getElementById("groupBySelect")?.value || 'village_pop';
	    applyBuildingsStyle(groupBy);
	  }

	  // 3. Refresca contadores/leyenda para buildings si lo necesitas
	  updateLegend();
	  updateLegendCounters();

	  console.log("Estilo de capa aplicado a ambas capas");
	}
	
	function initCatastroLayer() {
	    // Prevent duplicate initialization
	    if (window.catastroInitialized) return;
	    window.catastroInitialized = true;

	    const waitForMap = setInterval(() => {
	        if (window.GISDashboard?.getMap) {
	            clearInterval(waitForMap);
	            const map = GISDashboard.getMap();

	            // Create WMS layer with correct parameters for Flurstücke
	            if (!window.catastroLayer) {
					window.catastroLayer = L.tileLayer.wms('https://www.wms.nrw.de/geobasis/wms_nw_alkis', {
					    layers: 'adv_alkis_flurstuecke',
					    format: 'image/png',
					    transparent: true,
					    version: '1.3.0',
					    crs: L.CRS.EPSG25832,
					    attribution: '© Geobasis NRW',
					    tileSize: 512,
					    maxZoom: 22,
					    maxNativeZoom: 19,
					    dpiMode: 7,                         // Para activar DPI alto
					    uppercase: true,                   // Por compatibilidad con algunos servidores
					    // Estos dos parámetros extra marcan diferencia
					    detectRetina: true,                // 📌 fuerza tiles de alta resolución en pantallas retina
					    requestEncoding: 'KVP'            // Asegura que las peticiones se hagan por clave-valor
					});

	            }

	            // Create control button if it doesn't exist
	            if (!window.catastroControl) {
	                const toggleControl = L.control({ position: 'topleft' });

					toggleControl.onAdd = function() {
					    const div = L.DomUtil.create('div', 'leaflet-bar leaflet-control leaflet-control-custom');
					    
					    div.innerHTML = `
					        <button id="toggleCatastroBtn" title="Mostrar/Ocultar parcelas (Kataster NRW)" class="catastro-btn-dark">
					            <i class="bi bi-layers"></i>
					        </button>
					    `;

					    div.onclick = () => {
					        const btn = document.getElementById('toggleCatastroBtn');
					        if (map.hasLayer(window.catastroLayer)) {
					            map.removeLayer(window.catastroLayer);
					            btn.classList.remove("active");
					        } else {
					            map.addLayer(window.catastroLayer);
					            btn.classList.add("active");
					        }
					    };

					    return div;
					};



	                window.catastroControl = toggleControl;
	                toggleControl.addTo(map);
	            }
	        }
	    }, 100);
	}

    // API pública
    return {
        init,
        loadCivilWorksLayer,
		loadBuildingsLayer,
		clearBuildingsLayer,
        applyLayerStyle,
		toggleFilterSection,
		toggleLayerVisibility: function(layerType) {
		    const btn = document.querySelector(`.layer-visibility-btn[data-layer="${layerType}"]`);
		    if (btn) {
		        btn.click();
		    }
		},
        changeLayerOpacity: function(value) {
            if (activeLayer) {
                activeLayer.setStyle({
                    opacity: value,
                    fillOpacity: value * 0.7
                });
            }
        }
    };
})();


// Inicialización
document.addEventListener("DOMContentLoaded", () => {
    const waitForGISDashboard = setInterval(() => {
        if (window.GISDashboard?.getMap) {
            clearInterval(waitForGISDashboard);
            LayersModule.init();
        }
    }, 100);
});


window.LayersModule = LayersModule;