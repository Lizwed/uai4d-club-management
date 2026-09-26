/**
 * UAI4D Club - Profile JavaScript
 */

$(document).ready(function() {
    'use strict';

    console.log('Profile page loaded!');

    // ============================================================
    // LOAD USER PROFILE
    // ============================================================
    function loadProfile() {
        $.ajax({
            url: 'GetUserInfoServlet',
            method: 'GET',
            dataType: 'json',
            success: function(response) {
                if (response.success) {
                    var user = response;
                    
                    // Update header
                    $('#profileName').text(user.fullName || 'Member');
                    $('#profileEmail').text(user.email || 'member@uai4d.com');
                    $('#profileRole').text(user.role || 'MEMBER');
                    $('#memberSince').text('2026');
                    
                    var initial = (user.fullName || 'M').charAt(0).toUpperCase();
                    $('#profileAvatar').text(initial);
                    $('#userAvatar').text(initial);
                    $('#userFullName').text(user.fullName || 'Member');
                    $('#userRole').text(user.role || 'MEMBER');
                    
                    // Update form
                    $('#fullName').val(user.fullName || '');
                    $('#username').val(user.username || '');
                    $('#email').val(user.email || '');
                    $('#course').val(user.course || '');
                    $('#yearOfStudy').val(user.yearOfStudy || '');
                    $('#skills').val(user.skills || '');
                    $('#bio').val(user.bio || '');
                    $('#university').val(user.university || '');
                    $('#aiExperience').val(user.aiExperience || '');
                    
                    // Update stats
                    if (user.stats) {
                        $('#eventCount').text(user.stats.events || 0);
                        $('#projectCount').text(user.stats.projects || 0);
                    }
                    
                    // Update completion status
                    updateCompletionStatus();
                }
            },
            error: function() {
                showAlert('Could not load profile data', 'danger');
            }
        });
    }

    // ============================================================
    // UPDATE COMPLETION STATUS
    // ============================================================
    function updateCompletionStatus() {
        var fields = {
            fullName: $('#fullName').val().trim(),
            course: $('#course').val().trim(),
            yearOfStudy: $('#yearOfStudy').val(),
            university: $('#university').val(),
            aiExperience: $('#aiExperience').val(),
            skills: $('#skills').val().trim(),
            bio: $('#bio').val().trim()
        };
        
        var totalFields = Object.keys(fields).length;
        var completed = 0;
        
        $.each(fields, function(key, value) {
            var $item = $('.completion-items .item[data-field="' + key + '"]');
            if (value && value !== '') {
                completed++;
                $item.removeClass('pending').addClass('completed');
                $item.find('i').removeClass('fa-circle').addClass('fa-check-circle');
            } else {
                $item.removeClass('completed').addClass('pending');
                $item.find('i').removeClass('fa-check-circle').addClass('fa-circle');
            }
        });
        
        var percentage = Math.round((completed / totalFields) * 100);
        $('#completionPercentage').text(percentage + '%');
        $('#completionBar').css('width', percentage + '%');
        
        // Color coding
        if (percentage === 100) {
            $('#completionBar').css('background', 'linear-gradient(90deg, #27ae60, #2ecc71)');
        } else if (percentage >= 70) {
            $('#completionBar').css('background', 'linear-gradient(90deg, var(--gold), var(--gold-light))');
        } else if (percentage >= 40) {
            $('#completionBar').css('background', 'linear-gradient(90deg, #f39c12, #f1c40f)');
        } else {
            $('#completionBar').css('background', 'linear-gradient(90deg, #e74c3c, #f39c12)');
        }
    }

    // ============================================================
    // SAVE PROFILE
    // ============================================================
    $('#profileForm').on('submit', function(e) {
        e.preventDefault();
        
        var formData = {
            fullName: $('#fullName').val().trim(),
            course: $('#course').val().trim(),
            yearOfStudy: $('#yearOfStudy').val(),
            university: $('#university').val(),
            aiExperience: $('#aiExperience').val(),
            skills: $('#skills').val().trim(),
            bio: $('#bio').val().trim(),
            interests: $('#interests').val() ? $('#interests').val().join(',') : ''
        };
        
        // Validate required fields
        var requiredFields = ['fullName', 'course', 'yearOfStudy', 'university', 'aiExperience', 'skills', 'bio'];
        var isValid = true;
        
        $.each(requiredFields, function(index, field) {
            if (!formData[field] || formData[field] === '') {
                $('#' + field).addClass('is-invalid');
                isValid = false;
            } else {
                $('#' + field).removeClass('is-invalid').addClass('is-valid');
            }
        });
        
        if (!isValid) {
            showAlert('Please fill in all required fields (marked with *)', 'danger');
            return;
        }
        
        $('#saveBtn').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Saving...');
        
        $.ajax({
            url: 'UpdateProfileServlet',
            method: 'POST',
            data: formData,
            dataType: 'json',
            success: function(response) {
                $('#saveBtn').prop('disabled', false).html('<i class="fas fa-save"></i> Save Changes');
                if (response.success) {
                    showAlert('Profile updated successfully!', 'success');
                    loadProfile();
                } else {
                    showAlert(response.message || 'Update failed', 'danger');
                }
            },
            error: function() {
                $('#saveBtn').prop('disabled', false).html('<i class="fas fa-save"></i> Save Changes');
                showAlert('An error occurred. Please try again.', 'danger');
            }
        });
    });

    // ============================================================
    // CHANGE PASSWORD
    // ============================================================
    $('#passwordForm').on('submit', function(e) {
        e.preventDefault();
        
        var current = $('#currentPassword').val();
        var newPass = $('#newPassword').val();
        var confirm = $('#confirmPassword').val();
        
        if (!current || !newPass || !confirm) {
            showAlert('Please fill in all password fields.', 'danger');
            return;
        }
        
        if (newPass !== confirm) {
            showAlert('New passwords do not match.', 'danger');
            return;
        }
        
        if (newPass.length < 8) {
            showAlert('Password must be at least 8 characters.', 'danger');
            return;
        }
        
        $('#passwordBtn').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Updating...');
        
        $.ajax({
            url: 'ChangePasswordServlet',
            method: 'POST',
            data: {
                currentPassword: current,
                newPassword: newPass
            },
            dataType: 'json',
            success: function(response) {
                $('#passwordBtn').prop('disabled', false).html('<i class="fas fa-key"></i> Update Password');
                if (response.success) {
                    showAlert('Password changed successfully!', 'success');
                    $('#currentPassword, #newPassword, #confirmPassword').val('');
                } else {
                    showAlert(response.message || 'Password change failed', 'danger');
                }
            },
            error: function() {
                $('#passwordBtn').prop('disabled', false).html('<i class="fas fa-key"></i> Update Password');
                showAlert('An error occurred. Please try again.', 'danger');
            }
        });
    });

    // ============================================================
    // REAL-TIME COMPLETION UPDATE
    // ============================================================
    $('#profileForm input, #profileForm select, #profileForm textarea').on('change input', function() {
        updateCompletionStatus();
        $(this).removeClass('is-valid is-invalid');
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
        
        var alertHtml = `
            <div class="alert-custom" style="
                position: fixed;
                top: 80px;
                right: 20px;
                max-width: 400px;
                z-index: 9999;
                background: #0a1628;
                color: #fff;
                border-left: 4px solid ${colors[type] || '#D4AF37'};
                border-radius: 10px;
                padding: 15px 20px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                animation: slideInRight 0.4s ease;
            ">
                <i class="fas ${icons[type] || 'fa-info-circle'}" style="color: ${colors[type] || '#D4AF37'}; margin-right: 10px;"></i>
                ${message}
            </div>
        `;
        
        $('body').append(alertHtml);
        setTimeout(function() {
            $('.alert-custom').fadeOut(500, function() { $(this).remove(); });
        }, 4000);
    }

    // ============================================================
    // INITIALIZE
    // ============================================================
    loadProfile();
    
    console.log('Profile page ready!');
});