const ProjectsModule = (function() {
    let currentGeoJsonLayer = null;
    
    function fillClientProjects(projects) {
        const container = document.getElementById('clientProjectsContainer');
        if (!container) return;
        
        container.innerHTML = '';
        
        projects.forEach(project => {
            const card = createProjectCard(project);
            container.appendChild(card);
        });
    }

    function createProjectCard(project) {
        const card = document.createElement('div');
        card.className = 'project-card';
        card.innerHTML = `
            <div class="project-name">${project.name}</div>
            <div class="project-country">${project.country || 'N/A'} | ${project.phase || 'N/A'}</div>
            <div class="project-stats" style="margin-top:8px;font-size:0.8rem;color:#a0c4e4">
                <div>Subcontrata: ${project.subcontractor || 'N/A'}</div>
            </div>
        `;
        
        card.addEventListener('click', () => openProjectInMap(project.id));
        return card;
    }

	function openProjectInMap(projectId) {
		    const loadingOverlay = document.createElement('div');
		    loadingOverlay.className = 'loading-overlay';
		    loadingOverlay.innerHTML = '<div class="spinner"></div>';
		    document.querySelector('.gis-map-area').appendChild(loadingOverlay);

		    // 🔽 Cerrar panel de clientes
		    document.getElementById("sidePanel")?.classList.remove("active");

		    // 🔼 Abrir panel de capas
		    document.getElementById("layerPanel")?.classList.add("active");

		    // Cargar capa de civil works
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

		    LayersModule.loadBuildingsLayer(projectId)
		        .catch(err => {
		            console.error('Error al cargar buildings:', err);
		            alert('Error al cargar datos de buildings: ' + err.message);
		        })
		        .finally(() => {
		            loadingOverlay.remove();
		        });
		}
    return {
        fillClientProjects,
        openProjectInMap
    };
})();

window.ProjectsModule = ProjectsModule;