$(document).ready(function() {
    'use strict';

    const $form = $('#resetForm');
    const $password = $('#password');
    const $confirmPassword = $('#confirmPassword');
    const $resetBtn = $('#resetBtn');
    const $togglePassword = $('#togglePassword');
    const $showPassword = $('#showPassword');
    const $strengthBar = $('#strengthBar');
    const $alertContainer = $('#alertContainer');
    const $passwordMatchMsg = $('#passwordMatchMsg');

    // Get token from URL
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    
    if (token) {
        $('#token').val(token);
    } else {
        showAlert('Invalid or missing reset token. Please request a new password reset.', 'danger');
        setTimeout(function() {
            window.location.href = 'forgot-password.html';
        }, 3000);
    }

    // Toggle Password (Eye Icon)
    $togglePassword.on('click', function() {
        const type = $password.attr('type') === 'password' ? 'text' : 'password';
        $password.attr('type', type);
        $(this).find('i').toggleClass('fa-eye fa-eye-slash');
        $showPassword.prop('checked', type === 'text');
    });

    // Show Password Checkbox
    $showPassword.on('change', function() {
        const type = $(this).is(':checked') ? 'text' : 'password';
        $password.attr('type', type);
        $togglePassword.find('i').toggleClass('fa-eye fa-eye-slash');
    });

    // Password Strength
    $password.on('input', function() {
        const pwd = $(this).val();
        let score = 0;
        
        if (pwd.length >= 8) score++;
        if (/[A-Z]/.test(pwd)) score++;
        if (/[a-z]/.test(pwd)) score++;
        if (/\d/.test(pwd)) score++;
        if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]/.test(pwd)) score++;
        
        $strengthBar.removeClass('weak fair good strong');
        
        if (score === 0) {
            $strengthBar.css('width', '0%');
        } else if (score <= 2) {
            $strengthBar.addClass('weak').css('width', '25%');
        } else if (score <= 3) {
            $strengthBar.addClass('fair').css('width', '50%');
        } else if (score <= 4) {
            $strengthBar.addClass('good').css('width', '75%');
        } else {
            $strengthBar.addClass('strong').css('width', '100%');
        }
        
        checkPasswordMatch();
    });

    // Password Match
    function checkPasswordMatch() {
        const pwd = $password.val();
        const confirm = $confirmPassword.val();
        
        if (confirm.length === 0) {
            $passwordMatchMsg.text('').removeClass('match no-match');
            return;
        }
        
        if (pwd === confirm) {
            $passwordMatchMsg.text('Passwords match').addClass('match').removeClass('no-match');
            $confirmPassword.removeClass('is-invalid').addClass('is-valid');
        } else {
            $passwordMatchMsg.text('Passwords do not match').addClass('no-match').removeClass('match');
            $confirmPassword.removeClass('is-valid').addClass('is-invalid');
        }
    }

    $confirmPassword.on('input', checkPasswordMatch);

    // Form Submit
    $form.on('submit', function(e) {
        e.preventDefault();
        $alertContainer.empty();
        
        const token = $('#token').val();
        const password = $password.val();
        const confirmPassword = $confirmPassword.val();
        
        if (!token) {
            showAlert('Invalid reset token. Please request a new password reset.', 'danger');
            return;
        }
        
        if (password.length < 8) {
            showAlert('Password must be at least 8 characters.', 'danger');
            $password.addClass('is-invalid');
            return;
        }
        
        if (!/[A-Z]/.test(password) || !/[a-z]/.test(password) || !/\d/.test(password) || !/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]/.test(password)) {
            showAlert('Password must include uppercase, lowercase, number, and special character.', 'danger');
            $password.addClass('is-invalid');
            return;
        }
        
        if (password !== confirmPassword) {
            showAlert('Passwords do not match.', 'danger');
            $confirmPassword.addClass('is-invalid');
            return;
        }
        
        $password.removeClass('is-invalid');
        $confirmPassword.removeClass('is-invalid');
        
        submitReset(token, password);
    });

    // Submit Reset
    function submitReset(token, password) {
        $resetBtn.addClass('loading').prop('disabled', true);
        
        $.ajax({
            url: 'ResetPasswordServlet',
            method: 'POST',
            data: {
                token: token,
                password: password
            },
            dataType: 'json',
            success: function(response) {
                $resetBtn.removeClass('loading').prop('disabled', false);
                
                if (response.success) {
                    showAlert('Password reset successful! Redirecting to login...', 'success');
                    setTimeout(function() {
                        window.location.href = 'login.html?reset=1';
                    }, 2000);
                } else {
                    showAlert(response.message || 'Failed to reset password. Please try again.', 'danger');
                }
            },
            error: function(xhr) {
                $resetBtn.removeClass('loading').prop('disabled', false);
                let message = 'An error occurred. Please try again.';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                showAlert(message, 'danger');
            }
        });
    }

    // Alert
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

    // Clear validation
    $('.form-control').on('input', function() {
        $(this).removeClass('is-valid is-invalid');
    });

    console.log('UAI4D Reset Password loaded!');
});