import { useEffect, useState } from 'react'
import { getCurrentTrip } from '../services/tripService'
import styles from './DashBoardPage.module.css'

// Simbolos das moedas mais comuns; cai no proprio codigo (ex.: "JPY") se nao mapeado.
const CURRENCY_SYMBOLS = {
    BRL: 'R$', USD: 'US$', EUR: '€', JPY: '¥', GBP: '£',
    CHF: 'CHF', CAD: 'C$', AUD: 'A$', ARS: '$',
}
const symbolFor = (code) => CURRENCY_SYMBOLS[code] || code || ''

// Soma os gastos (na moeda da viagem) de um dia.
const daySpent = (day) =>
    (day.listExpenses || []).reduce((sum, e) => sum + (e.amount || 0), 0)

// Formata um valor inteiro na moeda estrangeira (com separador de milhar).
const fmt = (n) => Math.round(n || 0).toLocaleString('pt-BR')

// "07/06/2026" -> "07/06" (rotulo curto para as linhas por dia).
const dayLabel = (d) => (d || '').slice(0, 5)

function DashBoardPage({ userId, onCreateTrip, onAddExpense, onLogout }) {
    const [trip, setTrip] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        let active = true
        // Busca a viagem atual do usuário logado (escopada por X-User-Id).
        getCurrentTrip(userId)
            .then((data) => { if (active) setTrip(data) })
            .catch((err) => { if (active) setError(err.message) })
            .finally(() => { if (active) setLoading(false) })
        return () => { active = false }
    }, [userId])

    // --- Estados de carregamento / erro / vazio ---
    if (loading) {
        return (
            <div className={styles.stateScreen}>
                <div className={styles.stateText}>Carregando viagem...</div>
            </div>
        )
    }

    if (error) {
        return (
            <div className={styles.stateScreen}>
                <div className={styles.stateTitle}>Não foi possível carregar</div>
                <div className={`${styles.stateText} ${styles.stateError}`}>{error}</div>
                <button className={styles.logoutButton} onClick={onLogout}>Sair</button>
            </div>
        )
    }

    if (!trip) {
        return (
            <div className={styles.stateScreen}>
                <div className={styles.stateTitle}>Nenhuma viagem em andamento</div>
                <div className={styles.stateText}>
                    Crie uma viagem para acompanhar seus gastos aqui.
                </div>
                <button className={styles.stateButton} onClick={onCreateTrip}>Criar viagem</button>
                <button className={styles.logoutButton} onClick={onLogout}>Sair</button>
            </div>
        )
    }

    // --- Dados derivados (na moeda da viagem) ---
    const days = trip.dailyBudgetList || []
    const points = trip.listTuristic || []
    const symbol = symbolFor(trip.currency)
    const rate = trip.currencyValue || 1 // BRL por 1 unidade da moeda da viagem
    const toReal = (n) => (n * rate).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

    const budget = trip.budget || 0
    const perDayBudget = days.length ? budget / days.length : 0
    const totalSpent = days.reduce((sum, d) => sum + daySpent(d), 0)
    const remaining = budget - totalSpent
    const pctUsed = budget > 0 ? (totalSpent / budget) * 100 : 0
    const pctAvailable = Math.max(0, 100 - pctUsed)

    return (
        <div className={styles.shell}>

            <aside className={styles.sidebar}>
                <div className={styles.logo}>
                    <div className={styles.logoIcon}>✈</div>
                    <span className={styles.logoText}>TripFinance</span>
                </div>

                <span className={styles.navSection}>VIAGEM</span>
                <div className={`${styles.navItem} ${styles.active}`}>Visão geral</div>
                <div className={styles.navItem} onClick={onAddExpense}>Novo gasto</div>
                <div className={styles.navItem}>Histórico</div>
                <div className={styles.navItem}>Pontos turísticos</div>

                <span className={styles.navSection}>PLANEJAMENTO</span>
                <div className={styles.navItem}>Itinerário</div>
                <div className={styles.navItem}>Relatório</div>

                <div className={styles.tripPill}>
                    <div className={styles.tripPillLabel}>Viagem atual</div>
                    <div className={styles.tripPillName}>{trip.name}</div>
                    <div className={styles.tripPillDates}>{trip.startDate} — {trip.endDate}</div>
                </div>
            </aside>

            <main className={styles.main}>
                <div className={styles.header}>
                    <div>
                        <h1 className={styles.pageTitle}>Visão geral</h1>
                        <p className={styles.pageSubtitle}>
                            {trip.destination} · {trip.currency} ({symbol}) · {days.length} dias
                        </p>
                    </div>
                    <div className={styles.headerRight}>
                        <span className={styles.badge}>Em andamento</span>
                        <button className={styles.logoutButton} onClick={onAddExpense}>+ Novo gasto</button>
                        <button className={styles.logoutButton} onClick={onCreateTrip}>+ Nova viagem</button>
                        {/* Sai da conta e volta para a tela inicial */}
                        <button className={styles.logoutButton} onClick={onLogout}>Sair</button>
                    </div>
                </div>

                <div className={styles.metrics}>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Orçamento total</div>
                        <div className={styles.metricValue}>{symbol} {fmt(budget)}</div>
                        <div className={styles.metricSub}>R$ {toReal(budget)}</div>
                    </div>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Orçamento diário</div>
                        <div className={styles.metricValue}>{symbol} {fmt(perDayBudget)}</div>
                        <div className={styles.metricSub}>R$ {toReal(perDayBudget)} / dia</div>
                    </div>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Gasto até agora</div>
                        <div className={styles.metricValue}>{symbol} {fmt(totalSpent)}</div>
                        <div className={styles.metricSub}>R$ {toReal(totalSpent)}</div>
                    </div>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Saldo restante</div>
                        <div className={styles.metricValue}>{symbol} {fmt(remaining)}</div>
                        <div className={`${styles.metricSub} ${remaining < 0 ? styles.warn : ''}`}>
                            {pctAvailable.toFixed(1)}% disponível
                        </div>
                    </div>
                </div>

                <div className={styles.row}>
                    <div className={styles.card} style={{ flex: 1.5 }}>
                        <div className={styles.cardTitle}>ORÇAMENTO POR DIA</div>
                        {days.map((day, i) => {
                            const dayRemaining = day.budget - daySpent(day)
                            const pct = day.budget > 0
                                ? Math.max(0, Math.min(100, (dayRemaining / day.budget) * 100))
                                : 0
                            return (
                                <div key={i} className={styles.dayRow}>
                                    <span className={styles.dayDate}>{dayLabel(day.date)}</span>
                                    <div className={styles.barWrap}>
                                        <div className={styles.barFill} style={{ width: `${pct}%` }} />
                                    </div>
                                    <span className={styles.dayAmount}>{symbol} {fmt(dayRemaining)}</span>
                                    <span className={styles.dayConverted}>R$ {toReal(dayRemaining)}</span>
                                </div>
                            )
                        })}
                    </div>

                    <div className={styles.rightCol}>
                        <div className={styles.card} style={{ flex: 1 }}>
                            <div className={styles.cardTitle}>PONTOS TURÍSTICOS</div>
                            {points.length === 0 && (
                                <div className={styles.emptyHint}>Nenhum ponto turístico cadastrado.</div>
                            )}
                            {points.map((p, i) => (
                                <div key={i} className={styles.pointRow}>
                                    <span className={styles.pointName}>📍 {p.name}</span>
                                    <span className={styles.pointCost}>{symbol} {fmt(p.cost)}</span>
                                </div>
                            ))}
                        </div>

                        <div className={`${styles.card} ${styles.cardMuted}`}>
                            <div className={styles.cardTitle}>PROJEÇÃO DE GASTOS</div>
                            <div className={styles.progRow}>
                                <span className={styles.progLabel}>Gasto até agora</span>
                                <span className={styles.progValue}>{symbol} {fmt(totalSpent)}</span>
                            </div>
                            <div className={styles.progTrack}>
                                <div className={styles.progFill} style={{ width: `${Math.min(100, pctUsed)}%` }} />
                            </div>
                            <div className={styles.progFooter}>
                                <span className={styles.progFootItem}>Total: {symbol} {fmt(budget)}</span>
                                <span className={styles.progFootItem}>{pctUsed.toFixed(1)}% utilizado</span>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    )
}

export default DashBoardPage
