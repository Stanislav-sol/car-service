document.addEventListener('DOMContentLoaded', () => {

    // Эндпоинты REST API
    const API_URL = '/api/v1/cars';
    const SERVICES_API_URL = '/api/v1/services';
    const USER_API_URL = '/api/me';

    // Текущий авторизованный пользователь
    let currentUser = null;

    // Элементы DOM (Автомобили)
    const carForm = document.getElementById('carForm');
    const formTitle = document.getElementById('formTitle');
    const carIdInput = document.getElementById('carId');
    const brandInput = document.getElementById('brand');
    const modelInput = document.getElementById('model');
    const yearInput = document.getElementById('year');
    const designStyleInput = document.getElementById('designStyle');
    const saveBtn = document.getElementById('saveBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    const carsAdminGrid = document.getElementById('carsAdminGrid');

    // Элементы DOM (Модальное окно Сервиса)
    const serviceModal = document.getElementById('serviceModal');
    const serviceForm = document.getElementById('serviceForm');
    const serviceCarIdInput = document.getElementById('serviceCarId');
    const serviceNameInput = document.getElementById('serviceName');
    const executionTimeInput = document.getElementById('executionTime');
    const servicePriceInput = document.getElementById('servicePrice');

    // Первичная инициализация
    init();

    // Слушатели событий
    carForm.addEventListener('submit', handleFormSubmit);
    cancelBtn.addEventListener('click', resetForm);
    if (serviceForm) {
        serviceForm.addEventListener('submit', handleServiceSubmit);
    }

    /**
     * 0. Инициализация: загрузка данных пользователя и списка машин
     */
    async function init() {
        await fetchCurrentUser();
        await loadCars();
    }

    /**
     * Загрузка текущего авторизованного пользователя
     */
    async function fetchCurrentUser() {
        try {
            const response = await fetch(USER_API_URL);
            if (response.ok) {
                currentUser = await response.json();
            }
        } catch (error) {
            console.warn('Не удалось получить данные авторизованного пользователя:', error);
        }
    }

    /**
     * 1. GET: Загрузка всех автомобилей
     */
    async function loadCars() {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) {
                throw new Error(`Ошибка сервера: ${response.status}`);
            }
            const cars = await response.json();
            renderCars(cars);
        } catch (error) {
            console.error('Ошибка при загрузке:', error);
            carsAdminGrid.innerHTML = `
                <div class="error-state" style="color: var(--autumn-accent); padding: 20px;">
                    Не удалось загрузить автопарк. Проверьте подключение к серверу.
                </div>`;
        }
    }

    /**
     * 2. Отрисовка списка автомобилей в DOM с учетом прав пользователя
     */
    function renderCars(cars) {
        if (!cars || cars.length === 0) {
            carsAdminGrid.innerHTML = `
            <div class="loading-state" style="color: var(--text-muted);">
                Гараж пуст. Добавьте первый автомобиль через форму выше.
            </div>`;
            return;
        }

        carsAdminGrid.innerHTML = '';

        cars.forEach(car => {
            const card = document.createElement('div');
            card.className = 'manage-car-item';

            // Определение владельца и прав доступа
            const ownerId = car.user ? car.user.id : (car.userId || null);
            const ownerLogin = car.user ? (car.user.login || car.user.username) : 'Не назначен';

            const isOwner = currentUser && ownerId && (currentUser.id === ownerId);
            const isAdmin = currentUser && (currentUser.role === 'ROLE_ADMIN' || currentUser.admin === true);

            const canDelete = isAdmin || isOwner;
            const canEdit = isOwner || isAdmin;
            const canAddService = isOwner;

            // Формируем кнопки действия
            let buttonsHtml = '';

            if (canAddService) {
                buttonsHtml += `<button type="button" class="btn-service btn-edit" style="margin-right: 6px;"><span>+ Сервис</span></button>`;
            }
            if (canEdit) {
                buttonsHtml += `<button type="button" class="btn-edit"><span>Изменить</span></button>`;
            }
            if (canDelete) {
                buttonsHtml += `<button type="button" class="btn-delete"><span>Удалить</span></button>`;
            }

            card.innerHTML = `
                <div class="car-info">
                    <div class="car-brand">${escapeHtml(car.brand)}</div>
                    <div class="car-model">${escapeHtml(car.model)} (${car.year})</div>
                    <div class="car-style">${escapeHtml(car.designStyle)}</div>
                    <div class="car-owner" style="margin-top: 8px; font-size: 0.85rem; color: var(--text-muted, #a1a1aa);">
                        Владелец: <strong style="color: var(--autumn-copper, #c25927);">${escapeHtml(ownerLogin)}</strong>
                    </div>
                </div>
                <div class="action-buttons">
                    ${buttonsHtml}
                </div>
            `;

            // Навешиваем обработчики на созданные динамические кнопки
            if (canAddService) {
                card.querySelector('.btn-service')?.addEventListener('click', () => openServiceModal(car.id));
            }
            if (canEdit) {
                card.querySelector('.btn-edit:not(.btn-service)')?.addEventListener('click', () => fillFormForEdit(car));
            }
            if (canDelete) {
                card.querySelector('.btn-delete')?.addEventListener('click', () => deleteCar(car.id));
            }

            carsAdminGrid.appendChild(card);
        });
    }

    /**
     * 3. POST / PUT: Сохранение (создание или обновление авто)
     */
    async function handleFormSubmit(event) {
        event.preventDefault();

        const id = carIdInput.value;
        const carPayload = {
            brand: brandInput.value.trim(),
            model: modelInput.value.trim(),
            year: parseInt(yearInput.value, 10),
            designStyle: designStyleInput.value.trim()
        };

        const isUpdate = Boolean(id);
        const url = isUpdate ? `${API_URL}/${id}` : API_URL;
        const method = isUpdate ? 'PUT' : 'POST';

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(carPayload)
            });

            if (!response.ok) {
                throw new Error(`Не удалось сохранить данные: ${response.status}`);
            }

            resetForm();
            await loadCars(); // Обновляем список
        } catch (error) {
            console.error('Ошибка сохранения:', error);
            alert('Произошла ошибка при сохранении автомобиля!');
        }
    }

    /**
     * 4. Заполнение формы для редактирования
     */
    function fillFormForEdit(car) {
        formTitle.textContent = 'Редактировать машину';
        carIdInput.value = car.id;
        brandInput.value = car.brand;
        modelInput.value = car.model;
        yearInput.value = car.year;
        designStyleInput.value = car.designStyle || '';

        saveBtn.textContent = 'Сохранить';
        cancelBtn.style.display = 'block';

        // Прокрутка к форме
        carForm.scrollIntoView({ behavior: 'smooth' });
    }

    /**
     * 5. DELETE: Удаление автомобиля
     */
    async function deleteCar(id) {
        if (!confirm('Вы уверены, что хотите удалить этот автомобиль из гаража?')) {
            return;
        }

        try {
            const response = await fetch(`${API_URL}/${id}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error(`Ошибка при удалении: ${response.status}`);
            }

            await loadCars(); // Перерисовываем список
        } catch (error) {
            console.error('Ошибка удаления:', error);
            alert('Не удалось удалить автомобиль! (Возможно, недостаточно прав)');
        }
    }

    /**
     * 6. POST: Добавление сервисной услуги к авто
     */
    async function handleServiceSubmit(event) {
        event.preventDefault();

        const carId = serviceCarIdInput.value;
        const servicePayload = {
            name: serviceNameInput.value.trim(),
            executionTime: parseInt(executionTimeInput.value, 10),
            price: parseFloat(servicePriceInput.value),
            status: 'PLANNED',
            car: { id: parseInt(carId, 10) }
        };

        try {
            const response = await fetch(SERVICES_API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(servicePayload)
            });

            if (!response.ok) {
                throw new Error(`Ошибка при добавлении сервисной услуги: ${response.status}`);
            }

            closeServiceModal();
            alert('Сервисная запись успешно добавлена!');
        } catch (error) {
            console.error('Ошибка при сохранении сервиса:', error);
            alert('Не удалось добавить сервисную услугу!');
        }
    }

    /**
     * Управление модальным окном сервиса
     */
    function openServiceModal(carId) {
        if (!serviceModal) return;
        serviceCarIdInput.value = carId;
        serviceModal.style.display = 'flex';
    }

    function closeServiceModal() {
        if (!serviceModal) return;
        if (serviceForm) serviceForm.reset();
        serviceModal.style.display = 'none';
    }

    // Экспортируем функцию закрытия модалки в глобальную область (для кнопки «Отмена» onclick)
    window.closeServiceModal = closeServiceModal;

    /**
     * Сброс формы создания/редактирования машины
     */
    function resetForm() {
        formTitle.textContent = 'Добавить новую машину';
        carIdInput.value = '';
        carForm.reset();
        saveBtn.textContent = 'Добавить в автопарк';
        cancelBtn.style.display = 'none';
    }

    /**
     * Вспомогательная функция для безопасного вывода строк в HTML (XSS Protection)
     */
    function escapeHtml(text) {
        if (!text) return '';
        return String(text)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
});