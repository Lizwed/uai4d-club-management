/**
 * UAI4D Club - Events JavaScript
 */

$(document).ready(function() {
    'use strict';

    var currentFilter = 'all';
    var registerEventId = null;
    var isAdmin = false;
    var registerModal = new bootstrap.Modal(document.getElementById('registerModal'));

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
                        $('#createEventBtn').show();
                    } else {
                        isAdmin = false;
                        $('.admin-only').hide();
                        $('#createEventBtn').hide();
                    }
                }
            },
            error: function() {
                console.log('Could not load user info');
            }
        });
    }

    // ============================================================
    // LOAD EVENTS
    // ============================================================
    function loadEvents(filter) {
        $('#eventsContainer').html(`
            <div class="col-12 text-center py-5">
                <i class="fas fa-spinner fa-spin fa-2x" style="color: var(--gold);"></i>
                <p class="mt-2 text-muted">Loading events...</p>
            </div>
        `);
        
        $.ajax({
            url: 'GetEventsServlet',
            method: 'GET',
            data: { filter: filter || 'all' },
            dataType: 'json',
            success: function(response) {
                if (response.success && response.events && response.events.length > 0) {
                    renderEvents(response.events);
                } else {
                    renderNoEvents();
                }
            },
            error: function() {
                renderSampleEvents();
            }
        });
    }

    // ============================================================
    // RENDER EVENTS
    // ============================================================
    function renderEvents(events) {
        var html = '';
        
        $.each(events, function(index, event) {
            var statusClass = event.status ? event.status.toLowerCase() : 'upcoming';
            var typeClass = event.type ? event.type.toLowerCase() : 'meetup';
            var date = new Date(event.date);
            var day = date.getDate();
            var month = date.toLocaleString('default', { month: 'short' });
            var deleteStyle = isAdmin ? '' : 'display:none;';
            
            html += `
                <div class="col-md-6 col-lg-4 event-item" data-status="${statusClass}" data-type="${typeClass}">
                    <div class="event-card">
                        <div class="event-date-badge">
                            <span class="day">${day}</span>
                            ${month}
                        </div>
                        <span class="event-type">${event.type || 'Meetup'}</span>
                        <h5>${event.title || 'Event'}</h5>
                        <div class="event-meta"><i class="fas fa-calendar-day"></i> ${formatDate(event.date)}</div>
                        <div class="event-meta"><i class="fas fa-clock"></i> ${event.time || 'TBA'}</div>
                        <div class="event-meta"><i class="fas fa-map-marker-alt"></i> ${event.location || 'Online'}</div>
                        <p class="event-description">${event.description || 'No description available.'}</p>
                        <div class="event-footer">
                            <span class="attendees">
                                <i class="fas fa-users"></i> ${event.attendees || 0} / ${event.capacity || 50}
                            </span>
                            <div class="d-flex gap-2 align-items-center flex-wrap">
                                <span class="event-status ${statusClass}">${event.status || 'Upcoming'}</span>
                                <button class="btn-register ${event.registered ? 'registered' : ''}" 
                                        data-id="${event.id}" 
                                        ${event.status === 'completed' || event.status === 'cancelled' || event.registered ? 'disabled' : ''}>
                                    ${event.registered ? '✓ Registered' : 'Register'}
                                </button>
                                <button class="btn-delete admin-only delete-event" data-id="${event.id}" style="${deleteStyle}">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });
        
        $('#eventsContainer').html(html);
    }

    // ============================================================
    // RENDER NO EVENTS
    // ============================================================
    function renderNoEvents() {
        $('#eventsContainer').html(`
            <div class="col-12">
                <div class="no-events">
                    <i class="fas fa-calendar-times"></i>
                    <h5>No events found</h5>
                    <p class="text-muted">Check back later for upcoming events.</p>
                </div>
            </div>
        `);
    }

    // ============================================================
    // SAMPLE EVENTS
    // ============================================================
    function renderSampleEvents() {
        var sampleEvents = [
            {
                id: 1,
                title: 'Python for AI Workshop',
                type: 'Workshop',
                date: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
                time: '10:00 AM - 1:00 PM',
                location: 'Computer Lab 301',
                description: 'Learn Python programming for AI applications.',
                status: 'Upcoming',
                attendees: 18,
                capacity: 30,
                registered: false
            },
            {
                id: 2,
                title: 'AI in Agriculture Webinar',
                type: 'Webinar',
                date: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000),
                time: '2:00 PM - 4:00 PM',
                location: 'Online (Zoom)',
                description: 'Discover how AI is transforming agriculture.',
                status: 'Upcoming',
                attendees: 45,
                capacity: 100,
                registered: false
            }
        ];
        renderEvents(sampleEvents);
    }

    // ============================================================
    // FORMAT DATE
    // ============================================================
    function formatDate(dateString) {
        var date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            weekday: 'short', 
            month: 'short', 
            day: 'numeric', 
            year: 'numeric' 
        });
    }

    // ============================================================
    // DELETE EVENT
    // ============================================================
    $(document).on('click', '.delete-event', function() {
        var eventId = $(this).data('id');
        var eventTitle = $(this).closest('.event-card').find('h5').text();
        
        if (confirm('Are you sure you want to delete the event: "' + eventTitle + '"?')) {
            $.ajax({
                url: 'DeleteEventServlet',
                method: 'POST',
                data: { eventId: eventId },
                dataType: 'json',
                success: function(response) {
                    if (response.success) {
                        showAlert('Event deleted successfully!', 'success');
                        loadEvents(currentFilter);
                    } else {
                        showAlert(response.message || 'Failed to delete event.', 'danger');
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
        loadEvents(currentFilter);
    });

    // ============================================================
    // REGISTER BUTTON
    // ============================================================
    $(document).on('click', '.btn-register:not(:disabled)', function() {
        var eventId = $(this).data('id');
        var eventTitle = $(this).closest('.event-card').find('h5').text();
        var eventDate = $(this).closest('.event-card').find('.event-meta:first').text();
        var eventLocation = $(this).closest('.event-card').find('.event-meta:last').text();
        
        registerEventId = eventId;
        $('#registerEventTitle').text(eventTitle);
        $('#registerEventDate').text(eventDate);
        $('#registerEventLocation').text(eventLocation);
        registerModal.show();
    });

    // ============================================================
    // CONFIRM REGISTRATION
    // ============================================================
    $('#confirmRegisterBtn').on('click', function() {
        if (!registerEventId) return;
        
        $(this).prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Processing...');
        
        $.ajax({
            url: 'RegisterEventServlet',
            method: 'POST',
            data: { eventId: registerEventId },
            dataType: 'json',
            success: function(response) {
                $('#confirmRegisterBtn').prop('disabled', false).html('<i class="fas fa-check"></i> Confirm Registration');
                registerModal.hide();
                
                if (response.success) {
                    showAlert('Registration successful!', 'success');
                    loadEvents(currentFilter);
                } else {
                    showAlert(response.message || 'Registration failed.', 'danger');
                }
            },
            error: function() {
                $('#confirmRegisterBtn').prop('disabled', false).html('<i class="fas fa-check"></i> Confirm Registration');
                registerModal.hide();
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
    loadEvents('all');

    console.log('Events page ready!');
});