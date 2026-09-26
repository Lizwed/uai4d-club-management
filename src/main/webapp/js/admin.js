/**
 * UAI4D Club - Admin Panel JavaScript
 */

$(document).ready(function() {
    'use strict';

    console.log('Admin panel loaded!');

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
    // LOAD ADMIN DATA
    // ============================================================
    function loadAdminData() {
        $.ajax({
            url: 'GetAdminStatsServlet',
            method: 'GET',
            dataType: 'json',
            success: function(data) {
                if (data.success) {
                    // Update stats
                    $('#totalUsers').text(data.users || 0);
                    $('#totalEvents').text(data.events || 0);
                    $('#totalProjects').text(data.projects || 0);
                    $('#totalResources').text(data.resources || 0);
                    
                    // Update management card counts
                    $('#userCount').text(data.users || 0);
                    $('#eventCount').text(data.events || 0);
                    $('#projectCount').text(data.projects || 0);
                    $('#resourceCount').text(data.resources || 0);
                    $('#announcementCount').text(data.announcements || 0);
                    
                    // Update user info
                    if (data.adminName) {
                        var name = data.adminName || 'Administrator';
                        $('#userFullName').text(name);
                        $('#dropdownName').text(name);
                        $('#userAvatar').text(name.charAt(0).toUpperCase());
                    }
                }
            },
            error: function() {
                console.log('Could not load admin data');
                // Set default values
                $('#totalUsers').text('0');
                $('#totalEvents').text('0');
                $('#totalProjects').text('0');
                $('#totalResources').text('0');
                $('#userCount').text('0');
                $('#eventCount').text('0');
                $('#projectCount').text('0');
                $('#resourceCount').text('0');
                $('#announcementCount').text('0');
            }
        });
    }

    // ============================================================
    // MANAGEMENT CARD CLICKS (Only for coming soon items)
    // ============================================================
    $('#announcementsCard').click(function(e) {
        e.preventDefault();
        showAlert('📌 Announcements feature coming soon!', 'info');
    });

    $('#settingsCard').click(function(e) {
        e.preventDefault();
        showAlert('📌 Settings feature coming soon!', 'info');
    });

    // ============================================================
    // REFRESH BUTTON
    // ============================================================
    $('#refreshBtn').click(function() {
        $(this).html('<i class="fas fa-spinner fa-spin"></i> Refreshing...');
        loadAdminData();
        setTimeout(function() {
            $('#refreshBtn').html('<i class="fas fa-sync-alt"></i> Refresh');
            showAlert('Data refreshed successfully!', 'success');
        }, 1000);
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
            <div class="alert-custom" style="
                position: fixed;
                top: 80px;
                right: 20px;
                max-width: 400px;
                z-index: 9999;
                background: #0a1628;
                color: #fff;
                border-left: 4px solid ${color};
                border-radius: 10px;
                padding: 15px 20px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                animation: slideInRight 0.4s ease;
            ">
                <i class="fas ${icon}" style="color: ${color}; margin-right: 10px;"></i>
                ${message}
            </div>
        `;
        
        $('body').append(alertHtml);
        setTimeout(function() {
            $('.alert-custom').fadeOut(500, function() { $(this).remove(); });
        }, 3000);
    }

    // ============================================================
    // ADD CSS ANIMATION
    // ============================================================
    $('head').append(`
        <style>
            @keyframes slideInRight {
                from { opacity: 0; transform: translateX(40px); }
                to { opacity: 1; transform: translateX(0); }
            }
        </style>
    `);

    // ============================================================
    // INITIALIZE
    // ============================================================
    loadAdminData();

    console.log('Admin panel ready!');
});