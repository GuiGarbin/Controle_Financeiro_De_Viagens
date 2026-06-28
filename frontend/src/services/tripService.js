// Chamadas ao backend de viagens/gastos (Fase 4 do docs/APIintegration.md).
// Espelha o estilo de services/authService.js (fetch puro, sem Axios).
//
// O backend responde no formato ApiResponse<T> ({ success, message, data,
// timestamp }); aqui devolvemos apenas o `data`. O usuario logado, quando
// conhecido, vai no header X-User-Id (curto prazo, app desktop mono-usuario —
// ver secao 3 do plano); sem ele, o backend opera sobre todas as viagens.
const BASE_URL = 'http://localhost:8080/api'

function buildHeaders(userId) {
    const headers = { 'Content-Type': 'application/json' }
    if (userId) headers['X-User-Id'] = userId
    return headers
}

// Desembrulha o ApiResponse: retorna data em caso de sucesso, lanca Error caso contrario.
async function unwrap(response) {
    const body = await response.json().catch(() => null)
    if (!response.ok) {
        throw new Error((body && body.message) || 'Erro na requisicao')
    }
    return body ? body.data : null
}

// Viagem em andamento (status = true). Retorna null quando nao ha nenhuma (404),
// para a tela poder exibir um estado vazio em vez de erro.
export async function getCurrentTrip(userId) {
    const response = await fetch(`${BASE_URL}/trips/current`, { headers: buildHeaders(userId) })
    if (response.status === 404) return null
    return unwrap(response)
}

// Lista as viagens (do usuario, se userId informado; senao todas).
export async function getTrips(userId) {
    const response = await fetch(`${BASE_URL}/trips`, { headers: buildHeaders(userId) })
    return unwrap(response)
}

// Detalhe de uma viagem por id.
export async function getTrip(id, userId) {
    const response = await fetch(`${BASE_URL}/trips/${id}`, { headers: buildHeaders(userId) })
    return unwrap(response)
}

// Cria uma viagem. `trip` segue o corpo de POST /api/trips
// (name, budget, description, destination, currency, startDate, endDate).
export async function createTrip(trip, userId) {
    const response = await fetch(`${BASE_URL}/trips`, {
        method: 'POST',
        headers: buildHeaders(userId),
        body: JSON.stringify(trip),
    })
    return unwrap(response)
}

// Define qual viagem e a atual (ativa esta e desativa as outras do dono).
export async function activateTrip(id, userId) {
    const response = await fetch(`${BASE_URL}/trips/${id}/activate`, {
        method: 'PUT',
        headers: buildHeaders(userId),
    })
    return unwrap(response)
}

// Remove uma viagem.
export async function deleteTrip(id, userId) {
    const response = await fetch(`${BASE_URL}/trips/${id}`, {
        method: 'DELETE',
        headers: buildHeaders(userId),
    })
    return unwrap(response)
}

// Adiciona um gasto a uma viagem. `expense` = { description, amount, date, notes? }.
// Retorna o AddExpenseResponse (expense + verificacao de orcamento).
export async function addExpense(tripId, expense, userId) {
    const response = await fetch(`${BASE_URL}/trips/${tripId}/expenses`, {
        method: 'POST',
        headers: buildHeaders(userId),
        body: JSON.stringify(expense),
    })
    return unwrap(response)
}

// Lista os gastos de uma viagem (opcionalmente filtrando por data dd/MM/yyyy).
export async function getExpenses(tripId, date, userId) {
    const url = date
        ? `${BASE_URL}/trips/${tripId}/expenses?date=${encodeURIComponent(date)}`
        : `${BASE_URL}/trips/${tripId}/expenses`
    const response = await fetch(url, { headers: buildHeaders(userId) })
    return unwrap(response)
}
