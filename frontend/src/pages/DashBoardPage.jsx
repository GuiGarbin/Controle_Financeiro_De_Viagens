import styles from './DashBoardPage.module.css'

const CURRENCY_VALUE = 31.86  // yen - por enquanto

const trip = {
    name: 'Japão',
    destination: 'Japão',
    currency: 'yen',
    symbol: '¥',
    initialBudget: 2000 * CURRENCY_VALUE,  //
    startDate: '20 abr',
    endDate: '24 abr 2026',
    createdBy: 'Gui',
    dailyBudget: [
        { date: '20 abr', remaining: 2000 * CURRENCY_VALUE / 5 - 300, expenses: [{ description: 'gasto teste', amount: 300 }] },
        { date: '21 abr', remaining: 2000 * CURRENCY_VALUE / 5, expenses: [] },
        { date: '22 abr', remaining: 2000 * CURRENCY_VALUE / 5, expenses: [] },
        { date: '23 abr', remaining: 2000 * CURRENCY_VALUE / 5, expenses: [] },
        { date: '24 abr', remaining: 2000 * CURRENCY_VALUE / 5, expenses: [] },
    ],
    turisticPoints: [
        { name: 'Tah Mahal', cost: 135 }
    ]
}

const toReal = (yen) => (yen * (1 / CURRENCY_VALUE)).toFixed(2)
const totalSpent = trip.dailyBudget.reduce((sum, d) =>
    sum + d.expenses.reduce((s, e) => s + e.amount, 0), 0)
const remaining = trip.initialBudget - totalSpent
const dailyBudget = trip.initialBudget / trip.dailyBudget.length
const pctUsed = ((totalSpent / trip.initialBudget) * 100).toFixed(1)

function DashBoardPage() {
    return (
        <div className={styles.shell}>

            <aside className={styles.sidebar}>
                <div className={styles.logo}>
                    <div className={styles.logoIcon}>✈</div>
                    <span className={styles.logoText}>TripFinance</span>
                </div>

                <span className={styles.navSection}>VIAGEM</span>
                <div className={`${styles.navItem} ${styles.active}`}>Visão geral</div>
                <div className={styles.navItem}>Novo gasto</div>
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
                        <p className={styles.pageSubtitle}>{trip.destination} · {trip.currency} ({trip.symbol}) · {trip.dailyBudget.length} dias</p>
                    </div>
                    <span className={styles.badge}>{trip.createdBy}</span>
                </div>

                <div className={styles.metrics}>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Orçamento total</div>
                        <div className={styles.metricValue}>{trip.symbol} {Math.round(trip.initialBudget).toLocaleString()}</div>
                        <div className={styles.metricSub}>R$ {toReal(trip.initialBudget)}</div>
                    </div>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Orçamento diário</div>
                        <div className={styles.metricValue}>{trip.symbol} {Math.round(dailyBudget).toLocaleString()}</div>
                        <div className={styles.metricSub}>R$ {toReal(dailyBudget)} / dia</div>
                    </div>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Gasto até agora</div>
                        <div className={styles.metricValue}>{trip.symbol} {totalSpent.toLocaleString()}</div>
                        <div className={styles.metricSub}>R$ {toReal(totalSpent)}</div>
                    </div>
                    <div className={styles.metric}>
                        <div className={styles.metricLabel}>Saldo restante</div>
                        <div className={styles.metricValue}>{trip.symbol} {Math.round(remaining).toLocaleString()}</div>
                        <div className={`${styles.metricSub} ${styles.warn}`}>{100 - pctUsed}% disponível</div>
                    </div>
                </div>

                <div className={styles.row}>
                    <div className={styles.card} style={{ flex: 1.5 }}>
                        <div className={styles.cardTitle}>ORÇAMENTO POR DIA</div>
                        {trip.dailyBudget.map((day, i) => (
                            <div key={i} className={styles.dayRow}>
                                <span className={styles.dayDate}>{day.date}</span>
                                <div className={styles.barWrap}>
                                    <div
                                        className={styles.barFill}
                                        style={{ width: `${(day.remaining / dailyBudget) * 100}%` }}
                                    />
                                </div>
                                <span className={styles.dayAmount}>{trip.symbol} {Math.round(day.remaining).toLocaleString()}</span>
                                <span className={styles.dayConverted}>R$ {toReal(day.remaining)}</span>
                            </div>
                        ))}
                    </div>

                    <div className={styles.rightCol}>
                        <div className={styles.card} style={{ flex: 1 }}>
                            <div className={styles.cardTitle}>PONTOS TURÍSTICOS</div>
                            {trip.turisticPoints.map((p, i) => (
                                <div key={i} className={styles.pointRow}>
                                    <span className={styles.pointName}>📍 {p.name}</span>
                                    <span className={styles.pointCost}>{trip.symbol} {p.cost}</span>
                                </div>
                            ))}
                        </div>

                        <div className={`${styles.card} ${styles.cardMuted}`}>
                            <div className={styles.cardTitle}>PROJEÇÃO DE GASTOS</div>
                            <div className={styles.progRow}>
                                <span className={styles.progLabel}>Gasto até agora</span>
                                <span className={styles.progValue}>{trip.symbol} {totalSpent}</span>
                            </div>
                            <div className={styles.progTrack}>
                                <div className={styles.progFill} style={{ width: `${pctUsed}%` }} />
                            </div>
                            <div className={styles.progFooter}>
                                <span className={styles.progFootItem}>Total: {trip.symbol} {Math.round(trip.initialBudget).toLocaleString()}</span>
                                <span className={styles.progFootItem}>{pctUsed}% utilizado</span>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    )
}

export default DashBoardPage