import { useEffect, useState } from 'react'
import { getCurrentTrip, addExpense } from '../services/tripService'
import styles from './AddExpensePage.module.css'

// "aaaa-mm-dd" (input) <-> "dd/MM/yyyy" (backend).
const toBrDate = (iso) => {
    if (!iso) return ''
    const [y, m, d] = iso.split('-')
    return `${d}/${m}/${y}`
}
const toIsoDate = (br) => {
    if (!br) return ''
    const [d, m, y] = br.split('/')
    return `${y}-${m}-${d}`
}

function AddExpensePage({ userId, onAdded, goToDashboard }) {
    const [trip, setTrip] = useState(null)
    const [loadingTrip, setLoadingTrip] = useState(true)
    const [description, setDescription] = useState('')
    const [amount, setAmount] = useState('')
    const [date, setDate] = useState('')
    const [error, setError] = useState('')
    const [saving, setSaving] = useState(false)

    // Carrega a viagem atual para saber o id, a moeda e o intervalo de datas.
    useEffect(() => {
        let active = true
        getCurrentTrip(userId)
            .then((t) => {
                if (!active) return
                setTrip(t)
                if (t) setDate(toIsoDate(t.startDate)) // pre-seleciona o inicio da viagem
            })
            .catch((err) => { if (active) setError(err.message) })
            .finally(() => { if (active) setLoadingTrip(false) })
        return () => { active = false }
    }, [userId])

    async function handleAdd() {
        if (!description || !amount || !date) {
            setError('Preencha descrição, valor e data.')
            return
        }
        setSaving(true)
        setError('')
        try {
            await addExpense(trip.id, {
                description,
                amount: Number(amount),
                date: toBrDate(date),
            }, userId)
            onAdded()
        } catch (err) {
            setError(err.message)
        } finally {
            setSaving(false)
        }
    }

    return (
        <div className={styles.page}>
            <div className={styles.card}>
                <button className={styles.backButton} onClick={goToDashboard}>← Voltar</button>

                <h1 className={styles.title}>Novo gasto</h1>

                {loadingTrip && <p className={styles.stateText}>Carregando viagem...</p>}

                {!loadingTrip && !trip && (
                    <>
                        <p className={styles.subtitle}>Você ainda não tem uma viagem em andamento.</p>
                        <p className={styles.stateText}>Crie uma viagem antes de adicionar gastos.</p>
                    </>
                )}

                {!loadingTrip && trip && (
                    <>
                        <p className={styles.subtitle}>Registre um gasto na viagem atual.</p>
                        <div className={styles.context}>
                            {trip.name} · {trip.destination} · {trip.currency}
                        </div>

                        {error && <div className={styles.errorBox}>{error}</div>}

                        <div className={styles.field}>
                            <label className={styles.label}>Descrição</label>
                            <input className={styles.input} type="text" value={description}
                                   onChange={e => setDescription(e.target.value)} placeholder="Ex.: Jantar" />
                        </div>

                        <div className={styles.field}>
                            <label className={styles.label}>Valor ({trip.currency})</label>
                            <input className={styles.input} type="number" min="0" step="0.01" value={amount}
                                   onChange={e => setAmount(e.target.value)} placeholder="0,00" />
                        </div>

                        <div className={styles.field}>
                            <label className={styles.label}>Data</label>
                            <input className={styles.input} type="date" value={date}
                                   min={toIsoDate(trip.startDate)} max={toIsoDate(trip.endDate)}
                                   onChange={e => setDate(e.target.value)} />
                        </div>

                        <button className={styles.primaryButton} onClick={handleAdd} disabled={saving}>
                            {saving ? 'Salvando...' : 'Adicionar gasto'}
                        </button>
                    </>
                )}

                {error && !trip && !loadingTrip && <div className={styles.errorBox}>{error}</div>}
            </div>
        </div>
    )
}

export default AddExpensePage
