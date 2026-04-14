import { useState } from 'react'
import { login } from '../services/authService'
import styles from './LoginPage.module.css'

function LoginPage({ onLoginSuccess, goToBoot }) {

    const [email, setEmail]       = useState('')
    const [password, setPassword] = useState('')

    const [error, setError]       = useState('')

    const [loading, setLoading]   = useState(false)

    async function handleLogin() {
        if (!email || !password) {
            setError('Por favor preencha todos os campos.')
            return
        }

        setLoading(true)
        setError('')

        try {
            await login(email, password)
            onLoginSuccess()

        } catch (err) {
            setError(err.message)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>Login</h1>

            {error && <p style={{ color: 'red' }}>{error}</p>}

            <div>
                <label>E-mail</label>
                <br />
                <input
                    type="email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    placeholder="seu@email.com"
                />
            </div>

            <br />
            <div>
                <label>Senha</label>
                <br />
                <input
                    type="password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    placeholder="Sua senha"
                />
            </div>

            <br />
            <button onClick={handleLogin} disabled={loading}>
                {loading ? 'Logging in...' : 'Login'}
            </button>

            <button onClick={goToBoot}>Retornar</button>
        </div>
    )
}

export default LoginPage