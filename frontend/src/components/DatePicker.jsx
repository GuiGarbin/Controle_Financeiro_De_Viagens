import { useRef, useState } from 'react'
import styles from './DatePicker.module.css'

const MONTHS = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']
const WEEK = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S']

// Seletor de data em 3 etapas (ano -> mês -> dia), com transição por opacidade.
// Limites opcionais via props `min`/`max` (ISO aaaa-mm-dd): fora deles, meses e
// dias ficam desabilitados. Emite a data em ISO (aaaa-mm-dd) via onChange.
function DatePicker({ value, onChange, placeholder = 'Selecione a data', min, max }) {
    const now = new Date()
    const parseIso = (s) => { const [y, m, d] = s.split('-').map(Number); return new Date(y, m - 1, d) }
    const maxDate = max ? parseIso(max) : new Date(now.getFullYear() + 10, 11, 31)
    const minDate = min ? parseIso(min) : new Date(now.getFullYear() - 120, 0, 1)
    const maxYear = maxDate.getFullYear()
    const minYear = minDate.getFullYear()

    const [open, setOpen] = useState(false)
    const [step, setStep] = useState('year')   // year | month | day
    const [year, setYear] = useState(value ? Number(value.slice(0, 4)) : null)
    const [month, setMonth] = useState(value ? Number(value.slice(5, 7)) - 1 : null)
    const [winTop, setWinTop] = useState(0)     // janela de rolagem dos anos (0 = mais recente)
    const [dropUp, setDropUp] = useState(false) // abre para cima se faltar espaço abaixo
    const fieldRef = useRef(null)

    const totalYears = maxYear - minYear + 1
    const maxTop = Math.max(0, totalYears - 12)

    function openPicker() {
        // Decide se o seletor abre para baixo (padrão) ou para cima, conforme o
        // espaço disponível na janela — evita que ele fique cortado embaixo.
        const r = fieldRef.current && fieldRef.current.getBoundingClientRect()
        const POPOVER_H = 270
        setDropUp(!!r && r.bottom + POPOVER_H > window.innerHeight)
        setStep('year')
        setOpen(true)
    }
    function close() { setOpen(false) }

    // A troca de tela é animada por CSS (key={step} remonta e dispara o fade-in).
    function pickYear(y) { setYear(y); setMonth(null); setStep('month') }
    function pickMonth(m) { setMonth(m); setStep('day') }
    function goBack() { setStep(s => (s === 'day' ? 'month' : 'year')) }
    function pickDay(d) {
        onChange(`${year}-${String(month + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`)
        setOpen(false)
    }
    function scroll(delta) { setWinTop(t => Math.min(maxTop, Math.max(0, t + delta))) }

    const display = value
        ? `${value.slice(8, 10)}/${value.slice(5, 7)}/${value.slice(0, 4)}`
        : ''

    // Anos visíveis (mais recentes no topo; rolar para baixo mostra os mais antigos).
    const years = []
    for (let i = 0; i < 12; i++) {
        const y = maxYear - (winTop + i)
        if (y >= minYear) years.push(y)
    }

    // Células do calendário.
    const cells = []
    if (step === 'day' && year != null && month != null) {
        const firstDow = new Date(year, month, 1).getDay()
        const daysInMonth = new Date(year, month + 1, 0).getDate()
        for (let i = 0; i < firstDow; i++) cells.push(null)
        for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    }

    return (
        <div className={styles.wrap}>
            <button ref={fieldRef} type="button" className={`${styles.field} ${display ? '' : styles.placeholder}`}
                    onClick={() => (open ? close() : openPicker())}>
                {display || placeholder}
            </button>

            {open && (
                <>
                    <div className={styles.backdrop} onClick={close} />
                    <div className={`${styles.popover} ${dropUp ? styles.popoverUp : ''}`}>
                        <div key={step} className={styles.screen}>
                          <div className={styles.head}>
                              {step !== 'year' && (
                                  <button type="button" className={styles.back} onClick={goBack}>‹ Voltar</button>
                              )}
                          </div>
                          <div className={styles.body}>

                            {step === 'year' && (
                                <div className={styles.yearRow}
                                     onWheel={(e) => scroll(e.deltaY > 0 ? 4 : -4)}>
                                    <div className={styles.grid}>
                                        {years.map(y => (
                                            <button type="button" key={y}
                                                    className={`${styles.box} ${y === year ? styles.boxSel : ''}`}
                                                    onClick={() => pickYear(y)}>{y}</button>
                                        ))}
                                    </div>
                                    <div className={styles.scroller}>
                                        <button type="button" className={styles.arrow}
                                                onClick={() => scroll(-4)} disabled={winTop === 0}>▲</button>
                                        <span className={styles.dot} />
                                        <button type="button" className={styles.arrow}
                                                onClick={() => scroll(4)} disabled={winTop >= maxTop}>▼</button>
                                    </div>
                                </div>
                            )}

                            {step === 'month' && (
                                <div className={styles.grid}>
                                    {MONTHS.map((m, i) => {
                                        const disabled = (year === maxYear && i > maxDate.getMonth())
                                            || (year === minYear && i < minDate.getMonth())
                                        return (
                                            <button type="button" key={m} disabled={disabled}
                                                    className={`${styles.box} ${i === month ? styles.boxSel : ''}`}
                                                    onClick={() => pickMonth(i)}>{m}</button>
                                        )
                                    })}
                                </div>
                            )}

                            {step === 'day' && (
                                <div>
                                    <div className={styles.weekRow}>
                                        {WEEK.map((w, i) => <span key={i} className={styles.weekDay}>{w}</span>)}
                                    </div>
                                    <div className={styles.dayGrid}>
                                        {cells.map((d, i) => {
                                            if (d === null) return <span key={i} />
                                            const dd = new Date(year, month, d)
                                            const disabled = dd < minDate || dd > maxDate
                                            return (
                                                <button type="button" key={i} disabled={disabled}
                                                        className={styles.day} onClick={() => pickDay(d)}>{d}</button>
                                            )
                                        })}
                                    </div>
                                </div>
                            )}

                          </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    )
}

export default DatePicker
