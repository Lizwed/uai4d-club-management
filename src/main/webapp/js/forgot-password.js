$(document).ready(function() {
    'use strict';

    const $form = $('#forgotForm');
    const $email = $('#email');
    const $forgotBtn = $('#forgotBtn');
    const $alertContainer = $('#alertContainer');

    // Form Submit
    $form.on('submit', function(e) {
        e.preventDefault();
        $alertContainer.empty();
        
        const email = $email.val().trim();
        if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            $email.addClass('is-invalid');
            showAlert('Please enter a valid email address.', 'danger');
            return;
        }
        $email.removeClass('is-invalid').addClass('is-valid');
        
        $forgotBtn.addClass('loading').prop('disabled', true);
        
        $.ajax({
            url: 'ForgotPasswordServlet',
            method: 'POST',
            data: { email: email },
            dataType: 'json',
            success: function(response) {
                $forgotBtn.removeClass('loading').prop('disabled', false);
                if (response.success) {
                    showAlert('Reset link sent to your email! Check your inbox.', 'success');
                    setTimeout(function() {
                        window.location.href = 'login.html';
                    }, 3000);
                } else {
                    showAlert(response.message || 'Email not found. Please check and try again.', 'danger');
                }
            },
            error: function(xhr) {
                $forgotBtn.removeClass('loading').prop('disabled', false);
                let message = 'An error occurred. Please try again.';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                showAlert(message, 'danger');
            }
        });
    });
    
    function showAlert(message, type) {
        const icons = {
            success: 'fa-check-circle',
            danger: 'fa-exclamation-circle',
            info: 'fa-info-circle'
        };
        const icon = icons[type] || icons.info;
        $alertContainer.html('<div class="alert-custom alert-' + type + '"><i class="fas ' + icon + '"></i> ' + message + '</div>');
        setTimeout(function() {
            $alertContainer.find('.alert-custom').fadeOut(500, function() {
                $(this).remove();
            });
        }, 5000);
    }
    
    $email.on('input', function() {
        $(this).removeClass('is-valid is-invalid');
    });
});