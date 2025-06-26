// Chart instances storage
let chartInstances = {};

// Tab initialization
document.querySelectorAll('[data-bs-toggle="pill"]').forEach(tab => {
    tab.addEventListener('shown.bs.tab', (e) => {
        const target = e.target.getAttribute('data-bs-target');
        if (target === '#generalDashboard') {
            initGeneralDashboard();
        } else if (target === '#civilWorksDashboard') {
            initCivilWorksDashboard();
        } else if (target === '#contractsDashboard') {
            initContractsDashboard();
        } else if (target === '#cablesDashboard') {
            initCablesDashboard();
        } else if (target === '#timelineDashboard') {
            initTimelineChart();
        }
    });
});

// Chart cleanup
function destroyCharts() {
    Object.values(chartInstances).forEach(chart => {
        if (chart && typeof chart.destroy === 'function') {
            chart.destroy();
        }
    });
    chartInstances = {};
}

// GENERAL DASHBOARD
function initGeneralDashboard() {
    destroyCharts();

    const ctxDist = document.getElementById('projectDistributionChart')?.getContext('2d');
    if (!ctxDist) return;

    chartInstances.projectDistribution = new Chart(ctxDist, {
        type: 'doughnut',
        data: {
            labels: ['Civil Works', 'Contracts', 'Cables', 'Other'],
            datasets: [{
                data: [45, 25, 20, 10],
                backgroundColor: ['#3f51b5', '#ff6b6b', '#4CAF50', '#FF9800'],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            layout: {
                padding: { top: 10, bottom: 10 }
            },
            plugins: {
                legend: {
                    position: 'right',
                    align: 'center',
                    labels: { boxWidth: 15, padding: 15 }
                },
                tooltip: {
                    callbacks: {
                        label: (context) => {
                            const value = context.raw;
                            const total = context.dataset.data.reduce((acc, val) => acc + val, 0);
                            const percentage = Math.round((value / total) * 100);
                            return `${context.label}: ${value} (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });

    updateLastUpdateInfo();
}

// CIVIL WORKS DASHBOARD
function initCivilWorksDashboard() {
    destroyCharts();

    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'];
    const currentMonthIndex = CURRENT_DATE.getMonth() % 6;

    const cwtCtx = document.getElementById('civilWorksTimeline')?.getContext('2d');
    if (!cwtCtx) return;

    chartInstances.civilWorksTimeline = new Chart(cwtCtx, {
        type: 'line',
        data: {
            labels: months,
            datasets: [{
                label: 'Meters Built',
                data: [120, 190, 300, 500, 700, 900],
                borderColor: '#4CAF50',
                tension: 0.4,
                fill: true,
                backgroundColor: '#4CAF5020'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    title: { display: true, text: 'Meters' }
                },
                x: {
                    grid: {
                        color: (context) => context.index === currentMonthIndex ? '#ff000040' : undefined
                    }
                }
            }
        }
    });

    const cwiCtx = document.getElementById('civilWorksIssues')?.getContext('2d');
    if (!cwiCtx) return;

    chartInstances.civilWorksIssues = new Chart(cwiCtx, {
        type: 'bar',
        data: {
            labels: ['Delay', 'Safety', 'Quality', 'Other'],
            datasets: [{
                label: 'Issues',
                data: [12, 5, 8, 3],
                backgroundColor: ['#f44336', '#ff9800', '#9c27b0', '#2196f3']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        afterLabel: () => `Last updated: ${CURRENT_DATE.toLocaleDateString()}`
                    }
                }
            }
        }
    });

    updateLastUpdateInfo();
}

// CONTRACTS DASHBOARD
function initContractsDashboard() {
    destroyCharts();

    const cvCtx = document.getElementById('contractsValueChart')?.getContext('2d');
    if (!cvCtx) return;

    chartInstances.contractsValue = new Chart(cvCtx, {
        type: 'bar',
        data: {
            labels: ['Q1', 'Q2', 'Q3', 'Q4'],
            datasets: [{
                label: 'Contract Value (M€)',
                data: [1.2, 1.8, 2.1, 2.5],
                backgroundColor: '#9C27B0',
                borderColor: '#7B1FA2',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    title: { display: true, text: 'Millions €' }
                }
            }
        }
    });

    const csCtx = document.getElementById('contractStatusChart')?.getContext('2d');
    if (!csCtx) return;

    chartInstances.contractStatus = new Chart(csCtx, {
        type: 'pie',
        data: {
            labels: ['Active', 'Pending', 'Closed'],
            datasets: [{
                data: [16, 7, 8],
                backgroundColor: ['#4CAF50', '#FFC107', '#F44336']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {
                    callbacks: {
                        afterFooter: () => [`Updated by: ${CURRENT_USER}`, `Date: ${CURRENT_DATE.toLocaleString()}`]
                    }
                }
            }
        }
    });

    updateLastUpdateInfo();
}

// CABLES DASHBOARD
function initCablesDashboard() {
    destroyCharts();

    const cpCtx = document.getElementById('cableProgressChart')?.getContext('2d');
    if (!cpCtx) return;

    chartInstances.cableProgress = new Chart(cpCtx, {
        type: 'line',
        data: {
            labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4'],
            datasets: [{
                label: 'Installation Progress (km)',
                data: [50, 120, 200, 300],
                borderColor: '#2196F3',
                tension: 0.4,
                fill: true,
                backgroundColor: '#2196F320'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    title: { display: true, text: 'Kilometers' }
                }
            }
        }
    });

    const ctCtx = document.getElementById('cableTypesChart')?.getContext('2d');
    if (!ctCtx) return;

    chartInstances.cableTypes = new Chart(ctCtx, {
        type: 'doughnut',
        data: {
            labels: ['Fiber', 'Copper', 'Hybrid'],
            datasets: [{
                data: [65, 25, 10],
                backgroundColor: ['#ff9800', '#607d8b', '#4caf50']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right'
                }
            }
        }
    });

    updateLastUpdateInfo();
}

// TIMELINE DASHBOARD
function initTimelineChart() {
    destroyCharts();

    const ctx = document.getElementById('timelineChart')?.getContext('2d');
    if (!ctx) return;

    const projectStart = new Date('2024-03-01');

    const phases = [
        {
            phase: 'Planning',
            start: '2024-03-01',
            end: '2024-04-15',
            progress: 100,
            color: '#3f51b5'
        },
        {
            phase: 'Design',
            start: '2024-04-10',
            end: '2024-06-30',
            progress: 65,
            color: '#4CAF50'
        },
        {
            phase: 'Construction',
            start: '2024-07-01',
            end: '2024-10-15',
            progress: 20,
            color: '#F44336'
        }
    ];

    const data = phases.map(p => {
        const start = new Date(p.start);
        const end = new Date(p.end);
        return {
            label: p.phase,
            offset: (start - projectStart) / (1000 * 60 * 60 * 24),
            duration: (end - start) / (1000 * 60 * 60 * 24),
            color: p.color,
            progress: p.progress,
            start,
            end
        };
    });

    chartInstances.timeline = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(d => d.label),
            datasets: [
                {
                    label: 'Offset',
                    data: data.map(d => d.offset),
                    backgroundColor: 'rgba(0,0,0,0)',
                    stack: 'timeline',
                    borderWidth: 0
                },
                {
                    label: 'Duration',
                    data: data.map(d => d.duration),
                    backgroundColor: data.map(d => d.color + '80'),
                    borderColor: data.map(d => d.color),
                    borderWidth: 2,
                    stack: 'timeline',
                    barPercentage: 0.7,
                    categoryPercentage: 0.8
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: 'y',
            plugins: {
                tooltip: {
                    callbacks: {
                        label: (ctx) => {
                            if (ctx.dataset.label === 'Duration') {
                                const d = data[ctx.dataIndex];
                                return [
                                    `Start: ${d.start.toLocaleDateString()}`,
                                    `End: ${d.end.toLocaleDateString()}`,
                                    `Progress: ${d.progress}%`,
                                    `Updated: ${CURRENT_DATE.toLocaleString()}`
                                ];
                            }
                            return '';
                        }
                    }
                },
                legend: { display: false }
            },
            scales: {
                x: {
                    stacked: true,
                    min: 0,
                    max: Math.max(...data.map(d => d.offset + d.duration)) + 10,
                    title: { display: true, text: 'Days from project start' }
                },
                y: {
                    stacked: true
                }
            }
        }
    });

    updateLastUpdateInfo();
}

// Update last update info in the UI
function updateLastUpdateInfo() {
    const elements = document.querySelectorAll('.last-update-info');
    elements.forEach(el => {
        el.textContent = `Last updated: ${CURRENT_DATE.toLocaleString()} by ${CURRENT_USER}`;
    });
}

// INITIALIZATION: run on page load if a tab is already active
document.addEventListener("DOMContentLoaded", () => {
    const activeTab = document.querySelector('.nav-link.active[data-bs-toggle="pill"]');
    if (activeTab) {
        const target = activeTab.getAttribute('data-bs-target');
        if (target === '#generalDashboard') {
            initGeneralDashboard();
        } else if (target === '#civilWorksDashboard') {
            initCivilWorksDashboard();
        } else if (target === '#contractsDashboard') {
            initContractsDashboard();
        } else if (target === '#cablesDashboard') {
            initCablesDashboard();
        } else if (target === '#timelineDashboard') {
            initTimelineChart();
        }
    }
});
