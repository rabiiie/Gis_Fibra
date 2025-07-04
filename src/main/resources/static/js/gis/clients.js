const ClientsModule = (function () {
    const CLIENT_LOGO_MAP = {
        'Alpen': 'alpen.png',
        'Deutsche Glasfaser': 'dgf.png',
        'UGG': 'ugg.png',
        'GlasfaserPlus': 'glasfaserplus.png'
    };

    function renderClients(clients) {
        const container = document.getElementById('floatingCardsContainer');
        if (!container) return;

        window.cachedClients = clients;
        clearFloatingCards();

        if (history.state?.view !== 'clients') {
            history.pushState({ view: 'clients' }, '', '#clients');
        }

        clients.forEach(client => {
            const card = createClientCard(client);
            container.appendChild(card);
        });
    }

    function renderProjectsForClient(clientId) {
        clearFloatingCards();
        ProjectsModule.clearProjectView();
        showLoadingOverlay();

        GISDashboard.loadClientProjects(clientId)
            .then(projects => ProjectsModule.fillClientProjects(projects))
            .catch(err => alert("Error al cargar proyectos: " + err.message))
            .finally(removeLoadingOverlay);
    }

    function createClientCard(client) {
        const logoUrl = getClientLogoUrl(client.name);
        const stats = parseClientStats(client);

        const card = document.createElement('div');
        card.className = 'client-card';
        card.setAttribute('data-client-id', client.id);
        card.innerHTML = `
            <div class="client-header">
                <div class="client-logo">
                    <img src="${logoUrl}" alt="${client.name}" onerror="this.src='/images/default-logo.png'">
                </div>
                <div class="client-info">
                    <div class="client-name">${client.name}</div>
                    <div class="client-stats">
                        ${createStatItem('bi-rulers', `${stats.civilWorks.toLocaleString()} m`)}
                        ${createStatItem('bi-hdd-stack', stats.hps.toLocaleString())}
                        ${createStatItem('bi-lightning-charge', stats.activations.toLocaleString())}
                    </div>
                </div>
            </div>
        `;
        card.addEventListener('click', () => selectClient(client.id, card));
        return card;
    }

    function selectClient(clientId) {
        clearFloatingCards();
        NavigationModule.goToProjects(clientId);
    }

    function getClientLogoUrl(clientName) {
        const logoFile = Object.entries(CLIENT_LOGO_MAP).find(([name]) =>
            clientName.toLowerCase().includes(name.toLowerCase())
        )?.[1] || 'default-logo.png';
        return `/images/${logoFile}`;
    }

    function parseClientStats(client) {
        return {
            civilWorks: parseFloat(client.total_civil_works) || 0,
            hps: parseFloat(client.total_hps) || 0,
            activations: parseFloat(client.total_activations) || 0
        };
    }

    function createStatItem(iconClass, value) {
        return `
            <div class="stat-item">
                <i class="bi ${iconClass}"></i>
                <span class="stat-value">${value}</span>
            </div>
        `;
    }

    return {
        renderClients,
        renderProjectsForClient
    };
})();

window.ClientsModule = ClientsModule;
