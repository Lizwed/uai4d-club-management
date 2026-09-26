/**
 * UAI4D Club - Dashboard JavaScript
 * With Role-Based Feature Control
 */

$(document).ready(function() {

    console.log('Dashboard loaded!');

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
    // LOAD USER INFO FROM SESSION
    // ============================================================
    function loadUserInfo() {
        $.ajax({
            url: 'GetUserInfoServlet',
            method: 'GET',
            dataType: 'json',
            success: function(data) {
                if (data.success) {
                    var name = data.fullName || 'Member';
                    var role = data.role || 'MEMBER';
                    var email = data.email || 'member@uai4d.com';
                    var userId = data.userId || 0;
                    
                    // Update user info
                    $('#userFullName').text(name);
                    $('#welcomeName').text(name);
                    $('#dropdownName').text(name);
                    $('#dropdownEmail').text(email);
                    $('#userRole').text(role);
                    $('#userAvatar').text(name.charAt(0).toUpperCase());
                    
                    // Store role in data attribute for JS checks
                    $('body').data('userRole', role);
                    $('body').data('userId', userId);
                    
                    // ===== SHOW/HIDE ADMIN FEATURES =====
                    if (role === 'ADMIN') {
                        $('.admin-only').show();
                        // Show admin panel links
                        $('.admin-panel-link').show();
                        // Show manage users links
                        $('.manage-users-link').show();
                        // Show create event/project/resource links
                        $('.create-event-link').show();
                        $('.create-project-link').show();
                        $('.add-resource-link').show();
                        console.log('👑 Admin features enabled');
                    } else {
                        $('.admin-only').hide();
                        $('.admin-panel-link').hide();
                        $('.manage-users-link').hide();
                        $('.create-event-link').hide();
                        $('.create-project-link').hide();
                        $('.add-resource-link').hide();
                        console.log('👤 Member features enabled');
                    }
                    
                    // Update stats
                    if (data.stats) {
                        var stats = data.stats;
                        $('#totalMembers').text(stats.members || 0);
                        $('#totalEvents').text(stats.events || 0);
                        $('#totalProjects').text(stats.projects || 0);
                        $('#totalResources').text(stats.resources || 0);
                        $('#statMembers').text(stats.members || 0);
                        $('#statEvents').text(stats.events || 0);
                        $('#statProjects').text(stats.projects || 0);
                        $('#statResources').text(stats.resources || 0);
                    }
                }
            },
            error: function() {
                console.log('Could not load user info');
                // If session expired, redirect to login
                window.location.href = 'login.html';
            }
        });
    }

    // ============================================================
    // LOAD DASHBOARD STATS FROM DATABASE
    // ============================================================
    function loadDashboardStats() {
        $.ajax({
            url: 'GetDashboardStatsServlet',
            method: 'GET',
            dataType: 'json',
            success: function(data) {
                if (data.success) {
                    $('#totalMembers').text(data.members || 0);
                    $('#totalEvents').text(data.events || 0);
                    $('#totalProjects').text(data.projects || 0);
                    $('#totalResources').text(data.resources || 0);
                    $('#statMembers').text(data.members || 0);
                    $('#statEvents').text(data.events || 0);
                    $('#statProjects').text(data.projects || 0);
                    $('#statResources').text(data.resources || 0);
                }
            },
            error: function() {
                console.log('Could not load dashboard stats');
            }
        });
    }

    // ============================================================
    // LOAD ANNOUNCEMENTS
    // ============================================================
    function loadAnnouncements() {
        $('#announcementList').html('<div class="text-center text-muted py-3"><i class="fas fa-spinner fa-spin"></i> Loading...</div>');
        
        $.ajax({
            url: 'GetAnnouncementsServlet',
            method: 'GET',
            dataType: 'json',
            success: function(data) {
                if (data.success && data.announcements && data.announcements.length > 0) {
                    var html = '';
                    for (var i = 0; i < data.announcements.length; i++) {
                        var ann = data.announcements[i];
                        var priority = ann.priority || 'LOW';
                        var priorityClass = priority.toLowerCase();
                        var preview = ann.content ? ann.content.substring(0, 120) + '...' : '';
                        
                        html += '<div class="announcement-item">';
                        html += '  <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">';
                        html += '    <span class="ann-title">' + (ann.title || 'Announcement') + '</span>';
                        html += '    <span class="badge-priority ' + priorityClass + '">' + priority + '</span>';
                        html += '  </div>';
                        html += '  <span class="ann-date">' + (ann.date || 'Just now') + '</span>';
                        html += '  <p class="ann-preview">' + preview + '</p>';
                        html += '</div>';
                    }
                    $('#announcementList').html(html);
                } else {
                    showDefaultAnnouncements();
                }
            },
            error: function() {
                showDefaultAnnouncements();
            }
        });
    }

    // ============================================================
    // DEFAULT ANNOUNCEMENTS
    // ============================================================
    function showDefaultAnnouncements() {
        var html = '';
        html += '<div class="announcement-item">';
        html += '  <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">';
        html += '    <span class="ann-title">🚀 Welcome to UAI4D Club!</span>';
        html += '    <span class="badge-priority high">HIGH</span>';
        html += '  </div>';
        html += '  <span class="ann-date">August 25, 2026</span>';
        html += '  <p class="ann-preview">Welcome to the UDOM AI for Development community.</p>';
        html += '</div>';
        
        html += '<div class="announcement-item">';
        html += '  <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">';
        html += '    <span class="ann-title">📅 AI Workshop Coming Soon</span>';
        html += '    <span class="badge-priority medium">MEDIUM</span>';
        html += '  </div>';
        html += '  <span class="ann-date">August 20, 2026</span>';
        html += '  <p class="ann-preview">Join our upcoming AI workshop on September 5th.</p>';
        html += '</div>';
        
        html += '<div class="announcement-item">';
        html += '  <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">';
        html += '    <span class="ann-title">📚 New Learning Resources Added</span>';
        html += '    <span class="badge-priority low">LOW</span>';
        html += '  </div>';
        html += '  <span class="ann-date">August 15, 2026</span>';
        html += '  <p class="ann-preview">New Python and AI tutorials uploaded.</p>';
        html += '</div>';
        
        $('#announcementList').html(html);
    }

    // ============================================================
    // SESSION CHECK - Redirect if not logged in
    // ============================================================
    function checkSession() {
        $.ajax({
            url: 'CheckSessionServlet',
            method: 'GET',
            dataType: 'json',
            success: function(response) {
                if (!response.loggedIn) {
                    window.location.href = 'login.html';
                }
            },
            error: function() {
                window.location.href = 'login.html';
            }
        });
    }

    // ============================================================
    // SECURITY: Prevent back button after logout
    // ============================================================
    window.addEventListener('pageshow', function(e) {
        if (e.persisted) {
            // Page loaded from cache (back button)
            checkSession();
        }
    });

    // ============================================================
    // SECURITY: Disable right-click on admin elements
    // ============================================================
    $('.admin-only, .admin-panel-link, .manage-users-link, .create-event-link, .create-project-link, .add-resource-link')
        .on('contextmenu', function(e) {
            e.preventDefault();
            return false;
        });

    // ============================================================
    // INITIALIZE
    // ============================================================
    checkSession();
    loadDashboardStats();
    loadUserInfo();
    loadAnnouncements();

    console.log('Dashboard ready with role-based features!');
});