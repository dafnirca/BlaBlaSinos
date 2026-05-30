const HEADER_API_PERFIL = '/api/perfil';
const HEADER_USER_ID_KEY = 'userId';
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
            throw new Error('Não foi possível carregar seu perfil.');
        }
        usuario = await response.json();
    } catch (error) {
        console.error('Header: falha ao buscar perfil', error);
        logoutAndRedirect();
        return;
    }

    const motorista = isMotorista(usuario);
    if (MOTORISTA_PAGES.has(currentPage) && !motorista) {
        alert('Apenas motoristas podem acessar esta página. Atualize seu perfil para habilitar o modo motorista.');
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
        nav.appendChild(createNavLink('solicitacoes.html', 'Solicitações', currentPage));
    }

    const actions = document.createElement('div');
    actions.className = 'header-actions';
    const greeting = document.createElement('span');
    greeting.className = 'header-welcome';
    greeting.textContent = `Olá, ${usuario.nome || 'Usuário'}`;

    const logoutButton = document.createElement('button');
    logoutButton.type = 'button';
    logoutButton.className = 'header-logout';
    logoutButton.textContent = 'Sair';
    logoutButton.addEventListener('click', logoutAndRedirect);

    actions.appendChild(greeting);
    actions.appendChild(logoutButton);

    header.appendChild(brand);
    header.appendChild(nav);
    header.appendChild(actions);
    container.innerHTML = '';
    container.appendChild(header);
}

document.addEventListener('DOMContentLoaded', renderHeader);