$(document).ready(function() {
    'use strict';

    console.log('jQuery loaded!');
    console.log('register.js loaded!');

    const $form = $('#registerForm');
    const $fullName = $('#fullName');
    const $username = $('#username');
    const $email = $('#email');
    const $password = $('#password');
    const $confirmPassword = $('#confirmPassword');
    const $yearOfStudy = $('#yearOfStudy');
    const $termsCheck = $('#termsCheck');
    const $registerBtn = $('#registerBtn');
    const $togglePassword = $('#togglePassword');
    const $strengthBar = $('#strengthBar');
    const $alertContainer = $('#alertContainer');
    const $usernameAvailability = $('#usernameAvailability');
    const $passwordMatchMsg = $('#passwordMatchMsg');

    console.log('All DOM elements found!');

    function debounce(func, wait) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    // Toggle Password
    $togglePassword.on('click', function() {
        const type = $password.attr('type') === 'password' ? 'text' : 'password';
        $password.attr('type', type);
        $(this).find('i').toggleClass('fa-eye fa-eye-slash');
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

    // Username Availability
    const checkUsername = debounce(function() {
        const username = $username.val().trim();
        if (username.length < 3) {
            $usernameAvailability.text('').removeClass('available unavailable');
            return;
        }
        
        $usernameAvailability.text('Checking...').addClass('checking');
        
        $.ajax({
            url: 'CheckUsernameServlet',
            method: 'GET',
            data: { username: username },
            dataType: 'json',
            success: function(response) {
                if (response.available) {
                    $usernameAvailability.text('Username available').addClass('available').removeClass('unavailable checking');
                    $username.removeClass('is-invalid').addClass('is-valid');
                } else {
                    $usernameAvailability.text('Username taken').addClass('unavailable').removeClass('available checking');
                    $username.removeClass('is-valid').addClass('is-invalid');
                }
            },
            error: function() {
                $usernameAvailability.text('Error checking').removeClass('available unavailable checking');
            }
        });
    }, 500);

    $username.on('input', function() {
        if ($(this).val().trim().length >= 3) {
            checkUsername();
        } else {
            $usernameAvailability.text('').removeClass('available unavailable checking');
            $(this).removeClass('is-valid is-invalid');
        }
    });

    // Form Submit
    $form.on('submit', function(e) {
        e.preventDefault();
        console.log('Form submitted!');
        $alertContainer.empty();
        
        let isValid = true;
        
        if (!$fullName.val().trim()) {
            $fullName.addClass('is-invalid');
            isValid = false;
        } else {
            $fullName.removeClass('is-invalid').addClass('is-valid');
        }
        
        const username = $username.val().trim();
        if (username.length < 3) {
            $username.addClass('is-invalid');
            isValid = false;
        } else {
            $username.removeClass('is-invalid').addClass('is-valid');
        }
        
        const email = $email.val().trim();
        if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            $email.addClass('is-invalid');
            isValid = false;
        } else {
            $email.removeClass('is-invalid').addClass('is-valid');
        }
        
        const pwd = $password.val();
        if (pwd.length < 8) {
            isValid = false;
        }
        
        if (pwd !== $confirmPassword.val() || $confirmPassword.val().length === 0) {
            $confirmPassword.addClass('is-invalid');
            isValid = false;
        } else {
            $confirmPassword.removeClass('is-invalid').addClass('is-valid');
        }
        
        if (!$termsCheck.is(':checked')) {
            $termsCheck.addClass('is-invalid');
            isValid = false;
        } else {
            $termsCheck.removeClass('is-invalid').addClass('is-valid');
        }
        
        if (!isValid) {
            showAlert('Please fix all errors before submitting.', 'danger');
            return;
        }
        
        submitRegistration();
    });

    // Submit
    function submitRegistration() {
        console.log('submitRegistration() called');
        $registerBtn.addClass('loading').prop('disabled', true);
        
        const formData = {
            fullName: $fullName.val().trim(),
            username: $username.val().trim(),
            email: $email.val().trim(),
            password: $password.val(),
            yearOfStudy: $yearOfStudy.val()
        };
        
        console.log('Sending data:', formData);
        
        $.ajax({
            url: 'RegisterServlet',
            method: 'POST',
            data: formData,
            dataType: 'json',
            success: function(response) {
                console.log('AJAX Success:', response);
                $registerBtn.removeClass('loading').prop('disabled', false);
                if (response.success) {
                    showAlert(response.message, 'success');
                    setTimeout(function() {
                        window.location.href = 'login.html?registered=1';
                    }, 2000);
                } else {
                    showAlert(response.message || 'Registration failed.', 'danger');
                }
            },
            error: function(xhr, status, error) {
                console.error('AJAX Error:', error);
                console.error('Status:', status);
                console.error('Response:', xhr.responseText);
                $registerBtn.removeClass('loading').prop('disabled', false);
                showAlert('An error occurred. Please try again.', 'danger');
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

    $termsCheck.on('change', function() {
        $(this).removeClass('is-valid is-invalid');
    });

    console.log('UAI4D Registration fully loaded!');
});