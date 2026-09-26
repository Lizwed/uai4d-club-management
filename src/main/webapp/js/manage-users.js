/**
 * UAI4D Club - Manage Users JavaScript
 */

$(document).ready(function() {
    'use strict';

    var currentPage = 1;
    var pageSize = 10;
    var totalUsers = 0;

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
    // LOAD USERS
    // ============================================================
    function loadUsers(page, search, role, status) {
        $('#usersTableBody').html(`
            <tr>
                <td colspan="6" class="text-center py-4">
                    <i class="fas fa-spinner fa-spin" style="color: var(--gold);"></i>
                    <span class="ms-2 text-muted">Loading users...</span>
                </td>
            </tr>
        `);

        var params = {
            page: page || 1,
            search: search || '',
            role: role || 'all',
            status: status || 'all'
        };

        console.log('Loading users with params:', params);

        $.ajax({
            url: 'GetUsersServlet',
            method: 'GET',
            data: params,
            dataType: 'json',
            success: function(data) {
                console.log('Users response:', data);
                if (data.success) {
                    renderUsers(data.users);
                    updatePagination(data.total, page);
                } else {
                    showAlert(data.message || 'Error loading users', 'danger');
                    renderEmptyState();
                }
            },
            error: function(xhr, status, error) {
                console.error('Error loading users:', error);
                showAlert('Error loading users. Please refresh the page.', 'danger');
                renderEmptyState();
            }
        });
    }

    // ============================================================
    // RENDER EMPTY STATE
    // ============================================================
    function renderEmptyState() {
        $('#usersTableBody').html(`
            <tr>
                <td colspan="6">
                    <div class="no-results">
                        <i class="fas fa-users"></i>
                        <h6>No users found</h6>
                        <p class="text-muted">Try adjusting your search or filters</p>
                    </div>
                </td>
            </tr>
        `);
    }

    // ============================================================
    // RENDER USERS
    // ============================================================
    function renderUsers(users) {
        var html = '';
        
        if (!users || users.length === 0) {
            renderEmptyState();
            return;
        }

        $.each(users, function(index, user) {
            var roleClass = user.role ? user.role.toLowerCase() : 'member';
            var statusClass = user.isActive ? 'active' : 'inactive';
            var statusText = user.isActive ? 'Active' : 'Inactive';
            var initials = user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U';
            var date = user.createdAt ? new Date(user.createdAt).toLocaleDateString('en-US', { 
                year: 'numeric', 
                month: 'short', 
                day: 'numeric' 
            }) : 'N/A';

            html += `
                <tr>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <div class="user-avatar-sm">${initials}</div>
                            <div>
                                <div style="font-weight: 600;">${user.fullName || 'Unknown'}</div>
                                <div style="font-size: 0.7rem; color: var(--text-light);">@${user.username || 'N/A'}</div>
                            </div>
                        </div>
                    </td>
                    <td>${user.email || 'N/A'}</td>
                    <td><span class="role-badge ${roleClass}">${user.role || 'MEMBER'}</span></td>
                    <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                    <td>${date}</td>
                    <td>
                        <div class="action-btns">
                            <!-- Edit User -->
                            <button class="btn btn-outline-gold btn-sm edit-user" data-id="${user.userId}" title="Edit User">
                                <i class="fas fa-edit"></i>
                            </button>
                            <!-- Promote/Demote User -->
                            <button class="btn btn-outline-success btn-sm promote-user" data-id="${user.userId}" data-role="${user.role}" title="Change Role">
                                <i class="fas fa-arrow-up"></i>
                            </button>
                            <!-- Activate/Deactivate User -->
                            <button class="btn btn-outline-danger btn-sm toggle-user" data-id="${user.userId}" data-status="${user.isActive}" title="Toggle Status">
                                <i class="fas ${user.isActive ? 'fa-ban' : 'fa-check'}"></i>
                            </button>
                            <!-- Reset Password -->
                            <button class="btn btn-outline-warning btn-sm reset-password" data-id="${user.userId}" data-name="${user.fullName}" title="Reset Password to Default (UAI4d@123)">
                                <i class="fas fa-key"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });

        $('#usersTableBody').html(html);
    }

    // ============================================================
    // UPDATE PAGINATION
    // ============================================================
    function updatePagination(total, page) {
        totalUsers = total;
        var totalPages = Math.max(1, Math.ceil(total / pageSize));
        var start = total > 0 ? (page - 1) * pageSize + 1 : 0;
        var end = Math.min(page * pageSize, total);

        $('#startCount').text(start);
        $('#endCount').text(end);
        $('#totalCount').text(total);

        var html = '';
        html += `<li class="page-item ${page <= 1 ? 'disabled' : ''}">
                    <a class="page-link" href="#" data-page="prev">Previous</a>
                </li>`;

        var maxPages = Math.min(totalPages, 5);
        for (var i = 1; i <= maxPages; i++) {
            var isActive = i === page ? 'active' : '';
            html += `<li class="page-item ${isActive}">
                        <a class="page-link" href="#" data-page="${i}">${i}</a>
                    </li>`;
        }

        if (totalPages > 5) {
            html += `<li class="page-item disabled"><a class="page-link" href="#">...</a></li>`;
            html += `<li class="page-item">
                        <a class="page-link" href="#" data-page="${totalPages}">${totalPages}</a>
                    </li>`;
        }

        html += `<li class="page-item ${page >= totalPages ? 'disabled' : ''}">
                    <a class="page-link" href="#" data-page="next">Next</a>
                </li>`;

        $('#paginationControls').html(html);
    }

    // ============================================================
    // EVENT HANDLERS
    // ============================================================
    
    // Pagination clicks
    $(document).on('click', '.pagination .page-link', function(e) {
        e.preventDefault();
        var page = $(this).data('page');
        var totalPages = Math.max(1, Math.ceil(totalUsers / pageSize));

        if (page === 'prev') {
            page = Math.max(1, currentPage - 1);
        } else if (page === 'next') {
            page = Math.min(totalPages, currentPage + 1);
        } else {
            page = parseInt(page);
        }

        if (page !== currentPage) {
            currentPage = page;
            loadUsers(currentPage, $('#searchInput').val(), $('#roleFilter').val(), $('#statusFilter').val());
        }
    });

    // Search with debounce
    var searchTimeout;
    $('#searchInput').on('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function() {
            currentPage = 1;
            loadUsers(1, $('#searchInput').val(), $('#roleFilter').val(), $('#statusFilter').val());
        }, 500);
    });

    // Role filter
    $('#roleFilter').on('change', function() {
        currentPage = 1;
        loadUsers(1, $('#searchInput').val(), $(this).val(), $('#statusFilter').val());
    });

    // Status filter
    $('#statusFilter').on('change', function() {
        currentPage = 1;
        loadUsers(1, $('#searchInput').val(), $('#roleFilter').val(), $(this).val());
    });

    // Edit user
    $(document).on('click', '.edit-user', function() {
        var id = $(this).data('id');
        showAlert('Edit user ID: ' + id + ' coming soon!', 'info');
    });

    // Promote user
    $(document).on('click', '.promote-user', function() {
        var id = $(this).data('id');
        var currentRole = $(this).data('role');
        var newRole = currentRole === 'ADMIN' ? 'MEMBER' : 'ADMIN';
        
        if (confirm('Change user role to ' + newRole + '?')) {
            $.ajax({
                url: 'UpdateUserRoleServlet',
                method: 'POST',
                data: { userId: id, role: newRole },
                dataType: 'json',
                success: function(response) {
                    if (response.success) {
                        showAlert('User role updated successfully!', 'success');
                        loadUsers(currentPage, $('#searchInput').val(), $('#roleFilter').val(), $('#statusFilter').val());
                    } else {
                        showAlert(response.message || 'Update failed', 'danger');
                    }
                },
                error: function() {
                    showAlert('Error updating user role', 'danger');
                }
            });
        }
    });

    // Toggle user status
    $(document).on('click', '.toggle-user', function() {
        var id = $(this).data('id');
        var currentStatus = $(this).data('status');
        var action = currentStatus ? 'deactivate' : 'activate';
        
        if (confirm('Are you sure you want to ' + action + ' this user?')) {
            $.ajax({
                url: 'ToggleUserStatusServlet',
                method: 'POST',
                data: { userId: id, action: action },
                dataType: 'json',
                success: function(response) {
                    if (response.success) {
                        showAlert('User ' + action + 'd successfully!', 'success');
                        loadUsers(currentPage, $('#searchInput').val(), $('#roleFilter').val(), $('#statusFilter').val());
                    } else {
                        showAlert(response.message || 'Action failed', 'danger');
                    }
                },
                error: function() {
                    showAlert('Error updating user status', 'danger');
                }
            });
        }
    });

    // ============================================================
    // RESET PASSWORD
    // ============================================================
    $(document).on('click', '.reset-password', function() {
        var userId = $(this).data('id');
        var userName = $(this).data('name');
        
        if (confirm('Are you sure you want to reset the password for "' + userName + '" to the default password (UAI4d@123)?')) {
            if (confirm('The user will be required to change their password on next login. Continue?')) {
                $.ajax({
                    url: 'ResetUserPasswordServlet',
                    method: 'POST',
                    data: { userId: userId },
                    dataType: 'json',
                    success: function(response) {
                        if (response.success) {
                            showAlert('Password reset successfully! Default password: UAI4d@123', 'success');
                            loadUsers(currentPage, $('#searchInput').val(), $('#roleFilter').val(), $('#statusFilter').val());
                        } else {
                            showAlert(response.message || 'Failed to reset password.', 'danger');
                        }
                    },
                    error: function() {
                        showAlert('An error occurred. Please try again.', 'danger');
                    }
                });
            }
        }
    });

    // Add user
    $('#addUserBtn').click(function() {
        showAlert('Add user feature coming soon!', 'info');
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
        }, 4000);
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
    loadUsers(1);

    console.log('Manage Users page ready!');
});