// =============================================================================
// MAIN DASHBOARD JAVASCRIPT
// =============================================================================

// Global variables
let overviewMap;
let geoLayer;

// =============================================================================
// 1. DOM CONTENT LOADED EVENT
// =============================================================================
document.addEventListener("DOMContentLoaded", function () {
    initializeApp();
});

// =============================================================================
// 2. APP INITIALIZATION
// =============================================================================
function initializeApp() {
    initializeNavigation();
    initializeChat();
    initializeContactForm();
    initializeClientSearch();
    initializeMapComponents();
    initializeAPIButtons();
    initializeHomeDashboard();

    // Load initial view (Home)
    showHomeSection();
}

// =============================================================================
// 3. NAVIGATION SYSTEM
// =============================================================================
function initializeNavigation() {
    // Get navigation elements
    const navHome = document.getElementById('nav-home');
    const navClients = document.getElementById('nav-clients');
    const navOverviewMap = document.getElementById('nav-overviewMap');
    const navDashboard = document.getElementById('nav-dashboard');
    const navContact = document.getElementById('nav-contact');
    const navHelp = document.getElementById('nav-help');
    const navApi = document.getElementById('nav-api');

    // Add event listeners
    if (navHome) navHome.addEventListener('click', showHomeSection);
    if (navClients) navClients.addEventListener('click', showClientsSection);
    if (navOverviewMap) navOverviewMap.addEventListener('click', showOverviewMapSection);
    if (navDashboard) navDashboard.addEventListener('click', showDashboardSection);
    if (navContact) navContact.addEventListener('click', showContactSection);
    if (navHelp) navHelp.addEventListener('click', showHelpSection);
    if (navApi) navApi.addEventListener('click', showApiSection);

    // Initialize submenu toggle
    initializeSubmenuToggle();
}

function initializeSubmenuToggle() {
    const navApi = document.getElementById('nav-api');
    const apiSubmenu = document.getElementById('apiSubmenu');

    if (navApi && apiSubmenu) {
        navApi.addEventListener('click', function (e) {
            e.preventDefault();
            this.classList.toggle('collapsed');
            apiSubmenu.classList.toggle('show');
        });
    }
}

function resetSidebarActive() {
    const navItems = [
        'nav-home', 'nav-clients', 'nav-overviewMap',
        'nav-dashboard', 'nav-contact', 'nav-help', 'nav-api'
    ];

    navItems.forEach(id => {
        const element = document.getElementById(id);
        if (element) element.classList.remove('active');
    });
}

function hideAllSections() {
    const sections = [
        'homeSection', 'clientsSection', 'overviewMapSection',
        'dashboardSection', 'civilWorksSection', 'contactSection',
        'helpSection', 'projectDetailsSection', 'clientProjectsSection',
        'apiButtonsSection'
    ];

    sections.forEach(id => {
        const element = document.getElementById(id);
        if (element) element.classList.add('hidden');
    });
}

// =============================================================================
// 4. SECTION DISPLAY FUNCTIONS
// =============================================================================
function showHomeSection() {
    resetSidebarActive();
    const navHome = document.getElementById('nav-home');
    if (navHome) navHome.classList.add('active');

    hideAllSections();
    const homeSection = document.getElementById('homeSection');
    if (homeSection) homeSection.classList.remove('hidden');

    // Refresh map and load dashboard data
    setTimeout(() => {
        const homeMap = window.homeMap;
        if (homeMap) homeMap.invalidateSize();
        loadHomeDashboard();
    }, 300);
}

function showClientsSection() {
    resetSidebarActive();
    const navClients = document.getElementById('nav-clients');
    if (navClients) navClients.classList.add('active');

    hideAllSections();
    const clientsSection = document.getElementById('clientsSection');
    if (clientsSection) clientsSection.classList.remove('hidden');

    loadClientsFromAPI();
}

function showOverviewMapSection() {
    resetSidebarActive();
    const navOverviewMap = document.getElementById('nav-overviewMap');
    if (navOverviewMap) navOverviewMap.classList.add('active');

    hideAllSections();
    const overviewMapSection = document.getElementById('overviewMapSection');
    if (overviewMapSection) overviewMapSection.classList.remove('hidden');

    setTimeout(() => {
        if (overviewMap) overviewMap.invalidateSize();
    }, 300);
}

function showDashboardSection() {
    resetSidebarActive();
    const navDashboard = document.getElementById('nav-dashboard');
    if (navDashboard) navDashboard.classList.add('active');

    hideAllSections();
    const dashboardSection = document.getElementById('dashboardSection');
    if (dashboardSection) dashboardSection.classList.remove('hidden');

    loadDashboardData();
}

function showContactSection() {
    resetSidebarActive();
    const navContact = document.getElementById('nav-contact');
    if (navContact) navContact.classList.add('active');

    hideAllSections();
    const contactSection = document.getElementById('contactSection');
    if (contactSection) contactSection.classList.remove('hidden');
}

function showHelpSection() {
    resetSidebarActive();
    const navHelp = document.getElementById('nav-help');
    if (navHelp) navHelp.classList.add('active');

    hideAllSections();
    const helpSection = document.getElementById('helpSection');
    if (helpSection) helpSection.classList.remove('hidden');
}

function showApiSection() {
    resetSidebarActive();
    const navApi = document.getElementById('nav-api');
    if (navApi) navApi.classList.add('active');

    hideAllSections();
    const apiButtonsSection = document.getElementById('apiButtonsSection');
    if (apiButtonsSection) apiButtonsSection.classList.remove('hidden');
}

// =============================================================================
// 5. CHAT FUNCTIONALITY
// =============================================================================
function initializeChat() {
    const navChat = document.getElementById('nav-chat');
    const chatPrompt = document.getElementById('chatPrompt');
    const sendChatPrompt = document.getElementById('sendChatPrompt');

    if (navChat) {
        navChat.addEventListener('click', async () => {
            const modal = new bootstrap.Modal(document.getElementById('deepSeekChatModal'));
            modal.show();
            await loadChatHistory();
        });
    }

    if (chatPrompt) {
        chatPrompt.addEventListener('keydown', function (event) {
            if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault();
                const sendButton = document.getElementById('sendChatPrompt');
                if (sendButton) sendButton.click();
            }
        });
    }

    if (sendChatPrompt) {
        sendChatPrompt.addEventListener('click', async () => {
            const prompt = document.getElementById('chatPrompt')?.value.trim();
            if (!prompt) return;

            appendMessage("Usuario", prompt, "user-message", false);
            document.getElementById('chatPrompt').value = "";

            const thinkingElement = showThinkingAnimation();

            try {
                const response = await fetch('/api/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ question: prompt })
                });

                if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);

                const data = await response.json();
                hideThinkingAnimation(thinkingElement);
                appendMessage("DeepSeek", data.answer, "bot-message", true);
                await loadChatHistory();

            } catch (err) {
                hideThinkingAnimation(thinkingElement);
                appendMessage("Error", "Hubo un problema con el Chat: " + err.message, "text-danger", false);
            }
        });
    }
}

async function loadChatHistory() {
    try {
        const res = await fetch('/api/chat/history');
        if (!res.ok) throw new Error(`Error ${res.status}: ${res.statusText}`);

        const history = await res.json();
        const chatHistory = document.getElementById('chatHistory');
        if (chatHistory) {
            chatHistory.innerHTML = "";
            history.reverse().forEach(entry => {
                appendMessage("Usuario", entry.question, "user-message", false);
                appendMessage("DeepSeek", entry.answer, "bot-message", true);
            });
        }
    } catch (err) {
        appendMessage("Error", "No se pudo cargar el historial: " + err.message, "text-danger", false);
    }
}

function appendMessage(user, text, className, isMarkdown) {
    if (!text) return;

    const container = document.getElementById('chatHistory');
    if (!container) return;

    const div = document.createElement('div');
    div.classList.add("chat-message", className);

    if (isMarkdown && window.showdown) {
        const converter = new showdown.Converter();
        text = converter.makeHtml(text);
    }

    div.innerHTML = `<strong>${user}:</strong><br>${text}`;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

function showThinkingAnimation() {
    const thinkingElement = document.createElement('div');
    thinkingElement.classList.add("thinking-animation");
    thinkingElement.innerHTML = `
        <div class="thinking-dot"></div>
        <div class="thinking-dot"></div>
        <div class="thinking-dot"></div>
    `;

    const chatHistory = document.getElementById('chatHistory');
    if (chatHistory) chatHistory.appendChild(thinkingElement);
    return thinkingElement;
}

function hideThinkingAnimation(thinkingElement) {
    if (thinkingElement) thinkingElement.remove();
}

// =============================================================================
// 6. CLIENT MANAGEMENT
// =============================================================================
function initializeClientSearch() {
    const searchInput = document.getElementById("searchClient");
    if (searchInput) {
        searchInput.addEventListener("input", function () {
            const query = this.value.toLowerCase();
            const clientCards = document.querySelectorAll("#clientsContainer .client-card");
            clientCards.forEach(card => {
                card.style.display = card.innerText.toLowerCase().includes(query) ? "block" : "none";
            });
        });
    }
}

function loadClientsFromAPI() {
    fetch('/api/clients')
        .then(response => response.json())
        .then(data => {
            console.log("📡 Datos de clientes recibidos:", data);
            const clientsContainer = document.getElementById('clientsContainer');
            if (!clientsContainer) return;

            clientsContainer.innerHTML = '';

            data.forEach(client => {
                const card = document.createElement('div');
                card.className = 'card client-card shadow-sm';
                card.innerHTML = `
                    <img
                        src="${client.logourl || 'default-logo.png'}"
                        class="card-img-top p-2"
                        alt="Logo de ${client.name}"
                    >
                    <div class="card-body">
                        <h5 class="card-title">${client.name}</h5>
                        <p class="card-text text-center">
                            <b>Metros:</b> ${client.total_civil_works.toLocaleString()} m<br>
                            <b>HPS:</b> ${client.total_hps.toLocaleString()}<br>
                            <b>Activaciones:</b> ${client.total_activations.toLocaleString()}
                        </p>
                        <button
                            class="btn btn-light w-100 mt-2 fw-bold"
                            onclick="loadClientProjects(${client.id}, '${client.name}')"
                        >
                            Ver Proyectos
                        </button>
                    </div>
                `;
                clientsContainer.appendChild(card);
            });
        })
        .catch(error => console.error('❌ Error cargando clientes:', error));
}

function reloadClients() {
    loadClientsFromAPI();
}

// Make function globally available
window.loadClientProjects = function(clientId, clientName) {
    fetch(`/api/clients/${clientId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error ${response.status}: Cliente no encontrado`);
            }
            return response.json();
        })
        .then(clientData => {
            console.log("📡 Cliente recibido:", clientData);

            // Update UI with client data
            const elements = {
                clientNameSpan: document.getElementById('clientNameSpan'),
                clientLogo: document.getElementById('clientLogo'),
                totalCivilWorks: document.getElementById('totalCivilWorks'),
                totalHPs: document.getElementById('totalHPs'),
                totalActivations: document.getElementById('totalActivations')
            };

            if (elements.clientNameSpan) elements.clientNameSpan.textContent = clientData.name;
            if (elements.clientLogo) elements.clientLogo.src = clientData.logo_url || '/images/default-logo.png';
            if (elements.totalCivilWorks) elements.totalCivilWorks.textContent = `${clientData.total_civil_works || 0} m`;
            if (elements.totalHPs) elements.totalHPs.textContent = clientData.total_hps || 0;
            if (elements.totalActivations) elements.totalActivations.textContent = clientData.total_activations || 0;

            return fetch(`/api/clients/${clientId}/projects`);
        })
        .then(res => res.json())
        .then(projectsData => {
            console.log("📡 Proyectos recibidos:", projectsData);
            fillClientProjects(projectsData);
            const clientProjectsSection = document.getElementById('clientProjectsSection');
            if (clientProjectsSection) clientProjectsSection.classList.remove('hidden');
        })
        .catch(err => console.error('❌ Error al cargar cliente o proyectos:', err));
};

function fillClientProjects(projects) {
    const container = document.getElementById('clientProjectsContainer');
    if (!container) return;

    container.innerHTML = '';

    projects.forEach(proj => {
        const cardCol = document.createElement('div');
        cardCol.className = 'col-md-6 mb-3';
        cardCol.innerHTML = `
            <div class="card h-100 shadow-sm">
                <div class="card-body d-flex flex-column">
                    <h5 class="card-title text-primary">${proj.name || 'Unnamed Project'}</h5>
                    <p class="card-text mb-2">
                        <strong>Country:</strong> ${proj.country || 'N/A'}<br>
                        <strong>Phase:</strong> ${proj.phase || 'N/A'}<br>
                        <strong>Subcontractor:</strong> ${proj.subcontractor || 'N/A'}<br>
                        <strong>Engineering Manager:</strong> ${proj.engineering_manager || 'N/A'}<br>
                        <strong>HP:</strong> ${proj.hp || 0}<br>
                        <strong>Civil Works:</strong> ${proj.civilworks || 0} m
                    </p>
                    <button class="btn btn-info mt-auto" onclick="loadVillages(${proj.id})">
                        <i class="bi bi-geo-alt"></i> Show Villages
                    </button>
                </div>
            </div>
        `;
        container.appendChild(cardCol);
    });
}

// Make function globally available
window.loadVillages = async function(projectId) {
    console.log(`📡 Solicitando villages para el proyecto ID: ${projectId}`);

    try {
        const response = await fetch(`/api/projects/${projectId}/villages`);
        if (!response.ok) {
            throw new Error(`Error ${response.status}: No se encontraron villages`);
        }

        const villages = await response.json();
        console.log("✅ Villages recibidos:", villages);

        if (!Array.isArray(villages)) {
            throw new Error("La API no devolvió un array válido.");
        }

        showVillagesModal(projectId, villages);
    } catch (error) {
        console.error('❌ Error al cargar villages:', error);
        alert(`Error al cargar villages: ${error.message}`);
    }
};

function showVillagesModal(projectId, villages) {
    console.log(`🎯 Mostrando modal para projectId: ${projectId}`);

    const modalElement = document.getElementById('villagesModal');
    if (!modalElement) {
        console.error("❌ Error: villagesModal no existe en el DOM.");
        return;
    }

    const villagesTableBody = document.getElementById('villagesTableBody');
    if (!villagesTableBody) {
        console.error("❌ Error: villagesTableBody no encontrado en el DOM.");
        return;
    }

    villagesTableBody.innerHTML = '';

    villages.forEach(v => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${v.villageName || 'N/A'}</td>
            <td>
                <button class="btn btn-sm btn-primary me-2" onclick="openVillageInMap('${v.villageName}')">
                    Open in Map
                </button>
                <button class="btn btn-sm btn-warning" onclick="exportVillageReport('${v.villageName}')">
                    Export Report
                </button>
            </td>
        `;
        villagesTableBody.appendChild(tr);
    });

    const villagesModal = new bootstrap.Modal(modalElement);
    villagesModal.show();
}

// Make functions globally available
window.openVillageInMap = function(villageName) {
    if (!villageName) {
        alert("No se ha proporcionado un nombre de village.");
        return;
    }
    console.log("🌍 Redirigiendo a qgis_projects.html con village:", villageName);
    window.location.href = `/qgis-projects-map?village=${encodeURIComponent(villageName)}`;
};

window.exportVillageReport = function(villageName) {
    alert(`TODO: Exportar reporte del village: ${villageName}`);
};

// =============================================================================
// 7. CIVIL WORKS MANAGEMENT
// =============================================================================
function showCivilWorks(projectId) {
    resetSidebarActive();
    hideAllSections();
    const civilWorksSection = document.getElementById('civilWorksSection');
    if (civilWorksSection) civilWorksSection.classList.remove('hidden');
    loadCivilWorks(projectId);
}

function loadCivilWorks(projectId) {
    fetch('/api/civil-works/data', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            start: 0,
            length: 100,
            client: '',
            project_id: projectId
        })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => fillCivilWorksTable(data))
        .catch(error => {
            console.error('Error al cargar Civil Works:', error);
            alert('No se encontraron datos de Civil Works para este proyecto.');
        });
}

function fillCivilWorksTable(cwData) {
    console.log("Datos recibidos de Civil Works:", cwData);

    const cwTableBody = document.querySelector('#civilWorksTable tbody');
    if (!cwTableBody) return;

    cwTableBody.innerHTML = '';

    let arrayData;
    if (Array.isArray(cwData)) {
        arrayData = cwData;
    } else if (cwData && Array.isArray(cwData.data)) {
        arrayData = cwData.data;
    } else {
        console.warn("cwData no es un array. Asumiendo que es un solo objeto.", cwData);
        arrayData = [cwData];
    }

    arrayData.forEach(item => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${item.project || ''}</td>
            <td>${item.pop || ''}</td>
            <td>${item.dp || ''}</td>
            <td>${item.street || ''}</td>
            <td>${item.tzip || ''}</td>
            <td>${item.type || ''}</td>
            <td>${item.spec || ''}</td>
            <td>${item.length_meters || ''}</td>
            <td>${item.village || ''}</td>
        `;
        cwTableBody.appendChild(tr);
    });
}

// =============================================================================
// 8. API MANAGEMENT
// =============================================================================
function initializeAPIButtons() {
    const btnCivilWorksAPI = document.getElementById('btnCivilWorksAPI');
    const btnAddressesAPI = document.getElementById('btnAddressesAPI');
    const btnContractsAPI = document.getElementById('btnContractsAPI');

    if (btnCivilWorksAPI) {
        btnCivilWorksAPI.addEventListener('click', function() {
            const source = "Engineering";
            const projectId = 123;
            window.location.href = `/civil-works?source=${source}&project=${projectId}`;
        });
    }

    if (btnContractsAPI) {
        btnContractsAPI.addEventListener('click', function() {
            const source = "Engineering";
            const projectId = 123;
            window.location.href = `/contracts?source=${source}&project=${projectId}`;
        });
    }

    if (btnAddressesAPI) {
        btnAddressesAPI.addEventListener('click', () => fetchData('/api/addresses', btnAddressesAPI));
    }
}

function fetchData(endpoint, button) {
    button.disabled = true;
    const apiResults = document.getElementById('apiResults');

    fetch(endpoint)
        .then(response => response.json())
        .then(data => {
            console.log(`Respuesta de ${endpoint}:`, data);
            if (apiResults) {
                apiResults.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
            }
        })
        .catch(error => {
            console.error(`Error al llamar ${endpoint}:`, error);
            if (apiResults) {
                apiResults.innerHTML = `<p class="text-danger">Error al obtener datos de ${endpoint}</p>`;
            }
        })
        .finally(() => {
            button.disabled = false;
        });
}

// =============================================================================
// 9. MAP FUNCTIONALITY
// =============================================================================
function initializeMapComponents() {
    // Initialize overview map
    setTimeout(() => {
        initOverviewMap();
        initializeMapControls();
    }, 100);
}

function initOverviewMap() {
    const mapElement = document.getElementById("mapOverview");
    if (!mapElement) return;

    overviewMap = L.map("mapOverview").setView([48.2082, 16.3738], 15);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: "&copy; OpenStreetMap contributors"
    }).addTo(overviewMap);
}

function initializeMapControls() {
    const loadMapBtn = document.getElementById("loadMapBtn");
    if (loadMapBtn) {
        loadMapBtn.addEventListener("click", async () => {
            const layerSelector = document.getElementById("layerSelector");
            if (!layerSelector) return;

            const selected = layerSelector.value;
            const layerMap = {
                burgeln: "FibraInsyte:Burgeln",
                ovenhausen: "FibraInsyte:Ovenhausen",
                brenkhausen: "FibraInsyte:brenkhausen_final",
                lutmarsen: "FibraInsyte:Lutmarsen",
                furstenau: "FibraInsyte:Furstenau"
            };

            const wmsLayerName = layerMap[selected];
            if (!wmsLayerName) {
                alert("❌ No se ha seleccionado una capa válida.");
                return;
            }

            const bounds = await fetchLayerBounds(wmsLayerName);
            if (bounds && overviewMap) {
                overviewMap.fitBounds(bounds);
            } else {
                console.warn("⚠️ No se encontró BBOX para:", wmsLayerName);
            }

            loadGeoServerLayer(wmsLayerName);
        });
    }
}

async function fetchLayerBounds(layerName) {
    const url = `http://192.168.13.91:8081/api/geoserver/capabilities?layer=${encodeURIComponent(layerName)}`;
    const nameToMatch = layerName.includes(":") ? layerName.split(":")[1] : layerName;

    try {
        const res = await fetch(url);
        const text = await res.text();
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(text, "text/xml");
        const layers = xmlDoc.getElementsByTagName("Layer");

        for (let i = 0; i < layers.length; i++) {
            const nameNode = layers[i].getElementsByTagName("Name")[0];
            if (nameNode && nameNode.textContent === nameToMatch) {
                const bboxNode = layers[i].getElementsByTagName("LatLonBoundingBox")[0];
                if (bboxNode) {
                    const minx = parseFloat(bboxNode.getAttribute("minx"));
                    const miny = parseFloat(bboxNode.getAttribute("miny"));
                    const maxx = parseFloat(bboxNode.getAttribute("maxx"));
                    const maxy = parseFloat(bboxNode.getAttribute("maxy"));
                    return [[miny, minx], [maxy, maxx]];
                }
            }
        }
    } catch (err) {
        console.error("❌ Error al obtener BBOX:", err);
    }
    return null;
}

function loadGeoServerLayer(layerName) {
    if (!overviewMap) return;

    if (geoLayer) {
        overviewMap.removeLayer(geoLayer);
    }

    geoLayer = L.tileLayer.wms("http://192.168.13.91:8080/geoserver/FibraInsyte/ows", {
        layers: layerName,
        format: "image/png",
        transparent: true,
        version: "1.1.1",
        attribution: "GeoServer - Fibra Insyte",
        tiled: true
    }).addTo(overviewMap);
}

// =============================================================================
// 10. DASHBOARD DATA
// =============================================================================
function loadDashboardData() {
    fetch('/api/dashboard')
        .then(response => response.json())
        .then(data => {
            const elements = {
                constructionPerc: document.getElementById('constructionPerc'),
                documentationPerc: document.getElementById('documentationPerc'),
                complaints: document.getElementById('complaints')
            };

            if (elements.constructionPerc) elements.constructionPerc.innerText = data.constructionPerc.toFixed(2) + '%';
            if (elements.documentationPerc) elements.documentationPerc.innerText = data.documentationPerc.toFixed(2) + '%';
            if (elements.complaints) elements.complaints.innerText = data.complaints;
        })
        .catch(err => console.error('Error al cargar datos de dashboard:', err));
}

// =============================================================================
// 11. CONTACT FORM
// =============================================================================
function initializeContactForm() {
    const contactForm = document.getElementById('contactForm');
    if (contactForm) {
        contactForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const name = document.getElementById('contactName')?.value;
            const email = document.getElementById('contactEmail')?.value;
            const msg = document.getElementById('contactMsg')?.value;

            alert(`Gracias, ${name}. Tu mensaje se ha enviado.`);
            contactForm.reset();
        });
    }
}

// =============================================================================
// 12. HOME DASHBOARD CHARTS AND STATS
// =============================================================================
function initializeHomeDashboard() {
    // Load dashboard when home section is shown
    const navHome = document.getElementById('nav-home');
    if (navHome) {
        navHome.addEventListener("click", () => {
            setTimeout(loadHomeDashboard, 300);
        });
    }

    // Load initially if home is visible
    const homeSection = document.getElementById('homeSection');
    if (homeSection && !homeSection.classList.contains("hidden")) {
        loadHomeDashboard();
    }
}

function loadHomeDashboard() {
    loadDashboardStats();
    loadCivilWorksByTypeChart();
    loadProgressChart();
    loadActivationChart();
    loadSubcontractorChart();
}

function loadDashboardStats() {
    fetch('/api/dashboard/stats')
        .then(response => response.json())
        .then(data => {
            const elements = {
                totalCivilWorks: document.getElementById('totalCivilWorks'),
                totalMeters: document.getElementById('totalMeters'),
                activeProjects: document.getElementById('activeProjects'),
                completedProjects: document.getElementById('completedProjects')
            };

            if (elements.totalCivilWorks) elements.totalCivilWorks.innerText = data.totalWorks || "1750km";
            if (elements.totalMeters) elements.totalMeters.innerText = (data.totalMeters || "") + "m";
            if (elements.activeProjects) elements.activeProjects.innerText = data.activeProjects || 66;
            if (elements.completedProjects) elements.completedProjects.innerText = data.completedProjects || 357;
        })
        .catch(error => console.error('❌ Error cargando estadísticas:', error));
}

function loadCivilWorksByTypeChart() {
    fetch('/api/civil-works/by-type')
        .then(response => response.json())
        .then(data => {
            const ctx = document.getElementById("chartByClient").getContext("2d");

            if (window.clientChartInstance) {
                window.clientChartInstance.destroy();
            }

            window.clientChartInstance = new Chart(ctx, {
                type: "bar",
                data: {
                    labels: Object.keys(data),
                    datasets: [{
                        label: "Metros Totales",
                        data: Object.values(data),
                        backgroundColor: ["#4CAF50", "#FF5733", "#36A2EB", "#FFCE56"],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        x: {
                            title: { display: true, text: "Tipo de Obra" },
                            ticks: {
                                maxRotation: 45, // Rota las etiquetas para mejor visibilidad
                                minRotation: 0
                            }
                        },
                        y: {
                            title: { display: true, text: "Metros Totales" },
                            beginAtZero: true
                        }
                    },
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                label: function (tooltipItem) {
                                    return `${tooltipItem.label}: ${tooltipItem.raw.toLocaleString()} m`;
                                }
                            }
                        }
                    }
                }
            });

            // Ajusta el tamaño del contenedor dinámicamente
            document.getElementById('chartByClient').parentNode.style.height = "400px";
        })
        .catch(error => console.error('❌ Error cargando datos:', error));
}

// 📊 Gráfico de Progreso de Construcción con Datos Ficticios
function loadProgressChart() {
    // Datos ficticios para pruebas
    const mockData = {
        "Inicio": 0,
        "Excavación": 60,
        "Ductos": 45,
        "Cableado": 20,
        "Fusión": 70,
        "Finalizado": 100
    };

    const ctx = document.getElementById("chartProgress").getContext("2d");

    new Chart(ctx, {
        type: "line",
        data: {
            labels: Object.keys(mockData), // Fases de construcción
            datasets: [{
                label: "Progreso",
                data: Object.values(mockData), // Porcentaje de avance por fase
                fill: false,
                borderColor: "#007bff", // Azul moderno
                borderWidth: 3,
                pointBackgroundColor: ["#ff3d00", "#ffa000", "#4caf50", "#03a9f4", "#673ab7", "#43a047"], // Colores de hitos
                pointRadius: 8, // Tamaño de puntos grandes
                pointHoverRadius: 10,
                tension: 0.4 // Suaviza la curva
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false }, // Ocultar leyenda
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return ` Progreso: ${context.raw}%`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    title: { display: true, text: "Fases de Construcción", font: { weight: "bold" } }
                },
                y: {
                    min: -5,
                    max: 105,
                    ticks: { stepSize: 20 },
                    title: { display: true, text: "Avance (%)", font: { weight: "bold" } }
                }
            }
        }
    });
}

// Ejecutar al cargar la página
document.addEventListener("DOMContentLoaded", loadProgressChart);

const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: { position: "top" },
        tooltip: {
            backgroundColor: "rgba(0,0,0,0.8)",
            titleFont: { size: 16 },
            bodyFont: { size: 14 }
        }
    },
    scales: {
        x: {
            title: { display: true, text: "Villages", font: { size: 14, weight: "bold" } },
            grid: { display: true, color: "#ddd" } // Asegura que la línea del eje X se vea
        },
        y: {
            title: { display: true, text: "Cantidad", font: { size: 14, weight: "bold" } },
            beginAtZero: true
        }
    }
};



// 📊 Gráfico de Activaciones con ajuste de línea
function loadActivationChart() {
    const ctx = document.getElementById("chartActivations")?.getContext("2d");
    if (!ctx) return console.error("❌ No se encontró el canvas 'chartActivations'");

    new Chart(ctx, {
        type: "bar",
        data: {
            labels: ["Brenkhausen", "Lutmarsen", "Fürstenau"],
            datasets: [
                { label: "Clientes Conectados", data: [120, 95, 80], backgroundColor: "#36A2EB", borderRadius: 8 },
                { label: "Pendientes", data: [30, 25, 20], backgroundColor: "#FF6384", borderRadius: 8 }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            layout: {
                padding: { bottom: 10 } // Agregar espacio en la parte inferior
            },
            scales: {
                x: {
                    title: { display: true, text: "Villages", font: { size: 14, weight: "bold" } },
                    grid: { display: true, color: "#ddd" } // Asegura que la línea inferior se vea
                },
                y: {
                    title: { display: true, text: "Cantidad", font: { size: 14, weight: "bold" } },
                    beginAtZero: true
                }
            },
            plugins: {
                legend: { position: "top" },
                tooltip: {
                    backgroundColor: "rgba(0,0,0,0.8)",
                    titleFont: { size: 16 },
                    bodyFont: { size: 14 }
                }
            }
        }
    });
}



function loadSubcontractorChart() {
    const ctx = document.getElementById("chartSubcontractorPerformance")?.getContext("2d");
    if (!ctx) return console.error("❌ No se encontró el canvas 'chartSubcontractorPerformance'");

    new Chart(ctx, {
        type: "line",
        data: {
            labels: ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"], // Todos los meses
            datasets: [
                {
                    label: "PROCARBON",
                    data: [2000, 5000, 7000, 10000, 12000, 14000, 16000, 18000, 19000, 20000, 22000, 23000],
                    borderColor: "#FF5733",
                    backgroundColor: "rgba(255, 87, 51, 0.2)",
                    borderWidth: 2,
                    tension: 0.4,
                    pointRadius: 6,
                    pointHoverRadius: 8
                },
                {
                    label: "APROCON",
                    data: [1500, 4000, 6500, 9000, 13000, 15000, 17000, 19500, 21000, 22000, 24000, 25000],
                    borderColor: "#36A2EB",
                    backgroundColor: "rgba(54, 162, 235, 0.2)",
                    borderWidth: 2,
                    tension: 0.4,
                    pointRadius: 6,
                    pointHoverRadius: 8
                },
                {
                    label: "UNION TELECOM",
                    data: [1000, 3000, 6000, 8000, 16000, 18000, 19000, 20500, 22000, 23000, 25000, 27000],
                    borderColor: "#4CAF50",
                    backgroundColor: "rgba(76, 175, 80, 0.2)",
                    borderWidth: 2,
                    tension: 0.4,
                    pointRadius: 6,
                    pointHoverRadius: 8
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            layout: {
                padding: { bottom: 20 } // 🔹 Aumenta el margen inferior
            },
            scales: {
                x: {
                    title: { display: true, text: "Mes", font: { size: 14, weight: "bold" } }
                },
                y: {
                    title: { display: true, text: "Metros Construidos", font: { size: 14, weight: "bold" } },
                    beginAtZero: true
                }
            },
            plugins: {
                legend: { position: "top" },
                tooltip: {
                    backgroundColor: "rgba(0,0,0,0.8)",
                    titleFont: { size: 16 },
                    bodyFont: { size: 14 }
                }
            }
        }
    });
}



// funciones para cargar los datos al cambiar a Home
function loadHomeDashboard() {
    loadDashboardStats();
    loadCivilWorksByTypeChart();
    loadProgressChart();
    loadActivationChart();
    loadSubcontractorChart();
}

// 🔥 Ejecutar al cambiar a Home
navHome.addEventListener("click", () => {
    setTimeout(loadHomeDashboard, 300);
});

if (!homeSection.classList.contains("hidden")) {
    loadHomeDashboard();
}