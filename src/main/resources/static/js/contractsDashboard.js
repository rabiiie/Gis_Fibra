
let contractsDashboardCharts = {};

function destroyContractsCharts() {
  Object.values(contractsDashboardCharts).forEach(chart => {
    if (chart && typeof chart.destroy === 'function') {
      chart.destroy();
    }
  });
  contractsDashboardCharts = {};
}

function loadContractsDashboard() {
  destroyContractsCharts();

  fetch('/api/contracts/dashboard')
    .then(response => response.json())
    .then(data => {
      updateContractsCards(data.summary);
      renderContractsCharts(data.charts);
      renderContractsTable(data.list);
    })
    .catch(error => {
      console.error('Error al cargar contratos:', error);
      alert('Error al cargar los datos de contratos');
    });
}

function updateContractsCards(summary) {
  document.getElementById('activeContracts').textContent = summary.activeContracts ?? '-';
  document.getElementById('activeContractsValue').textContent = summary.activeValue ?? '-';
  document.getElementById('pendingContracts').textContent = summary.pendingApprovals ?? '-';
  document.getElementById('pendingOverdue').textContent = summary.overdue ?? '-';
}

function renderContractsCharts(chartsData) {
  const ctxValue = document.getElementById('contractsValueChart').getContext('2d');
  contractsDashboardCharts.contractsValue = new Chart(ctxValue, {
    type: 'bar',
    data: {
      labels: chartsData.quarters,
      datasets: [{
        label: 'Valor Contratos (€)',
        data: chartsData.values,
        backgroundColor: '#9C27B0'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: { y: { beginAtZero: true } }
    }
  });

  const ctxStatus = document.getElementById('contractStatusChart').getContext('2d');
  contractsDashboardCharts.contractStatus = new Chart(ctxStatus, {
    type: 'pie',
    data: {
      labels: chartsData.statusLabels,
      datasets: [{
        data: chartsData.statusValues,
        backgroundColor: ['#4CAF50', '#FFC107', '#F44336']
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false
    }
  });
}

function renderContractsTable(contractsList) {
  const tbody = document.querySelector('#contractsDashboardTable tbody');
  tbody.innerHTML = '';

  if (!contractsList.length) {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td colspan="5" class="text-center">No hay contratos disponibles</td>`;
    tbody.appendChild(tr);
    return;
  }

  contractsList.forEach(contract => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${contract.id}</td>
      <td>${contract.project}</td>
      <td>${contract.status}</td>
      <td>${contract.date}</td>
      <td>${contract.value}</td>
    `;
    tbody.appendChild(tr);
  });
}

// Lanzamos carga automática al cambiar de tab
document.addEventListener('DOMContentLoaded', () => {
  const contractsTab = document.querySelector('[data-bs-target="#contractsDashboard"]');
  if (contractsTab) {
    contractsTab.addEventListener('shown.bs.tab', () => {
      loadContractsDashboard();
    });
  }
});
