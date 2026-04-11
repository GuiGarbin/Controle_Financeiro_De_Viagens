import { useState } from 'react'

const API = 'http://localhost:8080/api'

export default function App() {
  const [inputText, setInputText] = useState('')
  const [labelText, setLabelText] = useState('The label will update here.')
  const [loading, setLoading]     = useState(false)

  async function handleSubmit() {
    if (!inputText.trim()) return
    setLoading(true)

    try {
      const res = await fetch(`${API}/echo`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({ text: inputText }),
      })
      const data = await res.json()
      setLabelText(data.result)
    } catch (err) {
      setLabelText('Error: could not reach the backend.')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  return (
      <div style={styles.container}>
        <p style={styles.label}>{labelText}</p>

        <input
            type="text"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
            placeholder="Type something..."
            style={styles.input}
        />

        <button
            onClick={handleSubmit}
            disabled={loading}
            style={styles.button}
        >
          {loading ? 'Sending...' : 'Send'}
        </button>
      </div>
  )
}

const styles = {
  container: {
    display:        'flex',
    flexDirection:  'column',
    alignItems:     'center',
    justifyContent: 'center',
    height:         '100vh',
    gap:            '16px',
    fontFamily:     'sans-serif',
    background:     '#f5f5f4',
  },
  label: {
    fontSize:   '18px',
    color:      '#1c1917',
    margin:     0,
  },
  input: {
    fontSize:     '15px',
    padding:      '8px 12px',
    borderRadius: '6px',
    border:       '1px solid #d6d3d1',
    width:        '280px',
    outline:      'none',
  },
  button: {
    fontSize:        '15px',
    padding:         '8px 24px',
    borderRadius:    '6px',
    border:          'none',
    background:      '#292524',
    color:           'white',
    cursor:          'pointer',
  },
}