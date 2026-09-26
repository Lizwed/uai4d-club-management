/**
 * UAI4D Club - Create Event JavaScript
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
    // SET DEFAULT DATE (Now + 7 days)
    // ============================================================
    function setDefaultDate() {
        var now = new Date();
        var future = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
        var year = future.getFullYear();
        var month = String(future.getMonth() + 1).padStart(2, '0');
        var day = String(future.getDate()).padStart(2, '0');
        var hours = String(future.getHours()).padStart(2, '0');
        var minutes = String(future.getMinutes()).padStart(2, '0');
        $('#eventDate').val(year + '-' + month + '-' + day + 'T' + hours + ':' + minutes);
    }

    setDefaultDate();

    // ============================================================
    // FORM SUBMIT
    // ============================================================
    $('#createEventForm').on('submit', function(e) {
        e.preventDefault();
        $('#alertContainer').empty();

        var title = $('#title').val().trim();
        var description = $('#description').val().trim();
        var eventDate = $('#eventDate').val();
        var location = $('#location').val().trim();
        var eventType = $('#eventType').val();
        var maxAttendees = $('#maxAttendees').val();
        var status = $('#status').val();

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

        if (!eventDate) {
            $('#eventDate').addClass('is-invalid');
            isValid = false;
        } else {
            $('#eventDate').removeClass('is-invalid').addClass('is-valid');
        }

        if (!location) {
            $('#location').addClass('is-invalid');
            isValid = false;
        } else {
            $('#location').removeClass('is-invalid').addClass('is-valid');
        }

        if (!eventType) {
            $('#eventType').addClass('is-invalid');
            isValid = false;
        } else {
            $('#eventType').removeClass('is-invalid').addClass('is-valid');
        }

        if (!isValid) {
            showAlert('Please fill in all required fields.', 'danger');
            return;
        }

        // Submit
        $('#submitBtn').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Creating...');

        var formData = {
            title: title,
            description: description,
            eventDate: eventDate,
            location: location,
            eventType: eventType,
            maxAttendees: maxAttendees || 50,
            status: status || 'UPCOMING'
        };

        $.ajax({
            url: 'CreateEventServlet',
            method: 'POST',
            data: formData,
            dataType: 'json',
            success: function(response) {
                $('#submitBtn').prop('disabled', false).html('<i class="fas fa-save"></i> Create Event');
                if (response.success) {
                    showAlert('✅ Event created successfully!', 'success');
                    setTimeout(function() {
                        window.location.href = 'events.html';
                    }, 2000);
                } else {
                    showAlert(response.message || 'Failed to create event.', 'danger');
                }
            },
            error: function() {
                $('#submitBtn').prop('disabled', false).html('<i class="fas fa-save"></i> Create Event');
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

    console.log('Create Event page ready!');
});