
import { useState } from 'react'
import styles from './BootPage.module.css'

function BootPage({ onLogin, onRegister }) {
    return (
        <div className={styles.container}>
            <h1 className={styles.title}>Bem-vindo</h1>
            <button onClick={onLogin}>Já tenho conta</button>
            <button onClick={onRegister}>Quero me cadastrar</button>
        </div>

    )
}

export default BootPage