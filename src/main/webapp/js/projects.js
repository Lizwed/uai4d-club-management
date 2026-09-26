/**
 * UAI4D Club - Projects JavaScript
 */

$(document).ready(function() {
    'use strict';

    var currentFilter = 'all';
    var joinProjectId = null;
    var isAdmin = false;
    var joinModal = new bootstrap.Modal(document.getElementById('joinModal'));

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
                        $('#createProjectBtn').show();
                    } else {
                        isAdmin = false;
                        $('.admin-only').hide();
                        $('#createProjectBtn').hide();
                    }
                }
            },
            error: function() {
                console.log('Could not load user info');
            }
        });
    }

    // ============================================================
    // LOAD PROJECTS
    // ============================================================
    function loadProjects(filter) {
        $('#projectsContainer').html(`
            <div class="col-12 text-center py-5">
                <i class="fas fa-spinner fa-spin fa-2x" style="color: var(--gold);"></i>
                <p class="mt-2 text-muted">Loading projects...</p>
            </div>
        `);
        
        $.ajax({
            url: 'GetProjectsServlet',
            method: 'GET',
            data: { filter: filter || 'all' },
            dataType: 'json',
            success: function(response) {
                if (response.success && response.projects && response.projects.length > 0) {
                    renderProjects(response.projects);
                } else {
                    renderNoProjects();
                }
            },
            error: function() {
                renderSampleProjects();
            }
        });
    }

    // ============================================================
    // RENDER PROJECTS
    // ============================================================
    function renderProjects(projects) {
        var html = '';
        
        $.each(projects, function(index, project) {
            var statusClass = project.status ? project.status.toLowerCase() : 'idea';
            var statusLabel = project.status || 'Idea';
            var deleteStyle = isAdmin ? '' : 'display:none;';
            
            html += `
                <div class="col-md-6 col-lg-4 project-item" data-status="${statusClass}">
                    <div class="project-card">
                        <span class="project-status ${statusClass}">${statusLabel}</span>
                        <div class="project-icon">
                            <i class="fas fa-robot"></i>
                        </div>
                        <h5>${project.name || 'Project'}</h5>
                        <div class="project-lead"><i class="fas fa-user-tie"></i> Lead: ${project.lead || 'TBD'}</div>
                        <div class="project-tech">
                            ${project.tech ? project.tech.split(',').map(function(t) { 
                                return '<span class="tech-tag">' + t.trim() + '</span>'; 
                            }).join('') : '<span class="tech-tag">Python</span>'}
                        </div>
                        <p class="project-description">${project.description || 'No description available.'}</p>
                        <div class="project-footer">
                            <span class="team-count">
                                <i class="fas fa-users"></i> ${project.members || 0} members
                            </span>
                            <div class="d-flex gap-2 align-items-center flex-wrap">
                                <button class="btn-join ${project.joined ? 'joined' : ''}" 
                                        data-id="${project.id}" 
                                        ${project.status === 'completed' || project.joined ? 'disabled' : ''}>
                                    ${project.joined ? '✓ Joined' : 'Join Project'}
                                </button>
                                <button class="btn-delete admin-only delete-project" data-id="${project.id}" style="${deleteStyle}">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });
        
        $('#projectsContainer').html(html);
    }

    // ============================================================
    // RENDER NO PROJECTS
    // ============================================================
    function renderNoProjects() {
        $('#projectsContainer').html(`
            <div class="col-12">
                <div class="no-projects">
                    <i class="fas fa-project-diagram"></i>
                    <h5>No projects available</h5>
                    <p class="text-muted">Check back later for new AI projects.</p>
                </div>
            </div>
        `);
    }

    // ============================================================
    // SAMPLE PROJECTS
    // ============================================================
    function renderSampleProjects() {
        var sampleProjects = [
            {
                id: 1,
                name: 'AI for Healthcare',
                description: 'Developing an AI system to assist in medical diagnosis.',
                status: 'Active',
                lead: 'Dr. Sarah Mwanga',
                tech: 'Python, TensorFlow, Django',
                members: 8,
                joined: false
            },
            {
                id: 2,
                name: 'Smart Agriculture Assistant',
                description: 'Using machine learning to predict crop yields.',
                status: 'Idea',
                lead: 'John Doe',
                tech: 'Python, Scikit-learn, React',
                members: 3,
                joined: false
            }
        ];
        renderProjects(sampleProjects);
    }

    // ============================================================
    // DELETE PROJECT
    // ============================================================
    $(document).on('click', '.delete-project', function() {
        var projectId = $(this).data('id');
        var projectName = $(this).closest('.project-card').find('h5').text();
        
        if (confirm('Are you sure you want to delete the project: "' + projectName + '"?')) {
            $.ajax({
                url: 'DeleteProjectServlet',
                method: 'POST',
                data: { projectId: projectId },
                dataType: 'json',
                success: function(response) {
                    if (response.success) {
                        showAlert('Project deleted successfully!', 'success');
                        loadProjects(currentFilter);
                    } else {
                        showAlert(response.message || 'Failed to delete project.', 'danger');
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
        loadProjects(currentFilter);
    });

    // ============================================================
    // JOIN BUTTON
    // ============================================================
    $(document).on('click', '.btn-join:not(:disabled)', function() {
        var projectId = $(this).data('id');
        var projectTitle = $(this).closest('.project-card').find('h5').text();
        var projectLead = $(this).closest('.project-card').find('.project-lead').text();
        
        joinProjectId = projectId;
        $('#joinProjectTitle').text(projectTitle);
        $('#joinProjectLead').text(projectLead);
        joinModal.show();
    });

    // ============================================================
    // CONFIRM JOIN
    // ============================================================
    $('#confirmJoinBtn').on('click', function() {
        if (!joinProjectId) return;
        
        $(this).prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Processing...');
        
        $.ajax({
            url: 'JoinProjectServlet',
            method: 'POST',
            data: { projectId: joinProjectId },
            dataType: 'json',
            success: function(response) {
                $('#confirmJoinBtn').prop('disabled', false).html('<i class="fas fa-check"></i> Confirm Join');
                joinModal.hide();
                
                if (response.success) {
                    showAlert('Successfully joined the project!', 'success');
                    loadProjects(currentFilter);
                } else {
                    showAlert(response.message || 'Failed to join project.', 'danger');
                }
            },
            error: function() {
                $('#confirmJoinBtn').prop('disabled', false).html('<i class="fas fa-check"></i> Confirm Join');
                joinModal.hide();
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
    loadProjects('all');

    console.log('Projects page ready!');
});