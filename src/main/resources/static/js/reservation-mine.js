const RESERVATION_API_ENDPOINT = '/reservations-mine';

document.addEventListener('DOMContentLoaded', () => {
  requestRead(RESERVATION_API_ENDPOINT)
      .then(render)
      .catch(error => {
        alert(error.message);
      });
});

function render(data) {
  const tableBody = document.getElementById('table-body');
  tableBody.innerHTML = '';

  data.forEach(item => {
    const row = tableBody.insertRow();

    row.insertCell().textContent = item.theme;
    row.insertCell().textContent = item.date;
    row.insertCell().textContent = item.time;
    row.insertCell().textContent = item.status;

    const actionCell = row.insertCell();

    if (item.waitingId != null) {
      const cancelButton = document.createElement('button');
      cancelButton.textContent = '취소';
      cancelButton.className = 'btn btn-danger';

      cancelButton.addEventListener('click', () => {
        cancelButton.disabled = true;

        requestDeleteWaiting(item.waitingId)
            .then(() => window.location.reload())
            .catch(error => {
              cancelButton.disabled = false;
              alert(error.message);
            });
      });

      actionCell.appendChild(cancelButton);
    }
  });
}

function requestRead(endpoint) {
  return fetch(endpoint)
      .then(response => {
        if (response.status === 401) {
          throw new Error('로그인 후 이용해주세요.');
        }

        if (!response.ok) {
          throw new Error('예약 목록을 불러오지 못했습니다.');
        }

        return response.json();
      });
}

function requestDeleteWaiting(id) {
  return fetch('/waitings/' + id, {
    method: 'DELETE'
  }).then(response => {
    if (response.status === 403) {
      throw new Error('본인의 예약 대기만 취소할 수 있습니다.');
    }

    if (response.status !== 204) {
      throw new Error('예약 대기를 취소하지 못했습니다.');
    }
  });
}
