const csrfToken = $('meta[name="_csrf"]').attr('content');
const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

function setCsrfHeader(xhr) {
    xhr.setRequestHeader(csrfHeader, csrfToken);
}

$('#addForm').on('submit', function (event) {
    event.preventDefault();
    const data = collectFormData('add');
    submitContact('/contacts/add', data);
});

$('#editForm').on('submit', function (event) {
    event.preventDefault();

    const data = {
        resourceName: $('#editResourceName').val(),
        givenName: $('#editFirstName').val(),
        familyName: $('#editLastName').val(),
        emails: $('input[name="editEmail"]').map(function () { return $(this).val(); }).get(),
        phones: $('input[name="editPhone"]').map(function () { return $(this).val(); }).get()
    };

    submitContact('/contacts/update', data);
});

function deleteContact(resourceName) {
    if (!confirm('Are you sure you want to delete this contact?')) {
        return; 
    }

    $.ajax({
        url: '/contacts/delete',
        type: 'POST',
        contentType: 'application/x-www-form-urlencoded',
        data: { resourceName: resourceName }, 
        beforeSend: setCsrfHeader,
        success: function (response) {
            console.log('Contact deleted:', response);
            location.reload(); 
        },
        error: function (xhr) {
            console.error('Error deleting contact:', xhr.responseJSON ? xhr.responseJSON.error : xhr.statusText);
            alert('Failed to delete contact.');
        }
    });
}

function addField(containerId, inputType) {
    const container = document.getElementById(containerId);
    const newInputRow = document.createElement('div');
    newInputRow.className = 'input-row';
    newInputRow.innerHTML = `
        <input type="${inputType === 'email' ? 'email' : 'text'}" name="${getInputName(containerId)}" required>
        <button type="button" class="icon-btn minus" onclick="removeField(this, '${containerId}', '${inputType}')">-</button>
    `;
    container.appendChild(newInputRow);
    updatePlusIcons(containerId, inputType);
}

function removeField(button, containerId, inputType) {
    button.parentElement.remove();
    updatePlusIcons(containerId, inputType);
}

function updatePlusIcons(containerId, inputType) {
    const container = document.getElementById(containerId);
    const rows = container.querySelectorAll('.input-row');

    container.querySelectorAll('.plus').forEach(btn => btn.remove());

    if (rows.length > 0) {
        rows.forEach((row, index) => {
            const minusButton = row.querySelector('.minus');
            if (index === 0) {
                if (minusButton) minusButton.style.display = 'none';
            } else {
                if (minusButton) minusButton.style.display = 'inline-flex'; 
            }
        });

        const lastRow = rows[rows.length - 1];
        const plusButton = document.createElement('button');
        plusButton.type = 'button';
        plusButton.className = 'icon-btn plus';
        plusButton.textContent = '+';
        plusButton.onclick = () => addField(containerId, inputType);
        lastRow.appendChild(plusButton);
    }
}

function showAddPopup() {
    document.getElementById('addPopup').style.display = 'block';
    document.getElementById('overlay').style.display = 'block';
    updatePlusIcons('addEmails', 'email');
    updatePlusIcons('addPhones', 'phone');
}

function showEditPopup(resourceName, name, email, phone) {
    document.getElementById('editResourceName').value = resourceName;

    const [firstName, ...lastName] = name.split(' ');
    document.getElementById('editFirstName').value = firstName || '';
    document.getElementById('editLastName').value = lastName.join(' ') || '';

    const emailContainer = document.getElementById('editEmails');
    const phoneContainer = document.getElementById('editPhones');
    emailContainer.innerHTML = '';
    phoneContainer.innerHTML = '';

    (email ? email.split(', ') : ['']).forEach((emailValue, index) => {
        const newInputRow = document.createElement('div');
        newInputRow.className = 'input-row';
        newInputRow.innerHTML = `
            <input type="email" name="editEmail" value="${emailValue}" required>
            ${index > 0 ? `<button type="button" class="icon-btn minus" onclick="removeField(this, 'editEmails', 'email')">-</button>` : ''}
        `;
        emailContainer.appendChild(newInputRow);
    });

    (phone ? phone.split(', ') : ['']).forEach((phoneValue, index) => {
        const newInputRow = document.createElement('div');
        newInputRow.className = 'input-row';
        newInputRow.innerHTML = `
            <input type="text" name="editPhone" value="${phoneValue}" required>
            ${index > 0 ? `<button type="button" class="icon-btn minus" onclick="removeField(this, 'editPhones', 'phone')">-</button>` : ''}
        `;
        phoneContainer.appendChild(newInputRow);
    });

    updatePlusIcons('editEmails', 'email');
    updatePlusIcons('editPhones', 'phone');

    document.getElementById('editPopup').style.display = 'block';
    document.getElementById('overlay').style.display = 'block';
}

function closeAddPopup() {
    document.getElementById('addPopup').style.display = 'none';
    document.getElementById('overlay').style.display = 'none';
}

function closeEditPopup() {
    document.getElementById('editPopup').style.display = 'none';
    document.getElementById('overlay').style.display = 'none';
}

function collectFormData(action) {
    return {
        resourceName: action === 'edit' ? $('#editResourceName').val() : '',
        givenName: $(`#${action}FirstName`).val(),
        familyName: $(`#${action}LastName`).val(),
        emails: $(`input[name="${action}Email"]`).map(function () { return $(this).val(); }).get(),
        phones: $(`input[name="${action}Phone"]`).map(function () { return $(this).val(); }).get()
    };
}

function submitContact(url, data) {
    console.log('Submitting data:', data);

    $.ajax({
        url: url,
        type: 'POST',
        contentType: 'application/x-www-form-urlencoded',
        data: $.param(data),
        beforeSend: setCsrfHeader,
        success: function () {
            location.reload();
        },
        error: function (error) {
            console.error('Error:', error.responseJSON ? error.responseJSON.error : error.statusText);
            alert('Failed to process contact: ' + (error.responseJSON ? error.responseJSON.error : 'Unknown error'));
        }
    });
}

function getInputName(containerId) {
    return containerId.includes('Email') ? 
        (containerId === 'addEmails' ? 'addEmail' : 'editEmail') : 
        (containerId === 'addPhones' ? 'addPhone' : 'editPhone');
}

document.addEventListener('DOMContentLoaded', () => {
    ['addEmails', 'addPhones', 'editEmails', 'editPhones'].forEach(id => {
        const inputType = id.includes('Email') ? 'email' : 'phone';
        updatePlusIcons(id, inputType);
    });
});
