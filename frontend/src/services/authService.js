// The base URL of your backend.
// All API calls start with this.
const BASE_URL = 'http://localhost:8080/api'

// This function calls the login endpoint.
// It is async because it has to wait for the server to respond.
export async function login(email, password) {

    // fetch() sends an HTTP request.
    const response = await fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',                                    // POST request
        headers: { 'Content-Type': 'application/json' },  // "I am sending JSON"
        body: JSON.stringify({ email, password })          // convert JS object to JSON text
    })

    // response.json() reads the JSON text the server sent back
    // and converts it to a JavaScript object.
    const data = await response.json()

    // If the server returned a non-200 status (like 401), throw an error.
    // This causes the catch() block in LoginPage to run.
    if (!response.ok) {
        throw new Error(data.message || 'Login failed')
    }

    return data
}