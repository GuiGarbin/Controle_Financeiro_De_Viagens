import styles from './TitleBar.module.css'

// Barra de título personalizada (estilo macOS): botões "traffic light" à
// esquerda. Apagados por padrão; acendem nas cores ao aproximar o mouse.
function TitleBar() {
    const wc = (typeof window !== 'undefined' && window.windowControls) || {}
    return (
        <div className={styles.titlebar}>
            <div className={styles.controls}>
                <button
                    className={`${styles.dot} ${styles.full}`}
                    onClick={() => wc.fullscreen && wc.fullscreen()}
                    title="Tela cheia"
                    aria-label="Tela cheia"
                />
                <button
                    className={`${styles.dot} ${styles.min}`}
                    onClick={() => wc.minimize && wc.minimize()}
                    title="Minimizar"
                    aria-label="Minimizar"
                />
                <button
                    className={`${styles.dot} ${styles.close}`}
                    onClick={() => wc.close && wc.close()}
                    title="Fechar"
                    aria-label="Fechar"
                />
            </div>
        </div>
    )
}

export default TitleBar
