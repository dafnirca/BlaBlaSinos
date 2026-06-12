const HEADER_API_PERFIL = '/api/perfil';
const HEADER_API_NOTIFICACOES = '/api/notificacoes';
const HEADER_USER_ID_KEY = 'userId';
const NOTIFICATIONS_LIMIT = 8;
const MOTORISTA_PAGES = new Set(['gerenciar-caronas.html', 'solicitacoes.html', 'motorista-dashboard.html']);

function logoutAndRedirect() {
    localStorage.removeItem(HEADER_USER_ID_KEY);
    localStorage.removeItem('userEmail');
    window.location.href = 'login.html';
}

function createNavLink(href, label, currentPage) {
    const link = document.createElement('a');
    link.href = href;
    link.textContent = label;
    if (href === currentPage) {
        link.classList.add('active');
    }
    return link;
}

function isMotorista(usuario) {
    return usuario && usuario.cnh && usuario.cnh.trim() !== ''
        && usuario.placaVeiculo && usuario.placaVeiculo.trim() !== '';
}

async function renderHeader() {
    const container = document.getElementById('site-header');
    if (!container) {
        return;
    }

    const currentPage = window.location.pathname.split('/').pop();
    const userId = localStorage.getItem(HEADER_USER_ID_KEY);
    if (!userId) {
        window.location.href = 'login.html';
        return;
    }

    let usuario;
    try {
        const response = await fetch(`${HEADER_API_PERFIL}?id=${encodeURIComponent(userId)}`);
        if (!response.ok) {
            throw new Error('Nao foi possivel carregar seu perfil.');
        }
        usuario = await response.json();
    } catch (error) {
        console.error('Header: falha ao buscar perfil', error);
        logoutAndRedirect();
        return;
    }

    const motorista = isMotorista(usuario);
    if (MOTORISTA_PAGES.has(currentPage) && !motorista) {
        alert('Apenas motoristas podem acessar esta pagina. Atualize seu perfil para habilitar o modo motorista.');
        window.location.href = 'perfil.html';
        return;
    }

    const header = document.createElement('div');
    header.className = 'site-header';

    const brand = document.createElement('div');
    brand.className = 'header-brand';
    brand.innerHTML = `
        <a href="perfil.html" class="brand-link">BlaBlaSinos</a>
        <span class="brand-subtitle">Conectando caronas na Unisinos</span>
    `;

    const nav = document.createElement('nav');
    nav.className = 'site-nav';
    nav.appendChild(createNavLink('perfil.html', 'Perfil', currentPage));
    nav.appendChild(createNavLink('buscar-caronas.html', 'Buscar Caronas', currentPage));
    if (motorista) {
        nav.appendChild(createNavLink('gerenciar-caronas.html', 'Minhas Caronas', currentPage));
        nav.appendChild(createNavLink('solicitacoes.html', 'Solicitacoes', currentPage));
    } else {
        // Passageiro: link para acompanhar viagens
        nav.appendChild(createNavLink('minhas-viagens.html', 'Minhas Viagens', currentPage));
    }

    const actions = document.createElement('div');
    actions.className = 'header-actions';

    const notifications = createNotificationsUI(userId);

    const greeting = document.createElement('span');
    greeting.className = 'header-welcome';
    greeting.textContent = `Ola, ${usuario.nome || 'Usuario'}`;

    const logoutButton = document.createElement('button');
    logoutButton.type = 'button';
    logoutButton.className = 'header-logout';
    logoutButton.textContent = 'Sair';
    logoutButton.addEventListener('click', logoutAndRedirect);

    actions.appendChild(notifications.wrapper);
    actions.appendChild(greeting);
    actions.appendChild(logoutButton);

    header.appendChild(brand);
    header.appendChild(nav);
    header.appendChild(actions);
    container.innerHTML = '';
    container.appendChild(header);

    await refreshNotifications(userId, notifications);
}

document.addEventListener('DOMContentLoaded', renderHeader);

function createNotificationsUI(userId) {
    const wrapper = document.createElement('div');
    wrapper.className = 'notifications';

    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'notifications-button';
    button.setAttribute('aria-label', 'Abrir notificacoes');
    button.innerHTML = '<span class="notifications-bell">&#128276;</span>';

    const badge = document.createElement('span');
    badge.className = 'notifications-badge hidden';
    badge.textContent = '0';
    button.appendChild(badge);

    const dropdown = document.createElement('div');
    dropdown.className = 'notifications-dropdown hidden';

    const title = document.createElement('div');
    title.className = 'notifications-title';
    title.textContent = 'Notificações';

    const list = document.createElement('div');
    list.className = 'notifications-list';

    dropdown.appendChild(title);
    dropdown.appendChild(list);
    wrapper.appendChild(button);
    wrapper.appendChild(dropdown);

    button.addEventListener('click', async () => {
        dropdown.classList.toggle('hidden');
        if (!dropdown.classList.contains('hidden')) {
            await refreshNotifications(userId, { badge, list });
        }
    });

    document.addEventListener('click', (event) => {
        if (!wrapper.contains(event.target)) {
            dropdown.classList.add('hidden');
        }
    });

    return { wrapper, badge, list };
}

async function refreshNotifications(userId, ui) {
    try {
        const response = await fetch(`${HEADER_API_NOTIFICACOES}?usuarioId=${encodeURIComponent(userId)}`);
        if (!response.ok) {
            throw new Error('Nao foi possivel carregar notificacoes.');
        }

        const notifications = await response.json();
        const unreadCount = notifications.filter((n) => !n.lida).length;
        renderBadge(ui.badge, unreadCount);
        renderNotificationsList(userId, ui, notifications.slice(0, NOTIFICATIONS_LIMIT));
    } catch (error) {
        console.error('Header: falha ao buscar notificacoes', error);
        renderBadge(ui.badge, 0);
        renderNotificationsError(ui.list);
    }
}

function renderBadge(badge, unreadCount) {
    badge.textContent = unreadCount > 99 ? '99+' : String(unreadCount);
    badge.classList.toggle('hidden', unreadCount === 0);
}

function renderNotificationsList(userId, ui, notifications) {
    ui.list.innerHTML = '';

    if (!notifications.length) {
        const empty = document.createElement('p');
        empty.className = 'notifications-empty';
        empty.textContent = 'Você não possui notificações no momento.';
        ui.list.appendChild(empty);
        return;
    }

    notifications.forEach((item) => {
        const row = document.createElement('button');
        row.type = 'button';
        row.className = `notification-item ${item.lida ? 'read' : 'unread'}`;

        const message = document.createElement('span');
        message.className = 'notification-message';
        message.textContent = item.mensagem || 'Nova notificacao';

        const meta = document.createElement('span');
        meta.className = 'notification-meta';
        meta.textContent = formatNotificationDate(item.criadaEm);

        row.appendChild(message);
        row.appendChild(meta);

        row.addEventListener('click', async () => {
            if (!item.lida) {
                await markNotificationAsRead(userId, item.id);
            }
            const target = await getNotificationTarget(userId, item);
            if (target) {
                window.location.href = target;
                return;
            }
            await refreshNotifications(userId, ui);
        });

        ui.list.appendChild(row);
    });
}

async function getNotificationTarget(userId, notification) {
    if (!notification || !notification.tipo) {
        return null;
    }

    if (notification.tipo === 'NOVA_SOLICITACAO') {
        if (notification.referenciaId) {
            return `solicitacoes.html?reservaId=${encodeURIComponent(notification.referenciaId)}`;
        }
        return 'solicitacoes.html';
    }

    if (notification.tipo === 'SOLICITACAO_ACEITA' || notification.tipo === 'SOLICITACAO_RECUSADA') {
        const caronaId = await resolveCaronaIdFromNotification(userId, notification);
        if (caronaId) {
            return `detalhe-carona.html?id=${encodeURIComponent(caronaId)}`;
        }
    }

    return null;
}

async function resolveCaronaIdFromNotification(userId, notification) {
    if (notification.referenciaId) {
        try {
            const response = await fetch(`/api/solicitacoes?passageiroId=${encodeURIComponent(userId)}`);
            if (response.ok) {
                const reservas = await response.json();
                const reserva = reservas.find((r) => String(r.id) === String(notification.referenciaId));
                if (reserva && reserva.caronaId) {
                    return reserva.caronaId;
                }
            }
        } catch (error) {
            console.error('Falha ao resolver carona da notificacao', error);
        }
    }

    const mensagem = notification.mensagem || '';
    const match = mensagem.match(/carona\\s*#(\\d+)/i);
    return match ? match[1] : null;
}

function renderNotificationsError(list) {
    list.innerHTML = '';
    const error = document.createElement('p');
    error.className = 'notifications-empty';
    error.textContent = 'Erro ao carregar notificacoes.';
    list.appendChild(error);
}

async function markNotificationAsRead(usuarioId, notificacaoId) {
    await fetch(HEADER_API_NOTIFICACOES, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usuarioId, notificacaoId })
    });
}

function formatNotificationDate(rawDate) {
    if (!rawDate) return '';

    const parsed = new Date(rawDate.replace(' ', 'T'));
    if (Number.isNaN(parsed.getTime())) {
        return '';
    }

    return parsed.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}
