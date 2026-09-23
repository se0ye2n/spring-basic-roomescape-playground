let isEditing = false;
const RESERVATION_API_ENDPOINT = '/reservations';
const TIME_API_ENDPOINT = '/times';
const THEME_API_ENDPOINT = '/themes';
const timesOptions = [];
const themesOptions = [];

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('add-button').addEventListener('click', addInputRow);

  requestRead(RESERVATION_API_ENDPOINT)
      .then(render)
      .catch(error => console.error('Error fetching reservations:', error));

  fetchTimes();
  fetchThemes();
});

function render(data) {
  const tableBody = document.getElementById('table-body');
  tableBody.innerHTML = '';

  data.forEach(item => {
    const row = tableBody.insertRow();

    row.insertCell(0).textContent = item.id;
    row.insertCell(1).textContent = item.name;
    row.insertCell(2).textContent = item.theme;
    row.insertCell(3).textContent = item.date;
    row.insertCell(4).textContent = item.time;

    const actionCell = row.insertCell(row.cells.length);
    actionCell.appendChild(createActionButton('삭제', 'btn-danger', deleteRow));
  });
}

function fetchTimes() {
  requestRead(TIME_API_ENDPOINT)
      .then(data => {
        timesOptions.push(...data);
      })
      .catch(error => console.error('Error fetching time:', error));
}

function fetchThemes() {
  requestRead(THEME_API_ENDPOINT)
      .then(data => {
        themesOptions.push(...data);
      })
      .catch(error => console.error('Error fetching theme:', error));
}

function createSelect(options, defaultText, selectId, textProperty) {
  const select = document.createElement('select');
  select.className = 'form-control';
  select.id = selectId;

  // 기본 옵션 추가
  const defaultOption = document.createElement('option');
  defaultOption.textContent = defaultText;
  select.appendChild(defaultOption);

  // 넘겨받은 옵션을 바탕으로 드롭다운 메뉴 아이템 생성
  options.forEach(optionData => {
    const option = document.createElement('option');
    option.value = optionData.id;
    option.textContent = optionData[textProperty]; // 동적 속성 접근
    select.appendChild(option);
  });

  return select;
}

function createActionButton(label, className, eventListener) {
  const button = document.createElement('button');
  button.textContent = label;
  button.classList.add('btn', className, 'mr-2');
  button.addEventListener('click', eventListener);
  return button;
}

function addInputRow() {
  if (isEditing) return;

  const tableBody = document.getElementById('table-body');
  const row = tableBody.insertRow();
  isEditing = true;

  const memberIdInput = createInput('number');
  memberIdInput.classList.add('member-id-input');
  memberIdInput.placeholder = '예약할 회원 ID';
  memberIdInput.min = '1';
  memberIdInput.step = '1';

  const dateInput = createInput('date');
  const timeDropdown = createSelect(
      timesOptions, '시간 선택', 'time-select', 'value'
  );
  const themeDropdown = createSelect(
      themesOptions, '테마 선택', 'theme-select', 'name'
  );

  timeDropdown.options[0].value = '';
  themeDropdown.options[0].value = '';

  const fields = [
    '',
    memberIdInput,
    themeDropdown,
    dateInput,
    timeDropdown
  ];

  fields.forEach((field, index) => {
    const cell = row.insertCell(index);

    if (typeof field === 'string') {
      cell.textContent = field;
    } else {
      cell.appendChild(field);
    }
  });

  const actionCell = row.insertCell();
  actionCell.appendChild(
      createActionButton('확인', 'btn-custom', saveRow)
  );
  actionCell.appendChild(
      createActionButton('취소', 'btn-secondary', () => {
        row.remove();
        isEditing = false;
      })
  );
}

function createInput(type) {
  const input = document.createElement('input');
  input.type = type;
  input.className = 'form-control';
  return input;
}

function createActionButton(label, className, eventListener) {
  const button = document.createElement('button');
  button.textContent = label;
  button.classList.add('btn', className, 'mr-2');
  button.addEventListener('click', eventListener);
  return button;
}

function saveRow(event) {
  event.stopPropagation();

  const row = event.target.closest('tr');
  const memberIdInput = row.querySelector('.member-id-input');
  const themeSelect = row.querySelector('#theme-select');
  const timeSelect = row.querySelector('#time-select');
  const dateInput = row.querySelector('input[type="date"]');

  const memberId = Number(memberIdInput.value);

  if (!Number.isSafeInteger(memberId) || memberId <= 0
      || !themeSelect.value
      || !timeSelect.value
      || !dateInput.value) {
    alert('회원 ID, 테마, 날짜, 시간을 올바르게 입력해주세요.');
    return;
  }

  const reservation = {
    memberId: memberId,
    theme: Number(themeSelect.value),
    date: dateInput.value,
    time: Number(timeSelect.value)
  };

  const button = event.target;
  button.disabled = true;

  requestCreate(reservation)
      .then(() => location.reload())
      .catch(error => {
        button.disabled = false;
        alert('예약에 실패했습니다. 회원 ID와 입력 내용을 확인해주세요.');
        console.error(error);
      });
}

function deleteRow(event) {
  const row = event.target.closest('tr');
  const reservationId = row.cells[0].textContent;

  requestDelete(reservationId)
      .then(() => row.remove())
      .catch(error => console.error('Error:', error));
}

function requestCreate(reservation) {
  const requestOptions = {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(reservation)
  };

  return fetch(RESERVATION_API_ENDPOINT, requestOptions)
      .then(response => {
        if (response.status === 201) return response.json();
        throw new Error('Create failed');
      });
}

function requestDelete(id) {
  const requestOptions = {
    method: 'DELETE',
  };

  return fetch(`${RESERVATION_API_ENDPOINT}/${id}`, requestOptions)
      .then(response => {
        if (response.status !== 204) throw new Error('Delete failed');
      });
}

function requestRead(endpoint) {
  return fetch(endpoint)
      .then(response => {
        if (response.status === 200) return response.json();
        throw new Error('Read failed');
      });
}