const NavigationModule = (function () {
    let lastClientId = null;

	function init() {
	    // Procesar la URL inicial
	    const hash = window.location.hash;
		if (hash.startsWith('#projects-')) {
	        const clientId = hash.split('-')[1];
	        lastClientId = clientId;
	        renderProjectsWithoutPush(clientId);
	    } else if (hash === '#clients' || hash === '') {
	        history.replaceState({ view: 'clients' }, '', '#clients');
	    }
	    
	    window.addEventListener('popstate', handlePopState);
	}

    function renderProjectsWithoutPush(clientId) {
        clearFloatingCards();
        ProjectsModule.clearProjectView();
        showLoadingOverlay();
        GISDashboard.loadClientProjects(clientId)
            .then(projects => ProjectsModule.fillClientProjects(projects))
            .catch(err => alert("Error al cargar proyectos: " + err.message))
            .finally(removeLoadingOverlay);
    }

	function handlePopState(event) {
	    const state = event.state;
	    clearFloatingCards();
	    document.getElementById("layerPanel")?.classList.remove("active");

	    // Extraer clientId del hash si no hay state (caso retroceso)
	    const hash = window.location.hash;
	    if (!state && hash.startsWith('#projects-')) {
	        const clientId = hash.split('-')[1];
	        lastClientId = clientId;
	        renderProjectsWithoutPush(clientId);
	        return;
	    }

	    if (state?.view === 'layers') {
	        // Retroceso desde layers → projects
	        if (lastClientId != null) {
	            history.replaceState({ view: 'projects', clientId: lastClientId }, '', `#projects-${lastClientId}`);
	            renderProjectsWithoutPush(lastClientId);
	        }
	        return;
	    }

	    if (state?.view === 'projects' && state.clientId) {
	        lastClientId = state.clientId;
	        renderProjectsWithoutPush(state.clientId);
	        return;
	    }

	    // Cualquier otro caso (incluye view:'clients')
	    ProjectsModule.clearProjectView();
	    ClientsModule.renderClients(window.cachedClients || []);
	}

    function goToClients() {
        clearFloatingCards();
        ProjectsModule.clearProjectView();
        document.getElementById("layerPanel")?.classList.remove("active");

        history.pushState({ view: 'clients' }, '', '#clients');
        ClientsModule.renderClients(window.cachedClients || []);
    }

    function goToProjects(clientId) {
        lastClientId = clientId;
        clearFloatingCards();
        ProjectsModule.clearProjectView();
        document.getElementById("layerPanel")?.classList.remove("active");

        history.pushState({ view: 'projects', clientId }, '', `#projects-${clientId}`);
		GISDashboard.loadClientProjects(clientId)
		  .then(projects => {
		      projects.forEach(p => p.client_id = clientId); // ← Esto asegura que cada proyecto tenga el client_id
		      ProjectsModule.fillClientProjects(projects);
		  })
		  .catch(err => alert("Error al cargar proyectos: " + err.message))
		  .finally(removeLoadingOverlay);

    }

	function goToLayerPanel() {
	    const panel = document.getElementById("layerPanel");
	    if (panel && !panel.classList.contains("active")) {
	        panel.classList.add("active");

	        // Solo hacer pushState si no estamos ya en layers
	        if (history.state?.view !== 'layers') {
	            const hash = lastClientId ? `#layers-${lastClientId}` : '#layers';
	            history.pushState({ view: 'layers', clientId: lastClientId }, '', hash);
	        }
	    }
	}
	function getLastClientId() {
	    return lastClientId;
	}
    return {
        init,
        goToClients,
        goToProjects,
        goToLayerPanel,
		getLastClientId
    };
})();

window.NavigationModule = NavigationModule;

// Inicializa en DOMContentLoaded
document.addEventListener('DOMContentLoaded', () => {
    NavigationModule.init();
});
