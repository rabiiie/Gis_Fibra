document.addEventListener("DOMContentLoaded", function () {

    function loadDashboardStats() {
        fetch('/api/dashboard/stats')
            .then(response => response.json())
            .then(data => {
                document.getElementById('totalCivilWorks').innerText = data.totalWorks ?? 1750;
                document.getElementById('totalMeters').innerText = (data.totalMeters ?? "0") + " m";
                document.getElementById('activeProjects').innerText = data.activeProjects ?? 66;
                document.getElementById('completedProjects').innerText = data.completedProjects ?? 357;
            })
            .catch(error => console.error('❌ Error cargando estadísticas:', error));
    }

    function loadCivilWorksByTypeChart() {
        fetch('/api/civil-works/by-type')
            .then(response => response.json())
            .then(data => {
                const canvas = document.getElementById("chartByClient");
                if (!canvas) return;
                const ctx = canvas.getContext("2d");

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
                                    maxRotation: 45,
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

                canvas.parentNode.style.height = "400px";
            })
            .catch(error => console.error('❌ Error cargando datos:', error));
    }

    function loadProgressChart() {
        const mockData = {
            "Inicio": 0,
            "Excavación": 60,
            "Ductos": 45,
            "Cableado": 20,
            "Fusión": 70,
            "Finalizado": 100
        };

        const canvas = document.getElementById("chartProgress");
        if (!canvas) return;
        const ctx = canvas.getContext("2d");

        new Chart(ctx, {
            type: "line",
            data: {
                labels: Object.keys(mockData),
                datasets: [{
                    label: "Progreso",
                    data: Object.values(mockData),
                    fill: false,
                    borderColor: "#007bff",
                    borderWidth: 3,
                    pointBackgroundColor: ["#ff3d00", "#ffa000", "#4caf50", "#03a9f4", "#673ab7", "#43a047"],
                    pointRadius: 8,
                    pointHoverRadius: 10,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: context => ` Progreso: ${context.raw}%`
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

    function loadActivationChart() {
        const canvas = document.getElementById("chartActivations");
        if (!canvas) return;
        const ctx = canvas.getContext("2d");

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
                    padding: { bottom: 10 }
                },
                scales: {
                    x: {
                        title: { display: true, text: "Villages", font: { size: 14, weight: "bold" } },
                        grid: { display: true, color: "#ddd" }
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
        const canvas = document.getElementById("chartSubcontractorPerformance");
        if (!canvas) return;
        const ctx = canvas.getContext("2d");

        new Chart(ctx, {
            type: "line",
            data: {
                labels: ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"],
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
                layout: { padding: { bottom: 20 } },
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

    function loadHomeDashboard() {
        loadDashboardStats();
        loadCivilWorksByTypeChart();
        loadProgressChart();
        loadActivationChart();
        loadSubcontractorChart();
    }

    const navHome = document.getElementById("navHome");
    const homeSection = document.getElementById("homeSection");

    if (navHome) {
        navHome.addEventListener("click", () => {
            setTimeout(loadHomeDashboard, 300);
        });
    }

    if (homeSection && !homeSection.classList.contains("hidden")) {
        loadHomeDashboard();
    }

});
