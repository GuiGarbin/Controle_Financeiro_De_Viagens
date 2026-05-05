import { useState } from 'react'
import styles from './RegisterPage.module.css'

function RegisterPage({ onRegisterSuccess, goToBoot }) {
    const [fullName, setFullName]   = useState('')
    const [birthDate, setBirthDate] = useState('')
    const [email, setEmail]         = useState('')
    const [password, setPassword]   = useState('')
    const [error, setError]         = useState('')
    const [loading, setLoading]     = useState(false)

    async function handleRegister() {
        if (!fullName || !birthDate || !email || !password) {
            setError('Por favor preencha todos os campos.')
            return
        }
        setLoading(true)
        setError('')
        try {
            // TODO: replace with real API call when backend is ready
            await new Promise(resolve => setTimeout(resolve, 800))
            onRegisterSuccess()
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

                <h1 className={styles.title}>Criar conta</h1>
                <p className={styles.subtitle}>Preencha seus dados para começar.</p>

                {error && <div className={styles.errorBox}>{error}</div>}

                <div className={styles.field}>
                    <label className={styles.label}>Nome completo</label>
                    <input
                        className={styles.input}
                        type="text"
                        value={fullName}
                        onChange={e => setFullName(e.target.value)}
                        placeholder="João Silva"
                    />
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>Data de nascimento</label>
                    <input
                        className={`${styles.input} ${styles.dateInput}`}
                        type="date"
                        value={birthDate}
                        onChange={e => setBirthDate(e.target.value)}
                    />
                </div>

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
                        placeholder="Mínimo 8 caracteres"
                    />
                </div>

                <button
                    className={styles.primaryButton}
                    onClick={handleRegister}
                    disabled={loading}
                >
                    {loading ? 'Criando conta...' : 'Criar conta'}
                </button>
            </div>
        </div>
    )
}

export default RegisterPage