/**
 * UAI4D Club - Create Project JavaScript
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
    $('#createProjectForm').on('submit', function(e) {
        e.preventDefault();
        $('#alertContainer').empty();

        var name = $('#name').val().trim();
        var description = $('#description').val().trim();
        var status = $('#status').val();
        var techStack = $('#techStack').val().trim();
        var githubRepo = $('#githubRepo').val().trim();
        var startDate = $('#startDate').val();
        var endDate = $('#endDate').val();
        var objectives = $('#objectives').val().trim();

        // Validate
        var isValid = true;

        if (!name) {
            $('#name').addClass('is-invalid');
            isValid = false;
        } else {
            $('#name').removeClass('is-invalid').addClass('is-valid');
        }

        if (!description) {
            $('#description').addClass('is-invalid');
            isValid = false;
        } else {
            $('#description').removeClass('is-invalid').addClass('is-valid');
        }

        if (!status) {
            $('#status').addClass('is-invalid');
            isValid = false;
        } else {
            $('#status').removeClass('is-invalid').addClass('is-valid');
        }

        if (!isValid) {
            showAlert('Please fill in all required fields.', 'danger');
            return;
        }

        // Submit
        $('#submitBtn').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Creating...');

        var formData = {
            name: name,
            description: description,
            status: status,
            techStack: techStack,
            githubRepo: githubRepo,
            startDate: startDate,
            endDate: endDate,
            objectives: objectives
        };

        $.ajax({
            url: 'CreateProjectServlet',
            method: 'POST',
            data: formData,
            dataType: 'json',
            success: function(response) {
                $('#submitBtn').prop('disabled', false).html('<i class="fas fa-save"></i> Create Project');
                if (response.success) {
                    showAlert('✅ Project created successfully!', 'success');
                    setTimeout(function() {
                        window.location.href = 'projects.html';
                    }, 2000);
                } else {
                    showAlert(response.message || 'Failed to create project.', 'danger');
                }
            },
            error: function() {
                $('#submitBtn').prop('disabled', false).html('<i class="fas fa-save"></i> Create Project');
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

    console.log('Create Project page ready!');
});