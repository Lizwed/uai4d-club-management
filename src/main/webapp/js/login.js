/**
 * UAI4D Club - Login JavaScript
 * With Full Page Loader & Security Features
 */

$(document).ready(function() {
    'use strict';

    const $form = $('#loginForm');
    const $username = $('#username');
    const $password = $('#password');
    const $loginBtn = $('#loginBtn');
    const $togglePassword = $('#togglePassword');
    const $showPassword = $('#showPassword');
    const $alertContainer = $('#alertContainer');
    const $fullPageLoader = $('#fullPageLoader');

    // ============================================================
    // TOGGLE PASSWORD VISIBILITY (Eye Icon)
    // ============================================================
    $togglePassword.on('click', function() {
        const type = $password.attr('type') === 'password' ? 'text' : 'password';
        $password.attr('type', type);
        $(this).find('i').toggleClass('fa-eye fa-eye-slash');
        $showPassword.prop('checked', type === 'text');
    });

    // ============================================================
    // SHOW PASSWORD CHECKBOX
    // ============================================================
    $showPassword.on('change', function() {
        const type = $(this).is(':checked') ? 'text' : 'password';
        $password.attr('type', type);
        $togglePassword.find('i').toggleClass('fa-eye fa-eye-slash');
    });

    // ============================================================
    // CHECK URL PARAMS (Registration success, etc.)
    // ============================================================
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('registered') === '1') {
        showAlert('Registration successful! Please login.', 'success');
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    if (urlParams.get('reset') === '1') {
        showAlert('Password reset successful! Please login with your new password.', 'success');
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    // ============================================================
    // FORM SUBMIT
    // ============================================================
    $form.on('submit', function(e) {
        e.preventDefault();
        $alertContainer.empty();
        
        let isValid = true;
        
        const username = $username.val().trim();
        const password = $password.val().trim();
        
        if (!username) {
            $username.addClass('is-invalid');
            isValid = false;
        } else {
            $username.removeClass('is-invalid').addClass('is-valid');
        }
        
        if (!password) {
            $password.addClass('is-invalid');
            isValid = false;
        } else {
            $password.removeClass('is-invalid').addClass('is-valid');
        }
        
        if (!isValid) {
            showAlert('Please fill in all fields.', 'danger');
            return;
        }
        
        submitLogin(username, password);
    });

    // ============================================================
    // SUBMIT LOGIN (With Full Page Loader)
    // ============================================================
    function submitLogin(username, password) {
        // Show full page loader
        $fullPageLoader.addClass('show');
        $loginBtn.addClass('loading').prop('disabled', true);

        $.ajax({
            url: 'LoginServlet',
            method: 'POST',
            data: {
                username: username,
                password: password,
                rememberMe: $('#rememberMe').is(':checked') ? 'on' : ''
            },
            dataType: 'json',
            success: function(response) {
                $loginBtn.removeClass('loading').prop('disabled', false);
                
                if (response.success) {
                    // Show success message
                    showAlert('Login successful! Redirecting...', 'success');
                    
                    // Redirect to dashboard after short delay
                    setTimeout(function() {
                        window.location.href = response.redirect || 'dashboard.html';
                    }, 1200);
                } else {
                    // Hide loader on error
                    $fullPageLoader.removeClass('show');
                    showAlert(response.message || 'Invalid username or password.', 'danger');
                    $password.val('').focus();
                }
            },
            error: function(xhr) {
                $loginBtn.removeClass('loading').prop('disabled', false);
                $fullPageLoader.removeClass('show');
                let message = 'An error occurred. Please try again.';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                showAlert(message, 'danger');
            }
        });
    }

    // ============================================================
    // ALERT FUNCTION
    // ============================================================
    function showAlert(message, type) {
        const icons = {
            success: 'fa-check-circle',
            danger: 'fa-exclamation-circle',
            info: 'fa-info-circle'
        };
        const icon = icons[type] || icons.info;
        
        const alertHtml = `
            <div class="alert-custom alert-${type}">
                <i class="fas ${icon}"></i> ${message}
            </div>
        `;
        
        $alertContainer.html(alertHtml);
        
        setTimeout(function() {
            $alertContainer.find('.alert-custom').fadeOut(500, function() {
                $(this).remove();
            });
        }, 5000);
    }

    // ============================================================
    // CLEAR VALIDATION
    // ============================================================
    $('.form-control').on('input', function() {
        $(this).removeClass('is-valid is-invalid');
    });

    // ============================================================
    // ENTER KEY SUPPORT
    // ============================================================
    $('.form-control').on('keypress', function(e) {
        if (e.which === 13) {
            $form.submit();
        }
    });

    // ============================================================
    // SESSION CHECK - Redirect if already logged in
    // ============================================================
    function checkSession() {
        $.ajax({
            url: 'CheckSessionServlet',
            method: 'GET',
            dataType: 'json',
            success: function(response) {
                if (response.loggedIn) {
                    // Already logged in, redirect to dashboard
                    window.location.href = 'dashboard.html';
                }
            }
        });
    }

    // ============================================================
    // SECURITY: Disable right-click on login page
    // ============================================================
    document.addEventListener('contextmenu', function(e) {
        e.preventDefault();
        return false;
    });

    // ============================================================
    // SECURITY: Prevent back button after logout
    // ============================================================
    window.addEventListener('pageshow', function(e) {
        if (e.persisted) {
            // Page loaded from cache (back button)
            // Refresh to ensure session is checked
            window.location.reload();
        }
    });

    // Initialize - Check if already logged in
    checkSession();

    console.log('✅ UAI4D Login loaded with security features!');
});