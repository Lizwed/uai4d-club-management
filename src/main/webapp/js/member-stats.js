/**
 * UAI4D Club - Member Statistics Dashboard
 * Intelligent member analytics
 */

$(document).ready(function() {
    'use strict';

    let yearChart = null;
    let aiExperienceChart = null;

    // ============================================================
    // LOAD MEMBER STATISTICS
    // ============================================================
    function loadMemberStats() {
        $.ajax({
            url: 'GetMemberStatsServlet',
            method: 'GET',
            dataType: 'json',
            success: function(data) {
                if (data.success) {
                    updateStats(data);
                    updateCharts(data);
                    updateInsights(data);
                    updateMemberList(data.members);
                }
            },
            error: function() {
                showError('Could not load member statistics');
            }
        });
    }

    // ============================================================
    // UPDATE STATS
    // ============================================================
    function updateStats(data) {
        $('#totalMembers').text(data.totalMembers || 0);
        $('#activeMembers').text(data.activeMembers || 0);
        $('#completedProfiles').text(data.completedProfiles || 0);
        $('#aiExperts').text(data.aiExperts || 0);
    }

    // ============================================================
    // UPDATE CHARTS
    // ============================================================
    function updateCharts(data) {
        // Year of Study Chart
        updateYearChart(data.yearDistribution);
        
        // AI Experience Chart
        updateAIExperienceChart(data.aiExperienceDistribution);
    }

    // ============================================================
    // YEAR CHART
    // ============================================================
    function updateYearChart(yearData) {
        const ctx = document.getElementById('yearChart').getContext('2d');
        
        const labels = ['Year 1', 'Year 2', 'Year 3', 'Year 4', 'Year 5', 'Year 6'];
        const values = [];
        const colors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'];
        
        for (let i = 1; i <= 6; i++) {
            values.push(yearData[i] || 0);
        }
        
        if (yearChart) {
            yearChart.destroy();
        }
        
        yearChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Members',
                    data: values,
                    backgroundColor: colors.map(c => c + '80'),
                    borderColor: colors,
                    borderWidth: 2,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    }

    // ============================================================
    // AI EXPERIENCE CHART
    // ============================================================
    function updateAIExperienceChart(expData) {
        const ctx = document.getElementById('aiExperienceChart').getContext('2d');
        
        const labels = ['Beginner', 'Intermediate', 'Advanced', 'Expert'];
        const colors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0'];
        const values = [
            expData.beginner || 0,
            expData.intermediate || 0,
            expData.advanced || 0,
            expData.expert || 0
        ];
        
        if (aiExperienceChart) {
            aiExperienceChart.destroy();
        }
        
        aiExperienceChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: values,
                    backgroundColor: colors.map(c => c + 'CC'),
                    borderColor: '#fff',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 12,
                            usePointStyle: true
                        }
                    }
                }
            }
        });
    }

    // ============================================================
    // UPDATE INSIGHTS
    // ============================================================
    function updateInsights(data) {
        // Completion Rate
        const completionRate = data.totalMembers > 0 
            ? Math.round((data.completedProfiles / data.totalMembers) * 100) 
            : 0;
        $('#completionRate').text(completionRate + '%');
        
        // Top Skills
        if (data.topSkills && data.topSkills.length > 0) {
            let skillsHtml = '';
            data.topSkills.slice(0, 5).forEach(function(skill, index) {
                const colors = ['#D4AF37', '#E8D57A', '#87CEEB', '#4BC0C0', '#9966FF'];
                skillsHtml += `<span class="badge" style="background: ${colors[index] || '#6B7280'}; color: #fff; margin: 2px; padding: 4px 10px;">${skill.skill}</span>`;
            });
            $('#topSkills').html(skillsHtml || 'No skills data');
        } else {
            $('#topSkills').html('<span class="text-muted">No skills data available</span>');
        }
        
        // Top University
        if (data.topUniversity) {
            $('#topUniversity').html(
                `<span class="highlight" style="font-size: 1.1rem;">${data.topUniversity.university}</span><br>
                <span style="font-size: 0.8rem; color: rgba(255,255,255,0.5);">${data.topUniversity.count} members</span>`
            );
        } else {
            $('#topUniversity').html('<span class="text-muted">No university data</span>');
        }
    }

    // ============================================================
    // UPDATE MEMBER LIST
    // ============================================================
    function updateMemberList(members) {
        const container = $('#memberList');
        
        if (!members || members.length === 0) {
            container.html(`
                <div class="text-center text-muted py-4">
                    <i class="fas fa-users"></i>
                    <p>No members found</p>
                </div>
            `);
            return;
        }
        
        let html = '';
        members.forEach(function(member) {
            const completion = member.completionPercentage || 0;
            const statusClass = completion >= 80 ? 'success' : (completion >= 50 ? 'warning' : 'danger');
            const statusText = completion >= 80 ? 'Complete' : (completion >= 50 ? 'Partial' : 'Incomplete');
            const initial = member.fullName ? member.fullName.charAt(0).toUpperCase() : '?';
            
            html += `
                <div class="member-item">
                    <div class="avatar-small">${initial}</div>
                    <div class="member-info">
                        <div class="name">${member.fullName || 'Unknown'}</div>
                        <div class="details">${member.email || ''} ${member.course ? '| ' + member.course : ''}</div>
                    </div>
                    <div class="completion-bar">
                        <div class="progress">
                            <div class="progress-bar" style="width: ${completion}%; background: ${completion >= 80 ? '#27ae60' : (completion >= 50 ? '#f39c12' : '#e74c3c')};"></div>
                        </div>
                    </div>
                    <span class="badge bg-${statusClass}">${statusText}</span>
                    <span class="completion-percent">${completion}%</span>
                </div>
            `;
        });
        
        container.html(html);
    }

    // ============================================================
    // REFRESH
    // ============================================================
    $('#refreshBtn').click(function() {
        $(this).html('<i class="fas fa-spinner fa-spin"></i> Loading...');
        loadMemberStats();
        setTimeout(function() {
            $('#refreshBtn').html('<i class="fas fa-sync-alt"></i> Refresh');
        }, 1000);
    });

    // ============================================================
    // SHOW ERROR
    // ============================================================
    function showError(message) {
        $('#memberList').html(`
            <div class="text-center text-danger py-4">
                <i class="fas fa-exclamation-circle"></i>
                <p>${message}</p>
            </div>
        `);
    }
    // ============================================================
// EXPORT FUNCTIONS
// ============================================================

// ===== Export to PDF =====
$('#exportPDF').click(function(e) {
    e.preventDefault();
    showAlert('Generating PDF report...', 'info');
    
    $.ajax({
        url: 'ExportReportServlet',
        method: 'POST',
        data: { format: 'pdf' },
        xhrFields: {
            responseType: 'blob'
        },
        success: function(data) {
            const blob = new Blob([data], { type: 'application/pdf' });
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = 'UAI4D_Member_Report.pdf';
            link.click();
            showAlert('PDF report downloaded successfully!', 'success');
        },
        error: function() {
            showAlert('Failed to generate PDF report', 'danger');
        }
    });
});

// ===== Export to Excel =====
$('#exportExcel').click(function(e) {
    e.preventDefault();
    showAlert('Generating Excel report...', 'info');
    
    $.ajax({
        url: 'ExportReportServlet',
        method: 'POST',
        data: { format: 'excel' },
        xhrFields: {
            responseType: 'blob'
        },
        success: function(data) {
            const blob = new Blob([data], { 
                type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
            });
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = 'UAI4D_Member_Report.xlsx';
            link.click();
            showAlert('Excel report downloaded successfully!', 'success');
        },
        error: function() {
            showAlert('Failed to generate Excel report', 'danger');
        }
    });
});

// ===== Export to CSV =====
$('#exportCSV').click(function(e) {
    e.preventDefault();
    showAlert('Generating CSV report...', 'info');
    
    $.ajax({
        url: 'ExportReportServlet',
        method: 'POST',
        data: { format: 'csv' },
        xhrFields: {
            responseType: 'blob'
        },
        success: function(data) {
            const blob = new Blob([data], { type: 'text/csv' });
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = 'UAI4D_Member_Report.csv';
            link.click();
            showAlert('CSV report downloaded successfully!', 'success');
        },
        error: function() {
            showAlert('Failed to generate CSV report', 'danger');
        }
    });
});

// ===== Alert Function (for export notifications) =====
function showAlert(message, type) {
    const colors = {
        success: '#27ae60',
        danger: '#e74c3c',
        info: '#3498db'
    };
    const icons = {
        success: 'fa-check-circle',
        danger: 'fa-exclamation-circle',
        info: 'fa-info-circle'
    };
    const icon = icons[type] || icons.info;
    const color = colors[type] || '#D4AF37';
    
    const alertHtml = `
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
    // INITIALIZE
    // ============================================================
    loadMemberStats();

    console.log('Member Statistics Dashboard loaded!');
});