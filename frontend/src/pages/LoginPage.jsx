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
        <div className={styles.page}>
            <div className={styles.card}>
                <button className={styles.backButton} onClick={goToBoot}>
                    ← Voltar
                </button>

                <h1 className={styles.title}>Bem-vindo de volta</h1>
                <p className={styles.subtitle}>Entre na sua conta para continuar.</p>

                {error && <div className={styles.errorBox}>{error}</div>}

                <div className={styles.field}>
                    <label className={styles.label}>E-mail</label>
                    <input
                        className={styles.input}
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        placeholder="seu@email.com"
                    />
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>Senha</label>
                    <input
                        className={styles.input}
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        placeholder="Sua senha"
                        onKeyDown={e => e.key === 'Enter' && handleLogin()}
                    />
                </div>

                <button
                    className={styles.primaryButton}
                    onClick={handleLogin}
                    disabled={loading}
                >
                    {loading ? 'Entrando...' : 'Entrar'}
                </button>
            </div>
        </div>
    )
}

export default LoginPage