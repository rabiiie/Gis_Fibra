/**
 * Limpia todos los cards flotantes (clientes o proyectos)
 */
function clearFloatingCards() {
    const container = document.getElementById('floatingCardsContainer');
    if (container) container.innerHTML = '';
}

/**
 * Muestra un overlay de carga con spinner en el mapa
 */
function showLoadingOverlay() {
    if (document.querySelector('.loading-overlay')) return;

    const overlay = document.createElement('div');
    overlay.className = 'loading-overlay';
    overlay.innerHTML = '<div class="spinner"></div>';
    document.querySelector('.gis-map-area').appendChild(overlay);
}

/**
 * Elimina el overlay de carga
 */
function removeLoadingOverlay() {
    const overlay = document.querySelector('.loading-overlay');
    if (overlay) overlay.remove();
}

/**
 * Formatea un número con separadores de miles
 * @param {number} value
 */
function formatNumber(value) {
    return parseFloat(value || 0).toLocaleString();
}

/**
 * Formatea metros con unidad
 * @param {number} value
 */
function formatMeters(value) {
    return `${formatNumber(value)} m`;
}