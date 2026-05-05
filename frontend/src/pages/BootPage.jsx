import styles from './BootPage.module.css'

function BootPage({ onLogin, onRegister }) {
    return (
        <div className={styles.page}>
            <div className={styles.card}>
                <div className={styles.badge}>✈ Controle de Viagens</div>
                <h1 className={styles.title}>Sua viagem,<br />sob controle.</h1>
                <p className={styles.subtitle}>
                    Planeje gastos, divida despesas e acompanhe seu orçamento em tempo real.
                </p>
                <div className={styles.actions}>
                    <button className={styles.primaryButton} onClick={onRegister}>
                        Criar conta
                    </button>
                    <button className={styles.secondaryButton} onClick={onLogin}>
                        Já tenho conta
                    </button>
                </div>
            </div>
        </div>
    )
}

export default BootPage