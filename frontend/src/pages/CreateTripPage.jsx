import { useState } from 'react'
import { createTrip } from '../services/tripService'
import DatePicker from '../components/DatePicker'
import styles from './CreateTripPage.module.css'

// Moedas suportadas pela API de câmbio (Frankfurter/BCE). O backend converte para BRL.
const CURRENCIES = ['BRL', 'USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'CNY', 'MXN']

// O <input type=date> entrega "aaaa-mm-dd"; o backend espera "dd/MM/yyyy".
const toBrDate = (iso) => {
    if (!iso) return ''
    const [y, m, d] = iso.split('-')
    return `${d}/${m}/${y}`
}

function CreateTripPage({ userId, onCreated, goToDashboard }) {
    const [name, setName] = useState('')
    const [budget, setBudget] = useState('')
    const [destination, setDestination] = useState('')
    const [currency, setCurrency] = useState('BRL')
    const [description, setDescription] = useState('')
    const [startDate, setStartDate] = useState('')
    const [endDate, setEndDate] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    async function handleCreate() {
        if (!name || !budget || !destination || !startDate || !endDate) {
            setError('Preencha nome, orçamento, destino e datas.')
            return
        }
        setLoading(true)
        setError('')
        try {
            // budget e informado em BRL; o backend converte para a moeda da viagem.
            await createTrip({
                name,
                budget: Number(budget),
                description,
                destination,
                currency,
                startDate: toBrDate(startDate),
                endDate: toBrDate(endDate),
            }, userId)
            onCreated()
        } catch (err) {
            setError(err.message)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className={styles.page}>
            <div className={styles.card}>
                <button className={styles.backButton} onClick={goToDashboard}>← Voltar</button>

                <h1 className={styles.title}>Nova viagem</h1>
                <p className={styles.subtitle}>Defina o orçamento e o período da viagem.</p>

                {error && <div className={styles.errorBox}>{error}</div>}

                <div className={styles.field}>
                    <label className={styles.label}>Nome</label>
                    <input className={styles.input} type="text" value={name}
                           onChange={e => setName(e.target.value)} placeholder="Ex.: Férias no Japão" />
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>Destino</label>
                    <input className={styles.input} type="text" value={destination}
                           onChange={e => setDestination(e.target.value)} placeholder="Ex.: Japão" />
                </div>

                <div className={styles.row2}>
                    <div className={styles.field}>
                        <label className={styles.label}>Orçamento total (R$)</label>
                        <input className={styles.input} type="number" min="0" step="0.01" value={budget}
                               onChange={e => setBudget(e.target.value)} placeholder="2000" />
                    </div>
                    <div className={styles.field}>
                        <label className={styles.label}>Moeda da viagem</label>
                        <select className={styles.select} value={currency}
                                onChange={e => setCurrency(e.target.value)}>
                            {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
                        </select>
                    </div>
                </div>

                <div className={styles.row2}>
                    <div className={styles.field}>
                        <label className={styles.label}>Início</label>
                        <DatePicker value={startDate} onChange={setStartDate} />
                    </div>
                    <div className={styles.field}>
                        <label className={styles.label}>Fim</label>
                        <DatePicker value={endDate} onChange={setEndDate} min={startDate} />
                    </div>
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>Descrição (opcional)</label>
                    <textarea className={styles.textarea} value={description}
                              onChange={e => setDescription(e.target.value)} placeholder="Anotações sobre a viagem" />
                </div>

                <button className={styles.primaryButton} onClick={handleCreate} disabled={loading}>
                    {loading ? 'Criando...' : 'Criar viagem'}
                </button>
            </div>
        </div>
    )
}

export default CreateTripPage
