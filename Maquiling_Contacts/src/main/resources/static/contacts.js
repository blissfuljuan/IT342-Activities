// Open Edit Modal
function openModal(button) {
    document.getElementById('editModal').style.display = 'block';
    document.getElementById('editResourceName').value = button.getAttribute('data-resource-name');
    document.getElementById('editName').value = button.getAttribute('data-name');
    document.getElementById('editEmail').value = button.getAttribute('data-email');
    document.getElementById('editPhone').value = button.getAttribute('data-phone');
}

// Close Edit Modal
function closeModal() {
    document.getElementById('editModal').style.display = 'none';
}

// Open Delete Modal
function openDeleteModal(button) {
    document.getElementById('deleteModal').style.display = 'block';
    document.getElementById('deleteResourceName').value = button.getAttribute('data-resource-name');
}

// Close Delete Modal
function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
}

// ✅ Open Add Contact Modal
function openAddModal() {
    document.getElementById('addModal').style.display = 'block';
}

// ✅ Close Add Contact Modal
function closeAddModal() {
    document.getElementById('addModal').style.display = 'none';
}

// Close modals when clicking outside of them
window.onclick = function(event) {
    if (event.target == document.getElementById('editModal')) {
        closeModal();
    }
    if (event.target == document.getElementById('deleteModal')) {
        closeDeleteModal();
    }
    if (event.target == document.getElementById('addModal')) {
        closeAddModal();
    }
}
