// tablecontracts.js

const viewColumnsConfig = {
  contracts_list: ["subtype", "access_location", "aufgabenstatus", "order_date", "building_id"],
  planner: ["projektname", "teilpolygon", "hausnummer", "anschlussstatus"],
  unit_task: ["unit_id", "building_id", "unit_status"],
  buildings_task: ["building_id", "building_status"],
  buildings_status_extended: ["home_id", "building_id", "subtypes", "access_location"],
  baulist_highlights: ["building_id", "pha", "status"],
  patchlist_highlights: ["building_id", "patch_status"]
};

let currentPage = 0;
const pageSize = 10;

function buildTableHeaders() {
  const viewName = document.querySelector("#viewSelectorNav .nav-link.active").getAttribute("data-view");
  const columns = viewColumnsConfig[viewName] || [];
  const tableHead = document.getElementById("tableHead");
  tableHead.innerHTML = "";

  const headerRow = document.createElement("tr");
  const filterRow = document.createElement("tr");

  columns.forEach((col) => {
    const thHeader = document.createElement("th");
    thHeader.textContent = col.replace(/_/g, " ");
    headerRow.appendChild(thHeader);

    const thFilter = document.createElement("th");
    const input = document.createElement("input");
    input.type = "text";
    input.className = "column-filter form-control form-control-sm";
    input.setAttribute("data-column", col);
    input.placeholder = "Filtrar...";
    input.addEventListener("input", () => {
      currentPage = 0;
      fetchContracts();
    });
    thFilter.appendChild(input);
    filterRow.appendChild(thFilter);
  });

  tableHead.appendChild(headerRow);
  tableHead.appendChild(filterRow);
}

function getFilters() {
  const filters = {};
  document.querySelectorAll(".column-filter").forEach((input) => {
    const key = input.getAttribute("data-column");
    const value = input.value.trim();
    if (value) filters[key] = value;
  });
  return filters;
}

function fetchContracts() {
  const filters = getFilters();
  const viewName = document.querySelector("#viewSelectorNav .nav-link.active").getAttribute("data-view");

  const requestBody = {
    draw: 1,
    start: currentPage * pageSize,
    length: pageSize,
    search: { value: "" },
    columns: [],
    filters: { ...filters, view: viewName },
  };

  fetch("/api/contracts/multiview", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(requestBody),
  })
    .then((resp) => resp.json())
    .then((data) => {
      renderTable(data.data);
      renderPagination(data.recordsFiltered, data.recordsTotal);
    })
    .catch((err) => {
      console.error("Error:", err);
      alert("Error al cargar datos");
    });
}

function renderTable(rows) {
  const tbody = document.getElementById("contractsTableBody");
  tbody.innerHTML = "";
  const viewName = document.querySelector("#viewSelectorNav .nav-link.active").getAttribute("data-view");
  const columns = viewColumnsConfig[viewName] || [];

  if (!rows || rows.length === 0) {
    tbody.innerHTML = `<tr><td colspan="${columns.length}" class="text-center">No hay datos</td></tr>`;
    return;
  }

  rows.forEach((row) => {
    const tr = document.createElement("tr");
    columns.forEach((col) => {
      const td = document.createElement("td");
      td.textContent = row[col] || "";
      tr.appendChild(td);
    });
    tbody.appendChild(tr);
  });
}

function renderPagination(filtered, total) {
  const info = document.getElementById("paginationInfo");
  const totalPages = Math.ceil(filtered / pageSize);
  info.textContent = `Página ${currentPage + 1} de ${totalPages}`;
  document.getElementById("prevPageBtn").disabled = currentPage === 0;
  document.getElementById("nextPageBtn").disabled = currentPage >= totalPages - 1;
}

// Navigation
function setupViewNavigation() {
  document.querySelectorAll("#viewSelectorNav .nav-link").forEach((link) => {
    link.addEventListener("click", (e) => {
      e.preventDefault();
      document.querySelectorAll("#viewSelectorNav .nav-link").forEach((l) => l.classList.remove("active"));
      e.currentTarget.classList.add("active");
      currentPage = 0;
      buildTableHeaders();
      fetchContracts();
    });
  });
}

// Export
function exportTableToCSV(filename) {
  fetchAllFilteredRows().then((rows) => {
    const viewName = document.querySelector("#viewSelectorNav .nav-link.active").getAttribute("data-view");
    const columns = viewColumnsConfig[viewName];
    const csv = [columns.join(",")].concat(
      rows.map((r) => columns.map((c) => ("" + (r[c] ?? "")).replace(/[\n\r]+/g, " ")).join(","))
    ).join("\n");

    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  });
}

async function fetchAllFilteredRows() {
  const filters = getFilters();
  const viewName = document.querySelector("#viewSelectorNav .nav-link.active").getAttribute("data-view");
  const requestBody = {
    draw: 1,
    start: 0,
    length: -1,
    search: { value: "" },
    columns: [],
    filters: { ...filters, view: viewName },
  };

  const resp = await fetch("/api/contracts/multiview", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(requestBody),
  });
  const data = await resp.json();
  return data.data || [];
}

// Botones export y paginación
function setupEvents() {
  document.getElementById("btnExportExcel").addEventListener("click", () => {
    exportTableToCSV("Contratos.csv");
  });

  document.getElementById("prevPageBtn").addEventListener("click", () => {
    if (currentPage > 0) {
      currentPage--;
      fetchContracts();
    }
  });

  document.getElementById("nextPageBtn").addEventListener("click", () => {
    currentPage++;
    fetchContracts();
  });
}

// Init
window.addEventListener("DOMContentLoaded", () => {
  setupViewNavigation();
  setupEvents();
  buildTableHeaders();
  fetchContracts();
});
