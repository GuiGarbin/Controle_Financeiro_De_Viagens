
const BASE_URL = 'http://localhost:8080/api'


export async function login(email, password) {


    const response = await fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    })


    const data = await response.json()

    if (!response.ok) {
        throw new Error(data.message || 'Login failed')
    }

    return data
}

// Cadastra um novo usuário no backend.
// birthDate deve estar no formato ISO (aaaa-mm-dd), que é o que o input de data envia.
export async function register(fullName, birthDate, email, password) {
    const response = await fetch(`${BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fullName, birthDate, email, password })
    })

    const data = await response.json()

    if (!response.ok) {
        throw new Error(data.message || 'Falha no cadastro')
    }

    return data
}