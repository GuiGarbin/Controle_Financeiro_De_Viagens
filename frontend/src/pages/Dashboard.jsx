import { useState } from 'react'
import styles from './Dashboard.module.css'

function Dashboard() {
    return (
        <div className={styles.container}>
            <h1 className={styles.title}>Dashboard</h1>
            <p>Você está logado!</p>
        </div>

    )
}

export default Dashboard