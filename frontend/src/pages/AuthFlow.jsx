import { useState } from 'react'
import { login, register } from '../services/authService'
import DatePicker from '../components/DatePicker'
import styles from './AuthFlow.module.css'

// Tela de autenticação com animação de "morph": o botão escolhido vira o card
// (marrom -> branco), o texto inicial some e os campos aparecem. Tudo é dirigido
// por um único estado `open` + classe na .stage (CSS cuida das transições).
function AuthFlow({ onAuthSuccess }) {
    const [mode, setMode] = useState('login')
    const [open, setOpen] = useState(false)
    const [exiting, setExiting] = useState(false) // limpa o card antes do white-out

    const [fullName, setFullName] = useState('')
    const [birthDate, setBirthDate] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    function choose(target) {
        setError('')
        setMode(target)
        setOpen(true)
    }

    function back() {
        setError('')
        setOpen(false)
    }

    async function submit() {
        if (mode === 'login') {
            if (!email || !password) { setError('Por favor preencha todos os campos.'); return }
        } else {
            if (!fullName || !birthDate || !email || !password) { setError('Por favor preencha todos os campos.'); return }
        }
        setLoading(true)
        setError('')
        try {
            const data = mode === 'login'
                ? await login(email, password)
                : await register(fullName, birthDate, email, password)
            setExiting(true) // some com os campos; o App assume com o white-out
            setTimeout(() => onAuthSuccess(data.userId), 240)
        } catch (err) {
            setError(err.message)
        } finally {
            setLoading(false)
        }
    }

    const isLogin = mode === 'login'

    return (
        <div className={`${styles.stage} ${open ? styles.stageOpen : ''} ${exiting ? styles.stageExiting : ''}`}>
            <div className={styles.hero}>
                <div className={styles.badge}>✈ Controle de Viagens</div>
                <h1 className={styles.title}>Sua viagem,<br />sob controle.</h1>
                <p className={styles.subtitle}>
                    Planeje gastos, divida despesas e acompanhe seu orçamento em tempo real.
                </p>
            </div>

            <div className={styles.card}>
                <div className={styles.boot}>
                    <button className={styles.primaryButton} onClick={() => choose('register')}>
                        Criar conta
                    </button>
                    <button className={styles.secondaryButton} onClick={() => choose('login')}>
                        Já tenho conta
                    </button>
                </div>

                <div className={styles.form}>
                    <button className={styles.backButton} onClick={back}>← Voltar</button>
                    <h2 className={styles.cardTitle}>{isLogin ? 'Bem-vindo de volta' : 'Criar conta'}</h2>
                    <p className={styles.cardSubtitle}>
                        {isLogin ? 'Entre na sua conta para continuar.' : 'Preencha seus dados para começar.'}
                    </p>

                    {error && <div className={styles.errorBox}>{error}</div>}

                    {!isLogin && (
                        <>
                            <div className={styles.field}>
                                <label className={styles.label}>Nome completo</label>
                                <input className={styles.input} type="text" value={fullName}
                                       onChange={e => setFullName(e.target.value)} placeholder="João Silva" />
                            </div>
                            <div className={styles.field}>
                                <label className={styles.label}>Data de nascimento</label>
                                <DatePicker value={birthDate} onChange={setBirthDate}
                                            placeholder="Clique aqui e selecione a data"
                                            max={new Date().toLocaleDateString('en-CA')} />
                            </div>
                        </>
                    )}

                    <div className={styles.field}>
                        <label className={styles.label}>E-mail</label>
                        <input className={styles.input} type="email" value={email}
                               onChange={e => setEmail(e.target.value)} placeholder="seu@email.com" />
                    </div>

                    <div className={styles.field}>
                        <label className={styles.label}>Senha</label>
                        <input className={styles.input} type="password" value={password}
                               onChange={e => setPassword(e.target.value)}
                               placeholder={isLogin ? 'Sua senha' : 'Mínimo 8 caracteres'}
                               onKeyDown={e => e.key === 'Enter' && submit()} />
                    </div>

                    <button className={styles.submit} onClick={submit} disabled={loading}>
                        {loading
                            ? (isLogin ? 'Entrando...' : 'Criando conta...')
                            : (isLogin ? 'Entrar' : 'Criar conta')}
                    </button>
                </div>
            </div>
        </div>
    )
}

export default AuthFlow
