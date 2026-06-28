import { useEffect, useState } from 'react'
import { getCurrentTrip, getTrips, activateTrip, deleteTrip, addTuristicPoint, addExpense } from '../services/tripService'
import DatePicker from '../components/DatePicker'
import styles from './DashBoardPage.module.css'

// Simbolos das moedas mais comuns; cai no proprio codigo (ex.: "JPY") se nao mapeado.
const CURRENCY_SYMBOLS = {
    BRL: 'R$', USD: 'US$', EUR: '€', JPY: '¥', GBP: '£',
    CHF: 'CHF', CAD: 'C$', AUD: 'A$', ARS: '$', CLP: '$',
}
const symbolFor = (code) => CURRENCY_SYMBOLS[code] || code || ''
const daySpent = (day) => (day.listExpenses || []).reduce((s, e) => s + (e.amount || 0), 0)
const tripSpent = (trip) => (trip.dailyBudgetList || []).reduce((s, d) => s + daySpent(d), 0)
const fmt = (n) => Math.round(n || 0).toLocaleString('pt-BR')
const fmt2 = (n) => (n || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const dayLabel = (d) => (d || '').slice(0, 5)
// Conversões entre "aaaa-mm-dd" (input date) e "dd/MM/yyyy" (backend).
const toBrDate = (iso) => { if (!iso) return ''; const [y, m, d] = iso.split('-'); return `${d}/${m}/${y}` }
const toIsoDate = (br) => { if (!br) return ''; const [d, m, y] = br.split('/'); return `${y}-${m}-${d}` }

// "dd/MM/yyyy" -> Date (meia-noite local).
const parseBr = (s) => {
    const [d, m, y] = (s || '').split('/').map(Number)
    return new Date(y, (m || 1) - 1, d || 1)
}
const todayMidnight = () => {
    const n = new Date()
    return new Date(n.getFullYear(), n.getMonth(), n.getDate())
}


const tripStatusLabel = (t) => {
    if (!t || !t.startDate || !t.endDate) return ''
    const today = todayMidnight()
    const start = parseBr(t.startDate)
    const end = parseBr(t.endDate)
    if (start <= today && today <= end) return 'Em andamento'
    if (start > today) return t.status ? 'Planejada' : 'Salva'
    return 'Encerrada'
}

const VIEW_TITLES = {
    overview: 'Visão geral',
    history: 'Histórico',
    trips: 'Minhas viagens',
    itinerary: 'Itinerário',
    report: 'Relatório',
    turistic: 'Pontos turísticos',
    expense: 'Novo gasto',
}

function DashBoardPage({ userId, onCreateTrip, onLogout }) {
    const [view, setView] = useState('overview')
    const [trip, setTrip] = useState(null)   // viagem atual (status = true)
    const [trips, setTrips] = useState([])    // todas as viagens do usuário
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)   // ações de ativar/excluir/adicionar em andamento
    const [tpName, setTpName] = useState('')  // form de ponto turístico
    const [tpCost, setTpCost] = useState('')
    const [exDesc, setExDesc] = useState('')  // form de novo gasto
    const [exAmount, setExAmount] = useState('')
    const [exDate, setExDate] = useState('')
    const [exMsg, setExMsg] = useState('')    // aviso de orçamento após adicionar
    const [openDays, setOpenDays] = useState({}) // dias expandidos no "orçamento por dia"

    // Busca a viagem atual + a lista de viagens do usuário logado.
    async function reload() {
        setError('')
        const [cur, list] = await Promise.all([getCurrentTrip(userId), getTrips(userId)])
        setTrip(cur)
        setTrips(list || [])
    }

    useEffect(() => {
        let active = true
        reload()
            .catch((e) => { if (active) setError(e.message) })
            .finally(() => { if (active) setLoading(false) })
        return () => { active = false }
    }, [userId])

    async function handleActivate(id) {
        setBusy(true)
        try { await activateTrip(id, userId); await reload(); setView('overview') }
        catch (e) { setError(e.message) }
        finally { setBusy(false) }
    }

    async function handleDelete(id) {
        setBusy(true)
        try { await deleteTrip(id, userId); await reload() }
        catch (e) { setError(e.message) }
        finally { setBusy(false) }
    }

    async function addPoint() {
        if (!tpName || !tpCost) { setError('Informe nome e custo do ponto turístico.'); return }
        setBusy(true)
        try {
            await addTuristicPoint(trip.id, { name: tpName, cost: Number(tpCost) }, userId)
            setTpName(''); setTpCost('')
            await reload()
        } catch (e) { setError(e.message) }
        finally { setBusy(false) }
    }

    async function submitExpense() {
        if (!exDesc || !exAmount || !exDate) { setError('Preencha descrição, valor e data.'); return }
        setBusy(true); setError(''); setExMsg('')
        try {
            const res = await addExpense(trip.id, { description: exDesc, amount: Number(exAmount), date: toBrDate(exDate) }, userId)
            setExDesc(''); setExAmount('')
            await reload()
            if (res && res.warning) setExMsg(res.warning)
        } catch (e) { setError(e.message) }
        finally { setBusy(false) }
    }

    if (loading) {
        return <div className={styles.stateScreen}><div className={styles.stateText}>Carregando...</div></div>
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

    // Valores derivados da viagem atual (na moeda da viagem).
    const days = trip ? (trip.dailyBudgetList || []) : []
    const points = trip ? (trip.listTuristic || []) : []
    const symbol = trip ? symbolFor(trip.currency) : ''
    const rate = trip ? (trip.currencyValue || 1) : 1 // BRL por 1 unidade da moeda
    const toReal = (n) => fmt2(n * rate)
    const budget = trip ? (trip.budget || 0) : 0
    const perDayBudget = days.length ? budget / days.length : 0
    const totalSpent = trip ? tripSpent(trip) : 0
    const remaining = budget - totalSpent
    const pctUsed = budget > 0 ? (totalSpent / budget) * 100 : 0
    const pctAvailable = Math.max(0, 100 - pctUsed)

    function subtitle() {
        if (view === 'trips') return `${trips.length} viagem(ns)`
        if (!trip) return 'Nenhuma viagem em andamento'
        if (view === 'history') {
            const n = days.reduce((s, d) => s + (d.listExpenses ? d.listExpenses.length : 0), 0)
            return `${trip.name} · ${n} gasto(s)`
        }
        if (view === 'turistic') return `${trip.name} · ${points.length} ponto(s)`
        if (view === 'expense') return `${trip.name} · ${trip.currency} (${symbol})`
        if (view === 'itinerary' || view === 'report') return `${trip.name} · ${trip.startDate} — ${trip.endDate}`
        return `${trip.destination} · ${trip.currency} (${symbol}) · ${days.length} dias`
    }

    // Item de navegação que troca a view interna; `active` destaca a atual.
    const navView = (label, key) => (
        <div className={`${styles.navItem} ${view === key ? styles.active : ''}`} onClick={() => setView(key)}>
            {label}
        </div>
    )

    function noTripBlock() {
        return (
            <div className={styles.card}>
                <div className={styles.stateText}>Nenhuma viagem em andamento.</div>
                <button className={styles.stateButton} style={{ marginTop: 12 }} onClick={onCreateTrip}>Criar viagem</button>
            </div>
        )
    }

    function renderOverview() {
        if (!trip) return noTripBlock()
        return (
            <>
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
                            const spent = daySpent(day)
                            const dayRemaining = day.budget - spent
                            // A barra mostra o quanto JÁ foi gasto do orçamento do dia.
                            const pctSpent = day.budget > 0 ? (spent / day.budget) * 100 : 0
                            const width = Math.min(100, pctSpent)
                            // Verde -> amarelo (75%) -> vermelho (90%) -> preto (100%).
                            const color = pctSpent >= 100 ? '#1c1917'
                                : pctSpent >= 90 ? '#dc2626'
                                    : pctSpent >= 75 ? '#febc2e'
                                        : '#28c840'
                            const expenses = day.listExpenses || []
                            return (
                                <div key={i}>
                                    <div className={styles.dayRow}>
                                        <button className={styles.dayToggle}
                                                onClick={() => setOpenDays(o => ({ ...o, [i]: !o[i] }))}
                                                title="Ver gastos do dia">
                                            {openDays[i] ? '▴' : '▾'}
                                        </button>
                                        <span className={styles.dayDate}>{dayLabel(day.date)}</span>
                                        <div className={styles.barWrap}>
                                            <div className={styles.barFill} style={{ width: `${width}%`, background: color }} />
                                        </div>
                                        <span className={styles.dayAmount}>{symbol} {fmt(dayRemaining)}</span>
                                        <span className={styles.dayConverted}>R$ {toReal(dayRemaining)}</span>
                                    </div>
                                    {openDays[i] && (
                                        <div className={styles.dayExpenses}>
                                            {expenses.length === 0
                                                ? <div className={styles.dayExpEmpty}>Sem gastos neste dia</div>
                                                : expenses.map((e, j) => (
                                                    <div key={e.id || j} className={styles.dayExpRow}>
                                                        <span>{e.description}</span>
                                                        <span>{symbol} {fmt(e.amount)} · R$ {toReal(e.amount)}</span>
                                                    </div>
                                                ))}
                                        </div>
                                    )}
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
            </>
        )
    }

    function renderTrips() {
        return (
            <div className={styles.card}>
                <div className={styles.toolbar}>
                    <div className={styles.cardTitle} style={{ marginBottom: 0 }}>SUAS VIAGENS</div>
                    <button className={styles.primaryAction} onClick={onCreateTrip}>+ Nova viagem</button>
                </div>
                {trips.length === 0 && <div className={styles.emptyHint}>Nenhuma viagem cadastrada.</div>}
                {trips.map((t) => {
                    const tSym = symbolFor(t.currency)
                    return (
                        <div key={t.id} className={styles.tripCard}>
                            <div>
                                <div className={styles.tripCardName}>{t.name}</div>
                                <div className={styles.tripCardMeta}>
                                    {t.destination} · {t.startDate} — {t.endDate} · {tSym} {fmt(t.budget)}
                                </div>
                                <span className={styles.lifecycle}>{tripStatusLabel(t)}</span>
                            </div>
                            <div className={styles.tripCardActions}>
                                {t.status
                                    ? <span className={styles.statusActive}>Ativa</span>
                                    : <button className={styles.smallBtn} disabled={busy} onClick={() => handleActivate(t.id)}>Ativar</button>}
                                <button className={`${styles.smallBtn} ${styles.smallBtnDanger}`} disabled={busy} onClick={() => handleDelete(t.id)}>Excluir</button>
                            </div>
                        </div>
                    )
                })}
            </div>
        )
    }

    function renderHistory() {
        if (!trip) return noTripBlock()
        const rows = []
        days.forEach((d) => (d.listExpenses || []).forEach((e) => rows.push(e)))
        return (
            <div className={styles.card}>
                <div className={styles.cardTitle}>HISTÓRICO DE GASTOS</div>
                {rows.length === 0 && <div className={styles.emptyHint}>Nenhum gasto registrado.</div>}
                {rows.map((e, i) => (
                    <div key={e.id || i} className={styles.histRow}>
                        <span className={styles.histDate}>{dayLabel(e.date)}</span>
                        <span className={styles.histDesc}>{e.description}</span>
                        <span className={styles.histAmount}>{symbol} {fmt(e.amount)}</span>
                        <span className={styles.histReal}>R$ {toReal(e.amount)}</span>
                    </div>
                ))}
            </div>
        )
    }

    function renderItinerary() {
        if (!trip) return noTripBlock()
        return (
            <div className={styles.card}>
                <div className={styles.cardTitle}>ITINERÁRIO POR DIA</div>
                {days.map((d, i) => {
                    const spent = daySpent(d)
                    const rem = d.budget - spent
                    const expenses = d.listExpenses || []
                    return (
                        <div key={i} className={styles.itinDay}>
                            <div className={styles.itinHeader}>
                                <span className={styles.itinDate}>{d.date}</span>
                                <span className={styles.itinMeta}>Restante {symbol} {fmt(rem)} de {symbol} {fmt(d.budget)}</span>
                            </div>
                            {expenses.length === 0
                                ? <div className={styles.itinExp}><span>Sem gastos</span><span /></div>
                                : expenses.map((e, j) => (
                                    <div key={e.id || j} className={styles.itinExp}>
                                        <span>{e.description}</span>
                                        <span>{symbol} {fmt(e.amount)}</span>
                                    </div>
                                ))}
                        </div>
                    )
                })}
            </div>
        )
    }

    function renderReport() {
        if (!trip) return noTripBlock()
        const allExp = []
        days.forEach((d) => (d.listExpenses || []).forEach((e) => allExp.push(e)))
        const avgPerDay = days.length ? totalSpent / days.length : 0
        const daysWith = days.filter((d) => daySpent(d) > 0).length
        const daysOver = days.filter((d) => daySpent(d) > d.budget).length
        const biggest = allExp.reduce((m, e) => (e.amount > (m ? m.amount : -1) ? e : m), null)
        return (
            <>
                <div className={styles.metrics}>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Orçamento total</div>
                        <div className={styles.metricValue}>{symbol} {fmt(budget)}</div>
                        <div className={styles.metricSub}>R$ {toReal(budget)}</div>
                    </div>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Gasto total</div>
                        <div className={styles.metricValue}>{symbol} {fmt(totalSpent)}</div>
                        <div className={styles.metricSub}>R$ {toReal(totalSpent)}</div>
                    </div>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Saldo restante</div>
                        <div className={styles.metricValue}>{symbol} {fmt(remaining)}</div>
                        <div className={`${styles.metricSub} ${remaining < 0 ? styles.warn : ''}`}>{pctUsed.toFixed(1)}% utilizado</div>
                    </div>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Média por dia</div>
                        <div className={styles.metricValue}>{symbol} {fmt(avgPerDay)}</div>
                        <div className={styles.metricSub}>R$ {toReal(avgPerDay)} / dia</div>
                    </div>
                </div>
                <div className={styles.card}>
                    <div className={styles.cardTitle}>RESUMO</div>
                    <div className={styles.histRow}><span className={styles.histDesc}>Dias da viagem</span><span className={styles.histAmount}>{days.length}</span></div>
                    <div className={styles.histRow}><span className={styles.histDesc}>Dias com gastos</span><span className={styles.histAmount}>{daysWith}</span></div>
                    <div className={styles.histRow}><span className={styles.histDesc}>Dias acima do orçamento diário</span><span className={styles.histAmount}>{daysOver}</span></div>
                    <div className={styles.histRow}>
                        <span className={styles.histDesc}>Maior gasto{biggest ? ` — ${biggest.description}` : ''}</span>
                        <span className={styles.histAmount}>{biggest ? `${symbol} ${fmt(biggest.amount)}` : '—'}</span>
                    </div>
                </div>
            </>
        )
    }

    function renderTuristic() {
        if (!trip) return noTripBlock()
        return (
            <div className={styles.card}>
                <div className={styles.toolbar}>
                    <div className={styles.cardTitle} style={{ marginBottom: 0 }}>PONTOS TURÍSTICOS</div>
                </div>
                <div className={styles.tpForm}>
                    <input className={styles.tpInput} type="text" placeholder="Nome do ponto"
                           value={tpName} onChange={e => setTpName(e.target.value)} />
                    <input className={styles.tpInputCost} type="number" min="0" step="0.01"
                           placeholder={`Custo (${symbol})`} value={tpCost}
                           onChange={e => setTpCost(e.target.value)} />
                    <button className={styles.primaryAction} disabled={busy} onClick={addPoint}>Adicionar</button>
                </div>
                {points.length === 0 && <div className={styles.emptyHint}>Nenhum ponto turístico cadastrado.</div>}
                {points.map((p, i) => (
                    <div key={i} className={styles.pointRow}>
                        <span className={styles.pointName}>📍 {p.name}</span>
                        <span className={styles.pointCost}>{symbol} {fmt(p.cost)} · R$ {toReal(p.cost)}</span>
                    </div>
                ))}
            </div>
        )
    }

    function renderExpense() {
        if (!trip) return noTripBlock()
        return (
            <div className={styles.card}>
                <div className={styles.cardTitle}>NOVO GASTO</div>
                {exMsg && <div className={styles.expenseMsg}>{exMsg}</div>}
                <div className={styles.formField}>
                    <label className={styles.formLabel}>Descrição</label>
                    <input className={styles.formInput} type="text" placeholder="Ex.: Jantar"
                           value={exDesc} onChange={e => setExDesc(e.target.value)} />
                </div>
                <div className={styles.formField}>
                    <label className={styles.formLabel}>Valor ({symbol})</label>
                    <input className={styles.formInput} type="number" min="0" step="0.01" placeholder="0,00"
                           value={exAmount} onChange={e => setExAmount(e.target.value)} />
                </div>
                <div className={styles.formField}>
                    <label className={styles.formLabel}>Data</label>
                    <DatePicker value={exDate} onChange={setExDate}
                                min={toIsoDate(trip.startDate)} max={toIsoDate(trip.endDate)} />
                </div>
                <button className={styles.primaryAction} disabled={busy} onClick={submitExpense}>
                    Adicionar gasto
                </button>
            </div>
        )
    }

    return (
        <div className={styles.shell}>
            <aside className={styles.sidebar}>
                <div className={styles.logo}>
                    <div className={styles.logoIcon}>✈</div>
                    <span className={styles.logoText}>TripFinance</span>
                </div>

                <span className={styles.navSection}>VIAGEM</span>
                {navView('Visão geral', 'overview')}
                {navView('Novo gasto', 'expense')}
                {navView('Histórico', 'history')}
                {navView('Minhas viagens', 'trips')}
                {navView('Pontos turísticos', 'turistic')}

                <span className={styles.navSection}>PLANEJAMENTO</span>
                {navView('Itinerário', 'itinerary')}
                {navView('Relatório', 'report')}

                <div className={styles.tripPill}>
                    <div className={styles.tripPillLabel}>Viagem atual</div>
                    <div className={styles.tripPillName}>{trip ? trip.name : '—'}</div>
                    {trip && <div className={styles.tripPillDates}>{trip.startDate} — {trip.endDate}</div>}
                </div>
            </aside>

            <main className={styles.main}>
                <div className={styles.header}>
                    <div>
                        <h1 className={styles.pageTitle}>{VIEW_TITLES[view]}</h1>
                        <p className={styles.pageSubtitle}>{subtitle()}</p>
                    </div>
                    <div className={styles.headerRight}>
                        {trip && <span className={styles.badge}>{tripStatusLabel(trip)}</span>}
                        {trip && <button className={styles.logoutButton} onClick={() => setView('expense')}>+ Novo gasto</button>}
                        <button className={styles.logoutButton} onClick={onCreateTrip}>+ Nova viagem</button>
                        {/* Sai da conta e volta para a tela inicial */}
                        <button className={styles.logoutButton} onClick={onLogout}>Sair</button>
                    </div>
                </div>

                {view === 'overview' && renderOverview()}
                {view === 'trips' && renderTrips()}
                {view === 'history' && renderHistory()}
                {view === 'itinerary' && renderItinerary()}
                {view === 'report' && renderReport()}
                {view === 'turistic' && renderTuristic()}
                {view === 'expense' && renderExpense()}
            </main>
        </div>
    )
}

export default DashBoardPage
