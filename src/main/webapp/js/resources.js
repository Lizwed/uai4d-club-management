/**
 * UAI4D Club - Resources JavaScript
 */

$(document).ready(function() {
    'use strict';

    var currentFilter = 'all';
    var searchQuery = '';
    var isAdmin = false;

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
    // LOAD USER INFO
    // ============================================================
    function loadUserInfo() {
        $.ajax({
            url: 'GetUserInfoServlet',
            method: 'GET',
            dataType: 'json',
            success: function(response) {
                if (response.success) {
                    var fullName = response.fullName || 'Member';
                    var role = response.role || 'MEMBER';
                    
                    $('#userFullName').text(fullName);
                    $('#userRole').text(role);
                    
                    var initial = fullName.charAt(0).toUpperCase();
                    $('#userAvatar').text(initial);
                    
                    if (role === 'ADMIN') {
                        isAdmin = true;
                        $('.admin-only').show();
                        $('#addResourceBtn').show();
                    } else {
                        isAdmin = false;
                        $('.admin-only').hide();
                        $('#addResourceBtn').hide();
                    }
                }
            },
            error: function() {
                console.log('Could not load user info');
            }
        });
    }

    // ============================================================
    // LOAD RESOURCES
    // ============================================================
    function loadResources(filter, search) {
        $('#resourcesContainer').html(`
            <div class="col-12 text-center py-5">
                <i class="fas fa-spinner fa-spin fa-2x" style="color: var(--gold);"></i>
                <p class="mt-2 text-muted">Loading resources...</p>
            </div>
        `);
        
        $.ajax({
            url: 'GetResourcesServlet',
            method: 'GET',
            data: { 
                filter: filter || 'all',
                search: search || ''
            },
            dataType: 'json',
            success: function(response) {
                if (response.success && response.resources && response.resources.length > 0) {
                    renderResources(response.resources);
                } else {
                    renderNoResources();
                }
            },
            error: function() {
                renderSampleResources();
            }
        });
    }

    // ============================================================
    // RENDER RESOURCES
    // ============================================================
    function renderResources(resources) {
        var html = '';
        var iconMap = {
            'video': 'video',
            'article': 'article',
            'tutorial': 'tutorial',
            'pdf': 'pdf',
            'github': 'github',
            'external_link': 'external_link'
        };
        
        $.each(resources, function(index, resource) {
            var iconClass = iconMap[resource.type ? resource.type.toLowerCase() : 'tutorial'] || 'tutorial';
            var typeLabel = resource.type || 'Tutorial';
            var deleteStyle = isAdmin ? '' : 'display:none;';
            
            html += `
                <div class="col-md-6 col-lg-4 resource-item" data-type="${resource.type || 'tutorial'}">
                    <div class="resource-card">
                        <div class="resource-icon ${iconClass}">
                            <i class="fas ${getIconClass(resource.type)}"></i>
                        </div>
                        <span class="resource-type">${typeLabel}</span>
                        <h5>${resource.title || 'Resource'}</h5>
                        <div class="resource-meta"><i class="fas fa-user"></i> By ${resource.author || 'UAI4D'}</div>
                        <p class="resource-description">${resource.description || 'No description available.'}</p>
                        <div class="resource-footer">
                            <span class="stats">
                                <i class="fas fa-eye"></i> ${resource.views || 0}
                                <i class="fas fa-download ms-2"></i> ${resource.downloads || 0}
                            </span>
                            <div class="d-flex gap-2 align-items-center flex-wrap">
                                <a href="${resource.url || '#'}" target="_blank" class="btn-access">
                                    <i class="fas fa-external-link-alt"></i> Access
                                </a>
                                <button class="btn-delete admin-only delete-resource" data-id="${resource.id}" style="${deleteStyle}">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });
        
        $('#resourcesContainer').html(html);
    }

    // ============================================================
    // GET ICON CLASS
    // ============================================================
    function getIconClass(type) {
        var icons = {
            'video': 'fa-video',
            'article': 'fa-newspaper',
            'tutorial': 'fa-graduation-cap',
            'pdf': 'fa-file-pdf',
            'github': 'fa-github',
            'external_link': 'fa-external-link-alt'
        };
        return icons[type ? type.toLowerCase() : 'tutorial'] || 'fa-file';
    }

    // ============================================================
    // RENDER NO RESOURCES
    // ============================================================
    function renderNoResources() {
        $('#resourcesContainer').html(`
            <div class="col-12">
                <div class="no-resources">
                    <i class="fas fa-book"></i>
                    <h5>No resources found</h5>
                    <p class="text-muted">Check back later for new learning materials.</p>
                </div>
            </div>
        `);
    }

    // ============================================================
    // SAMPLE RESOURCES
    // ============================================================
    function renderSampleResources() {
        var sampleResources = [
            {
                id: 1,
                title: 'Python Basics Tutorial',
                description: 'Complete Python programming tutorial for beginners.',
                type: 'Tutorial',
                author: 'UAI4D Team',
                url: '#',
                views: 156,
                downloads: 43
            },
            {
                id: 2,
                title: 'Introduction to Machine Learning',
                description: 'Understanding machine learning concepts and applications.',
                type: 'Video',
                author: 'Dr. Sarah Mwanga',
                url: '#',
                views: 89,
                downloads: 12
            }
        ];
        renderResources(sampleResources);
    }

    // ============================================================
    // DELETE RESOURCE
    // ============================================================
    $(document).on('click', '.delete-resource', function() {
        var resourceId = $(this).data('id');
        var resourceTitle = $(this).closest('.resource-card').find('h5').text();
        
        if (confirm('Are you sure you want to delete the resource: "' + resourceTitle + '"?')) {
            $.ajax({
                url: 'DeleteResourceServlet',
                method: 'POST',
                data: { resourceId: resourceId },
                dataType: 'json',
                success: function(response) {
                    if (response.success) {
                        showAlert('Resource deleted successfully!', 'success');
                        loadResources(currentFilter, searchQuery);
                    } else {
                        showAlert(response.message || 'Failed to delete resource.', 'danger');
                    }
                },
                error: function() {
                    showAlert('An error occurred. Please try again.', 'danger');
                }
            });
        }
    });

    // ============================================================
    // FILTER BUTTONS
    // ============================================================
    $('.filter-btn').on('click', function() {
        $('.filter-btn').removeClass('active');
        $(this).addClass('active');
        currentFilter = $(this).data('filter');
        loadResources(currentFilter, searchQuery);
    });

    // ============================================================
    // SEARCH INPUT
    // ============================================================
    var searchTimeout;
    $('#searchInput').on('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function() {
            searchQuery = $('#searchInput').val().trim();
            loadResources(currentFilter, searchQuery);
        }, 400);
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
    loadUserInfo();
    loadResources('all', '');

    console.log('Resources page ready!');
});