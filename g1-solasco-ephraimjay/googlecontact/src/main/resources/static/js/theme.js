// Theme management
function getTheme() {
    return localStorage.getItem('theme') || 'light';
}

function setTheme(theme) {
    localStorage.setItem('theme', theme);
    document.documentElement.setAttribute('data-theme', theme);
    document.body.style.background = theme === 'dark' ? 
        'var(--bg-gradient-dark)' : 
        'var(--bg-gradient-light)';
    updateThemeToggleButton(theme);
}

function updateThemeToggleButton(theme) {
    const button = document.getElementById('theme-toggle');
    if (button) {
        button.innerHTML = theme === 'dark' ? 
            '<i class="bi bi-sun-fill"></i>' : 
            '<i class="bi bi-moon-fill"></i>';
        button.setAttribute('title', `Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`);
        button.setAttribute('aria-label', `Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`);
    }
}

function toggleTheme() {
    const currentTheme = getTheme();
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
}

// Search functionality
function setupSearch() {
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const searchTerm = e.target.value.toLowerCase().trim();
            const rows = document.querySelectorAll('.contact-table tbody tr:not([data-empty-state])');
            const emptyStateRow = document.querySelector('.contact-table tbody tr[data-empty-state]');
            
            let hasVisibleRows = false;
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                const isVisible = text.includes(searchTerm);
                row.style.display = isVisible ? '' : 'none';
                if (isVisible) hasVisibleRows = true;
            });

            // Show/hide empty state message
            if (emptyStateRow) {
                emptyStateRow.style.display = !hasVisibleRows && searchTerm ? '' : 'none';
                if (!hasVisibleRows && searchTerm) {
                    const noResultsMessage = emptyStateRow.querySelector('p');
                    if (noResultsMessage) {
                        noResultsMessage.textContent = `No contacts found matching "${searchTerm}"`;
                    }
                }
            }
        });

        // Add clear search button functionality
        const searchBox = document.querySelector('.search-box');
        const clearButton = searchBox?.querySelector('.search-clear-btn');
        
        if (clearButton) {
            clearButton.addEventListener('click', () => {
                searchInput.value = '';
                searchInput.focus();
                searchInput.dispatchEvent(new Event('input'));
                clearButton.style.display = 'none';
            });

            searchInput.addEventListener('input', () => {
                clearButton.style.display = searchInput.value ? 'block' : 'none';
            });
        }
    }
}

// Initialize theme and search
document.addEventListener('DOMContentLoaded', () => {
    const savedTheme = getTheme();
    setTheme(savedTheme);
    setupSearch();
});
