/**
 * UAI4D Club - Add Resource JavaScript
 */

$(document).ready(function() {
    'use strict';

    // ============================================================
    // PROFILE DROPDOWN
    // ============================================================
    $('#userAvatar').click(function(e) {
        e.stopPropagation();
        $('#profileDropdown').toggleClass('show');
    });

    $(document).click(function() {
        $('#profileDropdown').removeClass('show');
    });

    // ============================================================
    // FORM SUBMIT
    // ============================================================
    $('#addResourceForm').on('submit', function(e) {
        e.preventDefault();
        $('#alertContainer').empty();

        var title = $('#title').val().trim();
        var description = $('#description').val().trim();
        var resourceType = $('#resourceType').val();
        var resourceUrl = $('#resourceUrl').val().trim();
        var category = $('#category').val().trim();
        var isPremium = $('#isPremium').is(':checked') ? 'true' : 'false';

        // Validate
        var isValid = true;

        if (!title) {
            $('#title').addClass('is-invalid');
            isValid = false;
        } else {
            $('#title').removeClass('is-invalid').addClass('is-valid');
        }

        if (!description) {
            $('#description').addClass('is-invalid');
            isValid = false;
        } else {
            $('#description').removeClass('is-invalid').addClass('is-valid');
        }

        if (!resourceType) {
            $('#resourceType').addClass('is-invalid');
            isValid = false;
        } else {
            $('#resourceType').removeClass('is-invalid').addClass('is-valid');
        }

        if (!resourceUrl) {
            $('#resourceUrl').addClass('is-invalid');
            isValid = false;
        } else {
            $('#resourceUrl').removeClass('is-invalid').addClass('is-valid');
        }

        if (!isValid) {
            showAlert('Please fill in all required fields.', 'danger');
            return;
        }

        // Submit
        $('#submitBtn').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Adding...');

        var formData = {
            title: title,
            description: description,
            resourceType: resourceType,
            resourceUrl: resourceUrl,
            category: category,
            isPremium: isPremium
        };

        $.ajax({
            url: 'AddResourceServlet',
            method: 'POST',
            data: formData,
            dataType: 'json',
            success: function(response) {
                $('#submitBtn').prop('disabled', false).html('<i class="fas fa-save"></i> Add Resource');
                if (response.success) {
                    showAlert('✅ Resource added successfully!', 'success');
                    setTimeout(function() {
                        window.location.href = 'resources.html';
                    }, 2000);
                } else {
                    showAlert(response.message || 'Failed to add resource.', 'danger');
                }
            },
            error: function() {
                $('#submitBtn').prop('disabled', false).html('<i class="fas fa-save"></i> Add Resource');
                showAlert('An error occurred. Please try again.', 'danger');
            }
        });
    });

    // ============================================================
    // ALERT FUNCTION
    // ============================================================
    function showAlert(message, type) {
        var colors = {
            success: '#27ae60',
            danger: '#e74c3c',
            info: '#3498db'
        };
        var icons = {
            success: 'fa-check-circle',
            danger: 'fa-exclamation-circle',
            info: 'fa-info-circle'
        };
        var icon = icons[type] || icons.info;
        var color = colors[type] || '#D4AF37';

        var alertHtml = `
            <div class="alert-custom alert-${type}">
                <i class="fas ${icon}"></i> ${message}
            </div>
        `;

        $('#alertContainer').html(alertHtml);
        setTimeout(function() {
            $('.alert-custom').fadeOut(500, function() { $(this).remove(); });
        }, 5000);
    }

    // ============================================================
    // CLEAR VALIDATION
    // ============================================================
    $('.form-control').on('input change', function() {
        $(this).removeClass('is-valid is-invalid');
    });

    console.log('Add Resource page ready!');
});