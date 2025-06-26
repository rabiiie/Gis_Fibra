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

// Ejecutar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    initializeContactForm();
});
