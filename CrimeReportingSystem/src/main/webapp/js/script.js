// This file can be used for any general client-side JavaScript.
// For now, most of the interactive logic (like displaying messages from URL params)
// is embedded directly in the HTML files for simplicity as requested.

// Example of a simple client-side validation function (not used in current HTML forms)
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;

    let isValid = true;
    const inputs = form.querySelectorAll('input[required], textarea[required]');

    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.classList.add('is-invalid'); // Add Bootstrap validation style
            isValid = false;
        } else {
            input.classList.remove('is-invalid');
        }
    });

    return isValid;
}

// You can add more global client-side functions here as your project grows.
// For instance, functions for dynamic content loading, animations, etc.
